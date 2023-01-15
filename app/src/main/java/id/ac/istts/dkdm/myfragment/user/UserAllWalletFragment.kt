package id.ac.istts.dkdm.myfragment.user

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import id.ac.istts.dkdm.R
import id.ac.istts.dkdm.databinding.FragmentUserAllWalletBinding
import id.ac.istts.dkdm.myadapter.RVWalletAdapter
import id.ac.istts.dkdm.myapiconnection.APIConnection
import id.ac.istts.dkdm.mydatabase.AppDatabase
import id.ac.istts.dkdm.mydatabase.WalletEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate

class UserAllWalletFragment(
    private var db: AppDatabase,
    private val coroutine: CoroutineScope,
    private var usernameLogin: String
) : Fragment() {
    private lateinit var loadingAnim: ConstraintLayout

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
        loadingAnim = requireActivity().findViewById(R.id.clLoading)

        coroutine.launch {
            requireActivity().runOnUiThread {
                APIConnection.getWallets(view.context, db)
                APIConnection.getCharities(view.context, db)
            }

            delay(500)

            requireActivity().runOnUiThread {
                // SET VIEW
                coroutine.launch {
                    val tempAllMyWallets = db.walletDao.getAllMyWallet(usernameLogin) as ArrayList<WalletEntity>
                    requireActivity().runOnUiThread {
                        binding.rvAllMyWallets.adapter = RVWalletAdapter(view.context, usernameLogin, tempAllMyWallets){ nameAction: String, usernameLogin: String, selectedWallet: WalletEntity ->
                            if(nameAction == "delete"){
                                coroutine.launch {
                                    val checkWalletFromCharity = db.charityDao.getCharityFromWallet(selectedWallet.wallet_id, LocalDate.now().toString())

                                    if(checkWalletFromCharity?.size != 0) {
                                        requireActivity().runOnUiThread {
                                            Toast.makeText(
                                                view.context,
                                                "Oopps! Can't delete this wallet because it's being used for charity!",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    } else {
                                        // UPDATE TO MAIN WALLET
                                        val getMainWallet = db.walletDao.getMainWallet(usernameLogin)
                                        getMainWallet!!.walletBalance += selectedWallet.walletBalance

                                        requireActivity().runOnUiThread {
                                            loadingAnim.visibility = View.VISIBLE
                                            APIConnection.updateWallet(view.context, db, getMainWallet!!)
                                            APIConnection.deleteWallet(view.context, db, selectedWallet.wallet_id)
                                        }

                                        delay(4000)

                                        requireActivity().runOnUiThread {
                                            loadingAnim.visibility = View.GONE

                                            // HABIS DI DELETE REFRESH PAGENYA SUPAYA RECYCLE VIEW E JUGA KE UPDATE
                                            parentFragmentManager
                                                .beginTransaction()
                                                .replace(R.id.mainFL, UserHomepageFragment(db, coroutine, usernameLogin))
                                                .commit()
                                        }
                                    }
                                }
                            }
                        }
                        binding.rvAllMyWallets.layoutManager = GridLayoutManager(view.context, 2)
                    }
                }
            }
        }
    }
}