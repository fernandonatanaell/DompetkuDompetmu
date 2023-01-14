package id.ac.istts.dkdm.myfragment.user

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import id.ac.istts.dkdm.R
import id.ac.istts.dkdm.databinding.FragmentUserProfileBinding
import id.ac.istts.dkdm.myactivity.user.UserEditProfileActivity
import id.ac.istts.dkdm.mydatabase.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class UserProfileFragment(
    private var db: AppDatabase,
    private val coroutine: CoroutineScope,
    private var usernameLogin: String
) : Fragment() {
    var userLogout:(()-> Unit)? = null
    private lateinit var binding: FragmentUserProfileBinding

    private val refreshLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result: ActivityResult ->
        if(result.resultCode == AppCompatActivity.RESULT_OK){
            // REFRESH NAME USER
            showNameUser()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentUserProfileBinding.bind(view)

        // SET VIEW
        showNameUser()

        binding.tvToEditPage.setOnClickListener {
            refreshLauncher.launch(Intent(view.context, UserEditProfileActivity::class.java).apply {
                putExtra("usernameLogin", usernameLogin)
            })
        }

        binding.clList1.setOnClickListener {
            parentFragmentManager
                .beginTransaction()
                .replace(R.id.mainFL, UserAllWalletFragment(db, coroutine, usernameLogin))
                .commit()
        }

        binding.clList2.setOnClickListener {
            parentFragmentManager
                .beginTransaction()
                .replace(R.id.mainFL, UserMyCharityFragment(db, coroutine, usernameLogin))
                .commit()
        }

        binding.clList3.setOnClickListener {
            val alert = AlertDialog.Builder(view.context)
            alert.setTitle("ALERT!")
            alert.setMessage("Do you really want to log out?")

            alert.setPositiveButton("Yes") { _, _ ->
                userLogout?.invoke()
            }

            alert.setNegativeButton("No") { _, _ ->

            }

            alert.show()
        }
    }

    private fun showNameUser(){
        coroutine.launch {
            val nameUser = db.userDao.getFromUsername(usernameLogin)!!.name
            requireActivity().runOnUiThread {
                binding.tvProfileName.text = nameUser
                binding.tvProfileUsername.text = usernameLogin
            }
        }
    }
}