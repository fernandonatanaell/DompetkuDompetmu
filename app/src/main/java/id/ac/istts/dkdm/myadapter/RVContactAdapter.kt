package id.ac.istts.dkdm.myadapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import id.ac.istts.dkdm.CurrencyUtils.toRupiah
import id.ac.istts.dkdm.R
import id.ac.istts.dkdm.mydatabase.AppDatabase
import id.ac.istts.dkdm.mydatabase.ContactEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


class RVContactAdapter(
    private var context: Context,
    private var db: AppDatabase,
    private val coroutine: CoroutineScope,
    private val contacts: ArrayList<ContactEntity>,
    private val deleteContact: (contact_id: Int)->Unit
) : RecyclerView.Adapter<RVContactAdapter.CustomViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val itemView = LayoutInflater.from(parent.context)
        return CustomViewHolder(itemView.inflate(
            R.layout.list_contact_layout, parent ,false
        ))
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val item = contacts[position]

        coroutine.launch {
            val nameFriend = db.userDao.getFromUsername(item.username_friend)

            (context as Activity).runOnUiThread {
                holder.tvNameContact.text = nameFriend!!.name
                holder.btnDeleteContact.setOnClickListener {
                    deleteContact(item.contact_id)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return contacts.size
    }

    inner class CustomViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val tvNameContact: TextView = itemView.findViewById(R.id.tvNameContact)
        val btnDeleteContact: TextView = itemView.findViewById(R.id.btnDeleteContact)
    }
}