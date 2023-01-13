package id.ac.istts.dkdm.mydatabase

import androidx.room.*

@Dao
interface HistoryDao {
    @Insert
    suspend fun insert(history: HistoryEntity)

    @Update
    suspend fun update(history: HistoryEntity)

    @Delete
    suspend fun delete(history: HistoryEntity)

    @Query("SELECT * FROM histories AS h JOIN wallets AS w ON h.id_wallet = w.wallet_id JOIN users AS u ON w.username_user = u.username WHERE u.username = :username AND w.deleted_at == \"null\" ORDER BY h.history_id DESC")
    suspend fun getAllMyHistory(username: String): List<HistoryEntity>

    @Query("SELECT * FROM histories where history_id = :history_id AND deleted_at == \"null\"")
    suspend fun getHistory(history_id: Int): HistoryEntity?
}