package id.ac.istts.dkdm.myactivity.user

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import id.ac.istts.dkdm.R
import id.ac.istts.dkdm.databinding.ActivityUserMainBinding
import id.ac.istts.dkdm.mydatabase.AppDatabase
import id.ac.istts.dkdm.myfragment.user.UserCharityFragment
import id.ac.istts.dkdm.myfragment.user.UserHomepageFragment
import id.ac.istts.dkdm.myfragment.user.UserNotificationFragment
import id.ac.istts.dkdm.myfragment.user.UserProfileFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

@Suppress("DEPRECATION")
class UserMainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserMainBinding
    private lateinit var db: AppDatabase
    private val coroutine = CoroutineScope(Dispatchers.IO)
    private var listenerEnabled = true
    private var currentItem = 0

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
        binding.mainNavbar.setOnNavigationItemSelectedListener { item ->
            if (listenerEnabled) {
                listenerEnabled = false
                binding.mainNavbar.isClickable = false

                Handler().postDelayed({
                    listenerEnabled = true
                    binding.mainNavbar.isClickable = true
                }, 700)

                when (item.itemId) {
                    R.id.menu_user_home -> {
                        swapToFrag("Homepage")
                    }
                    R.id.menu_user_charity -> {
                        swapToFrag("Charity")
                    }
                    R.id.menu_user_transfer -> {
                        refreshLauncher.launch(
                            Intent(
                                this,
                                UserTransferActivity::class.java
                            ).apply {
                                putExtra("usernameLogin", usernameLogin)
                            })
                    }
                    R.id.menu_user_inbox -> {
                        swapToFrag("Inbox")
                    }
                    R.id.menu_user_profile -> {
                        swapToFrag("Notification")
                    }
                    else -> {
                        Toast.makeText(this, "LOL", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                binding.mainNavbar.menu.getItem(currentItem).isChecked
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
                currentItem = 0

                UserHomepageFragment(db, coroutine, usernameLogin)
            }
            "Charity" -> { // Charity fragment
                lastFragmentActive = "Charity"
                binding.mainNavbar.menu.getItem(1).isChecked = true
                currentItem = 1

                UserCharityFragment(db, coroutine, usernameLogin)
            }
            "Inbox" -> { // Inbox fragment
                lastFragmentActive = "Inbox"
                binding.mainNavbar.menu.getItem(3).isChecked = true
                currentItem = 3

                UserNotificationFragment(db, coroutine, usernameLogin)
            }
            else -> { // Profile fragment
                lastFragmentActive = "Profile"
                binding.mainNavbar.menu.getItem(4).isChecked = true
                currentItem = 4

                val tempUserProfileFragment = UserProfileFragment(db, coroutine, usernameLogin)
                tempUserProfileFragment.userLogout = {
                    finish()
                }

                tempUserProfileFragment
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
            "Inbox" -> binding.mainNavbar.menu.getItem(3).isChecked = true
            else -> binding.mainNavbar.menu.getItem(4).isChecked = true
        }
    }
}