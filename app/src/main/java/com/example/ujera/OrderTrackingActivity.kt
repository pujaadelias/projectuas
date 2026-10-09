
package com.example.ujera

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.ujera.network.OrderResponse
import com.example.ujera.network.SupabaseApi
import kotlinx.coroutines.launch
import java.util.Locale

class OrderTrackingActivity : AppCompatActivity() {

    private lateinit var tvBack: TextView
    private lateinit var tvOrderNumber: TextView
    private lateinit var tvTrackingStatus: TextView
    private lateinit var tvTrackingMessage: TextView
    private lateinit var tvStepProcessing: TextView
    private lateinit var tvStepShipped: TextView
    private lateinit var tvStepDelivered: TextView
    private lateinit var btnBackHome: Button

    private var orderNumber: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_tracking)

        tvBack = findViewById(R.id.tvBack)
        tvOrderNumber = findViewById(R.id.tvOrderNumber)
        tvTrackingStatus = findViewById(R.id.tvTrackingStatus)
        tvTrackingMessage = findViewById(R.id.tvTrackingMessage)
        tvStepProcessing = findViewById(R.id.tvStepProcessing)
        tvStepShipped = findViewById(R.id.tvStepShipped)
        tvStepDelivered = findViewById(R.id.tvStepDelivered)
        btnBackHome = findViewById(R.id.btnBackHome)

        orderNumber = intent.getStringExtra("order_number").orEmpty()

        tvOrderNumber.text = orderNumber.ifBlank {
            "Nomor pesanan tidak tersedia"
        }

        tvBack.setOnClickListener { finish() }

        btnBackHome.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
            finish()
        }

        if (orderNumber.isBlank()) {
            showTrackingError("Nomor pesanan tidak ditemukan.")
        } else {
            loadTracking()
        }
    }

    private fun loadTracking() {
        tvTrackingStatus.text = "Memuat status..."
        tvTrackingMessage.text = "Mengambil informasi pesanan dari server."

        lifecycleScope.launch {
            try {
                val orders = SupabaseApi.orderApi.getOrders()
                val order = orders.firstOrNull {
                    it.order_number == orderNumber
                }

                if (order == null) {
                    showTrackingError(
                        "Pesanan tidak ditemukan. Pastikan nomor pesanan benar."
                    )
                } else {
                    displayTracking(order)
                }
            } catch (e: Exception) {
                showTrackingError(
                    "Status belum dapat dimuat. Periksa koneksi dan akses Supabase."
                )

                Toast.makeText(
                    this@OrderTrackingActivity,
                    "Gagal mengambil status pesanan.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun displayTracking(order: OrderResponse) {
        val status = order.status.lowercase(Locale.ROOT)

        tvTrackingStatus.text = when (status) {
            "pending" -> "Menunggu konfirmasi"
            "processing" -> "Pesanan diproses"
            "paid" -> "Pembayaran diterima"
            "shipped" -> "Pesanan dikirim"
            "delivered" -> "Pesanan selesai"
            "cancelled", "canceled" -> "Pesanan dibatalkan"
            else -> order.status
        }

        tvTrackingStatus.setTextColor(
            when (status) {
                "delivered" -> Color.parseColor("#477A43")
                "shipped" -> Color.parseColor("#4D7898")
                "cancelled", "canceled" -> Color.parseColor("#A94F65")
                else -> Color.parseColor("#A56B36")
            }
        )

        tvTrackingMessage.text = when (status) {
            "pending" ->
                "Pesanan tercatat dan sedang menunggu konfirmasi."
            "processing" ->
                "Pesanan sedang disiapkan oleh toko UJÉRA."
            "paid" ->
                "Pembayaran sudah tercatat. Pesanan menunggu proses berikutnya."
            "shipped" ->
                "Pesanan sudah ditandai dikirim oleh toko."
            "delivered" ->
                "Pesanan sudah ditandai selesai."
            "cancelled", "canceled" ->
                "Pesanan ini berstatus dibatalkan."
            else ->
                "Status mengikuti data terakhir yang tersimpan di server."
        }

        val cancelled = status == "cancelled" || status == "canceled"

        if (cancelled) {
            tvStepProcessing.text = "✕ Pesanan dibatalkan"
            tvStepShipped.visibility = View.GONE
            tvStepDelivered.visibility = View.GONE
            return
        }

        tvStepProcessing.text = if (
            status in listOf("processing", "paid", "shipped", "delivered")
        ) {
            "✓ Pesanan diproses"
        } else {
            "○ Menunggu proses pesanan"
        }

        tvStepShipped.text = if (
            status in listOf("shipped", "delivered")
        ) {
            "✓ Pesanan dikirim"
        } else {
            "○ Pesanan dikirim"
        }

        tvStepDelivered.text = if (status == "delivered") {
            "✓ Pesanan selesai"
        } else {
            "○ Pesanan selesai"
        }
    }

    private fun showTrackingError(message: String) {
        tvTrackingStatus.text = "Status belum tersedia"
        tvTrackingMessage.text = message
        tvStepProcessing.text = "○ Pesanan diproses"
        tvStepShipped.text = "○ Pesanan dikirim"
        tvStepDelivered.text = "○ Pesanan selesai"
    }
}
