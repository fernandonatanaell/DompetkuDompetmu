package id.ac.istts.dkdm.myadapter

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import id.ac.istts.dkdm.CurrencyUtils.toRupiah
import id.ac.istts.dkdm.R
import id.ac.istts.dkdm.mydatabase.CharityEntity
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class RVCharityAdapter(
    private val charities: ArrayList<CharityEntity>,
    private var charityWidthFull: Boolean,
    private val charityListener: (selectedCharityId: Int)->Unit
): RecyclerView.Adapter<RVCharityAdapter.CustomViewHolder>(){

    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val itemView = LayoutInflater.from(parent.context)
        context = parent.context
        return CustomViewHolder(itemView.inflate(
            R.layout.list_charity_layout, parent ,false
        ))
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val item = charities[position]

        if(!charityWidthFull){
            holder.wrapperCharity.maxWidth = 840
            val params = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            )

            if(position != charities.size - 1) {
                params.setMargins(0, 0, 35, 0)
            } else {
                params.setMargins(0, 0, 0, 0)
            }

            holder.wrapperCharity.layoutParams = params
        }

        holder.wrapperCharity.setOnClickListener {
            charityListener(item.charity_id)
        }

        val directory = File(context.filesDir, "DompetkuDompetmu")
        val file = File(directory, item.imgPath)
        if (file.exists()) {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            holder.ivListCharity.setImageBitmap(bitmap)
        }

        holder.charityTitle.text = item.charity_name
        holder.charityDescription.text = item.charity_description
        holder.pbCharity.max = item.fundsGoal.toInt()
        holder.pbCharity.progress = item.fundsRaised.toInt()
        holder.charityTotalFundsRaised.text = "Rp ${item.fundsRaised.toRupiah()},00"

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val dateNow = sdf.parse(sdf.format(Date()))
        val dateEnd = sdf.parse(item.charity_end_date)
        var diff: Long = (dateEnd.time - dateNow.time) / 1000 / 60

        if(diff < 1440){
            if(diff < 60){
                holder.tvTotalDays.text = "$diff minutes to go"
            } else {
                diff /= 60
                holder.tvTotalDays.text = "$diff hours to go"
            }
        } else {
            diff = diff / 60 / 24
            holder.tvTotalDays.text = "$diff days to go"
        }

        if(item.isCharityBanned)
            holder.tvBannedCharity.visibility = View.VISIBLE
    }

    override fun getItemCount(): Int {
        return charities.size
    }

    inner class CustomViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val ivListCharity: ImageView = itemView.findViewById(R.id.ivListCharity)
        val charityTitle: TextView = itemView.findViewById(R.id.tvTitleCharityRV)
        val charityDescription: TextView = itemView.findViewById(R.id.tvDescriptionCharityRV)
        val pbCharity: ProgressBar = itemView.findViewById(R.id.pbCharity)
        val charityTotalFundsRaised: TextView = itemView.findViewById(R.id.tvTotalFundsRaised)
        val tvBannedCharity: TextView = itemView.findViewById(R.id.tvBannedCharity)
        val tvTotalDays: TextView = itemView.findViewById(R.id.tvTotalDays)
        val wrapperCharity: ConstraintLayout = itemView.findViewById(R.id.wrapperCharity)
    }
}