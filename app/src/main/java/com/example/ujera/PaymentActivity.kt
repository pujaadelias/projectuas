
package com.example.ujera

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.ujera.network.OrderItemRequest
import com.example.ujera.network.OrderRequest
import com.example.ujera.network.SupabaseApi
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class PaymentActivity : AppCompatActivity() {

    private lateinit var tvSubtotal: TextView
    private lateinit var tvShipping: TextView
    private lateinit var tvTotal: TextView

    private lateinit var rgPayment: RadioGroup
    private lateinit var rbEwallet: RadioButton
    private lateinit var rbBank: RadioButton
    private lateinit var rbCard: RadioButton
    private lateinit var rbCod: RadioButton

    private lateinit var btnPayNow: Button

    private var customerName = ""
    private var customerPhone = ""
    private var customerAddress = ""

    private var shippingMethod = "Regular"
    private var shippingCost = 15000.0
    private var subtotal = 0.0
    private var totalPayment = 0.0

    private var isProcessingPayment = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        initializeViews()
        readCheckoutData()
        displaySummary()
        setupActions()
    }

    private fun initializeViews() {
        tvSubtotal = findViewById(R.id.tvSubtotal)
        tvShipping = findViewById(R.id.tvShipping)
        tvTotal = findViewById(R.id.tvTotal)

        rgPayment = findViewById(R.id.rgPayment)
        rbEwallet = findViewById(R.id.rbEwallet)
        rbBank = findViewById(R.id.rbBank)
        rbCard = findViewById(R.id.rbCard)
        rbCod = findViewById(R.id.rbCod)

        btnPayNow = findViewById(R.id.btnPayNow)
    }

    private fun readCheckoutData() {
        customerName = intent.getStringExtra("customer_name").orEmpty()
        customerPhone = intent.getStringExtra("customer_phone").orEmpty()
        customerAddress = intent.getStringExtra("customer_address").orEmpty()

        shippingMethod =
            intent.getStringExtra("shipping_method") ?: "Regular"

        shippingCost =
            intent.getDoubleExtra("shipping_cost", 15000.0)

        subtotal = intent.getDoubleExtra(
            "subtotal",
            CartManager.getCheckoutTotal()
        )

        totalPayment = intent.getDoubleExtra(
            "total_payment",
            subtotal + shippingCost
        )
    }

    private fun displaySummary() {
        tvSubtotal.text = "Subtotal: ${formatRupiah(subtotal)}"
        tvShipping.text = "Shipping: ${formatRupiah(shippingCost)}"
        tvTotal.text = "Total: ${formatRupiah(totalPayment)}"
    }

    private fun setupActions() {
        findViewById<TextView>(R.id.tvBack).setOnClickListener {
            finish()
        }

        rbEwallet.isChecked = true

        btnPayNow.setOnClickListener {
            processPayment()
        }
    }

    private fun getSelectedPaymentMethod(): String {
        return when (rgPayment.checkedRadioButtonId) {
            R.id.rbEwallet -> "E-Wallet"
            R.id.rbBank -> "Bank Transfer"
            R.id.rbCard -> "Credit/Debit Card"
            R.id.rbCod -> "Cash on Delivery"
            else -> ""
        }
    }

    private fun processPayment() {
        if (isProcessingPayment) return

        if (
            customerName.isBlank() ||
            customerPhone.isBlank() ||
            customerAddress.isBlank()
        ) {
            showMessage(
                "Data checkout belum lengkap. Silakan kembali ke checkout."
            )
            return
        }

        val checkoutItems = CartManager.getCheckoutItems()

        if (checkoutItems.isEmpty()) {
            showMessage("Tidak ada produk untuk dibayar.")
            return
        }

        if (subtotal <= 0.0 || totalPayment <= 0.0 || shippingCost < 0.0) {
            showMessage("Total pembayaran tidak valid.")
            return
        }

        val paymentMethod = getSelectedPaymentMethod()

        if (paymentMethod.isBlank()) {
            showMessage("Pilih metode pembayaran terlebih dahulu.")
            return
        }

        isProcessingPayment = true
        btnPayNow.isEnabled = false
        btnPayNow.text = "MEMPROSES..."

        lifecycleScope.launch {
            try {
                val orderNumber = generateOrderNumber()

                val orderRequest = OrderRequest(
                    order_number = orderNumber,
                    customer_name = customerName,
                    customer_phone = customerPhone,
                    customer_address = customerAddress,
                    shipping_method = shippingMethod,
                    shipping_cost = shippingCost,
                    subtotal = subtotal,
                    total_payment = totalPayment,
                    payment_method = paymentMethod,
                    status = "Processing"
                )

                val createdOrders =
                    SupabaseApi.orderApi.createOrder(orderRequest)

                val createdOrder = createdOrders.firstOrNull()
                    ?: throw IllegalStateException(
                        "Data pesanan tidak dikembalikan oleh server."
                    )

                val orderItems = checkoutItems.map { item ->
                    OrderItemRequest(
                        order_id = createdOrder.id,
                        product_id = item.productId,
                        product_name = item.name,
                        brand = item.brand,
                        price = item.price,
                        color = item.color,
                        size = item.size,
                        quantity = item.quantity
                    )
                }

                SupabaseApi.orderApi.createOrderItems(orderItems)

                // Jangan hapus keranjang biasa jika checkout memakai Buy Now.
                if (CartManager.isDirectCheckout()) {
                    CartManager.clearDirectCheckout()
                } else {
                    CartManager.clear()
                }

                val successIntent = Intent(
                    this@PaymentActivity,
                    PaymentSuccessActivity::class.java
                ).apply {
                    putExtra("order_number", orderNumber)
                    putExtra("payment_method", paymentMethod)
                    putExtra("total_payment", totalPayment)
                }

                startActivity(successIntent)
                finish()

            } catch (e: Exception) {
                showMessage(
                    "Pesanan gagal diproses. " +
                            "Periksa koneksi, struktur tabel, dan akses Supabase."
                )

                isProcessingPayment = false
                btnPayNow.isEnabled = true
                btnPayNow.text = "PAY NOW"
            }
        }
    }

    private fun generateOrderNumber(): String {
        val date = SimpleDateFormat(
            "yyyyMMdd",
            Locale.US
        ).format(Date())

        val suffix = UUID.randomUUID()
            .toString()
            .replace("-", "")
            .take(4)
            .uppercase(Locale.US)

        return "UJ-$date-$suffix"
    }

    private fun formatRupiah(price: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(
            Locale("id", "ID")
        )

        formatter.maximumFractionDigits = 0
        return formatter.format(price)
    }

    private fun showMessage(message: String) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_LONG
        ).show()
    }
}
