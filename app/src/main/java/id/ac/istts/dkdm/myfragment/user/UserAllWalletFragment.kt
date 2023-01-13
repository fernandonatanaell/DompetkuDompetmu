package id.ac.istts.dkdm.myfragment.user

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import id.ac.istts.dkdm.R
import id.ac.istts.dkdm.databinding.FragmentUserAllWalletBinding
import id.ac.istts.dkdm.myadapter.RVWalletAdapter
import id.ac.istts.dkdm.myapiconnection.APIConnection
import id.ac.istts.dkdm.mydatabase.AppDatabase
import id.ac.istts.dkdm.mydatabase.WalletEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class UserAllWalletFragment(
    private var db: AppDatabase,
    private val coroutine: CoroutineScope,
    private var usernameLogin: String
) : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_all_wallet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentUserAllWalletBinding.bind(view)

        APIConnection.getWallets(view.context, db)

        // SET VIEW
        coroutine.launch {
            val tempAllMyWallets = db.walletDao.getAllMyWallet(usernameLogin) as ArrayList<WalletEntity>
            requireActivity().runOnUiThread {
                binding.rvAllMyWallets.adapter = RVWalletAdapter(view.context, usernameLogin, tempAllMyWallets){ nameAction: String, usernameLogin: String, selectedWallet: WalletEntity ->
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
                binding.rvAllMyWallets.layoutManager = GridLayoutManager(view.context, 2)
            }
        }
    }
}