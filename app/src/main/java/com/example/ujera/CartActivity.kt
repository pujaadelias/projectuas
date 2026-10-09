
package com.example.ujera

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.ujera.network.CartResponse
import com.example.ujera.network.CartUpdateRequest
import com.example.ujera.network.SupabaseApi
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class CartActivity : AppCompatActivity() {

    private lateinit var cartContainer: LinearLayout
    private lateinit var tvSubtotal: TextView
    private lateinit var tvItemCount: TextView
    private lateinit var btnCheckout: Button

    private val cartItems = mutableListOf<CartResponse>()

    private var isLoading = false
    private var isUpdating = false

    private val pink = Color.parseColor("#D98999")
    private val lightBlue = Color.parseColor("#DDEEFF")
    private val dark = Color.parseColor("#292929")
    private val grey = Color.parseColor("#777777")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        initializeViews()
        setupActions()
        loadCart()
    }

    private fun initializeViews() {
        cartContainer = findViewById(R.id.cartContainer)
        tvSubtotal = findViewById(R.id.tvSubtotal)
        tvItemCount = findViewById(R.id.tvItemCount)
        btnCheckout = findViewById(R.id.btnCheckout)
    }

    private fun setupActions() {
        findViewById<TextView>(R.id.tvBack).setOnClickListener {
            finish()
        }

        btnCheckout.setOnClickListener {
            if (isUpdating || isLoading) {
                showMessage("Tunggu hingga keranjang selesai diperbarui.")
                return@setOnClickListener
            }

            if (cartItems.isEmpty()) {
                showMessage("Keranjang masih kosong.")
                return@setOnClickListener
            }

            syncCartManager()

            startActivity(
                Intent(this, CheckoutActivity::class.java)
            )
        }
    }

    override fun onResume() {
        super.onResume()

        // Muat ulang ketika kembali ke halaman keranjang,
        // kecuali saat permintaan sebelumnya masih berjalan.
        if (::cartContainer.isInitialized && !isLoading && !isUpdating) {
            loadCart()
        }
    }

    private fun loadCart() {
        if (isLoading || isUpdating) return

        isLoading = true
        btnCheckout.isEnabled = false

        lifecycleScope.launch {
            try {
                val result = SupabaseApi.cartApi.getCart()

                cartItems.clear()
                cartItems.addAll(result)

                syncCartManager()
                displayCart()
            } catch (e: Exception) {
                cartContainer.removeAllViews()

                val message = TextView(this@CartActivity).apply {
                    text = "Keranjang gagal dimuat.\nPeriksa koneksi dan akses Supabase, lalu coba lagi."
                    textSize = 14f
                    setTextColor(grey)
                    gravity = Gravity.CENTER
                    setPadding(dp(20), dp(32), dp(20), dp(32))
                }

                cartContainer.addView(message)

                showMessage("Gagal memuat keranjang.")
            } finally {
                isLoading = false
                btnCheckout.isEnabled = !isUpdating && cartItems.isNotEmpty()
                updateSummary()
            }
        }
    }

    private fun displayCart() {
        cartContainer.removeAllViews()

        if (cartItems.isEmpty()) {
            showEmptyCart()
            updateSummary()
            return
        }

        cartItems.forEach { item ->
            cartContainer.addView(createCartCard(item))
        }

        updateSummary()
    }

    private fun showEmptyCart() {
        val emptyLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(20), dp(45), dp(20), dp(45))
        }

        val icon = TextView(this).apply {
            text = "🛒"
            textSize = 44f
            gravity = Gravity.CENTER
        }

        val title = TextView(this).apply {
            text = "Your cart is empty"
            textSize = 19f
            setTextColor(dark)
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
        }

        val subtitle = TextView(this).apply {
            text = "Add your favorite shoes to the cart."
            textSize = 13f
            setTextColor(grey)
            gravity = Gravity.CENTER
        }

        emptyLayout.addView(icon)
        emptyLayout.addView(title)

        emptyLayout.addView(
            subtitle,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(6)
            }
        )

        cartContainer.addView(emptyLayout)
    }

    private fun createCartCard(item: CartResponse): LinearLayout {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(dp(12), dp(12), dp(12), dp(12))
        }

        val cardParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = dp(12)
        }

        card.layoutParams = cardParams

        val image = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(dp(100), dp(100))
            scaleType = ImageView.ScaleType.CENTER_CROP
            setBackgroundColor(Color.parseColor("#F2F2F2"))
        }

        Glide.with(this)
            .load(item.image_url)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.ic_menu_gallery)
            .into(image)

        card.addView(image)

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        card.addView(
            content,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                marginStart = dp(12)
            }
        )

        val name = TextView(this).apply {
            text = item.product_name
            textSize = 15f
            setTextColor(dark)
            setTypeface(null, Typeface.BOLD)
        }
        content.addView(name)

        val brand = TextView(this).apply {
            text = item.brand
            textSize = 11f
            setTextColor(Color.parseColor("#A94F65"))
        }

        content.addView(
            brand,
            topMarginParams(3)
        )

        val variation = TextView(this).apply {
            text = "Color: ${item.color}  •  Size: ${item.size}"
            textSize = 11f
            setTextColor(grey)
        }

        content.addView(
            variation,
            topMarginParams(5)
        )

        val price = TextView(this).apply {
            text = formatRupiah(item.price)
            textSize = 14f
            setTextColor(dark)
            setTypeface(null, Typeface.BOLD)
        }

        content.addView(
            price,
            topMarginParams(6)
        )

        val quantityRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        content.addView(
            quantityRow,
            topMarginParams(10).apply {
                height = dp(38)
            }
        )

        val minus = makeQuantityButton("−", lightBlue)
        val quantity = TextView(this).apply {
            text = item.quantity.toString()
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(dark)
        }
        val plus = makeQuantityButton("+", Color.parseColor("#F7C8D0"))

        quantityRow.addView(minus)
        quantityRow.addView(
            quantity,
            LinearLayout.LayoutParams(dp(38), dp(38))
        )
        quantityRow.addView(plus)

        val delete = TextView(this).apply {
            text = "Delete"
            textSize = 11f
            gravity = Gravity.CENTER
            setTextColor(Color.parseColor("#C05D6D"))
            setPadding(dp(8), 0, dp(4), 0)
        }

        quantityRow.addView(
            delete,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                dp(38)
            )
        )

        minus.setOnClickListener {
            if (item.quantity <= 1) {
                deleteCartItem(item)
            } else {
                updateQuantity(item, item.quantity - 1)
            }
        }

        plus.setOnClickListener {
            updateQuantity(item, item.quantity + 1)
        }

        delete.setOnClickListener {
            deleteCartItem(item)
        }

        return card
    }

    private fun makeQuantityButton(
        label: String,
        backgroundColor: Int
    ): TextView {
        return TextView(this).apply {
            text = label
            textSize = 20f
            gravity = Gravity.CENTER
            setTextColor(dark)
            setBackgroundColor(backgroundColor)
            layoutParams = LinearLayout.LayoutParams(dp(38), dp(38))
        }
    }

    private fun updateQuantity(
        item: CartResponse,
        newQuantity: Int
    ) {
        if (isUpdating || isLoading) return
        if (newQuantity < 1) return

        isUpdating = true
        btnCheckout.isEnabled = false

        lifecycleScope.launch {
            try {
                val response = SupabaseApi.cartApi.updateQuantity(
                    id = "eq.${item.id}",
                    body = CartUpdateRequest(quantity = newQuantity)
                )

                if (response.isEmpty()) {
                    showMessage(
                        "Jumlah tidak berubah. Periksa izin update tabel carts di Supabase."
                    )
                }

                // Muat ulang data dari server setelah permintaan selesai.
                refreshCartAfterMutation()
            } catch (e: Exception) {
                showMessage("Gagal mengubah jumlah produk.")
            } finally {
                isUpdating = false
                btnCheckout.isEnabled = !isLoading && cartItems.isNotEmpty()
            }
        }
    }

    private fun deleteCartItem(item: CartResponse) {
        if (isUpdating || isLoading) return

        isUpdating = true
        btnCheckout.isEnabled = false

        lifecycleScope.launch {
            try {
                val response = SupabaseApi.cartApi.deleteCartItem(
                    id = "eq.${item.id}"
                )

                if (response.isSuccessful) {
                    showMessage("Produk dihapus dari keranjang.")
                    refreshCartAfterMutation()
                } else {
                    showMessage("Produk gagal dihapus dari server.")
                }
            } catch (e: Exception) {
                showMessage("Gagal menghapus produk. Periksa koneksi.")
            } finally {
                isUpdating = false
                btnCheckout.isEnabled = !isLoading && cartItems.isNotEmpty()
            }
        }
    }

    private suspend fun refreshCartAfterMutation() {
        val result = SupabaseApi.cartApi.getCart()

        cartItems.clear()
        cartItems.addAll(result)

        syncCartManager()
        displayCart()
    }

    private fun updateSummary() {
        val subtotal = cartItems.sumOf {
            it.price * it.quantity
        }

        val itemCount = cartItems.sumOf {
            it.quantity
        }

        tvSubtotal.text = formatRupiah(subtotal)

        tvItemCount.text = if (itemCount == 1) {
            "1 item"
        } else {
            "$itemCount items"
        }

        btnCheckout.isEnabled =
            cartItems.isNotEmpty() && !isLoading && !isUpdating
    }

    private fun syncCartManager() {
        val localItems = cartItems.map { item ->
            CartItem(
                productId = item.product_id,
                name = item.product_name,
                brand = item.brand,
                price = item.price,
                imageUrl = item.image_url.orEmpty(),
                color = item.color,
                size = item.size,
                quantity = item.quantity
            )
        }

        CartManager.setItems(localItems)
    }

    private fun topMarginParams(marginDp: Int): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = dp(marginDp)
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
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
