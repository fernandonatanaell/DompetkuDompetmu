package id.ac.istts.dkdm.myadapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import id.ac.istts.dkdm.CurrencyUtils.toRupiah
import id.ac.istts.dkdm.R
import id.ac.istts.dkdm.mydatabase.AppDatabase
import id.ac.istts.dkdm.mydatabase.HistoryEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class RVHistoryAdapter(
    private var db: AppDatabase,
    private val coroutine: CoroutineScope,
    private val histories: ArrayList<HistoryEntity>
): RecyclerView.Adapter<RVHistoryAdapter.CustomViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val itemView = LayoutInflater.from(parent.context)
        return CustomViewHolder(itemView.inflate(
            R.layout.list_history_layout, parent ,false
        ))
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val item = histories[position]
        holder.historyDescription.text = item.historyDescription
        holder.historyAmount.text = "Rp ${item.historyAmount.toRupiah()},00"

        if(item.historyType == "Income"){
            holder.historyAmount.setTextColor(Color.parseColor("#3FAE20"))
        } else {
            holder.historyAmount.setTextColor(Color.parseColor("#EC1111"))
        }

        coroutine.launch {
            val selectedWallet = db.walletDao.get(item.id_wallet)
            holder.historyFrom.text = "Source : ${selectedWallet!!.walletName}"
        }
    }

    override fun getItemCount(): Int {
        return histories.size
    }

    inner class CustomViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val historyDescription: TextView = itemView.findViewById(R.id.tvHistoryDescriptionRV)
        val historyAmount: TextView = itemView.findViewById(R.id.tvHistoryAmountRV)
        val historyFrom: TextView = itemView.findViewById(R.id.tvHistoryFromRV)
    }
}