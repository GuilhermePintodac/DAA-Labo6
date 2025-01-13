/*
Auteur :  Shyshmarov Alexandre / Guilherme Pinto
 */
package ch.heigvd.iict.and.rest

import android.app.Application
import android.content.Context
import ch.heigvd.iict.and.rest.database.ContactsDatabase

class ContactsApplication : Application() {

    private val database by lazy { ContactsDatabase.getDatabase(this) }
    val repository by lazy { ContactsRepository(database.contactsDao()) }

    companion object {
        private lateinit var instance: ContactsApplication

        fun getContext(): Context = instance
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}