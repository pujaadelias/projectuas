
package com.example.ujera

object CartManager {

    private val items = mutableListOf<CartItem>()

    // Sesi Buy Now dipisahkan dari keranjang biasa.
    private var directCheckoutItems: List<CartItem>? = null

    @Synchronized
    fun getItems(): List<CartItem> = items.map { it.copy() }

    @Synchronized
    fun getCheckoutItems(): List<CartItem> {
        val source = directCheckoutItems ?: items
        return source.map { it.copy() }
    }

    @Synchronized
    fun isDirectCheckout(): Boolean {
        return directCheckoutItems != null
    }

    @Synchronized
    fun beginDirectCheckout(item: CartItem) {
        directCheckoutItems = listOf(item.copy())
    }

    @Synchronized
    fun clearDirectCheckout() {
        directCheckoutItems = null
    }

    @Synchronized
    fun setItems(newItems: List<CartItem>) {
        items.clear()
        items.addAll(newItems.map { it.copy() })
    }

    @Synchronized
    fun addItem(item: CartItem) {
        val existing = items.find {
            it.productId == item.productId &&
                    it.color.equals(item.color, ignoreCase = true) &&
                    it.size == item.size
        }

        if (existing != null) {
            existing.quantity += item.quantity
        } else {
            items.add(item.copy())
        }
    }

    @Synchronized
    fun increaseQuantity(position: Int) {
        if (position in items.indices) {
            items[position].quantity++
        }
    }

    @Synchronized
    fun decreaseQuantity(position: Int) {
        if (position !in items.indices) return

        if (items[position].quantity > 1) {
            items[position].quantity--
        } else {
            items.removeAt(position)
        }
    }

    @Synchronized
    fun removeItem(position: Int) {
        if (position in items.indices) {
            items.removeAt(position)
        }
    }

    @Synchronized
    fun clear() {
        items.clear()
        directCheckoutItems = null
    }

    @Synchronized
    fun getTotal(): Double {
        return items.sumOf { it.price * it.quantity }
    }

    @Synchronized
    fun getCheckoutTotal(): Double {
        return getCheckoutItems().sumOf {
            it.price * it.quantity
        }
    }

    @Synchronized
    fun getItemCount(): Int {
        return items.sumOf { it.quantity }
    }
}
