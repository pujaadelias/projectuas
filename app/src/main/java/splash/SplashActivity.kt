package com.example.ujera.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.ujera.R

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splash)

        // Tampilkan splash selama 2 detik
        window.decorView.postDelayed({

            val intent = Intent(
                this,
                com.example.ujera.MainActivity::class.java
            )

            startActivity(intent)

            finish()

        }, 2000)
    }
}