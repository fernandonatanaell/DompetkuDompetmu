package id.ac.istts.dkdm.myactivity.user

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import id.ac.istts.dkdm.R
import id.ac.istts.dkdm.databinding.ActivityUserMainBinding
import id.ac.istts.dkdm.mydatabase.AppDatabase
import id.ac.istts.dkdm.myfragment.user.UserCharityFragment
import id.ac.istts.dkdm.myfragment.user.UserHistoryFragment
import id.ac.istts.dkdm.myfragment.user.UserHomepageFragment
import id.ac.istts.dkdm.myfragment.user.UserNotificationFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

@Suppress("DEPRECATION")
class UserMainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserMainBinding
    private lateinit var db: AppDatabase
    private val coroutine = CoroutineScope(Dispatchers.IO)

    // USER ATTRIBUTE
    private lateinit var usernameLogin: String
    private lateinit var lastFragmentActive: String

    private val refreshLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result: ActivityResult ->
        if(result.resultCode == RESULT_OK){
            // REFRESH PAGE
            swapToFrag(lastFragmentActive)
        }

        setNavbarActive()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TO HIDE STATUS BAR
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // TO SET BINDING VIEW
        binding = ActivityUserMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = AppDatabase.build(this)

        // RECEIVE DATA FROM LOGIN INTENT
        usernameLogin = intent.getStringExtra("usernameLogin").toString()

        // SET NAVBAR
        binding.mainNavbar.setOnItemSelectedListener {
            when(it.itemId){
                R.id.menu_user_home ->{
                    swapToFrag("Homepage")
                }
                R.id.menu_user_charity ->{
                    swapToFrag("Charity")
                }
                R.id.menu_user_transfer ->{
                    refreshLauncher.launch(Intent(this, UserTransferActivity::class.java).apply {
                        putExtra("usernameLogin", usernameLogin)
                    })
                }
                R.id.menu_user_history ->{
                    swapToFrag("History")
                }
                R.id.menu_user_notification ->{
                    swapToFrag("Notification")
                }
                else ->{
                }
            }
            true
        }

        swapToFrag("Homepage")
    }

    private fun getNewFragment(fragmentName: String): Fragment {
        return when (fragmentName) {
            "Homepage" -> { // Homepage fragment
                lastFragmentActive = "Homepage"
                binding.mainNavbar.menu.getItem(0).isChecked = true
                val tempUserHomepageFragment = UserHomepageFragment(db, coroutine, usernameLogin)
                tempUserHomepageFragment.userLogout = {
                    finish()
                }

                tempUserHomepageFragment
            }
            "Charity" -> { // Charity fragment
                lastFragmentActive = "Charity"
                binding.mainNavbar.menu.getItem(1).isChecked = true
                UserCharityFragment(db, coroutine, usernameLogin)
            }
            "History" -> { // History fragment
                lastFragmentActive = "History"
                binding.mainNavbar.menu.getItem(3).isChecked = true
                UserHistoryFragment(db, coroutine, usernameLogin)
            }
            else -> { // Notification fragment
                lastFragmentActive = "Notification"
                binding.mainNavbar.menu.getItem(4).isChecked = true
                UserNotificationFragment(db, coroutine, usernameLogin)
            }
        }
    }

    private fun swapToFrag(fragmentName: String){
        val fragmentManager = supportFragmentManager.beginTransaction()
        fragmentManager.replace(R.id.mainFL, getNewFragment(fragmentName)).commit()
    }

    private fun setNavbarActive(){
        when (lastFragmentActive) {
            "Homepage" -> binding.mainNavbar.menu.getItem(0).isChecked = true
            "Charity" -> binding.mainNavbar.menu.getItem(1).isChecked = true
            "History" -> binding.mainNavbar.menu.getItem(3).isChecked = true
            else -> binding.mainNavbar.menu.getItem(4).isChecked = true
        }
    }
}