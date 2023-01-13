package id.ac.istts.dkdm.myfragment.user

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.ac.istts.dkdm.R
import id.ac.istts.dkdm.databinding.FragmentUserCharityBinding
import id.ac.istts.dkdm.myactivity.user.UserMakeDonationActivity
import id.ac.istts.dkdm.myactivity.user.UserSearchCharityActivity
import id.ac.istts.dkdm.myadapter.RVCharityAdapter
import id.ac.istts.dkdm.mydatabase.AppDatabase
import id.ac.istts.dkdm.mydatabase.CharityEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDate

class UserCharityFragment(
    private var db: AppDatabase,
    private val coroutine: CoroutineScope,
    private var usernameLogin: String
) : Fragment() {
    private lateinit var binding: FragmentUserCharityBinding

    private val refreshLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result: ActivityResult ->
        if(result.resultCode == AppCompatActivity.RESULT_OK){
            // REFRESH PAGE
            parentFragmentManager
                .beginTransaction()
                .replace(R.id.mainFL, UserCharityFragment(db, coroutine, usernameLogin))
                .commit()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_charity, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentUserCharityBinding.bind(view)

        // SET VIEW
        coroutine.launch {
            // FOR YOU
            val arr = db.charityDao.getAllCharityExceptThisUser(usernameLogin) as ArrayList<CharityEntity>
            val tempArr = ArrayList<CharityEntity>()
            arr.shuffle()
            for(k in 0..9) {
                if(k < arr.size) {
                    tempArr.add(arr[k])
                }
            }
            loadCharity(view, binding.rvForYouCharity , tempArr)

            // URGENT
            val current = LocalDate.now().toString()
            loadCharity(view, binding.rvUrgentCharity , db.charityDao.getAllCharityUrgent(usernameLogin, current) as ArrayList<CharityEntity>)

            // LATEST
            val currentLatest = LocalDate.now().toString()
            loadCharity(view, binding.rvLatestCharity , db.charityDao.getAllCharityLatest(usernameLogin, currentLatest) as ArrayList<CharityEntity>)
        }

        binding.btnToMyCharity.setOnClickListener {
            parentFragmentManager
                .beginTransaction()
                .replace(R.id.mainFL, UserMyCharityFragment(db, coroutine, usernameLogin))
                .commit()
        }

        binding.etSearchCharity.setOnClickListener {
            val intent = Intent(view.context, UserSearchCharityActivity::class.java)
            intent.putExtra("usernameLogin", usernameLogin)
            startActivity(intent)
        }
    }

    private fun loadCharity(view: View, rv: RecyclerView , listOfCharity: ArrayList<CharityEntity>){
        requireActivity().runOnUiThread {
            rv.adapter = RVCharityAdapter(listOfCharity, false){ selectedCharityId: Int ->
                refreshLauncher.launch(Intent(view.context, UserMakeDonationActivity::class.java).apply {
                    putExtra("usernameLogin", usernameLogin)
                    putExtra("selectedCharityId", selectedCharityId)
                })
            }
            rv.layoutManager = LinearLayoutManager(view.context, LinearLayoutManager.HORIZONTAL, false)
        }
    }
}