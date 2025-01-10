package ch.heigvd.iict.and.rest

import android.content.Context
import androidx.lifecycle.LiveData
import ch.heigvd.iict.and.rest.database.ContactsDao
import ch.heigvd.iict.and.rest.models.Contact
import ch.heigvd.iict.and.rest.models.OperationType
import ch.heigvd.iict.and.rest.network.RestApiService
import ch.heigvd.iict.and.rest.models.PhoneType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class ContactsRepository(private val contactsDao: ContactsDao) {

    val visibleContacts: LiveData<List<Contact>> = contactsDao.getVisibleContactsLiveData()


    // Supprimer les données locales
    suspend fun clearLocalData() = withContext(Dispatchers.IO) {
        contactsDao.clearAllContacts()
    }

    // Start enrolling
    suspend fun getNewUuidFromServer(): String = withContext(Dispatchers.IO) {
        runCatching {
            RestApiService.get("/enroll")
        }.getOrElse { e ->
            throw Exception("Erreur lors de la récupération de l'UUID : ${e.message}", e)
        }
    }

    fun saveUuid(uuid: String) {
        val sharedPreferences = ContactsApplication.getContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("user_uuid", uuid).apply()
    }

    fun getSavedUuid(): String? {
        val sharedPreferences = ContactsApplication.getContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("user_uuid", null)
    }

    suspend fun fetchContactsFromServer(uuid: String) = withContext(Dispatchers.IO){
        val response = RestApiService.get("/contacts", mapOf("X-UUID" to uuid)) // Appel API

        val jsonArray = JSONArray(response) // Convertit la réponse en tableau JSON

        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)

            val contact = Contact(
                id = jsonObject.optLong("id"),
                name = jsonObject.optString("name", ""),
                firstname = jsonObject.optString("firstname", ""),
                birthday = parseIsoDate(jsonObject.optString("birthday", null)),
                email = jsonObject.optString("email", ""),
                address = jsonObject.optString("address", ""),
                zip = jsonObject.optString("zip", ""),
                city = jsonObject.optString("city", ""),
                type = parsePhoneType(jsonObject.optString("type", null)),
                phoneNumber = jsonObject.optString("phoneNumber", "")
            )

            contactsDao.insert(contact)
        }
    }

    companion object {
        private val TAG = "ContactsRepository"
    }

    // Utilitaire : Parse une date ISO en Calendar
    private fun parseIsoDate(dateString: String?): Calendar? {
        if (dateString.isNullOrEmpty()) return null
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())
            val date = sdf.parse(dateString)
            Calendar.getInstance().apply { time = date }
        } catch (e: Exception) {
            null
        }
    }

    // Utilitaire : Parse un type de téléphone
    private fun parsePhoneType(typeString: String?): PhoneType? {
        return try {
            PhoneType.valueOf(typeString ?: "")
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    suspend fun insert(contact: Contact) {
        val uuid = getSavedUuid() ?: throw Exception("UUID non trouvé. Impossible d'insérer le contact.")

        try {

        // Appel REST pour créer le contact sur le serveur
        val contactJson = convertContactToJson(contact)
        val response = RestApiService.post("/contacts", mapOf("X-UUID" to uuid), contactJson)

        // Parse la réponse pour récupérer l'ID attribué par le serveur
        val jsonResponse = JSONObject(response)
        val serverId = jsonResponse.getLong("id")

        // Mettre à jour l'ID du contact local avec celui du serveur
        val contactWithServerId = contact.copy(id = serverId, isDirty = false, operationType = OperationType.NONE)

        contactsDao.insert(contactWithServerId)

        }catch (e: Exception) {
            e.printStackTrace()

            // Générer un ID temporaire négatif basé sur l'heure actuelle
            val temporaryId = -System.currentTimeMillis().toInt()

            // Si une erreur se produit, insérer le contact avec l'état "dirty"
            val contactDirty = contact.copy(id = temporaryId.toLong(),isDirty = true, operationType = OperationType.CREATE)
            contactsDao.insert(contactDirty)


//            throw Exception("Erreur lors de l'insertion du contact sur le serveur : ${e.message}")
        }
    }

    suspend fun delete(contact: Contact) {
        val uuid = getSavedUuid() ?: throw Exception("UUID non trouvé. Impossible de supprimer le contact.")

        try {
            // Tente de supprimer le contact sur le serveur
            RestApiService.delete("/contacts/${contact.id}", mapOf("X-UUID" to uuid))

            // Supprime le contact localement si réussi
            contactsDao.delete(contact)

        } catch (e: Exception) {
            e.printStackTrace()

            // Met le contact dans un état "dirty" si la suppression échoue
            val contactDirty = contact.copy(isDirty = true, operationType = OperationType.DELETE)
            contactsDao.update(contactDirty)
        }
    }


suspend fun update(contact: Contact) {
    val uuid = getSavedUuid() ?: throw Exception("UUID non trouvé. Impossible de mettre à jour le contact.")

    try {
        // Appel REST pour modifier le contact sur le serveur
        val contactJson = convertContactToJson(contact)
        RestApiService.put("/contacts/${contact.id}", mapOf("X-UUID" to uuid), contactJson)

        // Marquer le contact comme non dirty
        contactsDao.update(contact.copy(isDirty = false, operationType = OperationType.NONE))

    } catch (e: Exception) {
        e.printStackTrace()
        // Ignorer l'erreur et marquer le contact comme `isDirty = true`
        val contactDirty = contact.copy(isDirty = true, operationType = OperationType.UPDATE)
        contactsDao.update(contactDirty)
    }
}


    // Méthode utilitaire pour convertir un contact en JSON
    private fun convertContactToJson(contact: Contact): String {
        val birthdayIso = contact.birthday?.let { calendar ->
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())
            sdf.format(calendar.time)
        } ?: "null"

        return """
        {
            "id": ${contact.id ?: "null"},
            "name": "${contact.name}",
            "firstname": "${contact.firstname}",
            "email": "${contact.email}",
            "address": "${contact.address}",
            "zip": "${contact.zip}",
            "city": "${contact.city}",
            "type": "${contact.type}",
            "phoneNumber": "${contact.phoneNumber}",
            "birthday": "$birthdayIso"
        }
    """.trimIndent()
    }


    suspend fun refreshContacts() {
        try {
            // Étape 1 : Récupérer l'UUID enregistré dans les SharedPreferences
            val uuid = getSavedUuid() ?: throw Exception("Aucun UUID trouvé. Impossible de rafraîchir les contacts.")

            // Étape 2 : Effacer les données locales
            clearLocalData()

            // Étape 3 : Récupérer les contacts depuis le serveur
            fetchContactsFromServer(uuid)

        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("Erreur lors du rafraîchissement des contacts : ${e.message}")
        }
    }


    suspend fun synchronizeDirtyContacts() {
        val uuid = getSavedUuid() ?: throw Exception("UUID non trouvé.")
        val dirtyContacts = contactsDao.getDirtyContacts()

        for (contact in dirtyContacts) {
            try {
                when (contact.operationType) {
                    OperationType.CREATE -> {
                        // Tentative d'insertion
                        val contactJson = convertContactToJson(contact)
                        val response = RestApiService.post("/contacts", mapOf("X-UUID" to uuid), contactJson)

                        // Récupérer l'ID serveur
                        val jsonResponse = JSONObject(response)
                        val serverId = jsonResponse.getLong("id")

                        // Mettre à jour le contact avec l'ID serveur et marquer comme synchronisé
                        val updatedContact = contact.copy(
                            id = serverId,
                            isDirty = false,
                            operationType = OperationType.NONE
                        )
                        contactsDao.update(updatedContact)
                    }
                    OperationType.UPDATE -> {
                        // Tentative de mise à jour
                        val contactJson = convertContactToJson(contact)
                        RestApiService.put("/contacts/${contact.id}", mapOf("X-UUID" to uuid), contactJson)

                        // Marquer comme synchronisé si réussi
                        contactsDao.update(contact.copy(isDirty = false, operationType = OperationType.NONE))
                    }
                    OperationType.DELETE -> {
                        // Tentative de suppression
                        RestApiService.delete("/contacts/${contact.id}", mapOf("X-UUID" to uuid))

                        // Supprimer définitivement si réussi
                        contactsDao.delete(contact)
                    }
                    else -> {
                        // Ignorer les contacts avec OperationType.NONE
                    }
                }
            } catch (e: Exception) {
                // En cas d'échec, le contact reste dans son état dirty
                e.printStackTrace()

                // Vous pouvez ajouter des logs ou des notifications si nécessaire pour informer l'utilisateur
            }
        }
    }



}