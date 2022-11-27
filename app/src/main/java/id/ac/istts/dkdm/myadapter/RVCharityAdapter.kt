package id.ac.istts.dkdm.myadapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import id.ac.istts.dkdm.CurrencyUtils.toRupiah
import id.ac.istts.dkdm.R
import id.ac.istts.dkdm.mydatabase.CharityEntity

class RVCharityAdapter(
    private val charities: ArrayList<CharityEntity>,
    private var hideTheDonateButton: Boolean,
    private val charityListener: (selectedCharityId: Int)->Unit
): RecyclerView.Adapter<RVCharityAdapter.CustomViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val itemView = LayoutInflater.from(parent.context)
        return CustomViewHolder(itemView.inflate(
            R.layout.list_charity_layout, parent ,false
        ))
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val item = charities[position]

        holder.charityTitle.text = item.charity_name
        holder.charityDescription.text = item.charity_description
        holder.pbCharity.max = item.fundsGoal.toInt()
        holder.pbCharity.progress = item.fundsRaised.toInt()
        holder.charityTotalFundsRaised.text = "Rp ${item.fundsRaised.toRupiah()},00"
        holder.btnDonate.setOnClickListener {
            charityListener(item.charity_id)
        }

        if(hideTheDonateButton){
            holder.btnDonate.isVisible = false
        }

        if(item.isCharityBanned)
            holder.tvBannedCharity.visibility = View.VISIBLE
    }

    override fun getItemCount(): Int {
        return charities.size
    }

    inner class CustomViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val charityTitle: TextView = itemView.findViewById(R.id.tvTitleCharityRV)
        val charityDescription: TextView = itemView.findViewById(R.id.tvDescriptionCharityRV)
        val pbCharity: ProgressBar = itemView.findViewById(R.id.pbCharity)
        val charityTotalFundsRaised: TextView = itemView.findViewById(R.id.tvTotalFundsRaised)
        val tvBannedCharity: TextView = itemView.findViewById(R.id.tvBannedCharity)
        val btnDonate: TextView = itemView.findViewById(R.id.btnDonate)
    }
}