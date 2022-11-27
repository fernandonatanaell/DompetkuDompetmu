package id.ac.istts.dkdm.myactivity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import id.ac.istts.dkdm.databinding.ActivityLoginBinding
import id.ac.istts.dkdm.myactivity.admin.AdminMainActivity
import id.ac.istts.dkdm.myactivity.user.UserMainActivity
import id.ac.istts.dkdm.mydatabase.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var db: AppDatabase
    private val coroutine = CoroutineScope(Dispatchers.IO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TO HIDE STATUS BAR
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // TO SET BINDING VIEW
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = AppDatabase.build(this)

        // SET VIEW
        resetErrorInput()
        binding.btnLogin.setOnClickListener {
            val usernameLogin = binding.etUsernameLogin.text.toString()
            val passwordLogin = binding.etPasswordLogin.text.toString()
            resetErrorInput()

            if(usernameLogin.isBlank() || passwordLogin.isBlank()){
                if(usernameLogin.isBlank())
                    binding.tilUsernameLogin.error = "Username is required!"

                if(passwordLogin.isBlank())
                    binding.tilPasswordLogin.error = "Password is required!"
            } else {
                if(usernameLogin == "admin" && passwordLogin == "nimda"){
                    startActivity(Intent(this@LoginActivity, AdminMainActivity::class.java))
                } else {
                    coroutine.launch {
                        val tempUser = db.userDao.checkUsername(usernameLogin)

                        if (tempUser == null) {
                            runOnUiThread {
                                Toast.makeText(this@LoginActivity, "Huhhh.. User not found!", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            if(tempUser.password != passwordLogin){
                                runOnUiThread {
                                    binding.tilPasswordLogin.error = "Wrong password!"
                                }
                            } else {
                                val intent = Intent(this@LoginActivity, UserMainActivity::class.java)
                                intent.putExtra("usernameLogin", usernameLogin)
                                startActivity(intent)
                            }
                        }
                    }
                }
            }
        }

        binding.btnToRegister.setOnClickListener {
            startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
        }
    }

    private fun resetErrorInput(){
        binding.tilUsernameLogin.error = null
        binding.tilPasswordLogin.error = null
    }
}