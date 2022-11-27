package id.ac.istts.dkdm.myfragment.user

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.recyclerview.widget.LinearLayoutManager
import id.ac.istts.dkdm.CurrencyUtils.toRupiah
import id.ac.istts.dkdm.R
import id.ac.istts.dkdm.databinding.FragmentUserHistoryBinding
import id.ac.istts.dkdm.myadapter.RVHistoryAdapter
import id.ac.istts.dkdm.mydatabase.AppDatabase
import id.ac.istts.dkdm.mydatabase.HistoryEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class UserHistoryFragment(
    private var db: AppDatabase,
    private val coroutine: CoroutineScope,
    private var usernameLogin: String
) : Fragment() {
    private lateinit var listOfMyHistory: ArrayList<HistoryEntity>
    private lateinit var tempListOfMyHistory: ArrayList<HistoryEntity>
    private lateinit var adapterListOfMyWallet: RVHistoryAdapter

    private var selectedDate = "All"
    private var selectedType = "All"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentUserHistoryBinding.bind(view)

        // SET VIEW
        coroutine.launch {
            listOfMyHistory = db.historyDao.getAllMyHistory(usernameLogin) as ArrayList<HistoryEntity>

            tempListOfMyHistory = ArrayList()
            tempListOfMyHistory.addAll(listOfMyHistory)

            adapterListOfMyWallet = RVHistoryAdapter(db, coroutine, tempListOfMyHistory)

            requireActivity().runOnUiThread {
                binding.rvUserHistory.layoutManager = LinearLayoutManager(view.context)
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

            requireActivity().runOnUiThread {
                val adp1: ArrayAdapter<String> = ArrayAdapter<String>(view.context, android.R.layout.simple_list_item_1, listOfDate)
                adp1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                (binding.etDateHistory as? AutoCompleteTextView)?.setAdapter(adp1)
                binding.etDateHistory.setOnItemClickListener { adapterView, _, i, _ ->
                    selectedDate = adapterView.getItemAtPosition(i).toString()
                    filter(binding)
                }
                binding.etDateHistory.setText(binding.etDateHistory.adapter.getItem(0).toString(), false)

                val adp2: ArrayAdapter<String> = ArrayAdapter<String>(view.context, android.R.layout.simple_list_item_1, arrayListOf("All", "Withdraw", "Income"))
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

    private fun filter(binding: FragmentUserHistoryBinding){
        coroutine.launch {

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
                    if(history.historyType != selectedType){
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
            requireActivity().runOnUiThread {
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
    }
}