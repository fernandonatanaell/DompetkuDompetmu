package id.ac.istts.dkdm.myadapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import id.ac.istts.dkdm.R
import id.ac.istts.dkdm.myapiconnection.APIConnection
import id.ac.istts.dkdm.mydatabase.AppDatabase
import id.ac.istts.dkdm.mydatabase.CharityEntity
import id.ac.istts.dkdm.mydatabase.NotificationEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RVAdminCharityAdapter(
    private var db: AppDatabase,
    private val coroutine: CoroutineScope,
    private val charities: ArrayList<CharityEntity>
): RecyclerView.Adapter<RVAdminCharityAdapter.CustomViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val itemView = LayoutInflater.from(parent.context)
        return CustomViewHolder(itemView.inflate(
            R.layout.list_admin_charity_layout, parent ,false
        ))
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val item = charities[position]

        holder.nameCharity.text = item.charity_name
        holder.descCharity.text = item.charity_description

        if(item.isCharityBanned){
            holder.toggleCharity.setBackgroundColor(Color.GREEN)
            holder.toggleCharity.text = "UNBAN"
            holder.toggleCharity.setTextColor(Color.BLACK)
        }

        holder.toggleCharity.setOnClickListener {
            if(item.isCharityBanned){
                coroutine.launch {
                    item.isCharityBanned = false

                    APIConnection.updateCharity(it.context, db, item)

                    val ownerUsername = db.walletDao.get(item.source_id_wallet)!!.username_user
                    val newNotification = NotificationEntity(
                        notification_id = -1,
                        notification_text = "Your charity '${item.charity_name}' has been unbanned by Admin!",
                        username_user = ownerUsername,
                        deleted_at = "null"
                    )

                    APIConnection.insertNotification(it.context, db, newNotification)
                    delay(4000)
                }

                holder.toggleCharity.setBackgroundColor(Color.parseColor("#ba1a1a"))
                holder.toggleCharity.text = "BAN"
                holder.toggleCharity.setTextColor(Color.parseColor("#DEECFC"))
            } else {
                coroutine.launch {
                    item.isCharityBanned = true

                    APIConnection.updateCharity(it.context, db, item)

                    val ownerUsername = db.walletDao.get(item.source_id_wallet)!!.username_user
                    val newNotification = NotificationEntity(
                        notification_id = -1,
                        notification_text = "Your charity '${item.charity_name}' has been banned by Admin!",
                        username_user = ownerUsername,
                        deleted_at = "null"
                    )

                    APIConnection.insertNotification(it.context, db, newNotification)
                    delay(4000)
                }

                holder.toggleCharity.setBackgroundColor(Color.GREEN)
                holder.toggleCharity.text = "UNBAN"
                holder.toggleCharity.setTextColor(Color.BLACK)
            }
        }
    }

    override fun getItemCount(): Int {
        return charities.size
    }

    inner class CustomViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val nameCharity: TextView = itemView.findViewById(R.id.tvNameCharity)
        val descCharity: TextView = itemView.findViewById(R.id.tvDescCharity)
        val toggleCharity: Button = itemView.findViewById(R.id.btnToggleCharity)
    }
}