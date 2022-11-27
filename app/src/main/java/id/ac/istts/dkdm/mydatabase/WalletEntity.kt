package id.ac.istts.dkdm.mydatabase

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallets")
data class WalletEntity(
    @PrimaryKey (autoGenerate = true)
    var wallet_id: Int = 0,
    var username_user: String,
    var walletName:  String,
    var walletBalance:  Long,
    var isMainWallet: Boolean
){
    override fun toString(): String {
        return walletName
    }
}