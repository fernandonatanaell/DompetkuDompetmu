package id.ac.istts.dkdm.mydatabase

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "charities")
class CharityEntity (
    @PrimaryKey(autoGenerate = false)
    var charity_id: Int,
    var charity_name: String,
    var charity_description: String,
    var source_id_wallet: Int,
    var fundsGoal: Long,
    var fundsRaised: Long = 0,
    var charity_start_date: String,
    var charity_end_date: String,
    var isCharityIsOver: Boolean = false,
    var isCharityBanned: Boolean = false,
    var imgPath: String,
    var deleted_at: String
)

