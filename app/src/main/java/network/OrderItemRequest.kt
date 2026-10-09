
package com.example.ujera.network

data class OrderItemRequest(
    val order_id: Int,
    val product_id: Int,
    val product_name: String,
    val brand: String?,
    val price: Double,
    val color: String,
    val size: String,
    val quantity: Int
)
