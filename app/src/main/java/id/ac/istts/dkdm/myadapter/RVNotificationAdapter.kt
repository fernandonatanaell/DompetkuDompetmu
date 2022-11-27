package id.ac.istts.dkdm.myadapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import id.ac.istts.dkdm.R
import id.ac.istts.dkdm.mydatabase.AppDatabase
import id.ac.istts.dkdm.mydatabase.NotificationEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class RVNotificationAdapter(
    private var db: AppDatabase,
    private val coroutine: CoroutineScope,
    private val notifications: ArrayList<NotificationEntity>
): RecyclerView.Adapter<RVNotificationAdapter.CustomViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val itemView = LayoutInflater.from(parent.context)
        return CustomViewHolder(itemView.inflate(
            R.layout.notification_layout, parent ,false
        ))
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val item = notifications[position]

        holder.notificationDate.text = item.notification_date
        holder.notificationText.text = item.notification_text

        if(!item.userAlreadySee){
            holder.wrapperNotification.setBackgroundColor(Color.parseColor("#DEECFC"))

            item.userAlreadySee = true
            coroutine.launch {
                db.notificationDao.update(item)
            }
        }
    }

    override fun getItemCount(): Int {
        return notifications.size
    }

    inner class CustomViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val notificationDate: TextView = itemView.findViewById(R.id.tvNotificationDate)
        val notificationText: TextView = itemView.findViewById(R.id.tvNotificationText)
        val wrapperNotification: ConstraintLayout = itemView.findViewById(R.id.notificationWrapper)
    }
}