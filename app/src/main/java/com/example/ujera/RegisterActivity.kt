
package com.example.ujera

import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.ujera.network.AuthRequest
import com.example.ujera.network.SupabaseApi
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var tvBack: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etRegisterEmail)
        etPassword = findViewById(R.id.etRegisterPassword)
        btnRegister = findViewById(R.id.btnRegister)
        tvBack = findViewById(R.id.tvBack)

        btnRegister.setOnClickListener { register() }
        tvBack.setOnClickListener { finish() }
    }

    private fun register() {
        val name = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()

        if (name.isBlank()) {
            etName.error = "Nama wajib diisi"
            etName.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Masukkan alamat email yang valid"
            etEmail.requestFocus()
            return
        }

        if (password.length < 6) {
            etPassword.error = "Password minimal 6 karakter"
            etPassword.requestFocus()
            return
        }

        btnRegister.isEnabled = false
        btnRegister.text = "Membuat akun..."

        lifecycleScope.launch {
            try {
                val response = SupabaseApi.authApi.signUp(
                    AuthRequest(
                        email = email,
                        password = password,
                        data = mapOf("full_name" to name)
                    )
                )

                if (response.isSuccessful) {
                    val result = response.body()
                    val token = result?.access_token

                    if (!token.isNullOrBlank()) {
                        getSharedPreferences(
                            "ujera_session",
                            MODE_PRIVATE
                        ).edit()
                            .putString("access_token", token)
                            .putString("email", email)
                            .putString("name", name)
                            .apply()

                        Toast.makeText(
                            this@RegisterActivity,
                            "Akun berhasil dibuat!",
                            Toast.LENGTH_LONG
                        ).show()

                        startActivity(
                            android.content.Intent(
                                this@RegisterActivity,
                                HomeActivity::class.java
                            )
                        )
                        finishAffinity()
                    } else {
                        Toast.makeText(
                            this@RegisterActivity,
                            "Pendaftaran berhasil. Periksa email untuk konfirmasi, lalu login.",
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    }
                } else {
                    val error = response.errorBody()
                        ?.string()
                        .orEmpty()

                    val message = when {
                        error.contains(
                            "already registered",
                            ignoreCase = true
                        ) -> "Email ini sudah terdaftar."

                        error.contains(
                            "password",
                            ignoreCase = true
                        ) -> "Password tidak memenuhi ketentuan."

                        else -> "Pendaftaran gagal. Periksa email dan koneksi."
                    }

                    Toast.makeText(
                        this@RegisterActivity,
                        message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@RegisterActivity,
                    "Tidak dapat terhubung ke server. Periksa internet.",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                btnRegister.isEnabled = true
                btnRegister.text = "CREATE ACCOUNT"
            }
        }
    }
}
