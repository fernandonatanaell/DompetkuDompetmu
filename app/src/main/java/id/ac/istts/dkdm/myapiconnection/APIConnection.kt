package id.ac.istts.dkdm.myapiconnection

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Network
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.ImageRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import id.ac.istts.dkdm.mydatabase.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class APIConnection {

    companion object {
        val BASE_URL = "https://api-mdp.lukasbudi.my.id"
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val coroutine = CoroutineScope(Dispatchers.IO)

        fun toastError(context: Context, type: String) {
            var message = "Offline mode: Data may not be up-to-date."

            if (type == "insert"){
                message = "Offline mode: Data insertion unavailable."
            } else if (type == "update"){
                message = "Offline mode: Data update unavailable."
            } else if (type == "delete"){
                message = "Offline mode: Data deletion unavailable."
            }

            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }

        //    === USERS ===
        fun toDBUsers(obj: JSONArray, db: AppDatabase, sharedPref: SharedPreferences){
            coroutine.launch {
                for (i in 0 until obj.length()){
                    val user = obj.getJSONObject(i)
                    val username = user.getString("username")
                    val password = user.getString("password")
                    val name = user.getString("name")
                    val accountNumber = user.getLong("accountNumber")
                    val userPIN = user.getInt("userPin")
                    val isUserBanned = user.getInt("isUserBanned")
                    val deleted_at = user.getString("deleted_at")

                    val newUser = UserEntity(
                        name = name,
                        username = username,
                        password = password,
                        accountNumber = accountNumber,
                        userPIN = userPIN,
                        isUserBanned = isUserBanned == 1,
                        deleted_at = deleted_at
                    )

                    val checkUser = db.userDao.getFromUsername(username)
                    if (checkUser == null){
                        db.userDao.insert(newUser)
                    } else {
                        db.userDao.update(newUser)
                    }
                }
                sharedPref.edit().putString("users", LocalDateTime.now().format(formatter)).apply()
            }

        }

        fun getUsers(context: Context, db: AppDatabase){
            val sharedPref = context.getSharedPreferences("id.ac.istts.dkdm.myactivity", MODE_PRIVATE)

            var last_updated = sharedPref.getString("users", null)
            Log.d("last_updated", last_updated.toString())

            if (last_updated == ""){
                // get all users
                val strReq = object : StringRequest(
                    Method.GET,
                    "$BASE_URL/users",
                    Response.Listener {
                        val obj: JSONArray = JSONArray(it)
                        toDBUsers(obj, db, sharedPref)
                        sharedPref.edit().putString("status", "online").apply()
                    },
                    Response.ErrorListener {
                        if (sharedPref.getString("status", "offline") != "select"){
                            toastError(context, "select")
                            sharedPref.edit().putString("status", "select").apply()
                        }
                    }
                ){}

                val queue: RequestQueue = Volley.newRequestQueue(context)
                queue.add(strReq)
            } else {
                // get all users that updated after last_updated
                if (last_updated != null) {
                    last_updated = last_updated.replace(" ", "&")
                }
                val strReq = object : StringRequest(
                    Method.GET,
                    "$BASE_URL/users/latest/$last_updated",
                    Response.Listener {
                        val obj: JSONArray = JSONArray(it)
                        toDBUsers(obj, db, sharedPref)
                        sharedPref.edit().putString("status", "online").apply()
                    },
                    Response.ErrorListener {
                        if (sharedPref.getString("status", "offline") != "select"){
                            toastError(context, "select")
                            sharedPref.edit().putString("status", "select").apply()
                        }
                    }
                ){}

                val queue: RequestQueue = Volley.newRequestQueue(context)
                queue.add(strReq)
            }
        }

        fun insertUser(context: Context, db: AppDatabase, user: UserEntity): Boolean {
            val sharedPref = context.getSharedPreferences("id.ac.istts.dkdm.myactivity", MODE_PRIVATE)
            var success = false
            val strReq = object : StringRequest(
                Method.POST,
                "$BASE_URL/users",
                Response.Listener {
                    sharedPref.edit().putString("status", "online").apply()
                    getUsers(context, db)
                    success = true
                },
                Response.ErrorListener {
                    toastError(context, "insert")
                    sharedPref.edit().putString("status", "insert").apply()
                }
            ){
                override fun getParams(): MutableMap<String, String> {
                    val params = HashMap<String, String>()
                    params["username"] = user.username
                    params["password"] = user.password
                    params["name"] = user.name
                    params["accountNumber"] = user.accountNumber.toString()
                    params["userPin"] = user.userPIN.toString()
                    return params
                }
            }

            val queue: RequestQueue = Volley.newRequestQueue(context)
            queue.add(strReq)

            return success
        }

        fun updateUser(context: Context, db: AppDatabase, user: UserEntity): Boolean {
            val sharedPref = context.getSharedPreferences("id.ac.istts.dkdm.myactivity", MODE_PRIVATE)
            var success = false
            val strReq = object : StringRequest(
                Method.PUT,
                "$BASE_URL/users/username/${user.username}",
                Response.Listener {
                    sharedPref.edit().putString("status", "online").apply()
                    getUsers(context, db)
                    success = true
                },
                Response.ErrorListener {
                    toastError(context, "update")
                    sharedPref.edit().putString("status", "update").apply()
                }
            ){
                override fun getParams(): MutableMap<String, String> {
                    val params = HashMap<String, String>()
                    params["password"] = user.password
                    params["name"] = user.name
                    params["accountNumber"] = user.accountNumber.toString()
                    params["userPin"] = user.userPIN.toString()
                    params["isUserBanned"] = if (user.isUserBanned) "1" else "0"
                    return params
                }
            }

            val queue: RequestQueue = Volley.newRequestQueue(context)
            queue.add(strReq)

            return success
        }


        //    === WALLETS ===
        fun toDBWallets(obj: JSONArray, db: AppDatabase, sharedPref: SharedPreferences){
            coroutine.launch {
                for (i in 0 until obj.length()){
                    val wallet = obj.getJSONObject(i)
                    val wallet_id = wallet.getInt("wallet_id")
                    val username_user = wallet.getString("username_user")
                    val walletName = wallet.getString("walletName")
                    val walletBalance = wallet.getLong("walletBalance")
                    val isMainWallet = wallet.getInt("isMainWallet")
                    val deleted_at = wallet.getString("deleted_at")

                    val newWallet = WalletEntity(
                        wallet_id = wallet_id,
                        username_user = username_user,
                        walletName = walletName,
                        walletBalance = walletBalance,
                        isMainWallet = isMainWallet == 1,
                        deleted_at = deleted_at
                    )

                    val checkWallet = db.walletDao.get(wallet_id)
                    if (checkWallet == null){
                        db.walletDao.insert(newWallet)
                    } else {
                        db.walletDao.update(newWallet)
                    }
                }
                sharedPref.edit().putString("wallets", LocalDateTime.now().format(formatter)).apply()
            }
        }

        fun getWallets(context: Context, db: AppDatabase){
            val sharedPref = context.getSharedPreferences("id.ac.istts.dkdm.myactivity", MODE_PRIVATE)

            var last_updated = sharedPref.getString("wallets", null)

            if (last_updated == ""){
                // get all wallets
                val strReq = object : StringRequest(
                    Method.GET,
                    "$BASE_URL/wallets",
                    Response.Listener {
                        val obj: JSONArray = JSONArray(it)
                        toDBWallets(obj, db, sharedPref)
                        sharedPref.edit().putString("status", "online").apply()
                    },
                    Response.ErrorListener {
                        if (sharedPref.getString("status", "offline") != "select"){
                            toastError(context, "select")
                            sharedPref.edit().putString("status", "select").apply()
                        }
                    }
                ){}

                val queue: RequestQueue = Volley.newRequestQueue(context)
                queue.add(strReq)
            } else {
                // get all wallets that updated after last_updated
                if (last_updated != null) {
                    last_updated = last_updated.replace(" ", "&")
                }
                val strReq = object : StringRequest(
                    Method.GET,
                    "$BASE_URL/wallets/latest/$last_updated",
                    Response.Listener {
                        val obj: JSONArray = JSONArray(it)
                        toDBWallets(obj, db, sharedPref)
                        sharedPref.edit().putString("status", "online").apply()
                    },
                    Response.ErrorListener {
                        if (sharedPref.getString("status", "offline") != "select"){
                            toastError(context, "select")
                            sharedPref.edit().putString("status", "select").apply()
                        }
                    }
                ){}

                val queue: RequestQueue = Volley.newRequestQueue(context)
                queue.add(strReq)
            }
        }

        fun insertWallet(context: Context, db: AppDatabase, wallet: WalletEntity): Boolean{
            val sharedPref = context.getSharedPreferences("id.ac.istts.dkdm.myactivity", MODE_PRIVATE)
            var success = false
            val strReq = object : StringRequest(
                Method.POST,
                "$BASE_URL/wallets",
                Response.Listener {
                    sharedPref.edit().putString("status", "online").apply()
                    getWallets(context, db)
                    success = true
                },
                Response.ErrorListener {
                    toastError(context, "insert")
                    sharedPref.edit().putString("status", "insert").apply()
                }
            ){
                override fun getParams(): MutableMap<String, String> {
                    val params = HashMap<String, String>()
                    params["username_user"] = wallet.username_user
                    params["walletName"] = wallet.walletName
                    params["walletBalance"] = wallet.walletBalance.toString()
                    params["isMainWallet"] = if (wallet.isMainWallet) "1" else "0"
                    return params
                }
            }

            val queue: RequestQueue = Volley.newRequestQueue(context)
            queue.add(strReq)
            return success
        }

        fun updateWallet(context: Context, db: AppDatabase, wallet: WalletEntity): Boolean{
            val sharedPref = context.getSharedPreferences("id.ac.istts.dkdm.myactivity", MODE_PRIVATE)
            var success = false
            val strReq = object : StringRequest(
                Method.PUT,
                "$BASE_URL/wallets/id/${wallet.wallet_id}",
                Response.Listener {
                    sharedPref.edit().putString("status", "online").apply()
                    getWallets(context, db)
                    success = true
                },
                Response.ErrorListener {
                    toastError(context, "update")
                    sharedPref.edit().putString("status", "update").apply()
                }
            ){
                override fun getParams(): MutableMap<String, String> {
                    val params = HashMap<String, String>()
                    params["username_user"] = wallet.username_user
                    params["walletName"] = wallet.walletName
                    params["walletBalance"] = wallet.walletBalance.toString()
                    params["isMainWallet"] = if (wallet.isMainWallet) "1" else "0"
                    return params
                }
            }

            val queue: RequestQueue = Volley.newRequestQueue(context)
            queue.add(strReq)
            return success
        }

        fun deleteWallet(context: Context, db: AppDatabase, id: Int): Boolean{
            val sharedPref = context.getSharedPreferences("id.ac.istts.dkdm.myactivity", MODE_PRIVATE)
            var success = false
            val strReq = object : StringRequest(
                Method.DELETE,
                "$BASE_URL/wallets/id/${id}",
                Response.Listener {
                    sharedPref.edit().putString("status", "online").apply()
                    getWallets(context, db)
                    success = true
                },
                Response.ErrorListener {
                    toastError(context, "delete")
                    sharedPref.edit().putString("status", "delete").apply()
                }
            ){}

            val queue: RequestQueue = Volley.newRequestQueue(context)
            queue.add(strReq)
            return success
        }

        //    === HISTORIES ===
        fun toDBHistories(obj: JSONArray, db: AppDatabase, sharedPref: SharedPreferences){
            coroutine.launch {
                for (i in 0 until obj.length()){
                    val history = obj.getJSONObject(i)
                    val history_id = history.getInt("history_id")
                    val id_wallet = history.getInt("id_wallet")
                    val historyType = history.getString("historyType")
                    val historyDescription = history.getString("historyDescription")
                    val historyAmount = history.getLong("historyAmount")
                    val historyDate = history.getString("historyDate")
                    val deleted_at = history.getString("deleted_at")

                    val newHistory = HistoryEntity(
                        history_id = history_id,
                        id_wallet = id_wallet,
                        historyType = historyType,
                        historyDescription = historyDescription,
                        historyAmount = historyAmount,
                        historyDate = historyDate,
                        deleted_at = deleted_at
                    )

                    val checkHistory = db.historyDao.getHistory(history_id)
                    if (checkHistory == null){
                        db.historyDao.insert(newHistory)
                    } else {
                        db.historyDao.update(newHistory)
                    }
                }
                sharedPref.edit().putString("histories", LocalDateTime.now().format(formatter)).apply()
            }
        }

        fun getHistories(context: Context, db: AppDatabase){
            val sharedPref = context.getSharedPreferences("id.ac.istts.dkdm.myactivity", MODE_PRIVATE)
            var last_updated = sharedPref.getString("histories", null)
            if (last_updated == ""){
                // get all histories
                val strReq = object : StringRequest(
                    Method.GET,
                    "$BASE_URL/histories",
                    Response.Listener {
                        val obj: JSONArray = JSONArray(it)
                        toDBHistories(obj, db, sharedPref)
                        sharedPref.edit().putString("status", "online").apply()
                    },
                    Response.ErrorListener {
                        if (sharedPref.getString("status", "offline") != "select"){
                            toastError(context, "select")
                            sharedPref.edit().putString("status", "select").apply()
                        }
                    }
                ){}

                val queue: RequestQueue = Volley.newRequestQueue(context)
                queue.add(strReq)
            } else {
                // get all histories that updated after last_updated
                if (last_updated != null) {
                    last_updated = last_updated.replace(" ", "&")
                }
                val strReq = object : StringRequest(
                    Method.GET,
                    "$BASE_URL/histories/latest/$last_updated",
                    Response.Listener {
                        val obj: JSONArray = JSONArray(it)
                        toDBHistories(obj, db, sharedPref)
                        sharedPref.edit().putString("status", "online").apply()
                    },
                    Response.ErrorListener {
                        if (sharedPref.getString("status", "offline") != "select"){
                            toastError(context, "select")
                            sharedPref.edit().putString("status", "select").apply()
                        }
                    }
                ){}

                val queue: RequestQueue = Volley.newRequestQueue(context)
                queue.add(strReq)
            }
        }

        fun insertHistory(context: Context, db: AppDatabase, history: HistoryEntity): Boolean{
            val sharedPref = context.getSharedPreferences("id.ac.istts.dkdm.myactivity", MODE_PRIVATE)
            var success = false
            val strReq = object : StringRequest(
                Method.POST,
                "$BASE_URL/histories",
                Response.Listener {
                    sharedPref.edit().putString("status", "online").apply()
                    getHistories(context, db)
                    success = true
                },
                Response.ErrorListener {
                    toastError(context, "insert")
                    sharedPref.edit().putString("status", "insert").apply()
                }
            ){
                override fun getParams(): MutableMap<String, String> {
                    val params = HashMap<String, String>()
                    params["id_wallet"] = history.id_wallet.toString()
                    params["historyType"] = history.historyType
                    params["historyDescription"] = history.historyDescription
                    params["historyAmount"] = history.historyAmount.toString()
                    params["historyDate"] = history.historyDate
                    return params
                }
            }

            val queue: RequestQueue = Volley.newRequestQueue(context)
            queue.add(strReq)
            return success
        }

        //    === NOTIFICATIONS ===

        fun toDBNotifications(obj: JSONArray, db: AppDatabase, sharedPref: SharedPreferences){
            coroutine.launch {
                for (i in 0 until obj.length()){
                    val notification = obj.getJSONObject(i)
                    val notification_id = notification.getInt("notification_id")
                    val notification_text = notification.getString("notification_text")
                    val username_user = notification.getString("username_user")
                    val notification_date = notification.getString("notification_date")
                    val userAlreadySee = notification.getInt("userAlreadySee")
                    val deleted_at = notification.getString("deleted_at")

                    val newNotification = NotificationEntity(
                        notification_id = notification_id,
                        notification_text = notification_text,
                        username_user = username_user,
                        notification_date = notification_date,
                        userAlreadySee = userAlreadySee == 1,
                        deleted_at = deleted_at
                    )

                    val checkNotification = db.notificationDao.get(notification_id)
                    if (checkNotification == null){
                        db.notificationDao.insert(newNotification)
                    } else {
                        db.notificationDao.update(newNotification)
                    }
                }
                sharedPref.edit().putString("notifications", LocalDateTime.now().format(formatter)).apply()
            }
        }

        fun getNotifications(context: Context, db: AppDatabase){
            val sharedPref = context.getSharedPreferences("id.ac.istts.dkdm.myactivity", MODE_PRIVATE)
            var last_updated = sharedPref.getString("notifications", null)
            if (last_updated == ""){
                // get all notifications
                val strReq = object : StringRequest(
                    Method.GET,
                    "$BASE_URL/notifications",
                    Response.Listener {
                        val obj: JSONArray = JSONArray(it)
                        toDBNotifications(obj, db, sharedPref)
                        sharedPref.edit().putString("status", "online").apply()
                    },
                    Response.ErrorListener {
                        if (sharedPref.getString("status", "offline") != "select"){
                            toastError(context, "select")
                            sharedPref.edit().putString("status", "select").apply()
                        }
                    }
                ){}

                val queue: RequestQueue = Volley.newRequestQueue(context)
                queue.add(strReq)
            } else {
                // get all notifications that updated after last_updated
                if (last_updated != null) {
                    last_updated = last_updated.replace(" ", "&")
                }
                val strReq = object : StringRequest(
                    Method.GET,
                    "$BASE_URL/notifications/latest/$last_updated",
                    Response.Listener {
                        val obj: JSONArray = JSONArray(it)
                        toDBNotifications(obj, db, sharedPref)
                        sharedPref.edit().putString("status", "online").apply()
                    },
                    Response.ErrorListener {
                        if (sharedPref.getString("status", "offline") != "select"){
                            toastError(context, "select")
                            sharedPref.edit().putString("status", "select").apply()
                        }
                    }
                ){}

                val queue: RequestQueue = Volley.newRequestQueue(context)
                queue.add(strReq)
            }
        }

        fun insertNotification(context: Context, db: AppDatabase, notification: NotificationEntity): Boolean{
            val sharedPref = context.getSharedPreferences("id.ac.istts.dkdm.myactivity", MODE_PRIVATE)
            var success = false
            val strReq = object : StringRequest(
                Method.POST,
                "$BASE_URL/notifications",
                Response.Listener {
                    sharedPref.edit().putString("status", "online").apply()
                    getNotifications(context, db)
                    success = true
                },
                Response.ErrorListener {
                    toastError(context, "insert")
                    sharedPref.edit().putString("status", "insert").apply()
                }
            ){
                override fun getParams(): MutableMap<String, String> {
                    val params = HashMap<String, String>()
                    params["notification_text"] = notification.notification_text
                    params["username_user"] = notification.username_user
                    params["notification_date"] = notification.notification_date
                    params["userAlreadySee"] = if (notification.userAlreadySee) "1" else "0"
                    return params
                }
            }

            val queue: RequestQueue = Volley.newRequestQueue(context)
            queue.add(strReq)
            return success
        }

        fun updateNotification(context: Context, db: AppDatabase, notification: NotificationEntity): Boolean{
            val sharedPref = context.getSharedPreferences("id.ac.istts.dkdm.myactivity", MODE_PRIVATE)
            var success = false
            val strReq = object : StringRequest(
                Method.PUT,
                "$BASE_URL/notifications/id/${notification.notification_id}",
                Response.Listener {
                    sharedPref.edit().putString("status", "online").apply()
                    getNotifications(context, db)
                    success = true
                },
                Response.ErrorListener {
                    toastError(context, "update")
                    sharedPref.edit().putString("status", "update").apply()
                }
            ){
                override fun getParams(): MutableMap<String, String> {
                    val params = HashMap<String, String>()
                    params["notification_text"] = notification.notification_text
                    params["username_user"] = notification.username_user
                    params["notification_date"] = notification.notification_date
                    params["userAlreadySee"] = if (notification.userAlreadySee) "1" else "0"
                    return params
                }
            }

            val queue: RequestQueue = Volley.newRequestQueue(context)
            queue.add(strReq)
            return success
        }

        //    === CONTACTS ===
        fun toDBContacts(obj: JSONArray, db: AppDatabase, sharedPref: SharedPreferences){
            coroutine.launch {
                for (i in 0 until obj.length()){
                    val contact = obj.getJSONObject(i)
                    val contact_id = contact.getInt("contact_id")
                    val username_user = contact.getString("username_user")
                    val username_friend = contact.getString("username_friend")
                    val deleted_at = contact.getString("deleted_at")

                    val newContact = ContactEntity(
                        contact_id = contact_id,
                        username_user = username_user,
                        username_friend = username_friend,
                        deleted_at = deleted_at
                    )

                    val checkContact = db.contactDao.getContact(contact_id)
                    if (checkContact == null){
                        db.contactDao.insert(newContact)
                    } else {
                        db.contactDao.update(newContact)
                    }
                }
                sharedPref.edit().putString("contacts", LocalDateTime.now().format(formatter)).apply()
            }
        }

        fun getContacts(context: Context, db: AppDatabase){
            val sharedPref = context.getSharedPreferences("id.ac.istts.dkdm.myactivity", MODE_PRIVATE)
            var last_updated = sharedPref.getString("contacts", null)
            if (last_updated == ""){
                // get all contacts
                val strReq = object : StringRequest(
                    Method.GET,
                    "$BASE_URL/contacts",
                    Response.Listener {
                        val obj: JSONArray = JSONArray(it)
                        toDBContacts(obj, db, sharedPref)
                        sharedPref.edit().putString("status", "online").apply()
                    },
                    Response.ErrorListener {
                        if (sharedPref.getString("status", "offline") != "select"){
                            toastError(context, "select")
                            sharedPref.edit().putString("status", "select").apply()
                        }
                    }
                ){}

                val queue: RequestQueue = Volley.newRequestQueue(context)
                queue.add(strReq)
            } else {
                // get all contacts that updated after last_updated
                if (last_updated != null) {
                    last_updated = last_updated.replace(" ", "&")
                }
                val strReq = object : StringRequest(
                    Method.GET,
                    "$BASE_URL/contacts/latest/$last_updated",
                    Response.Listener {
                        val obj: JSONArray = JSONArray(it)
                        toDBContacts(obj, db, sharedPref)
                        sharedPref.edit().putString("status", "online").apply()
                    },
                    Response.ErrorListener {
                        if (sharedPref.getString("status", "offline") != "select"){
                            toastError(context, "select")
                            sharedPref.edit().putString("status", "select").apply()
                        }
                    }
                ){}

                val queue: RequestQueue = Volley.newRequestQueue(context)
                queue.add(strReq)
            }
        }

        fun insertContact(context: Context, db: AppDatabase, contact: ContactEntity): Boolean{
            val sharedPref = context.getSharedPreferences("id.ac.istts.dkdm.myactivity", MODE_PRIVATE)
            var success = false
            val strReq = object : StringRequest(
                Method.POST,
                "$BASE_URL/contacts",
                Response.Listener {
                    sharedPref.edit().putString("status", "online").apply()
                    getContacts(context, db)
                    success = true
                },
                Response.ErrorListener {
                    toastError(context, "insert")
                    sharedPref.edit().putString("status", "insert").apply()
                }
            ){
                override fun getParams(): MutableMap<String, String> {
                    val params = HashMap<String, String>()
                    params["username_user"] = contact.username_user
                    params["username_friend"] = contact.username_friend
                    return params
                }
            }

            val queue: RequestQueue = Volley.newRequestQueue(context)
            queue.add(strReq)
            return success
        }

        fun deleteContact(context: Context, db: AppDatabase, id: Int): Boolean{
            val sharedPref = context.getSharedPreferences("id.ac.istts.dkdm.myactivity", MODE_PRIVATE)
            var success = false
            val strReq = object : StringRequest(
                Method.DELETE,
                "$BASE_URL/contacts/id/$id",
                Response.Listener {
                    sharedPref.edit().putString("status", "online").apply()
                    getContacts(context, db)
                    success = true
                },
                Response.ErrorListener {
                    toastError(context, "delete")
                    sharedPref.edit().putString("status", "delete").apply()
                }
            ){}

            val queue: RequestQueue = Volley.newRequestQueue(context)
            queue.add(strReq)
            return success
        }

        //    === CHARITIES ===

        fun toDBCharities(obj: JSONArray, db: AppDatabase, sharedPref: SharedPreferences, context: Context){
            val directory = File(context.filesDir, "DompetkuDompetmu")
            coroutine.launch {
                for (i in 0 until obj.length()){
                    val charities = obj.getJSONObject(i)
                    val charity_id = charities.getInt("charity_id")
                    val charity_name = charities.getString("charity_name")
                    val charity_description = charities.getString("charity_description")
                    val source_id_wallet = charities.getInt("source_id_wallet")
                    val start_date = charities.getString("charity_start_date")
                    val end_date = charities.getString("charity_end_date")
                    val fundsGoal = charities.getLong("fundsGoal")
                    val fundsRaised = charities.getLong("fundsRaised")
                    val isCharityIsOver = charities.getInt("isCharityIsOver") == 1
                    val isCharityBanned = charities.getInt("isCharityBanned") == 1
                    val imgPath = charities.getString("imgPath")
                    val deleted_at = charities.getString("deleted_at")

                    val newCharity = CharityEntity(
                        charity_id = charity_id,
                        charity_name = charity_name,
                        charity_description = charity_description,
                        source_id_wallet = source_id_wallet,
                        charity_start_date = start_date,
                        charity_end_date = end_date,
                        fundsGoal = fundsGoal,
                        fundsRaised = fundsRaised,
                        isCharityIsOver = isCharityIsOver,
                        isCharityBanned = isCharityBanned,
                        imgPath = imgPath,
                        deleted_at = deleted_at
                    )

                    val checkCharity = db.charityDao.getCharity(charity_id)
                    if (checkCharity == null){
                        db.charityDao.insert(newCharity)
                    } else {
                        db.charityDao.update(newCharity)
                    }

                    val file = File(directory, newCharity.imgPath)
                    if (!file.exists()) {
                        getImageCharity(context, newCharity, sharedPref)
                    }
                }
                sharedPref.edit().putString("charities", LocalDateTime.now().format(formatter)).apply()
            }
        }

        fun getCharities(context: Context, db: AppDatabase){
            val sharedPref = context.getSharedPreferences("id.ac.istts.dkdm.myactivity", MODE_PRIVATE)
            var last_updated = sharedPref.getString("charities", null)
            if (last_updated == ""){
                // get all charities
                val strReq = object : StringRequest(
                    Method.GET,
                    "$BASE_URL/charities",
                    Response.Listener {
                        val obj: JSONArray = JSONArray(it)
                        toDBCharities(obj, db, sharedPref, context)
                        sharedPref.edit().putString("status", "online").apply()
                    },
                    Response.ErrorListener {
                        if (sharedPref.getString("status", "offline") != "select"){
                            toastError(context, "select")
                            sharedPref.edit().putString("status", "select").apply()
                        }
                    }
                ){}

                val queue: RequestQueue = Volley.newRequestQueue(context)
                queue.add(strReq)
            } else {
                // get all charities that updated after last_updated
                if (last_updated != null) {
                    last_updated = last_updated.replace(" ", "&")
                }
                val strReq = object : StringRequest(
                    Method.GET,
                    "$BASE_URL/charities/latest/$last_updated",
                    Response.Listener {
                        val obj: JSONArray = JSONArray(it)
                        toDBCharities(obj, db, sharedPref, context)
                        sharedPref.edit().putString("status", "online").apply()
                    },
                    Response.ErrorListener {
                        if (sharedPref.getString("status", "offline") != "select"){
                            toastError(context, "select")
                            sharedPref.edit().putString("status", "select").apply()
                        }
                    }
                ){}

                val queue: RequestQueue = Volley.newRequestQueue(context)
                queue.add(strReq)
            }
        }

        fun insertCharity(context: Context, db: AppDatabase, charity: CharityEntity, selectedImage: Uri): Boolean{
            val sharedPref = context.getSharedPreferences("id.ac.istts.dkdm.myactivity", MODE_PRIVATE)
            var success = false

            // process image
            val imageUpload = MediaStore.Images.Media.getBitmap(context.contentResolver, selectedImage)
            val baos = ByteArrayOutputStream()
            val mimeType = context.contentResolver.getType(selectedImage)
            val filename = charity.imgPath
            imageUpload.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val imageData = baos.toByteArray()

            val multipartRequest = VolleyMultipartRequest(
                Request.Method.POST,
                "$BASE_URL/charities",
                {
                    sharedPref.edit().putString("status", "online").apply()
                    getCharities(context, db)
                    success = true
                },
                {
                    toastError(context, "insert")
                    sharedPref.edit().putString("status", "insert").apply()
                }
            )

            multipartRequest.addMultipartString("charity_name", charity.charity_name)
            multipartRequest.addMultipartString("charity_description", charity.charity_description)
            multipartRequest.addMultipartString("source_id_wallet", charity.source_id_wallet.toString())
            multipartRequest.addMultipartString("fundsGoal", charity.fundsGoal.toString())
            multipartRequest.addMultipartString("imgPath", charity.imgPath)
            multipartRequest.addMultipartString("charity_end_date", charity.charity_end_date)
            multipartRequest.addMultipartFile("image", imageData, filename, mimeType!!)

            val queue: RequestQueue = Volley.newRequestQueue(context)
            queue.add(multipartRequest)
            return success
        }

        fun updateCharity(context: Context, db: AppDatabase, charity: CharityEntity): Boolean{
            val sharedPref = context.getSharedPreferences("id.ac.istts.dkdm.myactivity", MODE_PRIVATE)
            var success = false

            val strReq = object : StringRequest(
                Method.PUT,
                "$BASE_URL/charities/id/${charity.charity_id}",
                {
                    sharedPref.edit().putString("status", "online").apply()
                    getCharities(context, db)
                    success = true
                },
                {
                    toastError(context, "update")
                    sharedPref.edit().putString("status", "update").apply()
                }
            ){
                override fun getParams(): MutableMap<String, String> {
                    val params = HashMap<String, String>()
                    params["charity_name"] = charity.charity_name
                    params["charity_description"] = charity.charity_description
                    params["source_id_wallet"] = charity.source_id_wallet.toString()
                    params["fundsGoal"] = charity.fundsGoal.toString()
                    params["charity_start_date"] = charity.charity_start_date
                    params["fundsRaised"] = charity.fundsRaised.toString()
                    params["isCharityIsOver"] = if(charity.isCharityIsOver) "1" else "0"
                    params["isCharityBanned"] = if(charity.isCharityBanned) "1" else "0"
                    params["charity_end_date"] = charity.charity_end_date
                    return params
                }
            }

            val queue: RequestQueue = Volley.newRequestQueue(context)
            queue.add(strReq)
            return success
        }

        fun getImageCharity(context: Context, charity: CharityEntity, sharedPref: SharedPreferences) {
            val directory = File(context.filesDir, "DompetkuDompetmu")
            if (!directory.exists()) {
                directory.mkdirs()
            }

            val id = charity.charity_id

            val imageRequest = ImageRequest(
                "$BASE_URL/charities/id/$id/image",
                {
                    val file = File(directory, charity.imgPath)
                    if (!file.exists()) {
                        val outputStream = FileOutputStream(file)
                        it.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                        outputStream.flush()
                        outputStream.close()
                    }
                },
                0,
                0,
                ImageView.ScaleType.CENTER_CROP,
                Bitmap.Config.RGB_565,
                {
                    if (sharedPref.getString("status", "offline") != "select"){
                        toastError(context, "select")
                        sharedPref.edit().putString("status", "select").apply()
                    }
                }
            )

            val queue: RequestQueue = Volley.newRequestQueue(context)
            queue.add(imageRequest)
        }


    }
}