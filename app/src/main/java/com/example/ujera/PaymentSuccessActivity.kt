package com.example.ujera

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.NumberFormat
import java.util.Locale

class PaymentSuccessActivity : AppCompatActivity() {

    private lateinit var tvOrderNumber: TextView
    private lateinit var tvPaymentMethod: TextView
    private lateinit var tvTotalPayment: TextView

    private lateinit var btnTrackOrder: Button
    private lateinit var btnBackHome: Button


    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )

        setContentView(
            R.layout.activity_payment_success
        )

        initializeViews()

        displayPaymentData()

        setupActions()
    }


    // =========================================================
    // INITIALIZE
    // =========================================================

    private fun initializeViews() {

        tvOrderNumber =
            findViewById(
                R.id.tvOrderNumber
            )

        tvPaymentMethod =
            findViewById(
                R.id.tvPaymentMethod
            )

        tvTotalPayment =
            findViewById(
                R.id.tvTotalPayment
            )

        btnTrackOrder =
            findViewById(
                R.id.btnTrackOrder
            )

        btnBackHome =
            findViewById(
                R.id.btnBackHome
            )
    }


    // =========================================================
    // DISPLAY DATA
    // =========================================================

    private fun displayPaymentData() {

        val orderNumber =
            intent.getStringExtra(
                "order_number"
            )
                ?: "UJ-ORDER"


        val paymentMethod =
            intent.getStringExtra(
                "payment_method"
            )
                ?: "E-Wallet"


        val totalPayment =
            intent.getDoubleExtra(
                "total_payment",
                0.0
            )


        tvOrderNumber.text =
            orderNumber

        tvPaymentMethod.text =
            paymentMethod

        tvTotalPayment.text =
            formatRupiah(
                totalPayment
            )
    }


    // =========================================================
    // ACTIONS
    // =========================================================

    private fun setupActions() {

        btnTrackOrder.setOnClickListener {

            val intent =
                Intent(
                    this,
                    OrderTrackingActivity::class.java
                )


            intent.putExtra(
                "order_number",
                tvOrderNumber.text.toString()
            )


            startActivity(
                intent
            )
        }


        btnBackHome.setOnClickListener {

            val intent =
                Intent(
                    this,
                    HomeActivity::class.java
                )


            intent.flags =
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP


            startActivity(
                intent
            )

            finish()
        }
    }


    // =========================================================
    // RUPIAH
    // =========================================================

    private fun formatRupiah(
        price: Double
    ): String {

        val format =
            NumberFormat.getCurrencyInstance(
                Locale(
                    "id",
                    "ID"
                )
            )

        format.maximumFractionDigits =
            0

        return format.format(
            price
        )
    }
}