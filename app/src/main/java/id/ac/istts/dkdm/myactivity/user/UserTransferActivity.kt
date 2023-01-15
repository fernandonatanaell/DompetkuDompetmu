package id.ac.istts.dkdm.myactivity.user

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import id.ac.istts.dkdm.CurrencyUtils.toRupiah
import id.ac.istts.dkdm.databinding.ActivityUserTransferBinding
import id.ac.istts.dkdm.myapiconnection.APIConnection
import id.ac.istts.dkdm.mydatabase.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class UserTransferActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserTransferBinding
    private lateinit var db: AppDatabase
    private val coroutine = CoroutineScope(Dispatchers.IO)

    // USER ATTRIBUTE
    private lateinit var usernameLogin: String
    private var selectedContact: ContactEntity? = null
    private lateinit var selectedWallet: WalletEntity

    private val refreshLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result: ActivityResult ->
        if(result.resultCode == RESULT_OK){
            // REFRESH PAGE
            initSpinner()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TO HIDE STATUS BAR
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // TO SET BINDING VIEW
        binding = ActivityUserTransferBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = AppDatabase.build(this)

        APIConnection.getContacts(this, db)
        APIConnection.getWallets(this, db)

        // RECEIVE DATA usernameLogin
        usernameLogin = intent.getStringExtra("usernameLogin").toString()

        binding.btnBackFromTransfer.setOnClickListener {
            val resultIntent = Intent()
            setResult(Activity.RESULT_CANCELED, resultIntent)
            finish()
        }

        binding.btnToMyContact.setOnClickListener {
            refreshLauncher.launch(Intent(this, UserContactActivity::class.java).apply {
                putExtra("usernameLogin", usernameLogin)
            })
        }

        binding.btnSubmitTransfer.setOnClickListener {
            val transferDescription = binding.etDescriptionTransfer.text.toString()
            val transferAmount = binding.etAmountTransfer.text.toString()
            val userPIN = binding.etUserPINTransfer.text.toString()
            resetErrorInput()

            if(transferDescription.isBlank() || transferAmount.isBlank() || userPIN.isBlank() || selectedContact == null){
                if(transferDescription.isBlank())
                    binding.tilDescriptionTransfer.error = "Description required!"

                if(transferAmount.isBlank())
                    binding.tilAmountTransfer.error = "Amount required!"

                if(userPIN.isBlank())
                    binding.tilUserPINTransfer.error = "PIN required!"

                if(selectedContact == null)
                    binding.etSelectContactTransfer.error = "Contact must be selected!"
            } else if(transferAmount.toLong() >= 1000) {
                coroutine.launch {
                    val getUserPin = db.userDao.getFromUsername(usernameLogin)!!.userPIN

                    if(userPIN.toInt() != getUserPin){
                        runOnUiThread {
                            Toast.makeText(
                                this@UserTransferActivity,
                                "Oopps! Your PIN is incorrect!",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        var isBalanceWalletEnough = true
                        if(selectedWallet.walletBalance < transferAmount.toLong()){
                            isBalanceWalletEnough = false
                        }

                        if(isBalanceWalletEnough){

                            // FROM USER
                            var newHistory = HistoryEntity(
                                history_id = -1,
                                id_wallet = selectedWallet.wallet_id,
                                historyType = "Withdraw",
                                historyDescription = transferDescription,
                                historyAmount = transferAmount.toLong(),
                                deleted_at = "null"
                            )

                            runOnUiThread {
                                binding.clLoadingTransfer.visibility = View.VISIBLE
                                APIConnection.insertHistory(this@UserTransferActivity, db, newHistory)
                            }

                            delay(1000)

                            var selectedWallet = db.walletDao.get(newHistory.id_wallet)
                            selectedWallet!!.walletBalance -= newHistory.historyAmount

                            runOnUiThread {
                                APIConnection.updateWallet(this@UserTransferActivity, db, selectedWallet!!)
                            }

                            delay(1000)

                            var newNotifications = NotificationEntity(
                                notification_id = -1,
                                notification_text = "Your transfer of Rp ${transferAmount.toLong().toRupiah()} in ${selectedWallet!!.walletName} to ${selectedContact!!.username_friend} has been successful.",
                                username_user = usernameLogin,
                                deleted_at = "null"
                            )

                            runOnUiThread {
                                APIConnection.insertNotification(this@UserTransferActivity, db, newNotifications)
                            }

                            delay(1000)

                            // TO USER
                            selectedWallet = db.walletDao.getMainWallet(selectedContact!!.username_friend)
                            selectedWallet!!.walletBalance += transferAmount.toLong()

                            runOnUiThread {
                                APIConnection.updateWallet(this@UserTransferActivity, db, selectedWallet!!)
                            }

                            delay(1000)

                            newHistory = HistoryEntity(
                                history_id = -1,
                                id_wallet = selectedWallet!!.wallet_id,
                                historyType = "Income",
                                historyDescription = transferDescription,
                                historyAmount = transferAmount.toLong(),
                                deleted_at = "null"
                            )

                            runOnUiThread {
                                APIConnection.insertHistory(this@UserTransferActivity, db, newHistory)
                            }

                            delay(1000)

                            newNotifications = NotificationEntity(
                                notification_id = -1,
                                notification_text = "You get Rp ${transferAmount.toLong().toRupiah()} to ${selectedWallet.walletName} from ${usernameLogin}.",
                                username_user = selectedContact!!.username_friend,
                                deleted_at = "null"
                            )

                            runOnUiThread {
                                APIConnection.insertNotification(this@UserTransferActivity, db, newNotifications)
                            }

                            delay(1000)

                            runOnUiThread {
                                binding.clLoadingTransfer.visibility = View.GONE

                                val resultIntent = Intent()
                                setResult(Activity.RESULT_OK, resultIntent)
                                finish()
                            }
                        } else {
                            runOnUiThread {
                                Toast.makeText(
                                    this@UserTransferActivity,
                                    "Oopps! You have insufficient balance!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            } else {
                binding.tilAmountTransfer.error = "Amount must be greater than Rp 999!"
            }
        }

        initSpinner()
    }

    private fun initSpinner(){
        selectedContact = null

        coroutine.launch {
            // INIT CONTACT SPINNER
            val listOfMyContact = db.contactDao.getAllContacts(usernameLogin) as ArrayList<ContactEntity>

            runOnUiThread {
                val adp1: ArrayAdapter<ContactEntity> = ArrayAdapter<ContactEntity>(this@UserTransferActivity, android.R.layout.simple_list_item_1, listOfMyContact)
                adp1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                (binding.etSelectContactTransfer as? AutoCompleteTextView)?.setAdapter(adp1)
                binding.etSelectContactTransfer.setOnItemClickListener { adapterView, _, i, _ ->
                    selectedContact = adapterView.getItemAtPosition(i) as ContactEntity
                }
                if(listOfMyContact.size > 0) {
                    binding.etSelectContactTransfer.setText(binding.etSelectContactTransfer.adapter.getItem(0).toString(), false)
                    selectedContact = listOfMyContact[0]
                } else {
                    binding.etSelectContactTransfer.setText("", false)
                }
            }

            // INIT WALLET SPINNER
            val listOfMyWallet = db.walletDao.getAllMyWallet(usernameLogin) as ArrayList<WalletEntity>
            selectedWallet = listOfMyWallet[0]

            runOnUiThread {
                val adp2: ArrayAdapter<WalletEntity> = ArrayAdapter<WalletEntity>(this@UserTransferActivity, android.R.layout.simple_list_item_1, listOfMyWallet)
                adp2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                (binding.etSelectWalletTransfer as? AutoCompleteTextView)?.setAdapter(adp2)
                binding.etSelectWalletTransfer.setOnItemClickListener { adapterView, _, i, _ ->
                    selectedWallet = adapterView.getItemAtPosition(i) as WalletEntity
                }
                binding.etSelectWalletTransfer.setText(binding.etSelectWalletTransfer.adapter.getItem(0).toString(), false)
            }
        }
    }

    private fun resetErrorInput(){
        binding.etSelectContactTransfer.error = null
        binding.tilDescriptionTransfer.error = null
        binding.tilAmountTransfer.error = null
        binding.tilUserPINTransfer.error = null
    }
}