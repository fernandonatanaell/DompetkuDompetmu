package id.ac.istts.dkdm.mydatabase

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
class ContactEntity (
    @PrimaryKey(autoGenerate = false)
    var contact_id: Int,
    var username_user: String,
    var username_friend: String,
    var deleted_at: String
) {
    override fun toString(): String {
        return username_friend
    }
}