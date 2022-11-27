package id.ac.istts.dkdm.myadapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import id.ac.istts.dkdm.CurrencyUtils.toRupiah
import id.ac.istts.dkdm.R
import id.ac.istts.dkdm.mydatabase.WalletEntity

class RVWalletAdapter(
    private val context: Context,
    private var usernameLogin: String,
    private val wallets: ArrayList<WalletEntity>,
    private val refreshWallets: (nameAction: String, usernameLogin: String, selectedWallet: WalletEntity)->Unit
): RecyclerView.Adapter<RVWalletAdapter.CustomViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val itemView = LayoutInflater.from(parent.context)
        return CustomViewHolder(itemView.inflate(
            R.layout.wallet_layout, parent ,false
        ))
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val item = wallets[position]
        holder.walletName.text = item.walletName
        holder.walletBalance.text = "Rp ${item.walletBalance.toRupiah()},00"
        holder.wrapperWallet.setOnClickListener {
            val popUp = PopupMenu(context, holder.wrapperWallet)
            popUp.menuInflater.inflate(R.menu.wallet_menu, popUp.menu)
            popUp.setOnMenuItemClickListener {
                return@setOnMenuItemClickListener when(it.itemId){
                    R.id.menu_delete->{
                        if(!item.isMainWallet){
                            refreshWallets("delete", usernameLogin, item)
                        } else {
                            Toast.makeText(context, "Oopps! You can't delete Main pocket!", Toast.LENGTH_SHORT).show()
                        }

                        true
                    }
                    else->{
                        false
                    }
                }
            }
            popUp.show()
        }
    }

    override fun getItemCount(): Int {
        return wallets.size
    }

    inner class CustomViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val walletName: TextView = itemView.findViewById(R.id.tvWalletNameRV)
        val walletBalance: TextView = itemView.findViewById(R.id.tvWalletBalanceRV)
        val wrapperWallet: ConstraintLayout = itemView.findViewById(R.id.wrapperPocket)
    }
}