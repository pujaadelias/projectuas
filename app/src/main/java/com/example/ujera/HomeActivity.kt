package com.example.ujera

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.GridLayout
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

class HomeActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "UJERA_PRODUCTS"
    }

    private lateinit var etSearch: EditText
    private lateinit var productGrid: GridLayout
    private lateinit var categoryAll: TextView
    private lateinit var categorySneakers: TextView
    private lateinit var categoryRunning: TextView
    private lateinit var categorySports: TextView
    private lateinit var categoryCasual: TextView

    private val allProducts = mutableListOf<Product>()
    private var selectedCategory: String? = null
    private var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        initializeViews()
        setupSearch()
        setupCategories()
        setupNavigation()
        loadProducts()
    }

    private fun initializeViews() {
        etSearch = findViewById(R.id.etSearch)
        productGrid = findViewById(R.id.productGrid)
        categoryAll = findViewById(R.id.categoryAll)
        categorySneakers = findViewById(R.id.categorySneakers)
        categoryRunning = findViewById(R.id.categoryRunning)
        categorySports = findViewById(R.id.categorySports)
        categoryCasual = findViewById(R.id.categoryCasual)
    }

    private fun setupNavigation() {
        findViewById<TextView>(R.id.tvCartIcon).setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }

        findViewById<TextView>(R.id.tvSeeAllCategory).setOnClickListener {
            startActivity(Intent(this, CategoryActivity::class.java))
        }

        findViewById<TextView>(R.id.tvSeeAllProducts).setOnClickListener {
            startActivity(Intent(this, ProductListActivity::class.java))
        }

        findViewById<TextView>(R.id.tvBottomCategory).setOnClickListener {
            startActivity(Intent(this, CategoryActivity::class.java))
        }

        findViewById<TextView>(R.id.tvBottomOrders).setOnClickListener {
            startActivity(Intent(this, OrderHistoryActivity::class.java))
        }

        findViewById<TextView>(R.id.tvBottomCart).setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }

        findViewById<TextView>(R.id.tvBottomProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun setupSearch() {
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
                renderFilteredProducts()
            }

            override fun afterTextChanged(s: Editable?) = Unit
        })
    }

    private fun setupCategories() {
        categoryAll.setOnClickListener {
            selectedCategory = null
            etSearch.setText("") // Reset input pencarian saat tekan All
            renderFilteredProducts()
        }

        categorySneakers.setOnClickListener {
            selectCategory("Sneakers")
        }

        categoryRunning.setOnClickListener {
            selectCategory("Running")
        }

        categorySports.setOnClickListener {
            selectCategory("Sports")
        }

        categoryCasual.setOnClickListener {
            selectCategory("Casual")
        }
    }

    private fun selectCategory(category: String) {
        selectedCategory = category
        renderFilteredProducts()
    }

    private fun loadProducts() {
        if (isLoading) return

        isLoading = true
        showMessage("Memuat produk...")

        lifecycleScope.launch {
            try {
                val response = SupabaseApi.productApi.getProductsResponse()

                Log.d(TAG, "HTTP status: ${response.code()}")

                if (!response.isSuccessful) {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Supabase error: $errorBody")

                    showMessage("Gagal memuat produk (HTTP ${response.code()})")
                    return@launch
                }

                val products = response.body().orEmpty()
                Log.d(TAG, "Jumlah produk diterima: ${products.size}")

                allProducts.clear()
                allProducts.addAll(products)

                if (allProducts.isEmpty()) {
                    showMessage("Data produk di Supabase masih kosong.\nPastikan RLS (Row Level Security) tabel 'products' sudah di-ENABLE untuk SELECT.")
                } else {
                    renderFilteredProducts()
                }
            } catch (e: Exception) {
                Log.e(TAG, "UJERA PRODUCTS ERROR", e)
                showMessage("Gagal terhubung ke server: ${e.localizedMessage}")
            } finally {
                isLoading = false
            }
        }
    }

    private fun renderFilteredProducts() {
        val keyword = etSearch.text?.toString()?.trim().orEmpty()

        val filtered = allProducts.filter { product ->
            val matchesKeyword = keyword.isEmpty() ||
                    product.name.contains(keyword, ignoreCase = true) ||
                    product.brand.contains(keyword, ignoreCase = true) ||
                    product.category.contains(keyword, ignoreCase = true)

            val matchesCategory = selectedCategory.isNullOrBlank() ||
                    product.category.trim().equals(selectedCategory?.trim(), ignoreCase = true)

            matchesKeyword && matchesCategory
        }

        showProducts(filtered)
    }

    private fun showProducts(products: List<Product>) {
        productGrid.removeAllViews()

        if (products.isEmpty()) {
            val message = when {
                allProducts.isEmpty() ->
                    "Belum ada produk yang berhasil dimuat dari server."

                !etSearch.text.isNullOrBlank() ->
                    "Produk dengan kata kunci \"${etSearch.text.toString().trim()}\" tidak ditemukan."

                !selectedCategory.isNullOrBlank() ->
                    "Belum ada produk untuk kategori \"$selectedCategory\"."

                else ->
                    "Produk tidak ditemukan."
            }

            showMessage(message)
            return
        }

        products.forEach { product ->
            productGrid.addView(createProductCard(product))
        }
    }

    private fun showMessage(message: String) {
        productGrid.removeAllViews()

        val textView = TextView(this).apply {
            text = message
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(Color.parseColor("#777777"))
            setPadding(dp(16), dp(24), dp(16), dp(24))
        }

        val params = GridLayout.LayoutParams().apply {
            width = GridLayout.LayoutParams.MATCH_PARENT
            height = GridLayout.LayoutParams.WRAP_CONTENT
            columnSpec = GridLayout.spec(0, 2, 1f)
        }

        productGrid.addView(textView, params)
    }

    private fun createProductCard(product: Product): View {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(dp(8), dp(8), dp(8), dp(12))
            elevation = dp(2).toFloat()
            isClickable = true
            isFocusable = true
        }

        val cardParams = GridLayout.LayoutParams().apply {
            width = 0
            height = GridLayout.LayoutParams.WRAP_CONTENT
            columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f)
            setMargins(dp(5), dp(5), dp(5), dp(12))
        }

        card.layoutParams = cardParams

        val image = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(150)
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
            setBackgroundColor(Color.parseColor("#F2F2F2"))
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
            image.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        card.addView(image)

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(4), dp(10), dp(4), dp(2))
        }

        card.addView(content)

        val brand = TextView(this).apply {
            text = product.brand.uppercase(Locale.ROOT)
            textSize = 10f
            setTextColor(Color.parseColor("#A94F65"))
            setTypeface(null, Typeface.BOLD)
        }
        content.addView(brand)

        val name = TextView(this).apply {
            text = product.name
            textSize = 14f
            setTextColor(Color.parseColor("#292929"))
            setTypeface(null, Typeface.BOLD)
            maxLines = 2
        }
        content.addView(name)

        val rating = TextView(this).apply {
            text = "★ ${product.rating ?: 0.0} (${product.review_count ?: 0})"
            textSize = 11f
            setTextColor(Color.parseColor("#777777"))
        }
        content.addView(rating)

        val price = TextView(this).apply {
            text = formatRupiah(product.price)
            textSize = 15f
            setTextColor(Color.parseColor("#292929"))
            setTypeface(null, Typeface.BOLD)
        }
        content.addView(price)

        card.setOnClickListener {
            openProductDetail(product)
        }

        return card
    }

    private fun openProductDetail(product: Product) {
        val intent = Intent(this, ProductDetailActivity::class.java).apply {
            putExtra("product_id", product.id)
            putExtra("product_name", product.name)
            putExtra("product_brand", product.brand)
            putExtra("product_category", product.category)
            putExtra("product_price", product.price)
            putExtra("product_rating", product.rating ?: 0.0)
            putExtra("product_review_count", product.review_count ?: 0)
            putExtra("product_description", product.description.orEmpty())
            putExtra("product_image_url", product.image_url.orEmpty())
        }

        startActivity(intent)
    }

    private fun formatRupiah(price: Double): String {
        return NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
            maximumFractionDigits = 0
        }.format(price)
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}