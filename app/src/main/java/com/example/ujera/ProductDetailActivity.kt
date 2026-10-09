
package com.example.ujera

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.ujera.network.CartRequest
import com.example.ujera.network.SupabaseApi
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class ProductDetailActivity : AppCompatActivity() {

    private lateinit var ivProduct: ImageView
    private lateinit var tvBrand: TextView
    private lateinit var tvProductName: TextView
    private lateinit var tvRating: TextView
    private lateinit var tvPrice: TextView
    private lateinit var tvDescription: TextView
    private lateinit var tvReviewCount: TextView
    private lateinit var btnAddToCart: Button
    private lateinit var btnBuyNow: Button
    private lateinit var tvBack: TextView

    private var productId = 0
    private var productName = "Urban Runner"
    private var productBrand = "UJÉRA"
    private var productPrice = 399000.0

    private var productImage: String? = null
    private var whiteImage: String? = null
    private var pinkImage: String? = null
    private var blackImage: String? = null

    private var selectedColor = "Pink"
    private var selectedSize = "40"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        initializeViews()
        loadProductData()
        setupColorButtons()
        setupSizeButtons()
        setupActions()
    }

    private fun initializeViews() {
        ivProduct = findViewById(R.id.ivProduct)
        tvBrand = findViewById(R.id.tvBrand)
        tvProductName = findViewById(R.id.tvProductName)
        tvRating = findViewById(R.id.tvRating)
        tvPrice = findViewById(R.id.tvPrice)
        tvDescription = findViewById(R.id.tvDescription)
        tvReviewCount = findViewById(R.id.tvReviewCount)
        btnAddToCart = findViewById(R.id.btnAddToCart)
        btnBuyNow = findViewById(R.id.btnBuyNow)
        tvBack = findViewById(R.id.tvBack)
    }

    private fun loadProductData() {
        productId = intent.getIntExtra("product_id", 0)

        productName = intent.getStringExtra("product_name")
            ?: "Urban Runner"

        productBrand = intent.getStringExtra("product_brand")
            ?: "UJÉRA"

        productPrice = intent.getDoubleExtra(
            "product_price",
            399000.0
        )

        val rating = intent.getDoubleExtra(
            "product_rating",
            0.0
        )

        val reviewCount = intent.getIntExtra(
            "product_reviews",
            intent.getIntExtra("product_review_count", 0)
        )

        val description = intent.getStringExtra("product_description")
            ?: "Sepatu nyaman dengan desain modern untuk aktivitas sehari-hari."

        productImage = intent.getStringExtra("product_image")
            ?: intent.getStringExtra("product_image_url")

        whiteImage = intent.getStringExtra("product_image_white")
            ?: intent.getStringExtra("image_url_white")

        pinkImage = intent.getStringExtra("product_image_pink")
            ?: intent.getStringExtra("image_url_pink")

        blackImage = intent.getStringExtra("product_image_black")
            ?: intent.getStringExtra("image_url_black")

        tvBrand.text = productBrand.uppercase(Locale.ROOT)
        tvProductName.text = productName
        tvRating.text = "★ $rating  ($reviewCount reviews)"
        tvPrice.text = formatRupiah(productPrice)
        tvDescription.text = description
        tvReviewCount.text = "$reviewCount reviews"

        loadImageForSelectedColor()
    }

    private fun loadImageForSelectedColor() {
        val selectedImage = when (selectedColor) {
            "White" -> whiteImage
            "Pink" -> pinkImage
            "Black" -> blackImage
            else -> null
        } ?: productImage

        Glide.with(this)
            .load(selectedImage)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.ic_menu_gallery)
            .into(ivProduct)
    }

    private fun setupColorButtons() {
        val white = findViewById<Button>(R.id.btnColorWhite)
        val pink = findViewById<Button>(R.id.btnColorPink)
        val black = findViewById<Button>(R.id.btnColorBlack)

        white.setOnClickListener {
            selectedColor = "White"
            updateColorSelection(white, pink, black)
            loadImageForSelectedColor()
        }

        pink.setOnClickListener {
            selectedColor = "Pink"
            updateColorSelection(white, pink, black)
            loadImageForSelectedColor()
        }

        black.setOnClickListener {
            selectedColor = "Black"
            updateColorSelection(white, pink, black)
            loadImageForSelectedColor()
        }

        updateColorSelection(white, pink, black)
    }

    private fun updateColorSelection(
        white: Button,
        pink: Button,
        black: Button
    ) {
        white.backgroundTintList =
            ColorStateList.valueOf(Color.parseColor("#EDEDED"))

        pink.backgroundTintList =
            ColorStateList.valueOf(Color.parseColor("#F7C8D0"))

        black.backgroundTintList =
            ColorStateList.valueOf(Color.parseColor("#D8D8D8"))

        when (selectedColor) {
            "White" -> white.backgroundTintList =
                ColorStateList.valueOf(Color.parseColor("#C9E4F6"))

            "Pink" -> pink.backgroundTintList =
                ColorStateList.valueOf(Color.parseColor("#D98999"))

            "Black" -> black.backgroundTintList =
                ColorStateList.valueOf(Color.parseColor("#B8B8B8"))
        }
    }

    private fun setupSizeButtons() {
        val buttons = listOf(
            findViewById<Button>(R.id.btnSize38),
            findViewById<Button>(R.id.btnSize39),
            findViewById<Button>(R.id.btnSize40),
            findViewById<Button>(R.id.btnSize41),
            findViewById<Button>(R.id.btnSize42)
        )

        buttons.forEach { button ->
            button.setOnClickListener {
                selectedSize = button.text.toString()
                updateSizeSelection(buttons, button)
            }
        }

        val defaultButton = buttons.firstOrNull {
            it.text.toString() == "40"
        } ?: buttons.first()

        updateSizeSelection(buttons, defaultButton)
    }

    private fun updateSizeSelection(
        buttons: List<Button>,
        selectedButton: Button
    ) {
        buttons.forEach { button ->
            button.backgroundTintList =
                ColorStateList.valueOf(Color.WHITE)
        }

        selectedButton.backgroundTintList =
            ColorStateList.valueOf(Color.parseColor("#F7C8D0"))
    }

    private fun setupActions() {
        tvBack.setOnClickListener {
            finish()
        }

        btnAddToCart.setOnClickListener {
            addProductToCart()
        }

        btnBuyNow.setOnClickListener {
            buyNow()
        }
    }

    private fun createCartItem(): CartItem {
        val selectedImage = when (selectedColor) {
            "White" -> whiteImage
            "Pink" -> pinkImage
            "Black" -> blackImage
            else -> null
        } ?: productImage.orEmpty()

        return CartItem(
            productId = productId,
            name = productName,
            brand = productBrand,
            price = productPrice,
            imageUrl = selectedImage,
            color = selectedColor,
            size = selectedSize,
            quantity = 1
        )
    }

    private fun addProductToCart() {
        if (productId <= 0) {
            showMessage("ID produk tidak ditemukan.")
            return
        }

        if (productPrice <= 0.0) {
            showMessage("Harga produk tidak valid.")
            return
        }

        setButtonsEnabled(false)
        btnAddToCart.text = "MENAMBAHKAN..."

        lifecycleScope.launch {
            try {
                val request = CartRequest(
                    product_id = productId,
                    product_name = productName,
                    brand = productBrand,
                    price = productPrice,
                    image_url = createCartItem().imageUrl,
                    color = selectedColor,
                    size = selectedSize,
                    quantity = 1
                )

                SupabaseApi.cartApi.addToCart(request)
                CartManager.addItem(createCartItem())

                showMessage(
                    "$productName berhasil ditambahkan ke keranjang."
                )
            } catch (e: Exception) {
                showMessage(
                    "Gagal menambahkan ke keranjang. Periksa koneksi dan akses Supabase."
                )
            } finally {
                setButtonsEnabled(true)
                btnAddToCart.text = "ADD TO CART"
            }
        }
    }

    private fun buyNow() {
        if (productId <= 0) {
            showMessage("ID produk tidak ditemukan.")
            return
        }

        if (productPrice <= 0.0) {
            showMessage("Harga produk tidak valid.")
            return
        }

        CartManager.beginDirectCheckout(createCartItem())

        startActivity(
            Intent(this, CheckoutActivity::class.java)
        )
    }

    private fun setButtonsEnabled(enabled: Boolean) {
        btnAddToCart.isEnabled = enabled
        btnBuyNow.isEnabled = enabled
    }

    private fun showMessage(message: String) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_LONG
        ).show()
    }

    private fun formatRupiah(price: Double): String {
        val format = NumberFormat.getCurrencyInstance(
            Locale("id", "ID")
        )

        format.maximumFractionDigits = 0
        return format.format(price)
    }
}
