package id.ac.istts.dkdm.myadapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import id.ac.istts.dkdm.R
import id.ac.istts.dkdm.mydatabase.AppDatabase
import id.ac.istts.dkdm.mydatabase.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class RVAdminUserAdapter(
    private var db: AppDatabase,
    private val coroutine: CoroutineScope,
    private val users: ArrayList<UserEntity>
): RecyclerView.Adapter<RVAdminUserAdapter.CustomViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val itemView = LayoutInflater.from(parent.context)
        return CustomViewHolder(itemView.inflate(
            R.layout.list_admin_users_layout, parent ,false
        ))
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val item = users[position]

        holder.nameUser.text = item.name
        holder.usernameUser.text = item.username

        if(item.isUserBanned){
            holder.toggleUsers.setBackgroundColor(Color.GREEN)
            holder.toggleUsers.text = "UNBAN"
            holder.toggleUsers.setTextColor(Color.BLACK)
        }

        holder.toggleUsers.setOnClickListener {
            if(item.isUserBanned){
                coroutine.launch {
                    item.isUserBanned = false
                    db.userDao.update(item)
                }
                holder.toggleUsers.setBackgroundColor(Color.parseColor("#ba1a1a"))
                holder.toggleUsers.text = "BAN"
                holder.toggleUsers.setTextColor(Color.parseColor("#DEECFC"))
            } else {
                coroutine.launch {
                    item.isUserBanned = true
                    db.userDao.update(item)
                }
                holder.toggleUsers.setBackgroundColor(Color.GREEN)
                holder.toggleUsers.text = "UNBAN"
                holder.toggleUsers.setTextColor(Color.BLACK)
            }
        }
    }

    override fun getItemCount(): Int {
        return users.size
    }

    inner class CustomViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val nameUser: TextView = itemView.findViewById(R.id.tvNameUser)
        val usernameUser: TextView = itemView.findViewById(R.id.tvUsernameUser)
        val toggleUsers: Button = itemView.findViewById(R.id.btnToggleUsers)
    }
}