
package com.example.ujera.network

import com.google.gson.annotations.SerializedName

data class OrderResponse(
    val id: Int,
    val order_number: String,
    val customer_name: String,
    val customer_phone: String,
    val customer_address: String,
    val payment_method: String?,
    val shipping_method: String,
    val shipping_cost: Double,
    val subtotal: Double,
    val total_payment: Double,

    @SerializedName("order_status")
    val status: String,

    val created_at: String?
)
