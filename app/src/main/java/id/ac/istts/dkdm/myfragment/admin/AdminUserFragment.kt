package id.ac.istts.dkdm.myfragment.admin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import id.ac.istts.dkdm.R
import id.ac.istts.dkdm.databinding.FragmentAdminUserBinding
import id.ac.istts.dkdm.myadapter.RVAdminUserAdapter
import id.ac.istts.dkdm.mydatabase.AppDatabase
import id.ac.istts.dkdm.mydatabase.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class AdminUserFragment(
    private var db: AppDatabase,
    private val coroutine: CoroutineScope
) : Fragment() {
    private lateinit var binding: FragmentAdminUserBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_admin_user, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAdminUserBinding.bind(view)

        // SET VIEW
        coroutine.launch {
            val adapterListOfUsers = RVAdminUserAdapter(db, coroutine, db.userDao.getAllUsers() as ArrayList<UserEntity>)

            requireActivity().runOnUiThread {
                binding.rvAdminUsers.layoutManager = LinearLayoutManager(view.context)
                binding.rvAdminUsers.adapter = adapterListOfUsers
            }
        }
    }
}