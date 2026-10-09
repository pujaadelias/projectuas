
package com.example.ujera

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.ujera.network.OrderResponse
import com.example.ujera.network.SupabaseApi
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class OrderHistoryActivity : AppCompatActivity() {

    private lateinit var tvBack: TextView
    private lateinit var tvOrderLoading: TextView
    private lateinit var orderContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_history)

        tvBack = findViewById(R.id.tvBack)
        tvOrderLoading = findViewById(R.id.tvOrderLoading)
        orderContainer = findViewById(R.id.orderContainer)

        tvBack.setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        loadOrders()
    }

    private fun loadOrders() {
        tvOrderLoading.visibility = View.VISIBLE
        tvOrderLoading.text = "Memuat pesanan kamu..."
        orderContainer.removeAllViews()

        lifecycleScope.launch {
            try {
                val orders = SupabaseApi.orderApi.getOrders()

                orderContainer.removeAllViews()

                if (orders.isEmpty()) {
                    tvOrderLoading.visibility = View.GONE
                    showEmptyOrders()
                } else {
                    orders.forEach { order ->
                        orderContainer.addView(createOrderCard(order))
                    }
                    tvOrderLoading.visibility = View.GONE
                }
            } catch (e: Exception) {
                orderContainer.removeAllViews()
                tvOrderLoading.visibility = View.VISIBLE
                tvOrderLoading.text =
                    "Pesanan belum dapat dimuat. Periksa koneksi dan akses Supabase."

                Toast.makeText(
                    this@OrderHistoryActivity,
                    "Gagal memuat pesanan. Periksa koneksi dan policy tabel orders.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun showEmptyOrders() {
        val emptyText = TextView(this).apply {
            text = "Belum ada pesanan.\nYuk, temukan sepatu favoritmu! 👟"
            textSize = 16f
            gravity = Gravity.CENTER
            setTextColor(Color.parseColor("#806D77"))
            setPadding(dp(20), dp(36), dp(20), dp(36))
        }

        orderContainer.addView(emptyText)
    }

    private fun createOrderCard(order: OrderResponse): LinearLayout {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
            setBackgroundColor(Color.WHITE)
        }

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = dp(14)
        }
        card.layoutParams = params

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val orderNumber = TextView(this).apply {
            text = order.order_number
            textSize = 14f
            setTextColor(Color.parseColor("#392631"))
            setTypeface(null, Typeface.BOLD)
        }

        header.addView(
            orderNumber,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        val status = TextView(this).apply {
            text = displayStatus(order.status)
            textSize = 11f
            setPadding(dp(9), dp(5), dp(9), dp(5))
            setTextColor(statusTextColor(order.status))
            setBackgroundColor(statusBackgroundColor(order.status))
        }

        header.addView(status)
        card.addView(header)

        addCardText(card, "Pengiriman: ${order.shipping_method}", 12)
        addCardText(
            card,
            "Pembayaran: ${order.payment_method ?: "Belum dipilih"}",
            5
        )
        addCardText(
            card,
            "Total: ${formatRupiah(order.total_payment)}",
            8,
            bold = true,
            textSize = 16f
        )

        order.created_at?.let { createdAt ->
            addCardText(
                card,
                "Tanggal: ${createdAt.replace("T", " ").take(16)}",
                6
            )
        }

        val trackButton = Button(this).apply {
            text = "Lacak pesanan"
            isAllCaps = false
            setTextColor(Color.WHITE)
            setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                    Color.parseColor("#D982A0")
                )
            )
            setOnClickListener {
                val intent = Intent(
                    this@OrderHistoryActivity,
                    OrderTrackingActivity::class.java
                )
                intent.putExtra("order_number", order.order_number)
                startActivity(intent)
            }
        }

        val buttonParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dp(48)
        ).apply {
            topMargin = dp(12)
        }
        card.addView(trackButton, buttonParams)

        if (order.status.equals("Delivered", ignoreCase = true)) {
            val reviewButton = Button(this).apply {
                text = "★ Tulis ulasan"
                isAllCaps = false
                setTextColor(Color.parseColor("#76506B"))
                setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                        Color.parseColor("#E9DDF7")
                    )
                )
                setOnClickListener {
                    val intent = Intent(
                        this@OrderHistoryActivity,
                        ReviewActivity::class.java
                    )
                    intent.putExtra("order_number", order.order_number)
                    startActivity(intent)
                }
            }

            val reviewParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(48)
            ).apply {
                topMargin = dp(8)
            }
            card.addView(reviewButton, reviewParams)
        }

        return card
    }

    private fun addCardText(
        card: LinearLayout,
        value: String,
        topMargin: Int = 0,
        bold: Boolean = false,
        textSize: Float = 13f
    ) {
        val textView = TextView(this).apply {
            text = value
            this.textSize = textSize
            setTextColor(Color.parseColor("#65545D"))

            if (bold) {
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.parseColor("#392631"))
            }
        }

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            this.topMargin = dp(topMargin)
        }

        card.addView(textView, params)
    }

    private fun displayStatus(status: String): String {
        return when (status.lowercase(Locale.ROOT)) {
            "processing" -> "Diproses"
            "pending" -> "Menunggu"
            "paid" -> "Dibayar"
            "shipped" -> "Dikirim"
            "delivered" -> "Selesai"
            "cancelled", "canceled" -> "Dibatalkan"
            else -> status
        }
    }

    private fun statusTextColor(status: String): Int {
        return when (status.lowercase(Locale.ROOT)) {
            "delivered" -> Color.parseColor("#477A43")
            "shipped" -> Color.parseColor("#4D7898")
            "cancelled", "canceled" -> Color.parseColor("#A94F65")
            else -> Color.parseColor("#8A6A35")
        }
    }

    private fun statusBackgroundColor(status: String): Int {
        return when (status.lowercase(Locale.ROOT)) {
            "delivered" -> Color.parseColor("#DDEFD9")
            "shipped" -> Color.parseColor("#D9EAF7")
            "cancelled", "canceled" -> Color.parseColor("#F7DDE3")
            else -> Color.parseColor("#FFF0D5")
        }
    }

    private fun formatRupiah(price: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        format.maximumFractionDigits = 0
        return format.format(price)
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
