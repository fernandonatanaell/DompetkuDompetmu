package id.ac.istts.dkdm.mydatabase

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Entity(tableName = "histories")
data class HistoryEntity(
    @PrimaryKey (autoGenerate = true)
    var history_id: Int = 0,
    var id_wallet: Int = 0,
    var historyType: String,
    var historyDescription:  String,
    var historyAmount:  Long,
    var historyDate:  String = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))
)
