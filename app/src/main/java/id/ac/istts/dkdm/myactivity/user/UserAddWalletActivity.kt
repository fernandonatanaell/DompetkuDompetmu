package id.ac.istts.dkdm.myactivity.user

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import id.ac.istts.dkdm.databinding.ActivityUserAddWalletBinding
import id.ac.istts.dkdm.mydatabase.AppDatabase
import id.ac.istts.dkdm.mydatabase.WalletEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class UserAddWalletActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserAddWalletBinding
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
        binding = ActivityUserAddWalletBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = AppDatabase.build(this)

        // RECEIVE DATA FROM HOMEPAGE FRAGMENT
        usernameLogin = intent.getStringExtra("usernameLogin").toString()

        // SET VIEW
        binding.btnSubmitAddWallet.setOnClickListener {
            val newWalletName = binding.etNewWalletName.text.toString()
            val newWalletBalance = binding.etNewWalletBalance.text.toString()
            val userPIN = binding.etUserPINAddWallet.text.toString()
            resetErrorInput()

            if(newWalletName.isBlank() || newWalletBalance.isBlank() || userPIN.isBlank()){
                if(newWalletName.isBlank())
                    binding.tilNewWalletName.error = "Wallet name required!"

                if(newWalletBalance.isBlank())
                    binding.tilNewWalletBalance.error = "The balance required!"

                if(userPIN.isBlank())
                    binding.tilUserPINAddWallet.error = "PIN required!"
            } else {
                coroutine.launch {
                    val getUserPin = db.userDao.getFromUsername(usernameLogin)!!.userPIN

                    if(userPIN.toInt() != getUserPin){
                        runOnUiThread {
                            Toast.makeText(
                                this@UserAddWalletActivity,
                                "Oopps! Your PIN is incorrect!",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        val earlyBalanceWallet = newWalletBalance.toLong()

                        if(earlyBalanceWallet < 5000L){
                            runOnUiThread {
                                binding.tilNewWalletBalance.error = "The balance should be greater than Rp 5.000!"
                            }
                        } else {
                            val newWallet = WalletEntity(
                                username_user = usernameLogin,
                                walletName = newWalletName,
                                walletBalance = earlyBalanceWallet,
                                isMainWallet = false,
                                deleted_at = null
                            )

                            db.walletDao.insert(newWallet)
                            runOnUiThread {
                                Toast.makeText(
                                    this@UserAddWalletActivity,
                                    "Yayy! Successfully added new wallet!",
                                    Toast.LENGTH_LONG
                                ).show()

                                val resultIntent = Intent()
                                setResult(Activity.RESULT_OK, resultIntent)
                                finish()
                            }
                        }
                    }
                }
            }
        }

        binding.btnBackFromAddWallet.setOnClickListener {
            val resultIntent = Intent()
            setResult(Activity.RESULT_CANCELED, resultIntent)
            finish()
        }
    }

    private fun resetErrorInput(){
        binding.tilNewWalletName.error = null
        binding.tilNewWalletBalance.error = null
        binding.tilUserPINAddWallet.error = null
    }
}