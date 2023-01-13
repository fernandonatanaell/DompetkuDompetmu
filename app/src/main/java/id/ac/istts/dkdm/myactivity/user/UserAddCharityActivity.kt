package id.ac.istts.dkdm.myactivity.user

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.WindowManager
import android.webkit.MimeTypeMap
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.google.android.material.datepicker.MaterialDatePicker
import id.ac.istts.dkdm.databinding.ActivityUserAddCharityBinding
import id.ac.istts.dkdm.myapiconnection.APIConnection
import id.ac.istts.dkdm.mydatabase.AppDatabase
import id.ac.istts.dkdm.mydatabase.CharityEntity
import id.ac.istts.dkdm.mydatabase.WalletEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@Suppress("DEPRECATION")
class UserAddCharityActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserAddCharityBinding
    private lateinit var db: AppDatabase
    private val coroutine = CoroutineScope(Dispatchers.IO)

    // USER ATTRIBUTE
    private lateinit var usernameLogin: String
    private lateinit var selectedWallet: WalletEntity

    private var selectedImage: Uri? = null
    private var tempCharityEndDate: String = ""

    private val uploadImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result: ActivityResult ->
        if(result.resultCode == RESULT_OK){
            val data = result.data

            // Save the selected image to a global variable
            selectedImage = data?.data
            binding.tvNamePicture.text = getFileName(selectedImage!!)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TO HIDE STATUS BAR
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // TO SET BINDING VIEW
        binding = ActivityUserAddCharityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = AppDatabase.build(this)
        usernameLogin = intent.getStringExtra("usernameLogin").toString()

        APIConnection.getWallets(this, db)

        // SET VIEW
        coroutine.launch {
            val listOfMyWallet = db.walletDao.getAllMyWallet(usernameLogin) as ArrayList<WalletEntity>
            selectedWallet = listOfMyWallet[0]

            runOnUiThread {
                val adp1: ArrayAdapter<WalletEntity> = ArrayAdapter<WalletEntity>(this@UserAddCharityActivity, android.R.layout.simple_list_item_1, listOfMyWallet)
                adp1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                (binding.etWalletCharityList as? AutoCompleteTextView)?.setAdapter(adp1)
                binding.etWalletCharityList.setOnItemClickListener { adapterView, _, i, _ ->
                    selectedWallet = adapterView.getItemAtPosition(i) as WalletEntity
                }
                binding.etWalletCharityList.setText(binding.etWalletCharityList.adapter.getItem(0).toString(), false)
            }
        }

        binding.etEndCharityDate.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker().build()
            datePicker.show(supportFragmentManager, "DatePicker")

            // Setting up the event for when ok is clicked
            datePicker.addOnPositiveButtonClickListener {
                // formatting date in dd-mm-yyyy format.
                var dateFormatter = SimpleDateFormat("dd/MM/yyyy")
                binding.etEndCharityDate.setText(dateFormatter.format(Date(it)))

                dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                tempCharityEndDate = dateFormatter.format(Date(it))
            }
        }

        binding.btnSubmitCharity.setOnClickListener {
            val charityName = binding.etNewCharityName.text.toString()
            val charityDescription = binding.etNewCharityDescription.text.toString()
            val charityFundsGoal = binding.etNewCharityFundsGoal.text.toString()
            val userPIN = binding.etUserPINAddCharity.text.toString()
            val charityEndDate = tempCharityEndDate
            resetErrorInput()

            if(charityName.isBlank() || charityDescription.isBlank() || charityFundsGoal.isBlank() || userPIN.isBlank() || charityEndDate.isBlank() || selectedImage == null){
                if(charityName.isBlank())
                    binding.tilNewCharityName.error = "Charity name required!"

                if(charityDescription.isBlank())
                    binding.tilNewCharityDescription.error = "Charity description required!"

                if(charityFundsGoal.isBlank())
                    binding.tilAddCharityFundsGoal.error = "Fund goal required!"

                if(userPIN.isBlank())
                    binding.tilUserPINAddCharity.error = "PIN required!"

                if(charityEndDate.isBlank())
                    binding.tilEndCharityDate.error = "End date required!"
                
                if(selectedImage == null)
                    Toast.makeText(this, "Please select an image first!", Toast.LENGTH_SHORT).show()
            } else {
                coroutine.launch {
                    val getUserPin = db.userDao.getFromUsername(usernameLogin)!!.userPIN

                    if(userPIN.toInt() != getUserPin){
                        runOnUiThread {
                            Toast.makeText(
                                this@UserAddCharityActivity,
                                "Oopps! Your PIN is incorrect!",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        val dateNow = sdf.parse(sdf.format(Date()))
                        val dateEnd = sdf.parse(charityEndDate)
                        val diffMinutes: Long = (dateEnd.time - dateNow.time) / 1000 / 60 / 60

                        if (diffMinutes < 24) {
                            runOnUiThread {
                                binding.tilEndCharityDate.error = "End date must be 1 day earlier than current date"
                            }
                        } else {
                            if(charityFundsGoal.toLong() > 2000000000){
                                runOnUiThread {
                                    binding.tilAddCharityFundsGoal.error = "Fund goal can't be greater than Rp. 2 billion!"
                                }
                            } else if(charityFundsGoal.toInt() >= 10000) {
                                val getUser = db.userDao.getFromUsername(usernameLogin)

                                if(getUser!!.isUserBanned){
                                    runOnUiThread {
                                        val alert = AlertDialog.Builder(this@UserAddCharityActivity)
                                        alert.setTitle("Ooooppss!")
                                        alert.setMessage("Your account has been banned by Admin so you can't add new charities!!")

                                        alert.setPositiveButton("OK") { _, _ ->
                                        }

                                        alert.show()
                                    }
                                } else {
                                    val formatedDateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
                                    val imgName = "${System.currentTimeMillis()}.${MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(selectedImage!!))}"

                                    val newCharity = CharityEntity(
                                        charity_id = -1,
                                        charity_name = charityName,
                                        charity_description = charityDescription,
                                        source_id_wallet = selectedWallet.wallet_id,
                                        fundsGoal = charityFundsGoal.toLong(),
                                        charity_start_date = formatedDateTime,
                                        charity_end_date = charityEndDate,
                                        imgPath = imgName,
                                        deleted_at = "null"
                                    )

                                    runOnUiThread {
                                        val success = APIConnection.insertCharity(this@UserAddCharityActivity, db, newCharity, selectedImage!!)

                                        if (success){
                                            Toast.makeText(
                                                this@UserAddCharityActivity,
                                                "Yayy! A new charity was successfully added!",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            finish()
                                        }
                                    }
                                }
                            } else {
                                binding.tilAddCharityFundsGoal.error = "Fund goal must be greater than or equal to Rp 10.000!"
                            }
                        }
                    }
                }
            }
        }

        binding.btnSelectImage.setOnClickListener {
            openGallery()
        }

        binding.btnBackFromAddCharity.setOnClickListener {
            finish()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        uploadImageLauncher.launch(intent)
    }

    private fun resetErrorInput(){
        binding.tilNewCharityName.error = null
        binding.tilNewCharityDescription.error = null
        binding.tilAddCharityFundsGoal.error = null
        binding.tilUserPINAddCharity.error = null
        binding.tilEndCharityDate.error = null
    }

    private fun getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (columnIndex != -1) {
                        result = cursor.getString(columnIndex)
                    }
                }
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result!!.lastIndexOf('/')
            if (cut != -1) {
                result = result.substring(cut + 1)
            }
        }
        return result
    }
}