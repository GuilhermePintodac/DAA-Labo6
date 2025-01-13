/*
Auteur :  Shyshmarov Alexandre / Guilherme Pinto
 */
package ch.heigvd.iict.and.rest.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import ch.heigvd.iict.and.rest.models.Contact

@Dao
interface ContactsDao {

    @Insert
    fun insert(contact: Contact) : Long

    @Update
    fun update(contact: Contact)

    @Delete
    fun delete(contact: Contact)

    @Query("SELECT * FROM Contact")
    fun getAllContactsLiveData() : LiveData<List<Contact>>

    @Query("SELECT * FROM Contact")
    fun getAllContacts() : List<Contact>

    @Query("SELECT * FROM Contact WHERE id = :id")
    fun getContactById(id : Long) : Contact?

    @Query("SELECT COUNT(*) FROM Contact")
    fun getCount() : Int

    @Query("DELETE FROM Contact")
    fun clearAllContacts()

    @Query("SELECT * FROM Contact WHERE isDirty = 1")
    fun getDirtyContacts(): List<Contact>

    @Query("UPDATE Contact SET isDirty = :isDirty WHERE id = :id")
    suspend fun markContactAsDirty(id: Long, isDirty: Boolean)

    @Query("SELECT * FROM Contact WHERE operationType != 'DELETE'")
    fun getVisibleContactsLiveData(): LiveData<List<Contact>>

}