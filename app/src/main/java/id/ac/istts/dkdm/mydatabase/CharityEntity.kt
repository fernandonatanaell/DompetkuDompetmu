package id.ac.istts.dkdm.mydatabase

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "charities")
class CharityEntity (
    @PrimaryKey(autoGenerate = true)
    var charity_id: Int = 0,
    var charity_name: String,
    var charity_description: String,
    var source_id_wallet: Int,
    var fundsGoal: Long,
    var fundsRaised: Long = 0,
    var isCharityIsOver: Boolean = false,
    var isCharityBanned: Boolean = false,
    var imgPath: String = ""
)

