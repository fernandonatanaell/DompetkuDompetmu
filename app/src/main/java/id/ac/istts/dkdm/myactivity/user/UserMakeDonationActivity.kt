package id.ac.istts.dkdm.myactivity.user

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.core.text.bold
import id.ac.istts.dkdm.CurrencyUtils.toRupiah
import id.ac.istts.dkdm.databinding.ActivityUserMakeDonationBinding
import id.ac.istts.dkdm.mydatabase.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class UserMakeDonationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserMakeDonationBinding
    private lateinit var db: AppDatabase
    private val coroutine = CoroutineScope(Dispatchers.IO)

    // USER ATTRIBUTE
    private lateinit var usernameLogin: String
    private lateinit var selectedCharity: CharityEntity
    private lateinit var selectedWallet: WalletEntity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TO HIDE STATUS BAR
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // TO SET BINDING VIEW
        binding = ActivityUserMakeDonationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = AppDatabase.build(this)

        // RECEIVE DATA
        usernameLogin = intent.getStringExtra("usernameLogin").toString()

        // SET VIEW
        coroutine.launch {
            selectedCharity = db.charityDao.getCharity(intent.getIntExtra("selectedCharityId", -1))
            val listOfMyWallet = db.walletDao.getAllMyWallet(usernameLogin) as ArrayList<WalletEntity>
            selectedWallet = listOfMyWallet[0]

            runOnUiThread {
                val adp1: ArrayAdapter<WalletEntity> = ArrayAdapter<WalletEntity>(this@UserMakeDonationActivity, android.R.layout.simple_list_item_1, listOfMyWallet)
                adp1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                (binding.etWalletListDonation as? AutoCompleteTextView)?.setAdapter(adp1)
                binding.etWalletListDonation.setOnItemClickListener { adapterView, _, i, _ ->
                    selectedWallet = adapterView.getItemAtPosition(i) as WalletEntity
                }
                binding.etWalletListDonation.setText(binding.etWalletListDonation.adapter.getItem(0).toString(), false)

                binding.tvNameCharityDonation.text = selectedCharity.charity_name
                binding.tvDescCharityDonation.text = selectedCharity.charity_description
                binding.tvfFundRaisedDonation.text = "Rp ${selectedCharity.fundsRaised.toRupiah()},00"
                binding.tvfFundGoalDonation.text = SpannableStringBuilder().append("Raised from ").bold { append("Rp ${selectedCharity.fundsGoal.toRupiah()},00") }

                binding.pbDonation.max = selectedCharity.fundsGoal.toInt()
                binding.pbDonation.progress = selectedCharity.fundsRaised.toInt()
            }
        }

        binding.btnSubmitDonation.setOnClickListener {
            val amountDonation = binding.etAmountDonation.text.toString()
            val userPIN = binding.etUserPINMakeDonation.text.toString()
            resetErrorInput()

            if(amountDonation.isBlank() || userPIN.isBlank()){
                if(amountDonation.isBlank())
                    binding.tilAmountDonation.error = "Amount required!"

                if(userPIN.isBlank())
                    binding.tilUserPINMakeDonation.error = "PIN required!"
            } else if(amountDonation.toLong() < 5000){
                binding.tilAmountDonation.error = "Amount must be greater than Rp 5000!"
            } else {
                coroutine.launch {
                    val getUserPin = db.userDao.getFromUsername(usernameLogin)!!.userPIN

                    if(userPIN.toInt() != getUserPin){
                        runOnUiThread {
                            Toast.makeText(this@UserMakeDonationActivity, "Oopps! Your PIN is incorrect!", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        if(amountDonation.toLong() > selectedWallet.walletBalance) {
                            runOnUiThread {
                                Toast.makeText(
                                    this@UserMakeDonationActivity,
                                    "Oopps! You have insufficient balance!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            if(selectedCharity.fundsRaised + amountDonation.toLong() > selectedCharity.fundsGoal){
                                runOnUiThread {
                                    Toast.makeText(
                                        this@UserMakeDonationActivity,
                                        "Oopps! Your donations exceed the limit!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                selectedCharity.fundsRaised += amountDonation.toLong()

                                // UPDATE CHARITY
                                if(selectedCharity.fundsRaised == selectedCharity.fundsGoal){
                                    selectedCharity.isCharityIsOver = true
                                }
                                db.charityDao.update(selectedCharity)


                                // UPDATE WALLET BALANCE USER WHO DONATE
                                selectedWallet.walletBalance -= amountDonation.toLong()
                                db.walletDao.update(selectedWallet)

                                // USER WHO DONATED TO THE CHARITY
                                var newNotifications = NotificationEntity(
                                    notification_text = "Your Rp ${amountDonation.toLong().toRupiah()} to '${selectedCharity.charity_name}' is successful donated.",
                                    username_user = usernameLogin
                                )
                                db.notificationDao.insert(newNotifications)
                                var newHistory = HistoryEntity(
                                    id_wallet = selectedWallet.wallet_id,
                                    historyType = "Withdraw",
                                    historyDescription = "Make a donation to '${selectedCharity.charity_name}'",
                                    historyAmount = amountDonation.toLong()
                                )
                                db.historyDao.insert(newHistory)


                                // UPDATE WALLET BALANCE USER WHO RECEIVED DONATED
                                val walletDonated = db.walletDao.get(selectedCharity.source_id_wallet)
                                walletDonated!!.walletBalance += amountDonation.toLong()
                                db.walletDao.update(walletDonated!!)

                                // OWNER CHARITY
                                newNotifications = NotificationEntity(
                                    notification_text = "Your '${selectedCharity.charity_name}' have received a donation of Rp ${amountDonation.toLong().toRupiah()} from ${usernameLogin}.",
                                    username_user = walletDonated.username_user
                                )
                                db.notificationDao.insert(newNotifications)
                                newHistory = HistoryEntity(
                                    id_wallet = selectedCharity.source_id_wallet,
                                    historyType = "Income",
                                    historyDescription = "Received a donation from $usernameLogin to '${selectedCharity.charity_name}'",
                                    historyAmount = amountDonation.toLong()
                                )
                                db.historyDao.insert(newHistory)

                                runOnUiThread {
                                    Toast.makeText(
                                        this@UserMakeDonationActivity,
                                        "Yayy! Successfully donated!",
                                        Toast.LENGTH_SHORT
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
        }

        binding.btnBackFromDonation.setOnClickListener {
            val resultIntent = Intent()
            setResult(Activity.RESULT_CANCELED, resultIntent)
            finish()
        }
    }

    private fun resetErrorInput(){
        binding.tilAmountDonation.error = null
        binding.tilUserPINMakeDonation.error = null
    }
}