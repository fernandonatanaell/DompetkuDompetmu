package id.ac.istts.dkdm.mydatabase

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallets")
data class WalletEntity(
    @PrimaryKey
    var wallet_id: Int,
    var username_user: String,
    var walletName:  String,
    var walletBalance:  Long,
    var isMainWallet: Boolean,
    var deleted_at: String
){
    override fun toString(): String {
        return walletName
    }
}