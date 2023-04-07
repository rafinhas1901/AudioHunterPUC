package com.example.audiohunter.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.audiohunter.MainActivity
import com.example.audiohunter.R
import com.example.audiohunter.databinding.ActivitySpalshBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySpalshBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Will not allow null views, and don't need these findViewById
        binding = ActivitySpalshBinding.inflate(layoutInflater)
        setContentView(binding.root)

        splashScreen()
    }

    private fun splashScreen() {
        //Setting the opacity of our image to 0
        binding.imgMic.alpha = 0f

        binding.imgMic.animate().setDuration(2500).alpha(1f).withEndAction {
            val loginIntent = Intent(this, MainActivity::class.java)
            startActivity(loginIntent)
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out)
            finish()
        }
    }
}