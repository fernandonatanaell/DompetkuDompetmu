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

    @Query("SELECT * FROM wallets WHERE username_user = :username")
    suspend fun getAllMyWallet(username: String): List<WalletEntity>

    @Query("SELECT * FROM wallets WHERE wallet_id = :wallet_id")
    suspend fun get(wallet_id: Int): WalletEntity?

    @Query("SELECT * FROM wallets WHERE username_user = :username AND isMainWallet = 1")
    suspend fun getMainWallet(username: String): WalletEntity?
}