package ch.heigvd.iict.and.rest

import android.content.Context
import ch.heigvd.iict.and.rest.database.ContactsDao
import ch.heigvd.iict.and.rest.models.Contact
import ch.heigvd.iict.and.rest.network.RestApiService
import ch.heigvd.iict.and.rest.models.PhoneType
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*

class ContactsRepository(private val contactsDao: ContactsDao) {

    val allContacts = contactsDao.getAllContactsLiveData()

    // Supprimer les données locales
    suspend fun clearLocalData() {
        contactsDao.clearAllContacts()
    }

    suspend fun getNewUuidFromServer(): String {
        return try {
            RestApiService.get("/enroll")
        } catch (e: Exception) {
            throw Exception("Erreur lors de la récupération de l'UUID : ${e.message}")
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

    suspend fun fetchContactsFromServer(uuid: String): List<Contact> {
        val response = RestApiService.get("/contacts", mapOf("X-UUID" to uuid)) // Appel API

        // Parse le JSON en une liste de contacts
        val contacts = mutableListOf<Contact>()
        val jsonArray = JSONArray(response) // Convertit la réponse en tableau JSON

        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)

            val contact = Contact(
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
            contacts.add(contact)
        }

        return contacts
    }

    suspend fun insertAllContacts(contacts: List<Contact>) {
        for (contact in contacts) {
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
        contactsDao.insert(contact)
    }

    suspend fun update(contact: Contact) {
        contactsDao.update(contact)
    }

    suspend fun refreshContacts() {
        try {
            // Étape 1 : Récupérer l'UUID enregistré dans les SharedPreferences
            val uuid = getSavedUuid() ?: throw Exception("Aucun UUID trouvé. Impossible de rafraîchir les contacts.")

            // Étape 2 : Récupérer les contacts depuis le serveur
            val contactsFromServer = fetchContactsFromServer(uuid)

            // Étape 3 : Effacer les données locales
            clearLocalData()

            // Étape 4 : Insérer les nouveaux contacts dans la base locale
            insertAllContacts(contactsFromServer)
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("Erreur lors du rafraîchissement des contacts : ${e.message}")
        }
    }

    suspend fun enroll() {
        try {
            // Étape 1 : Supprimer les données locales
            clearLocalData()

            // Étape 2 : Obtenir un nouvel UUID via `/enroll`
            val newUuid = getNewUuidFromServer()

            // Étape 3 : Stocker l'UUID obtenu
            saveUuid(newUuid)

            // Étape 4 : Récupérer les contacts associés à l'UUID via `/contacts`
            val contactsFromServer = fetchContactsFromServer(newUuid)

            // Étape 5 : Insérer les contacts récupérés dans la base locale
            insertAllContacts(contactsFromServer)
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("Erreur lors de l'enrollment : ${e.message}")
        }
    }

}