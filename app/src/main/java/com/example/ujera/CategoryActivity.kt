package com.example.ujera

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class CategoryActivity : AppCompatActivity() {

    private lateinit var tvBack: TextView

    private lateinit var categorySneakers: LinearLayout
    private lateinit var categoryRunning: LinearLayout
    private lateinit var categorySports: LinearLayout
    private lateinit var categoryCasual: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_category)

        tvBack = findViewById(R.id.tvBack)

        categorySneakers =
            findViewById(R.id.categorySneakers)

        categoryRunning =
            findViewById(R.id.categoryRunning)

        categorySports =
            findViewById(R.id.categorySports)

        categoryCasual =
            findViewById(R.id.categoryCasual)

        setupActions()
    }

    private fun setupActions() {

        tvBack.setOnClickListener {
            finish()
        }

        categorySneakers.setOnClickListener {
            openProductList("Sneakers")
        }

        categoryRunning.setOnClickListener {
            openProductList("Running")
        }

        categorySports.setOnClickListener {
            openProductList("Sports")
        }

        categoryCasual.setOnClickListener {
            openProductList("Casual")
        }
    }

    private fun openProductList(
        category: String
    ) {

        val intent =
            Intent(
                this,
                ProductListActivity::class.java
            )

        intent.putExtra(
            "category",
            category
        )

        startActivity(intent)
    }
}