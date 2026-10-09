
package com.example.ujera

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.ujera.network.Product
import com.example.ujera.network.SupabaseApi
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class ProductListActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "UJERA_PRODUCTS"
    }

    private lateinit var productContainer: LinearLayout
    private lateinit var etSearch: EditText
    private lateinit var tvTitle: TextView

    private var allProducts: List<Product> = emptyList()
    private var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_list)

        initializeViews()
        setupActions()
        loadProducts()
    }

    private fun initializeViews() {
        productContainer = findViewById(R.id.productContainer)
        etSearch = findViewById(R.id.etSearch)
        tvTitle = findViewById(R.id.tvTitle)
    }

    private fun setupActions() {
        findViewById<TextView>(R.id.tvBack).setOnClickListener {
            finish()
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) = Unit

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                filterProducts(s?.toString().orEmpty())
            }

            override fun afterTextChanged(s: Editable?) = Unit
        })
    }

    private fun loadProducts() {
        if (isLoading) return

        isLoading = true
        showLoading()

        lifecycleScope.launch {
            try {
                Log.d(TAG, "Meminta produk dari Supabase")

                val response =
                    SupabaseApi.productApi.getProductsResponse()

                if (!response.isSuccessful) {
                    val errorBody = response.errorBody()?.string()

                    Log.e(
                        TAG,
                        "HTTP ${response.code()}: $errorBody"
                    )

                    showError(
                        "HTTP ${response.code()}: " +
                                (errorBody ?: "Gagal mengambil produk")
                    )
                    return@launch
                }

                allProducts = response.body().orEmpty()

                Log.d(
                    TAG,
                    "Jumlah produk diterima: ${allProducts.size}"
                )

                if (allProducts.isEmpty()) {
                    showEmpty("Data produk dari server kosong.")
                } else {
                    filterProducts(etSearch.text?.toString().orEmpty())
                }

            } catch (e: Exception) {
                Log.e(TAG, "Gagal mengambil produk", e)

                showError(
                    "${e.javaClass.simpleName}: " +
                            (e.message ?: "Terjadi kesalahan")
                )
            } finally {
                isLoading = false
            }
        }
    }

    private fun showLoading() {
        productContainer.removeAllViews()
        addMessage("Memuat produk...")
    }

    private fun showError(message: String) {
        productContainer.removeAllViews()
        addMessage("Gagal memuat produk.\n$message")

        Toast.makeText(
            this,
            message.take(180),
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showEmpty(message: String) {
        productContainer.removeAllViews()
        addMessage(message)
    }

    private fun addMessage(message: String) {
        val text = TextView(this).apply {
            this.text = message
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(Color.parseColor("#777777"))
            setPadding(dp(16), dp(32), dp(16), dp(32))
        }

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        productContainer.addView(text, params)
    }

    private fun filterProducts(keyword: String) {
        val query = keyword.trim()

        val filtered = allProducts.filter { product ->
            product.name.contains(query, ignoreCase = true) ||
                    product.brand.contains(query, ignoreCase = true) ||
                    product.category.contains(query, ignoreCase = true)
        }

        displayProducts(filtered)
    }

    private fun displayProducts(products: List<Product>) {
        productContainer.removeAllViews()

        if (products.isEmpty()) {
            val message = if (allProducts.isEmpty()) {
                "Belum ada produk yang berhasil dimuat."
            } else {
                "Produk tidak ditemukan."
            }

            addMessage(message)
            return
        }

        products.forEach { product ->
            productContainer.addView(createProductCard(product))
        }
    }

    private fun createProductCard(product: Product): LinearLayout {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(dp(12), dp(12), dp(12), dp(16))
            isClickable = true
            isFocusable = true
            elevation = dp(2).toFloat()
        }

        val cardParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        cardParams.bottomMargin = dp(16)
        card.layoutParams = cardParams

        val image = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(200)
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
            setBackgroundColor(Color.parseColor("#EAF4FB"))
            contentDescription = product.name
        }

        if (!product.image_url.isNullOrBlank()) {
            Glide.with(this)
                .load(product.image_url)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .centerCrop()
                .into(image)
        } else {
            image.setImageResource(
                android.R.drawable.ic_menu_gallery
            )
        }

        card.addView(image)

        val brand = TextView(this).apply {
            text = product.brand.uppercase(Locale.ROOT)
            textSize = 10f
            setTextColor(Color.parseColor("#A95F6D"))
            setTypeface(null, Typeface.BOLD)
        }
        addText(card, brand, 10)

        val name = TextView(this).apply {
            text = product.name
            textSize = 16f
            setTextColor(Color.parseColor("#292929"))
            setTypeface(null, Typeface.BOLD)
            maxLines = 2
        }
        addText(card, name, 4)

        val category = TextView(this).apply {
            text = product.category
            textSize = 11f
            setTextColor(Color.parseColor("#777777"))
        }
        addText(card, category, 4)

        val price = TextView(this).apply {
            text = formatRupiah(product.price)
            textSize = 16f
            setTextColor(Color.parseColor("#292929"))
            setTypeface(null, Typeface.BOLD)
        }
        addText(card, price, 8)

        val rating = TextView(this).apply {
            text = "★ ${product.rating ?: 0.0} • " +
                    "${product.review_count ?: 0} reviews"
            textSize = 11f
            setTextColor(Color.parseColor("#777777"))
        }
        addText(card, rating, 5)

        card.setOnClickListener {
            openProductDetail(product)
        }

        image.setOnClickListener {
            openProductDetail(product)
        }

        return card
    }

    private fun addText(
        parent: LinearLayout,
        text: TextView,
        topMargin: Int
    ) {
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        params.topMargin = dp(topMargin)
        parent.addView(text, params)
    }

    private fun openProductDetail(product: Product) {
        val intent = Intent(
            this,
            ProductDetailActivity::class.java
        ).apply {
            putExtra("product_id", product.id)
            putExtra("product_name", product.name)
            putExtra("product_brand", product.brand)
            putExtra("product_category", product.category)
            putExtra("product_price", product.price)
            putExtra("product_rating", product.rating ?: 0.0)
            putExtra("product_reviews", product.review_count ?: 0)
            putExtra(
                "product_review_count",
                product.review_count ?: 0
            )
            putExtra(
                "product_description",
                product.description.orEmpty()
            )
            putExtra("product_image", product.image_url.orEmpty())
            putExtra(
                "product_image_url",
                product.image_url.orEmpty()
            )
        }

        startActivity(intent)
    }

    private fun formatRupiah(price: Double): String {
        return NumberFormat.getCurrencyInstance(
            Locale("id", "ID")
        ).apply {
            maximumFractionDigits = 0
        }.format(price)
    }

    private fun dp(value: Int): Int {
        return (
                value * resources.displayMetrics.density
                ).toInt()
    }
}
