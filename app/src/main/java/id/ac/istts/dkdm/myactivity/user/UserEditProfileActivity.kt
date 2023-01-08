package id.ac.istts.dkdm.myactivity.user

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import id.ac.istts.dkdm.databinding.ActivityUserEditProfileBinding
import id.ac.istts.dkdm.mydatabase.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class UserEditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserEditProfileBinding
    private lateinit var db: AppDatabase
    private val coroutine = CoroutineScope(Dispatchers.IO)

    // USER ATTRIBUTE
    private lateinit var usernameLogin: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TO HIDE STATUS BAR
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // TO SET BINDING VIEW
        binding = ActivityUserEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = AppDatabase.build(this)

        // RECEIVE DATA FROM TRANSFER ACTIVITY
        usernameLogin = intent.getStringExtra("usernameLogin").toString()

        // SET VIEW
        binding.btnBackFromEditProfile.setOnClickListener {
            finish()
        }

        coroutine.launch {
            val nameUser = db.userDao.getFromUsername(usernameLogin)!!.name
            runOnUiThread {
                binding.etNameUser.setText(nameUser)
            }
        }

        binding.btnToUpdateProfile.setOnClickListener {

        }
    }

    private fun resetErrorInput(){
        binding.tilNameUser.error = null
        binding.tilNewPasswordUser.error = null
        binding.tilConfirmNewPasswordUser.error = null
    }
}