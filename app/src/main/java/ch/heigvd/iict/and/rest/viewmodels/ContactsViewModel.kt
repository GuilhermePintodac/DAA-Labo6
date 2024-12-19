package ch.heigvd.iict.and.rest.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ch.heigvd.iict.and.rest.ContactsRepository
import kotlinx.coroutines.launch

class ContactsViewModel(private val repository: ContactsRepository) : ViewModel() {

    val allContacts = repository.allContacts
    // actions
    fun enroll() {
        viewModelScope.launch {
            // TODO
            try {
                // Étape 1 : Supprimer les données locales
                repository.clearLocalData()

                // Étape 2 : Obtenir un nouvel UUID via l'API `/enroll`
                val newUuid = repository.getNewUuidFromServer()

                // Étape 3 : Stocker l'UUID obtenu
                repository.saveUuid(newUuid)

                // Étape 4 : Récupérer les contacts associés à l'UUID via `/contacts`
                val contactsFromServer = repository.getContactsFromServer(newUuid)

                // Étape 5 : Insérer les contacts récupérés dans la base locale
                repository.insertContacts(contactsFromServer)

            } catch (e: Exception) {
                // Gérer les erreurs
                e.printStackTrace()
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            // TODO
            repository.refreshContacts()
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