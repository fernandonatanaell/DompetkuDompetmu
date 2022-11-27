package id.ac.istts.dkdm.myactivity.user

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
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
                R.id.menu_user_history ->{
                    swapToFrag("History")
                }
                R.id.menu_user_logout ->{
                    val alert = AlertDialog.Builder(this)
                    alert.setTitle("ALERT!")
                    alert.setMessage("Do you really want to log out?")

                    alert.setPositiveButton("Yes") { _, _ ->
                        finish()
                    }

                    alert.setNegativeButton("No") { _, _ ->
                        if(lastFragmentActive == "Homepage")
                            binding.mainNavbar.menu.getItem(0).isChecked = true
                        else if(lastFragmentActive == "Charity")
                            binding.mainNavbar.menu.getItem(1).isChecked = true
                        else
                            binding.mainNavbar.menu.getItem(2).isChecked = true
                    }

                    alert.show()
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
                UserHomepageFragment(db, coroutine, usernameLogin)
            }
            "Charity" -> { // Charity fragment
                lastFragmentActive = "Charity"
                binding.mainNavbar.menu.getItem(1).isChecked = true
                UserCharityFragment(db, coroutine, usernameLogin)
            }
            else -> { // History fragment
                lastFragmentActive = "History"
                binding.mainNavbar.menu.getItem(2).isChecked = true
                UserHistoryFragment(db, coroutine, usernameLogin)
            }
        }
    }

    private fun swapToFrag(fragmentName: String){
        val fragmentManager = supportFragmentManager.beginTransaction()
        fragmentManager.replace(R.id.mainFL, getNewFragment(fragmentName)).commit()
    }
}