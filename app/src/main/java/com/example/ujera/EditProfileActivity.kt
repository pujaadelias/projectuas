
package com.example.ujera

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class EditProfileActivity : AppCompatActivity() {

    private lateinit var tvBack: TextView
    private lateinit var ivProfilePhoto: ImageView
    private lateinit var etName: EditText
    private lateinit var btnChoosePhoto: Button
    private lateinit var btnSaveProfile: Button

    private val preferences by lazy {
        getSharedPreferences("ujera_session", MODE_PRIVATE)
    }

    private var selectedPhotoUri: Uri? = null

    private val photoPicker =
        registerForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri ->
            if (uri != null) {
                try {
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: SecurityException) {
                    // Izin permanen tidak tersedia dari penyedia foto ini.
                }

                selectedPhotoUri = uri
                showPhoto(uri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        initializeViews()
        loadCurrentProfile()
        setupActions()
    }

    private fun initializeViews() {
        tvBack = findViewById(R.id.tvBack)
        ivProfilePhoto = findViewById(R.id.ivProfilePhoto)
        etName = findViewById(R.id.etName)
        btnChoosePhoto = findViewById(R.id.btnChoosePhoto)
        btnSaveProfile = findViewById(R.id.btnSaveProfile)
    }

    private fun setupActions() {
        tvBack.setOnClickListener {
            finish()
        }

        btnChoosePhoto.setOnClickListener {
            photoPicker.launch(arrayOf("image/*"))
        }

        btnSaveProfile.setOnClickListener {
            saveProfile()
        }
    }

    private fun loadCurrentProfile() {
        val savedName = preferences.getString("name", "").orEmpty()
        val email = preferences.getString("email", "").orEmpty()

        etName.setText(
            savedName.ifBlank {
                email.substringBefore("@")
            }
        )

        val savedPhoto =
            preferences.getString("profile_photo_uri", null)

        if (!savedPhoto.isNullOrBlank()) {
            try {
                val uri = Uri.parse(savedPhoto)
                selectedPhotoUri = uri
                showPhoto(uri)
            } catch (_: Exception) {
                showDefaultPhoto()
            }
        } else {
            showDefaultPhoto()
        }
    }

    private fun showPhoto(uri: Uri) {
        try {
            ivProfilePhoto.setImageURI(uri)
        } catch (_: Exception) {
            showDefaultPhoto()
        }
    }

    private fun showDefaultPhoto() {
        ivProfilePhoto.setImageResource(
            android.R.drawable.ic_menu_camera
        )
    }

    private fun saveProfile() {
        val name = etName.text.toString().trim()

        if (name.isBlank()) {
            etName.error = "Nama tidak boleh kosong"
            etName.requestFocus()
            return
        }

        val editor = preferences.edit()
            .putString("name", name)

        selectedPhotoUri?.let { uri ->
            editor.putString(
                "profile_photo_uri",
                uri.toString()
            )
        }

        editor.apply()

        Toast.makeText(
            this,
            "Profil berhasil disimpan!",
            Toast.LENGTH_SHORT
        ).show()

        setResult(RESULT_OK)
        finish()
    }
}
