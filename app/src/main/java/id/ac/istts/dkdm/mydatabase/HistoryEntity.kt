package id.ac.istts.dkdm.mydatabase

import android.annotation.SuppressLint
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@Entity(tableName = "histories")
data class HistoryEntity(
    @PrimaryKey
    var history_id: Int,
    var id_wallet: Int = 0,
    var historyType: String,
    var historyDescription:  String,
    var historyAmount:  Long,
    @SuppressLint("SimpleDateFormat") var historyDate:  String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date()),
    var deleted_at: String
)
