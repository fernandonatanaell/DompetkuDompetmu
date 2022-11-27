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
            resetErrorInput()

            if(newWalletName.isBlank() || newWalletBalance.isBlank()){
                if(newWalletName.isBlank())
                    binding.tilNewWalletName.error = "Wallet name is required!"

                if(newWalletBalance.isBlank())
                    binding.tilNewWalletBalance.error = "Wallet balance is required!"
            } else {
                val earlyBalanceWallet = newWalletBalance.toLong()

                if(earlyBalanceWallet <= 0L){
                    binding.tilNewWalletBalance.error = "Wallet balance must be greater than 0!"
                } else {
                    val newWallet = WalletEntity(
                        username_user = usernameLogin,
                        walletName = newWalletName,
                        walletBalance = earlyBalanceWallet,
                        isMainWallet = false
                    )

                    coroutine.launch {
                        db.walletDao.insert(newWallet)
                        runOnUiThread {
                            Toast.makeText(this@UserAddWalletActivity, "Yayy! Successfully add a new wallet!", Toast.LENGTH_LONG).show()
                            val resultIntent = Intent()
                            setResult(Activity.RESULT_OK, resultIntent)
                            finish()
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
    }
}