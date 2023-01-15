package id.ac.istts.dkdm.myfragment.user

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import id.ac.istts.dkdm.CurrencyUtils.toRupiah
import id.ac.istts.dkdm.R
import id.ac.istts.dkdm.databinding.FragmentUserHomepageBinding
import id.ac.istts.dkdm.myactivity.user.UserAddCharityActivity
import id.ac.istts.dkdm.myactivity.user.UserAddWalletActivity
import id.ac.istts.dkdm.myactivity.user.UserHistoryActivity
import id.ac.istts.dkdm.myactivity.user.UserTransactionActivity
import id.ac.istts.dkdm.myadapter.RVWalletAdapter
import id.ac.istts.dkdm.myapiconnection.APIConnection
import id.ac.istts.dkdm.mydatabase.AppDatabase
import id.ac.istts.dkdm.mydatabase.WalletEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class UserHomepageFragment(
    private var db: AppDatabase,
    private val coroutine: CoroutineScope,
    private var usernameLogin: String
) : Fragment() {
    private val refreshLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result: ActivityResult ->
        if(result.resultCode == AppCompatActivity.RESULT_OK){
            // REFRESH PAGE
            parentFragmentManager
                .beginTransaction()
                .replace(R.id.mainFL, UserHomepageFragment(db, coroutine, usernameLogin))
                .commit()

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_homepage, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentUserHomepageBinding.bind(view)

        APIConnection.getWallets(view.context, db)

        // SET VIEW
        binding.tvDateToday.text = SimpleDateFormat("EEEE\ndd MMM yyyy").format(Date())

        binding.ivAddNewWallet.setOnClickListener {
            refreshLauncher.launch(Intent(view.context, UserAddWalletActivity::class.java).apply {
                putExtra("usernameLogin", usernameLogin)
            })
        }

        binding.btnToTopup.setOnClickListener {
            refreshLauncher.launch(Intent(view.context, UserTransactionActivity::class.java).apply {
                putExtra("usernameLogin", usernameLogin)
                putExtra("isWithdraw", "false")
            })
        }

        binding.btnToWithdraw.setOnClickListener {
            refreshLauncher.launch(Intent(view.context, UserTransactionActivity::class.java).apply {
                putExtra("usernameLogin", usernameLogin)
                putExtra("isWithdraw", "true")
            })
        }

        binding.btnToMyCharityHomepage.setOnClickListener {
            val intent = Intent(view.context, UserAddCharityActivity::class.java)
            intent.putExtra("usernameLogin", usernameLogin)
            startActivity(intent)
        }

        binding.btnToHistory.setOnClickListener {
            val intent = Intent(view.context, UserHistoryActivity::class.java)
            intent.putExtra("usernameLogin", usernameLogin)
            startActivity(intent)
        }

        coroutine.launch {
            val userLogin = db.userDao.getFromUsername(usernameLogin)
            requireActivity().runOnUiThread {
                binding.tvUsernameHomepage.text = usernameLogin
                binding.tvNameUserHomepage.text = userLogin!!.name
                binding.tvNorekUserHomepage.text = userLogin!!.accountNumber.toString()
            }

            val tempMyTopFourWallet = db.walletDao.getTop4(usernameLogin) as ArrayList<WalletEntity>
            val tempAllMyWallet = db.walletDao.getAllMyWallet(usernameLogin) as ArrayList<WalletEntity>
            var totalBalanceUser: Long = 0

            for (wallet in tempAllMyWallet){
                totalBalanceUser += wallet.walletBalance
            }

            requireActivity().runOnUiThread {
                binding.tvTotalAssetHomepage.text = "Rp ${totalBalanceUser.toRupiah()},00"
                binding.rvUserWallets.adapter = RVWalletAdapter(view.context, usernameLogin, tempMyTopFourWallet){ nameAction: String, usernameLogin: String, selectedWallet: WalletEntity ->
                    if(nameAction == "delete"){
                        coroutine.launch {
                            // UPDATE TO MAIN WALLET
                            val getMainWallet = db.walletDao.getMainWallet(usernameLogin)
                            getMainWallet!!.walletBalance += selectedWallet.walletBalance

                            requireActivity().runOnUiThread {
                                APIConnection.updateWallet(view.context, db, getMainWallet!!)
                                APIConnection.deleteWallet(view.context, db, selectedWallet.wallet_id)
                            }

                            // HABIS DI DELETE REFRESH PAGENYA SUPAYA RECYCLE VIEW E JUGA KE UPDATE
                            requireActivity().runOnUiThread {
                                parentFragmentManager
                                    .beginTransaction()
                                    .replace(R.id.mainFL, UserHomepageFragment(db, coroutine, usernameLogin))
                                    .commit()
                            }
                        }
                    }
                }
                binding.rvUserWallets.layoutManager = GridLayoutManager(view.context, 2)
            }
        }
    }
}