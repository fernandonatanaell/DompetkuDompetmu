package id.ac.istts.dkdm.mydatabase

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Entity(tableName = "notifications")
data class NotificationEntity (
    @PrimaryKey(autoGenerate = true)
    var notification_id: Int = 0,
    var notification_text: String,
    var username_user: String,
    var notification_date: String = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")),
    var userAlreadySee: Boolean = false
)