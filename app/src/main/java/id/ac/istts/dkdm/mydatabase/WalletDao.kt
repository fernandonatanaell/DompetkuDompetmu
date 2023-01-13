package id.ac.istts.dkdm.mydatabase

import androidx.room.*

@Dao
interface WalletDao {
    @Insert
    suspend fun insert(wallet: WalletEntity)

    @Update
    suspend fun update(wallet: WalletEntity)

    @Delete
    suspend fun delete(wallet: WalletEntity)

    @Query("SELECT * FROM wallets WHERE username_user = :username AND deleted_at == \"null\" ORDER BY walletBalance desc LIMIT 4 OFFSET 0")
    suspend fun getTop4(username: String): List<WalletEntity>

    @Query("SELECT * FROM wallets WHERE username_user = :username AND deleted_at == \"null\"")
    suspend fun getAllMyWallet(username: String): List<WalletEntity>

    @Query("SELECT * FROM wallets WHERE wallet_id = :wallet_id AND deleted_at == \"null\"")
    suspend fun get(wallet_id: Int): WalletEntity?

    @Query("SELECT * FROM wallets WHERE username_user = :username AND isMainWallet = 1")
    suspend fun getMainWallet(username: String): WalletEntity?
}