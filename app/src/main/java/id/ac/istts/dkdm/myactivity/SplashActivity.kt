package id.ac.istts.dkdm.myactivity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import id.ac.istts.dkdm.databinding.ActivitySplashBinding
import id.ac.istts.dkdm.myapiconnection.APIConnection

@Suppress("DEPRECATION")
class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPref = this.getSharedPreferences("id.ac.istts.dkdm.myactivity", MODE_PRIVATE)

        if (!sharedPref.contains("users")){
            sharedPref.edit().putString("users", "").apply()
        }

        if (!sharedPref.contains("wallets")){
            sharedPref.edit().putString("wallets", "").apply()
        }

        if (!sharedPref.contains("notifications")){
            sharedPref.edit().putString("notifications", "").apply()
        }

        if (!sharedPref.contains("histories")){
            sharedPref.edit().putString("histories", "").apply()
        }

        if (!sharedPref.contains("contacts")){
            sharedPref.edit().putString("contacts", "").apply()
        }

        if (!sharedPref.contains("charities")){
            sharedPref.edit().putString("charities", "").apply()
        }

        sharedPref.edit().putString("status", "offline").apply()


        // TO HIDE STATUS BAR
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // TO SET BINDING VIEW
        binding = ActivitySplashBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }, 3000)
    }
}