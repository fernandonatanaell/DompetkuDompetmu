package id.ac.istts.dkdm.mydatabase

import android.annotation.SuppressLint
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@Entity(tableName = "notifications")
data class NotificationEntity (
    @PrimaryKey(autoGenerate = true)
    var notification_id: Int = 0,
    var notification_text: String,
    var username_user: String,
    @SuppressLint("SimpleDateFormat") var notification_date: String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date()),
    var userAlreadySee: Boolean = false,
    var deleted_at: String?
)