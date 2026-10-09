package com.example.ujera.network

data class CartRequest(
    val product_id: Int,
    val product_name: String,
    val brand: String,
    val price: Double,
    val image_url: String?,
    val color: String,
    val size: String,
    val quantity: Int
)