package id.ac.istts.dkdm.myactivity.admin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import id.ac.istts.dkdm.R
import id.ac.istts.dkdm.databinding.ActivityAdminMainBinding
import id.ac.istts.dkdm.mydatabase.AppDatabase
import id.ac.istts.dkdm.myfragment.admin.AdminCharityFragment
import id.ac.istts.dkdm.myfragment.admin.AdminUserFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

@Suppress("DEPRECATION")
class AdminMainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminMainBinding
    private lateinit var db: AppDatabase
    private val coroutine = CoroutineScope(Dispatchers.IO)

    private lateinit var lastFragmentActive: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TO HIDE STATUS BAR
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // TO SET BINDING VIEW
        binding = ActivityAdminMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = AppDatabase.build(this)

        // SET NAVBAR
        binding.mainNavbar.setOnItemSelectedListener {
            when(it.itemId){
                R.id.menu_admin_user ->{
                    swapToFrag("User")
                }
                R.id.menu_admin_charity ->{
                    swapToFrag("Charity")
                }
                R.id.menu_admin_logout ->{
                    val alert = AlertDialog.Builder(this)
                    alert.setTitle("ALERT!")
                    alert.setMessage("Do you really want to log out?")

                    alert.setPositiveButton("Yes") { _, _ ->
                        finish()
                    }

                    alert.setNegativeButton("No") { _, _ ->
                        if(lastFragmentActive == "User")
                            binding.mainNavbar.menu.getItem(0).isChecked = true
                        else
                            binding.mainNavbar.menu.getItem(1).isChecked = true
                    }

                    alert.show()
                }
                else ->{
                }
            }
            true
        }

        swapToFrag("User")
    }

    private fun getNewFragment(fragmentName: String): Fragment {
        return when (fragmentName) {
            "User" -> { // User fragment
                lastFragmentActive = "User"
                binding.mainNavbar.menu.getItem(0).isChecked = true
                AdminUserFragment(db, coroutine)
            }
            else -> { // Charity fragment
                lastFragmentActive = "Charity"
                binding.mainNavbar.menu.getItem(1).isChecked = true
                AdminCharityFragment(db, coroutine)
            }
        }
    }

    private fun swapToFrag(fragmentName: String){
        val fragmentManager = supportFragmentManager.beginTransaction()
        fragmentManager.replace(R.id.mainFL, getNewFragment(fragmentName)).commit()
    }
}