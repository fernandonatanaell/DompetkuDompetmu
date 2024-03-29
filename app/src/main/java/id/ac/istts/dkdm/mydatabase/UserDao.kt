package id.ac.istts.dkdm.mydatabase

import androidx.room.*

@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: UserEntity)

    @Update
    suspend fun update(user: UserEntity)

    @Query("SELECT * FROM users WHERE username = :username AND deleted_at == \"null\"")
    suspend fun getFromUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE deleted_at == \"null\"")
    suspend fun getAllUsers(): List<UserEntity>

    @Query("SELECT * FROM users WHERE accountNumber = :accountNumber AND deleted_at == \"null\"")
    suspend fun checkAccountNumber(accountNumber: Long): UserEntity?
}