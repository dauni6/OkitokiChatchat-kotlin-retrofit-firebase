package com.chat.okitokichatchat.activities

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.chat.okitokichatchat.R
import com.google.firebase.remoteconfig.BuildConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : BaseActivity() {

    private lateinit var remoteConfig: FirebaseRemoteConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        remoteConfig = FirebaseRemoteConfig.getInstance()

        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setDeveloperModeEnabled(BuildConfig.DEBUG)
            .build()

        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.default_config)

        remoteConfig.fetch(0)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    remoteConfig.fetchAndActivate()
                } else {
                    Log.d("remoteConfigError" , "remoteConfig error took place")
                }
                displayRemoteConfigMessage()
            }
    }

    private fun displayRemoteConfigMessage() {
        val splashBackground = remoteConfig.getString("splash_background")
        val caps = remoteConfig.getBoolean("splash_message_caps")
        val splashMessage = remoteConfig.getString("splash_message")

        if (caps) {
            splash_layout.setBackgroundColor(Color.parseColor(splashBackground))
            splash_image.setImageDrawable(null)

            AlertDialog.Builder(this)
                .setMessage(splashMessage)
                .setPositiveButton("확인") { dialog, which ->
                    finish()
                }
                .setCancelable(false)
                .show()
        } else {
            startActivity(LoginActivity.newIntent(this))
            finish()
        }
    }



}
