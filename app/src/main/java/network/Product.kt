
package com.example.ujera.network

import com.google.gson.annotations.SerializedName

data class Product(
    @SerializedName("id")
    val id: Int = 0,

    @SerializedName("name")
    val name: String = "",

    @SerializedName("brand")
    val brand: String = "",

    @SerializedName("category")
    val category: String = "",

    @SerializedName("price")
    val price: Double = 0.0,

    @SerializedName("image_url")
    val image_url: String? = null,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("stock")
    val stock: Int = 0,

    @SerializedName("created_at")
    val created_at: String? = null,

    @SerializedName("rating")
    val rating: Double? = null,

    @SerializedName("review_count")
    val review_count: Int? = null
)
