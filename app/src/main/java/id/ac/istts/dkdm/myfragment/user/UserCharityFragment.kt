package id.ac.istts.dkdm.myfragment.user

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import id.ac.istts.dkdm.R
import id.ac.istts.dkdm.databinding.FragmentUserCharityBinding
import id.ac.istts.dkdm.myactivity.user.UserMakeDonationActivity
import id.ac.istts.dkdm.myadapter.RVCharityAdapter
import id.ac.istts.dkdm.mydatabase.AppDatabase
import id.ac.istts.dkdm.mydatabase.CharityEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

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
            loadCharity(view, db.charityDao.getAllCharityExceptThisUser(usernameLogin) as ArrayList<CharityEntity>)
        }

        binding.btnToMyCharity.setOnClickListener {
//            val getImageFromGalleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
//            startActivity(getImageFromGalleryIntent)

            parentFragmentManager
                .beginTransaction()
                .replace(R.id.mainFL, UserMyCharityFragment(db, coroutine, usernameLogin))
                .commit()
        }

        binding.etSearchCharity.addTextChangedListener (object: TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                coroutine.launch {
                    if(s.toString().isBlank()){
                        loadCharity(view, db.charityDao.getAllCharityExceptThisUser(usernameLogin) as ArrayList<CharityEntity>)
                    } else {
                        loadCharity(view, db.charityDao.getAllCharityExceptThisUserFilter(usernameLogin, binding.etSearchCharity.text.toString()) as ArrayList<CharityEntity>)
                    }
                }
            }
        })
    }

    private fun loadCharity(view: View, listOfCharity: ArrayList<CharityEntity>){
        requireActivity().runOnUiThread {
            binding.rvAllCharity.adapter = RVCharityAdapter(listOfCharity, false){ selectedCharityId: Int ->
                refreshLauncher.launch(Intent(view.context, UserMakeDonationActivity::class.java).apply {
                    putExtra("usernameLogin", usernameLogin)
                    putExtra("selectedCharityId", selectedCharityId)
                })
            }
            binding.rvAllCharity.layoutManager = LinearLayoutManager(view.context)
        }
    }
}