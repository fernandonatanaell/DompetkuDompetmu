package id.ac.istts.dkdm.mydatabase

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface NotificationDao {
    @Insert
    suspend fun insert(notification: NotificationEntity)

    @Update
    suspend fun update(notification: NotificationEntity)

    @Query("SELECT * FROM notifications WHERE username_user = :username AND deleted_at == \"null\" ORDER BY notification_id DESC")
    suspend fun getAllNotifications(username: String): List<NotificationEntity>

    @Query("SELECT * FROM notifications WHERE notification_id = :id")
    suspend fun get(id: Int): NotificationEntity?
}