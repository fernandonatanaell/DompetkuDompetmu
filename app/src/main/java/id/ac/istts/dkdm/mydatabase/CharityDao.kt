package id.ac.istts.dkdm.mydatabase

import androidx.room.*

@Dao
interface CharityDao {
    @Insert
    suspend fun insert(charity: CharityEntity)

    @Update
    suspend fun update(charity: CharityEntity)

    @Delete
    suspend fun delete(charity: CharityEntity)

    @Query("SELECT * FROM charities AS c JOIN wallets AS w ON c.source_id_wallet = w.wallet_id " +
            "WHERE c.isCharityIsOver = 0 AND c.isCharityBanned = 0 " +
            "AND w.username_user <> :username " +
            "ORDER BY c.charity_start_date ")
    suspend fun getAllCharityExceptThisUser(username: String): List<CharityEntity>

    @Query("SELECT * FROM charities AS c JOIN wallets AS w ON c.source_id_wallet = w.wallet_id " +
            "WHERE c.isCharityIsOver = 0 AND c.isCharityBanned = 0 " +
            "AND w.username_user <> :username " +
            "AND substr(c.charity_end_date, 1, 10) >= :tanggalbatas " +
            "ORDER BY c.charity_start_date DESC")
    suspend fun getAllCharityUrgent(username: String, tanggalbatas: String): List<CharityEntity>

    @Query("SELECT * FROM charities AS c JOIN wallets AS w ON c.source_id_wallet = w.wallet_id " +
            "WHERE c.isCharityIsOver = 0 AND c.isCharityBanned = 0 " +
            "AND w.username_user <> :username " +
            "AND substr(c.charity_end_date, 1, 10) >= :tanggalbatas " +
            "ORDER BY c.charity_start_date ASC")
    suspend fun getAllCharityLatest(username: String, tanggalbatas: String): List<CharityEntity>

    @Query("SELECT * FROM charities AS c JOIN wallets AS w ON c.source_id_wallet = w.wallet_id WHERE c.isCharityIsOver = 0 AND c.isCharityBanned = 0 AND w.username_user <> :username AND c.charity_name LIKE '%' || :charity_name || '%' ")
    suspend fun getAllCharityExceptThisUserFilter(username: String, charity_name: String): List<CharityEntity>

    @Query("SELECT * FROM charities AS c JOIN wallets AS w ON c.source_id_wallet = w.wallet_id WHERE c.isCharityIsOver = 0 AND w.username_user = :username")
    suspend fun getAllMyCharity(username: String): List<CharityEntity>


    @Query("SELECT * FROM charities WHERE charity_id = :charity_id")
    suspend fun getCharity(charity_id: Int): CharityEntity

    @Query("SELECT * FROM charities")
    suspend fun getAllCharity(): List<CharityEntity>
}