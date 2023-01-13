package id.ac.istts.dkdm.myfragment.user

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import id.ac.istts.dkdm.R
import id.ac.istts.dkdm.databinding.FragmentUserNotificationBinding
import id.ac.istts.dkdm.myadapter.RVNotificationAdapter
import id.ac.istts.dkdm.myapiconnection.APIConnection
import id.ac.istts.dkdm.mydatabase.AppDatabase
import id.ac.istts.dkdm.mydatabase.NotificationEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class UserNotificationFragment(
    private var db: AppDatabase,
    private val coroutine: CoroutineScope,
    private var usernameLogin: String
) : Fragment() {
    private lateinit var binding: FragmentUserNotificationBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_notification, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentUserNotificationBinding.bind(view)

        APIConnection.getNotifications(view.context, db)

        // SET VIEW
        coroutine.launch {
            val tempAllNotification = db.notificationDao.getAllNotifications(usernameLogin) as ArrayList<NotificationEntity>
            requireActivity().runOnUiThread {
                binding.rvNotification.adapter = RVNotificationAdapter(db, coroutine, tempAllNotification)
                binding.rvNotification.layoutManager = LinearLayoutManager(view.context) }
        }
    }
}