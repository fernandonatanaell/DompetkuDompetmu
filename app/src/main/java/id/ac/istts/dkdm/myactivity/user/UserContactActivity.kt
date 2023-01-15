package id.ac.istts.dkdm.myactivity.user

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import id.ac.istts.dkdm.databinding.ActivityUserContactBinding
import id.ac.istts.dkdm.myadapter.RVContactAdapter
import id.ac.istts.dkdm.myapiconnection.APIConnection
import id.ac.istts.dkdm.mydatabase.AppDatabase
import id.ac.istts.dkdm.mydatabase.ContactEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class UserContactActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserContactBinding
    private lateinit var db: AppDatabase
    private val coroutine = CoroutineScope(Dispatchers.IO)

    // USER ATTRIBUTE
    private lateinit var usernameLogin: String
    private lateinit var adapterContact: RVContactAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TO HIDE STATUS BAR
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // TO SET BINDING VIEW
        binding = ActivityUserContactBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = AppDatabase.build(this)

        APIConnection.getUsers(this, db)
        APIConnection.getContacts(this, db)

        // RECEIVE DATA FROM TRANSFER ACTIVITY
        usernameLogin = intent.getStringExtra("usernameLogin").toString()

        // SET VIEW
        binding.btnBackFromContact.setOnClickListener {
            val resultIntent = Intent()
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        binding.btnAddFriend.setOnClickListener {
            val tempAccountNumber = binding.etFriendAccountNumber.text.toString()
            val userPIN = binding.etUserPINContact.text.toString()
            resetErrorInput()

            if(tempAccountNumber.isBlank() || userPIN.isBlank()){
                if(tempAccountNumber.isBlank())
                    binding.tilFriendAccountNumber.error = "Account number required!"

                if(userPIN.isBlank())
                    binding.tilUserPINContact.error = "PIN required!"
            } else {
                coroutine.launch {
                    val getUserPin = db.userDao.getFromUsername(usernameLogin)!!.userPIN

                    if(userPIN.toInt() != getUserPin){
                        runOnUiThread {
                            Toast.makeText(
                                this@UserContactActivity,
                                "Oopps! Your PIN is incorrect!",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        val getFriendUser = db.userDao.checkAccountNumber(tempAccountNumber.toLong())

                        if(getFriendUser == null){
                            runOnUiThread {
                                Toast.makeText(
                                    this@UserContactActivity,
                                    "Oopps! User not found!",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } else if(getFriendUser.username == usernameLogin) {
                            runOnUiThread {
                                Toast.makeText(
                                    this@UserContactActivity,
                                    "Oopps! You can't enter your own PIN!",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } else {
                            if(db.contactDao.checkContact(usernameLogin, getFriendUser.username) != null){
                                runOnUiThread {
                                    Toast.makeText(
                                        this@UserContactActivity,
                                        "Oopps! User is already in the contacts!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            } else {
                                val newContact = ContactEntity (
                                    contact_id = -1,
                                    username_user = usernameLogin,
                                    username_friend = getFriendUser.username,
                                    deleted_at = "null"
                                )

                                runOnUiThread {
                                    APIConnection.insertContact(this@UserContactActivity, db, newContact)
                                    binding.etFriendAccountNumber.setText("")
                                    binding.etUserPINContact.setText("")
                                    initRV()
                                }
                            }
                        }
                    }
                }
            }
        }

        initRV()
    }

    private fun resetErrorInput(){
        binding.tilFriendAccountNumber.error = null
        binding.tilUserPINContact.error = null
    }

    private fun initRV(){
        coroutine.launch {
            adapterContact = RVContactAdapter(this@UserContactActivity, db, coroutine, db.contactDao.getAllContacts(usernameLogin) as ArrayList<ContactEntity>){ contact_id->
                coroutine.launch {
                    val getContact = db.contactDao.getContact(contact_id)
                    val nameDeletedUser =  db.userDao.getFromUsername(getContact!!.username_friend)!!.name

                    runOnUiThread {
                        APIConnection.deleteContact(this@UserContactActivity, db, contact_id)

                        initRV()
                    }
                }
            }

            runOnUiThread {
                binding.rvContact.layoutManager = LinearLayoutManager(this@UserContactActivity)
                binding.rvContact.adapter = adapterContact
            }
        }
    }
}