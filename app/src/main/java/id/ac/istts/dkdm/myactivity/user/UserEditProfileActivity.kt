package id.ac.istts.dkdm.myactivity.user

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import id.ac.istts.dkdm.databinding.ActivityUserEditProfileBinding
import id.ac.istts.dkdm.myapiconnection.APIConnection
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
            val newNameUser = binding.etNameUser.text.toString()
            resetErrorInput()

            if(newNameUser.isBlank()){
                binding.tilNameUser.error = "Name required!"
            } else {
                coroutine.launch {
                    val newPasswordUser = binding.etNewPasswordUser.text.toString()
                    val newConfirmPasswordUser = binding.etConfirmNewPasswordUser.text.toString()
                    val getCurrentUser = db.userDao.getFromUsername(usernameLogin)

                    getCurrentUser!!.name = newNameUser
                    if(newPasswordUser.isNotBlank()) {
                        if(newPasswordUser != newConfirmPasswordUser) {
                            runOnUiThread {
                                binding.tilConfirmNewPasswordUser.error = "Password confirmation must match Password!"
                            }
                            return@launch
                        }

                        getCurrentUser.password = newPasswordUser
                    }

                    runOnUiThread {
                        APIConnection.updateUser(this@UserEditProfileActivity, db, getCurrentUser)
                        Toast.makeText(this@UserEditProfileActivity, "Yayy! Your account has been successfully updated!", Toast.LENGTH_LONG).show()
                    }

                    val resultIntent = Intent()
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                }
            }
        }
    }

    private fun resetErrorInput(){
        binding.tilNameUser.error = null
        binding.tilNewPasswordUser.error = null
        binding.tilConfirmNewPasswordUser.error = null
    }
}