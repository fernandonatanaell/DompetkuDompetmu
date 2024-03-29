package id.ac.istts.dkdm.myactivity.user

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.recyclerview.widget.LinearLayoutManager
import id.ac.istts.dkdm.CurrencyUtils.toRupiah
import id.ac.istts.dkdm.databinding.ActivityUserHistoryBinding
import id.ac.istts.dkdm.myadapter.RVHistoryAdapter
import id.ac.istts.dkdm.myapiconnection.APIConnection
import id.ac.istts.dkdm.mydatabase.AppDatabase
import id.ac.istts.dkdm.mydatabase.HistoryEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class UserHistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserHistoryBinding
    private lateinit var db: AppDatabase
    private val coroutine = CoroutineScope(Dispatchers.IO)

    // USER ATTRIBUTE
    private lateinit var usernameLogin: String

    private lateinit var listOfMyHistory: ArrayList<HistoryEntity>
    private lateinit var tempListOfMyHistory: ArrayList<HistoryEntity>
    private lateinit var adapterListOfMyWallet: RVHistoryAdapter

    private var selectedDate = "All"
    private var selectedType = "All"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TO HIDE STATUS BAR
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // TO SET BINDING VIEW
        binding = ActivityUserHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = AppDatabase.build(this)

        // RECEIVE DATA FROM TRANSFER ACTIVITY
        usernameLogin = intent.getStringExtra("usernameLogin").toString()

        coroutine.launch {
           runOnUiThread {
                APIConnection.getHistories(this@UserHistoryActivity, db)
            }

            delay(500)

            runOnUiThread {
                // SET VIEW
                coroutine.launch {
                    listOfMyHistory = db.historyDao.getAllMyHistory(usernameLogin) as ArrayList<HistoryEntity>

                    tempListOfMyHistory = ArrayList()
                    tempListOfMyHistory.addAll(listOfMyHistory)

                    adapterListOfMyWallet = RVHistoryAdapter(db, coroutine, tempListOfMyHistory)

                    runOnUiThread {
                        binding.rvUserHistory.layoutManager = LinearLayoutManager(this@UserHistoryActivity)
                        binding.rvUserHistory.adapter = adapterListOfMyWallet
                    }

                    val listOfDate: ArrayList<String> = ArrayList()
                    listOfDate.add("All")
                    var totalSpendingUser: Long = 0
                    var totalIncomeUser: Long = 0

                    for (history in tempListOfMyHistory){
                        if(history.historyType == "Withdraw"){
                            totalSpendingUser += history.historyAmount
                        } else {
                            totalIncomeUser += history.historyAmount
                        }

                        if(!listOfDate.contains(history.historyDate))
                            listOfDate.add(history.historyDate)
                    }

                    runOnUiThread {
                        val adp1: ArrayAdapter<String> = ArrayAdapter<String>(this@UserHistoryActivity, android.R.layout.simple_list_item_1, listOfDate)
                        adp1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        (binding.etDateHistory as? AutoCompleteTextView)?.setAdapter(adp1)
                        binding.etDateHistory.setOnItemClickListener { adapterView, _, i, _ ->
                            selectedDate = adapterView.getItemAtPosition(i).toString()
                            filter(binding)
                        }
                        binding.etDateHistory.setText(binding.etDateHistory.adapter.getItem(0).toString(), false)

                        val adp2: ArrayAdapter<String> = ArrayAdapter<String>(this@UserHistoryActivity, android.R.layout.simple_list_item_1, arrayListOf("All", "Withdraw", "Income"))
                        adp2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        (binding.etTypeHistory as? AutoCompleteTextView)?.setAdapter(adp2)
                        binding.etTypeHistory.setOnItemClickListener { adapterView, _, i, _ ->
                            selectedType = adapterView.getItemAtPosition(i).toString()
                            filter(binding)
                        }
                        binding.etTypeHistory.setText(binding.etTypeHistory.adapter.getItem(0).toString(), false)

                        if(totalSpendingUser > 0)
                            binding.tvTotalSpendingHistory.text = "Rp ${totalSpendingUser.toRupiah()},00"

                        if(totalIncomeUser > 0)
                            binding.tvTotalIncomeHistory.text = "Rp ${totalIncomeUser.toRupiah()},00"
                    }
                }
            }
        }

        binding.btnBackFromHistory.setOnClickListener {
            val resultIntent = Intent()
            setResult(Activity.RESULT_CANCELED, resultIntent)
            finish()
        }
    }

    private fun filter(binding: ActivityUserHistoryBinding){
            var totalSpendingUser: Long = 0
            var totalIncomeUser: Long = 0
            tempListOfMyHistory.clear()

            for (history in listOfMyHistory){
                var isSafe = true

                if(selectedDate != "All"){
                    if(history.historyDate != selectedDate){
                        isSafe = false
                    }
                }

                if(selectedType != "All"){
                    if(history.historyDate != selectedType){
                        isSafe = false
                    }
                }

                if(isSafe){
                    if(history.historyType == "Withdraw")
                        totalSpendingUser += history.historyAmount
                    else
                        totalIncomeUser += history.historyAmount

                    tempListOfMyHistory.add(history)
                }
            }

            if(totalSpendingUser > 0)
                binding.tvTotalSpendingHistory.text = "Rp ${totalSpendingUser.toRupiah()},00"
            else
                binding.tvTotalSpendingHistory.text = "-"

            if(totalIncomeUser > 0)
                binding.tvTotalIncomeHistory.text = "Rp ${totalIncomeUser.toRupiah()},00"
            else
                binding.tvTotalIncomeHistory.text = "-"

            adapterListOfMyWallet.notifyDataSetChanged()
    }
}