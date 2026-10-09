package com.example.ujera

data class CartItem(

    val productId: Int,

    val name: String,

    val brand: String,

    val price: Double,

    val imageUrl: String,

    var color: String,

    var size: String,

    var quantity: Int

)