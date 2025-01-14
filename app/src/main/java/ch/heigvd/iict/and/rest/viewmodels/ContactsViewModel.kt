/*
Auteur :  Shyshmarov Alexandre / Guilherme Pinto
 */
package ch.heigvd.iict.and.rest.viewmodels

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ch.heigvd.iict.and.rest.ContactsRepository
import ch.heigvd.iict.and.rest.models.Contact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ContactsViewModel(private val repository: ContactsRepository) : ViewModel() {

    val visibleContacts = repository.visibleContacts
    val selectedContact = MutableLiveData<Contact?>() // Permet de partager le contact sélectionné

    fun enroll() {
        viewModelScope.launch(Dispatchers.IO) {
            try {

                // Étape 1 : Supprimer les données locales
                repository.clearLocalData()

                // Étape 2 : Obtenir un nouvel UUID via `/enroll`
                val newUuid = repository.getNewUuidFromServer()

                // Étape 3 : Stocker l'UUID obtenu
                repository.saveUuid(newUuid)

                // Étape 4 : Récupérer les contacts associés à l'UUID via `/contacts`
                repository.fetchContactsFromServer(newUuid)

            } catch (e: Exception) {
                e.printStackTrace()
                throw Exception("Erreur lors de l'enrollment : ${e.message}")
            }
        }
    }

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.synchronizeDirtyContacts()
        }
    }

    fun insertContact(contact: Contact) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(contact)
        }
    }

    fun deleteContact(contact: Contact) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(contact)
        }
    }


    fun updateContact(contact: Contact) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.update(contact)
        }
    }

    fun saveSelectedContactState(outState: Bundle) {
        selectedContact.value?.let { contact ->
            outState.putLong("selectedContactId", contact.id ?: -1L)
        }
    }

    fun restoreSelectedContactState(savedInstanceState: Bundle) {
        val selectedContactId = savedInstanceState.getLong("selectedContactId", -1L)
        if (selectedContactId != -1L) {
            viewModelScope.launch(Dispatchers.IO) {
                val contact = repository.getContactById(selectedContactId)
                selectedContact.postValue(contact)
            }
        }
    }

}


class ContactsViewModelFactory(private val repository: ContactsRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContactsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ContactsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}