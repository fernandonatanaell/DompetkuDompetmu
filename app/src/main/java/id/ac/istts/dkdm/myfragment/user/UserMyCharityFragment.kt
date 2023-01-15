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
import androidx.recyclerview.widget.LinearLayoutManager
import id.ac.istts.dkdm.R
import id.ac.istts.dkdm.databinding.FragmentUserMyCharityBinding
import id.ac.istts.dkdm.myactivity.user.UserMakeDonationActivity
import id.ac.istts.dkdm.myadapter.RVCharityAdapter
import id.ac.istts.dkdm.myapiconnection.APIConnection
import id.ac.istts.dkdm.mydatabase.AppDatabase
import id.ac.istts.dkdm.mydatabase.CharityEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class UserMyCharityFragment(
    private var db: AppDatabase,
    private val coroutine: CoroutineScope,
    private var usernameLogin: String
) : Fragment() {
    private lateinit var binding: FragmentUserMyCharityBinding

    private val refreshLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result: ActivityResult ->
        if(result.resultCode == AppCompatActivity.RESULT_OK){
            // REFRESH PAGE
            parentFragmentManager
                .beginTransaction()
                .replace(R.id.mainFL, UserMyCharityFragment(db, coroutine, usernameLogin))
                .commit()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_my_charity, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentUserMyCharityBinding.bind(view)

        coroutine.launch {
            requireActivity().runOnUiThread {
                APIConnection.getCharities(view.context, db)
            }

            delay(500)

            requireActivity().runOnUiThread {
                // SET VIEW
                coroutine.launch {
                    val tempAllCharity = db.charityDao.getAllMyCharity(usernameLogin) as ArrayList<CharityEntity>
                    requireActivity().runOnUiThread {
                        binding.rvAllMyCharity.adapter = RVCharityAdapter(tempAllCharity, true){ selectedCharityId: Int ->
                            refreshLauncher.launch(Intent(view.context, UserMakeDonationActivity::class.java).apply {
                                putExtra("usernameLogin", usernameLogin)
                                putExtra("selectedCharityId", selectedCharityId)
                            })
                        }
                        binding.rvAllMyCharity.layoutManager = LinearLayoutManager(view.context)
                    }
                }
            }
        }
    }
}