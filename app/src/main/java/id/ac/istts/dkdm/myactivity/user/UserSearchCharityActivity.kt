package id.ac.istts.dkdm.myactivity.user

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import id.ac.istts.dkdm.databinding.ActivityUserSearchCharityBinding
import id.ac.istts.dkdm.myadapter.RVCharityAdapter
import id.ac.istts.dkdm.mydatabase.AppDatabase
import id.ac.istts.dkdm.mydatabase.CharityEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class UserSearchCharityActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserSearchCharityBinding
    private lateinit var db: AppDatabase
    private val coroutine = CoroutineScope(Dispatchers.IO)

    // USER ATTRIBUTE
    private lateinit var usernameLogin: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TO HIDE STATUS BAR
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // TO SET BINDING VIEW
        binding = ActivityUserSearchCharityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = AppDatabase.build(this)

        // RECEIVE DATA
        usernameLogin = intent.getStringExtra("usernameLogin").toString()

        coroutine.launch {
            loadCharity(db.charityDao.getAllCharityExceptThisUser(usernameLogin) as ArrayList<CharityEntity>)
        }

        binding.etSearchCharity.addTextChangedListener (object: TextWatcher {
            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                coroutine.launch {
                    if(s.toString().isBlank()){
                        loadCharity(db.charityDao.getAllCharityExceptThisUser(usernameLogin) as ArrayList<CharityEntity>)
                    } else {
                        loadCharity(db.charityDao.getAllCharityExceptThisUserFilter(usernameLogin, binding.etSearchCharity.text.toString()) as ArrayList<CharityEntity>)
                    }
                }
            }
        })

        binding.btnBackFromSearchCharity.setOnClickListener {
            finish()
        }
    }

    private fun loadCharity(listOfCharity: ArrayList<CharityEntity>){
        runOnUiThread {
            binding.rvSearchCharity.adapter = RVCharityAdapter(listOfCharity, true){ selectedCharityId: Int ->
                val intent = Intent(this, UserMakeDonationActivity::class.java)
                intent.putExtra("usernameLogin", usernameLogin)
                intent.putExtra("selectedCharityId", selectedCharityId)
                startActivity(intent)
            }
            binding.rvSearchCharity.layoutManager = LinearLayoutManager(this)
        }
    }
}