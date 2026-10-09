
package com.example.ujera

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.ujera.network.ReviewRequest
import com.example.ujera.network.SupabaseApi
import kotlinx.coroutines.launch

class ReviewActivity : AppCompatActivity() {

    private lateinit var tvBack: TextView
    private lateinit var ratingBar: RatingBar
    private lateinit var etReview: EditText
    private lateinit var btnSubmitReview: Button

    private var orderNumber = ""
    private var isSubmitting = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review)

        initializeViews()

        orderNumber = intent.getStringExtra("order_number").orEmpty()

        setupActions()

        if (orderNumber.isBlank()) {
            btnSubmitReview.isEnabled = false
            showMessage("Nomor pesanan tidak ditemukan.")
        }
    }

    private fun initializeViews() {
        tvBack = findViewById(R.id.tvBack)
        ratingBar = findViewById(R.id.ratingBar)
        etReview = findViewById(R.id.etReview)
        btnSubmitReview = findViewById(R.id.btnSubmitReview)
    }

    private fun setupActions() {
        tvBack.setOnClickListener {
            finish()
        }

        btnSubmitReview.setOnClickListener {
            submitReview()
        }
    }

    private fun submitReview() {
        if (isSubmitting) return

        if (orderNumber.isBlank()) {
            showMessage("Nomor pesanan tidak tersedia.")
            return
        }

        val rating = ratingBar.rating.toInt()
        val reviewText = etReview.text.toString().trim()

        if (rating !in 1..5) {
            showMessage("Silakan pilih rating 1 sampai 5 bintang.")
            return
        }

        if (reviewText.isBlank()) {
            etReview.error = "Ulasan wajib diisi."
            etReview.requestFocus()
            return
        }

        isSubmitting = true
        btnSubmitReview.isEnabled = false
        btnSubmitReview.text = "Mengirim ulasan..."

        lifecycleScope.launch {
            try {
                val response = SupabaseApi.reviewApi.addReview(
                    ReviewRequest(
                        order_number = orderNumber,
                        rating = rating,
                        review_text = reviewText
                    )
                )

                if (response.isSuccessful) {
                    Toast.makeText(
                        this@ReviewActivity,
                        "Ulasan berhasil disimpan. Terima kasih!",
                        Toast.LENGTH_LONG
                    ).show()

                    setResult(RESULT_OK)
                    finish()
                } else {
                    val errorBody = response.errorBody()
                        ?.string()
                        .orEmpty()

                    showMessage(
                        "Ulasan gagal disimpan. Periksa tabel dan policy Supabase." +
                                if (errorBody.isNotBlank()) {
                                    "\n$errorBody"
                                } else {
                                    ""
                                }
                    )
                }
            } catch (e: Exception) {
                showMessage(
                    "Tidak dapat mengirim ulasan. Periksa koneksi internet dan konfigurasi Supabase."
                )
            } finally {
                isSubmitting = false

                if (!isFinishing) {
                    btnSubmitReview.isEnabled = orderNumber.isNotBlank()
                    btnSubmitReview.text = "Submit Review"
                }
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
