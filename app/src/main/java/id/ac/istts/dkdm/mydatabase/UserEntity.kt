package id.ac.istts.dkdm.mydatabase

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    var username: String,
    var password: String,
    var name:  String,
    var accountNumber: Long,
    var userPIN: Int,
    var isUserBanned: Boolean = false
)
