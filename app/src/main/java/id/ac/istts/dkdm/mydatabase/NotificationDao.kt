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

    @Query("SELECT * FROM notifications WHERE username_user = :username ORDER BY notification_id DESC")
    suspend fun getAllNotifications(username: String): List<NotificationEntity>
}