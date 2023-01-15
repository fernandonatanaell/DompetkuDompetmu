package id.ac.istts.dkdm.myactivity.user

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import id.ac.istts.dkdm.CurrencyUtils.toRupiah
import id.ac.istts.dkdm.databinding.ActivityUserTransactionBinding
import id.ac.istts.dkdm.myapiconnection.APIConnection
import id.ac.istts.dkdm.mydatabase.AppDatabase
import id.ac.istts.dkdm.mydatabase.HistoryEntity
import id.ac.istts.dkdm.mydatabase.NotificationEntity
import id.ac.istts.dkdm.mydatabase.WalletEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class UserTransactionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserTransactionBinding
    private lateinit var db: AppDatabase
    private val coroutine = CoroutineScope(Dispatchers.IO)

    // USER ATTRIBUTE
    private lateinit var usernameLogin: String
    private var inWithdrawMode: Boolean = true
    private lateinit var selectedWallet: WalletEntity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TO HIDE STATUS BAR
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // TO SET BINDING VIEW
        binding = ActivityUserTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = AppDatabase.build(this)

        // SET MODE WITHDRAW / TOP UP
        if(intent.getStringExtra("isWithdraw").toString() == "false"){
            inWithdrawMode = false
            changeMode("Top up")
        }
        usernameLogin = intent.getStringExtra("usernameLogin").toString()

        APIConnection.getWallets(this, db)

        // SET VIEW
        coroutine.launch {
            val listOfMyWallet = db.walletDao.getAllMyWallet(usernameLogin) as ArrayList<WalletEntity>
            selectedWallet = listOfMyWallet[0]

            runOnUiThread {
                val adp1: ArrayAdapter<WalletEntity> = ArrayAdapter<WalletEntity>(this@UserTransactionActivity, android.R.layout.simple_list_item_1, listOfMyWallet)
                adp1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                (binding.etWalletList as? AutoCompleteTextView)?.setAdapter(adp1)
                binding.etWalletList.setOnItemClickListener { adapterView, _, i, _ ->
                    selectedWallet = adapterView.getItemAtPosition(i) as WalletEntity
                }
                binding.etWalletList.setText(binding.etWalletList.adapter.getItem(0).toString(), false)
            }
        }

        binding.btnToTopupMode.setOnClickListener {
            changeMode("Top up")
        }

        binding.btnToSpendingMode.setOnClickListener {
            changeMode("Withdraw")
        }

        binding.btnSubmitTransaction.setOnClickListener {
            val transactionDescription = binding.etDescriptionTransaction.text.toString()
            val transactionAmount = binding.etAmountTransaction.text.toString()
            val userPIN = binding.etUserPINTransfer.text.toString()
            resetErrorInput()

            if(transactionAmount.isBlank() || transactionDescription.isBlank() || userPIN.isBlank()){
                if(transactionAmount.isBlank())
                    binding.tilAmountTransaction.error = "Amount required!"

                if(transactionDescription.isBlank())
                    binding.tilDescriptionTransaction.error = "Description required!"

                if(userPIN.isBlank())
                    binding.tilUserPINTransfer.error = "PIN required!"
            } else if(transactionAmount.toLong() > 499) {
                coroutine.launch {
                    val getUserPin = db.userDao.getFromUsername(usernameLogin)!!.userPIN

                    if(userPIN.toInt() != getUserPin){
                        runOnUiThread {
                            Toast.makeText(
                                this@UserTransactionActivity,
                                "Oopps! Your PIN is incorrect!",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        var isBalanceWalletEnough = true
                        if(inWithdrawMode && selectedWallet.walletBalance < transactionAmount.toLong()){
                            isBalanceWalletEnough = false
                        }

                        if(isBalanceWalletEnough){
                            val newHistory = HistoryEntity(
                                history_id = -1,
                                id_wallet = selectedWallet.wallet_id,
                                historyType = if (inWithdrawMode) "Withdraw" else "Income",
                                historyDescription = transactionDescription,
                                historyAmount = transactionAmount.toLong(),
                                deleted_at = "null"
                            )

                            runOnUiThread {
                                APIConnection.insertHistory(this@UserTransactionActivity, db, newHistory)
                            }

                            val newNotifications: NotificationEntity

                            val selectedWallet = db.walletDao.get(newHistory.id_wallet)
                            if(newHistory.historyType == "Withdraw"){
                                selectedWallet!!.walletBalance -= newHistory.historyAmount

                                newNotifications = NotificationEntity(
                                    notification_id = -1,
                                    notification_text = "Your withdraw of Rp ${transactionAmount.toLong().toRupiah()} in ${selectedWallet!!.walletName} is successful.",
                                    username_user = usernameLogin,
                                    deleted_at = "null"
                                )
                            } else {
                                selectedWallet!!.walletBalance += newHistory.historyAmount

                                newNotifications = NotificationEntity(
                                    notification_id = -1,
                                    notification_text = "Your top up of Rp ${transactionAmount.toLong().toRupiah()} in ${selectedWallet!!.walletName} is successful.",
                                    username_user = usernameLogin,
                                    deleted_at = "null"
                                )
                            }

                            runOnUiThread {
                                binding.clLoadingTransaction.visibility = View.VISIBLE
                                APIConnection.updateWallet(this@UserTransactionActivity, db, selectedWallet)
                                APIConnection.insertNotification(this@UserTransactionActivity, db, newNotifications)
                            }

                            delay(5000)

                            runOnUiThread {
                                binding.clLoadingTransaction.visibility = View.GONE

                                val resultIntent = Intent()
                                setResult(Activity.RESULT_OK, resultIntent)
                                finish()
                            }
                        } else {
                            runOnUiThread {
                                Toast.makeText(
                                    this@UserTransactionActivity,
                                    "Oopps! You have insufficient balance!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            } else {
                binding.tilAmountTransaction.error = "Amount must be greater than Rp 499!"
            }
        }

        binding.btnBackFromTransaction.setOnClickListener {
            val resultIntent = Intent()
            setResult(Activity.RESULT_CANCELED, resultIntent)
            finish()
        }
    }

    @SuppressLint("ResourceAsColor")
    private fun changeMode(nameMode: String){
        if(nameMode == "Withdraw"){
            binding.btnToSpendingMode.setBackgroundColor(Color.parseColor("#0161a3"))
            binding.btnToSpendingMode.setTextColor(Color.parseColor("#FFFFFF"))

            binding.btnToTopupMode.setBackgroundColor(Color.parseColor("#DEECFC"))
            binding.btnToTopupMode.setTextColor(Color.parseColor("#000000"))

            inWithdrawMode = true
        } else {
            binding.btnToTopupMode.setBackgroundColor(Color.parseColor("#0161a3"))
            binding.btnToTopupMode.setTextColor(Color.parseColor("#FFFFFF"))

            binding.btnToSpendingMode.setBackgroundColor(Color.parseColor("#DEECFC"))
            binding.btnToSpendingMode.setTextColor(Color.parseColor("#000000"))

            inWithdrawMode = false
        }
    }

    private fun resetErrorInput(){
        binding.tilDescriptionTransaction.error = null
        binding.tilAmountTransaction.error = null
        binding.tilUserPINTransfer.error = null
    }
}