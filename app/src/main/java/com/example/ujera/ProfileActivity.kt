
package com.example.ujera

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {

    private lateinit var tvBack: TextView
    private lateinit var ivProfilePhoto: ImageView
    private lateinit var tvProfileName: TextView
    private lateinit var tvProfileEmail: TextView
    private lateinit var btnMyOrders: Button
    private lateinit var btnMyCart: Button
    private lateinit var btnEditProfile: Button
    private lateinit var btnHelp: Button
    private lateinit var btnLogout: Button

    private val preferences by lazy {
        getSharedPreferences("ujera_session", MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        tvBack = findViewById(R.id.tvBack)
        ivProfilePhoto = findViewById(R.id.ivProfilePhoto)
        tvProfileName = findViewById(R.id.tvProfileName)
        tvProfileEmail = findViewById(R.id.tvProfileEmail)
        btnMyOrders = findViewById(R.id.btnMyOrders)
        btnMyCart = findViewById(R.id.btnMyCart)
        btnEditProfile = findViewById(R.id.btnEditProfile)
        btnHelp = findViewById(R.id.btnHelp)
        btnLogout = findViewById(R.id.btnLogout)

        tvBack.setOnClickListener {
            finish()
        }

        btnMyOrders.setOnClickListener {
            startActivity(
                Intent(this, OrderHistoryActivity::class.java)
            )
        }

        btnMyCart.setOnClickListener {
            startActivity(
                Intent(this, CartActivity::class.java)
            )
        }

        btnEditProfile.setOnClickListener {
            startActivity(
                Intent(this, EditProfileActivity::class.java)
            )
        }

        btnHelp.setOnClickListener {
            Toast.makeText(
                this,
                "Hubungi customer service UJÉRA melalui kontak resmi toko.",
                Toast.LENGTH_LONG
            ).show()
        }

        btnLogout.setOnClickListener {
            preferences.edit().clear().apply()

            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            startActivity(intent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        displayProfile()
    }

    private fun displayProfile() {
        val email = preferences.getString("email", "").orEmpty()
        val savedName = preferences.getString("name", "").orEmpty()
        val displayName = savedName.ifBlank {
            email.substringBefore("@").ifBlank { "UJÉRA User" }
        }

        tvProfileName.text = displayName
        tvProfileEmail.text = email.ifBlank { "Email belum tersedia" }

        val savedPhoto = preferences.getString("profile_photo_uri", null)

        if (!savedPhoto.isNullOrBlank()) {
            try {
                ivProfilePhoto.setImageURI(
                    android.net.Uri.parse(savedPhoto)
                )
            } catch (_: Exception) {
                ivProfilePhoto.setImageResource(
                    android.R.drawable.ic_menu_camera
                )
            }
        } else {
            ivProfilePhoto.setImageResource(
                android.R.drawable.ic_menu_camera
            )
        }
    }
}
