
package com.example.ujera

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.NumberFormat
import java.util.Locale

class CheckoutActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etPhone: EditText
    private lateinit var etAddress: EditText

    private lateinit var rgShipping: RadioGroup
    private lateinit var rbRegular: RadioButton
    private lateinit var rbExpress: RadioButton

    private lateinit var tvSubtotal: TextView
    private lateinit var tvShipping: TextView
    private lateinit var tvTotal: TextView

    private lateinit var btnContinuePayment: Button

    private var shippingCost = 15000.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        initializeViews()
        setupShipping()
        setupActions()
        displaySummary()
    }

    private fun initializeViews() {
        etName = findViewById(R.id.etName)
        etPhone = findViewById(R.id.etPhone)
        etAddress = findViewById(R.id.etAddress)

        rgShipping = findViewById(R.id.rgShipping)
        rbRegular = findViewById(R.id.rbRegular)
        rbExpress = findViewById(R.id.rbExpress)

        tvSubtotal = findViewById(R.id.tvSubtotal)
        tvShipping = findViewById(R.id.tvShipping)
        tvTotal = findViewById(R.id.tvTotal)

        btnContinuePayment =
            findViewById(R.id.btnContinuePayment)
    }

    private fun setupShipping() {
        rbRegular.isChecked = true
        shippingCost = 15000.0

        rgShipping.setOnCheckedChangeListener { _, checkedId ->
            shippingCost = when (checkedId) {
                R.id.rbExpress -> 30000.0
                else -> 15000.0
            }

            displaySummary()
        }
    }

    private fun setupActions() {
        findViewById<TextView>(R.id.tvBack).setOnClickListener {
            finish()
        }

        btnContinuePayment.setOnClickListener {
            continueToPayment()
        }
    }

    private fun displaySummary() {
        val subtotal = CartManager.getCheckoutTotal()
        val total = subtotal + shippingCost

        tvSubtotal.text = "Subtotal: ${formatRupiah(subtotal)}"
        tvShipping.text = "Shipping: ${formatRupiah(shippingCost)}"
        tvTotal.text = "Total: ${formatRupiah(total)}"
    }

    private fun continueToPayment() {
        val name = etName.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val address = etAddress.text.toString().trim()

        if (name.isEmpty()) {
            etName.error = "Nama wajib diisi"
            etName.requestFocus()
            return
        }

        if (phone.isEmpty()) {
            etPhone.error = "Nomor telepon wajib diisi"
            etPhone.requestFocus()
            return
        }

        if (address.isEmpty()) {
            etAddress.error = "Alamat wajib diisi"
            etAddress.requestFocus()
            return
        }

        val checkoutItems = CartManager.getCheckoutItems()

        if (checkoutItems.isEmpty()) {
            Toast.makeText(
                this,
                "Tidak ada produk untuk checkout.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val subtotal = CartManager.getCheckoutTotal()
        val total = subtotal + shippingCost
        val shippingMethod =
            if (rbExpress.isChecked) "Express" else "Regular"

        val paymentIntent = Intent(
            this,
            PaymentActivity::class.java
        ).apply {
            putExtra("customer_name", name)
            putExtra("customer_phone", phone)
            putExtra("customer_address", address)
            putExtra("shipping_method", shippingMethod)
            putExtra("shipping_cost", shippingCost)
            putExtra("subtotal", subtotal)
            putExtra("total_payment", total)
        }

        startActivity(paymentIntent)
    }

    private fun formatRupiah(price: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(
            Locale("id", "ID")
        )
        formatter.maximumFractionDigits = 0
        return formatter.format(price)
    }
}
