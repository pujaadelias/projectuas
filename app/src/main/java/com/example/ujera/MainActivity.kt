
package com.example.ujera

import android.content.Intent
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

class MainActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnGoogle: Button
    private lateinit var tvRegister: TextView

    private val preferences by lazy {
        getSharedPreferences("ujera_session", MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Pulihkan sesi yang pernah disimpan.
        val savedToken = preferences.getString("access_token", null)
        SupabaseApi.setAccessToken(savedToken)

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnGoogle = findViewById(R.id.btnGoogle)
        tvRegister = findViewById(R.id.tvRegister)

        btnLogin.setOnClickListener {
            login()
        }

        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        btnGoogle.setOnClickListener {
            Toast.makeText(
                this,
                "Login Google memerlukan konfigurasi OAuth Supabase terlebih dahulu.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun login() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Masukkan alamat email yang valid"
            etEmail.requestFocus()
            return
        }

        if (password.isBlank()) {
            etPassword.error = "Password wajib diisi"
            etPassword.requestFocus()
            return
        }

        btnLogin.isEnabled = false
        btnLogin.text = "Memeriksa akun..."

        lifecycleScope.launch {
            try {
                val response = SupabaseApi.authApi.signIn(
                    request = AuthRequest(
                        email = email,
                        password = password
                    )
                )

                if (response.isSuccessful) {
                    val result = response.body()
                    val token = result?.access_token

                    if (token.isNullOrBlank()) {
                        showMessage(
                            "Login belum menghasilkan sesi. " +
                                    "Periksa konfirmasi email di Supabase."
                        )
                    } else {
                        val accountEmail = result.user?.email ?: email

                        preferences.edit()
                            .putString("access_token", token)
                            .putString("email", accountEmail)
                            .apply()

                        // Gunakan token akun untuk permintaan API berikutnya.
                        SupabaseApi.setAccessToken(token)

                        Toast.makeText(
                            this@MainActivity,
                            "Login berhasil!",
                            Toast.LENGTH_SHORT
                        ).show()

                        val homeIntent = Intent(
                            this@MainActivity,
                            HomeActivity::class.java
                        )

                        startActivity(homeIntent)
                        finish()
                    }
                } else {
                    val error = response.errorBody()
                        ?.string()
                        .orEmpty()

                    val message = when {
                        error.contains("email_not_confirmed", true) ->
                            "Konfirmasi email kamu terlebih dahulu."

                        error.contains("invalid_credentials", true) ->
                            "Email atau password salah."

                        error.contains("user_not_found", true) ->
                            "Akun tidak ditemukan."

                        else ->
                            "Login gagal. Periksa akun dan koneksi internet."
                    }

                    showMessage(message)
                }
            } catch (e: Exception) {
                showMessage(
                    "Tidak dapat terhubung ke server. " +
                            "Periksa koneksi internet kamu."
                )
            } finally {
                btnLogin.isEnabled = true
                btnLogin.text = "LOGIN"
            }
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_LONG
        ).show()
    }
}