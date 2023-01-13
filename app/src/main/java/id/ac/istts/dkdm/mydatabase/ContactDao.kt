package id.ac.istts.dkdm.mydatabase

import androidx.room.*

@Dao
interface ContactDao {
    @Insert
    suspend fun insert(contact: ContactEntity)

    @Delete
    suspend fun delete(contact: ContactEntity)

    @Query("SELECT * FROM contacts WHERE username_user = :username_user AND deleted_at == \"null\"")
    suspend fun getAllContacts(username_user: String): List<ContactEntity>

    @Query("SELECT * FROM contacts WHERE contact_id = :contact_id AND deleted_at == \"null\"")
    suspend fun getContact(contact_id: Int): ContactEntity?

    @Query("SELECT * FROM contacts WHERE username_user = :username_user AND username_friend = :username_friend AND deleted_at == \"null\"")
    suspend fun checkContact(username_user: String, username_friend: String): ContactEntity?
}