package id.ac.istts.dkdm.myactivity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import id.ac.istts.dkdm.databinding.ActivityRegisterBinding
import id.ac.istts.dkdm.myapiconnection.APIConnection
import id.ac.istts.dkdm.mydatabase.AppDatabase
import id.ac.istts.dkdm.mydatabase.UserEntity
import id.ac.istts.dkdm.mydatabase.WalletEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.floor

@Suppress("DEPRECATION")
class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
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
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = AppDatabase.build(this)

        // SET VIEW
        resetErrorInput()
        binding.btnRegister.setOnClickListener {
            val nameRegister = binding.etNameRegister.text.toString()
            val usernameRegister = binding.etUsernameRegister.text.toString().lowercase()
            val passwordRegister = binding.etPasswordRegister.text.toString()
            val passwordConfirmationRegister = binding.etPasswordConfirmationRegister.text.toString()
            val userPINRegister = binding.etUserPIN.text.toString()
            resetErrorInput()

            if(nameRegister.isBlank() || usernameRegister.isBlank() || passwordRegister.isBlank() || passwordConfirmationRegister.isBlank() || userPINRegister.isBlank()){
                if(nameRegister.isBlank())
                    binding.tilNameRegister.error = "Name is required!"

                if(usernameRegister.isBlank())
                    binding.tilUsernameRegister.error = "Username is required!"

                if(passwordRegister.isBlank())
                    binding.tilPasswordRegister.error = "Password is required!"

                if(passwordConfirmationRegister.isBlank())
                    binding.tilPasswordConfirmationRegister.error = "Confirmation password is required!"

                if(userPINRegister.isBlank())
                    binding.tilUserPIN.error = "PIN is required!"
            } else if(usernameRegister == "admin") {
                binding.tilUsernameRegister.error = "Username can't be admin!"
            } else {
                coroutine.launch {
                    val tempUser = db.userDao.getFromUsername(usernameRegister)

                    if (tempUser != null) {
                        runOnUiThread {
                            binding.tilUsernameRegister.error = "This username is unavailable!"
                        }
                    } else {
                        if(passwordRegister != passwordConfirmationRegister){
                            runOnUiThread {
                                binding.tilPasswordConfirmationRegister.error = "Password confirmation must match Password!"
                            }
                        } else {
                            var isAccountNumberUnique = false
                            var tempAccountNumber: Long = 0

                            while (!isAccountNumberUnique){
                                isAccountNumberUnique = true
                                tempAccountNumber = floor(Math.random() * 9000000000L).toLong() + 1000000000L

                                for (user in db.userDao.getAllUsers()){
                                    if(user.accountNumber == tempAccountNumber){
                                        isAccountNumberUnique = false
                                        break
                                    }
                                }
                            }

                            val user = UserEntity(
                                name = nameRegister,
                                username = usernameRegister,
                                password = passwordRegister,
                                accountNumber = tempAccountNumber,
                                userPIN = userPINRegister.toInt(),
                                deleted_at = "null"
                            )

                            runOnUiThread {
                                APIConnection.insertUser(this@RegisterActivity, db, user)
                            }


                            val newWallet = WalletEntity(
                                wallet_id = -1,
                                username_user = usernameRegister,
                                walletName = "Main wallet",
                                walletBalance = 0,
                                isMainWallet = true,
                                deleted_at = "null"
                            )

                            runOnUiThread {
                                APIConnection.insertWallet(this@RegisterActivity, db, newWallet)
                                cleanInput()
                            }
                        }
                    }
                }
            }
        }

        binding.btnToLogin.setOnClickListener {
            finish()
        }
    }

    private fun resetErrorInput(){
        binding.tilNameRegister.error = null
        binding.tilUsernameRegister.error = null
        binding.tilPasswordRegister.error = null
        binding.tilPasswordConfirmationRegister.error = null
        binding.tilUserPIN.error = null
    }

    private fun cleanInput(){
        binding.etNameRegister.setText("")
        binding.etUsernameRegister.setText("")
        binding.etPasswordRegister.setText("")
        binding.etPasswordConfirmationRegister.setText("")
        binding.etUserPIN.setText("")
    }
}