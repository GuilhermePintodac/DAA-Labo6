package ch.heigvd.iict.and.rest

import android.content.Context
import ch.heigvd.iict.and.rest.database.ContactsDao
import ch.heigvd.iict.and.rest.models.Contact
import ch.heigvd.iict.and.rest.network.RestApiService
import ch.heigvd.iict.and.rest.models.PhoneType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class ContactsRepository(private val contactsDao: ContactsDao) {

    val allContacts = contactsDao.getAllContactsLiveData()

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
        val contactWithServerId = contact.copy(id = serverId)

        contactsDao.insert(contactWithServerId)

        }catch (e: Exception) {
            e.printStackTrace()
            throw Exception("Erreur lors de l'insertion du contact sur le serveur : ${e.message}")
        }
    }

    suspend fun delete(contact: Contact) {
        val uuid = getSavedUuid() ?: throw Exception("UUID non trouvé. Impossible de supprimer le contact.")

        try {
            // Appel REST pour supprimer le contact sur le serveur
            RestApiService.delete("/contacts/${contact.id}", mapOf("X-UUID" to uuid))
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("Erreur lors de la suppression du contact sur le serveur : ${e.message}")
        }

        // Supprimer le contact localement
        contactsDao.delete(contact)
    }

    suspend fun update(contact: Contact) {
        val uuid = getSavedUuid() ?: throw Exception("UUID non trouvé. Impossible de mettre à jour le contact.")

        try {
            // Appel REST pour modifier le contact sur le serveur
            val contactJson = convertContactToJson(contact)
            val response = RestApiService.put("/contacts/${contact.id}", mapOf("X-UUID" to uuid), contactJson)

            println("Réponse serveur pour la mise à jour : $response")
        } catch (e: Exception) {
            if (e.message?.contains("404") == true) {
                throw Exception("Le contact avec l'ID ${contact.id} n'existe pas sur le serveur.")
            } else {
                throw Exception("Erreur lors de la mise à jour du contact sur le serveur : ${e.message}")
            }
        }

        contactsDao.update(contact)
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
}