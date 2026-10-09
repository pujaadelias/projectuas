
package com.example.ujera.network

import com.google.gson.annotations.SerializedName

data class OrderRequest(
    val order_number: String,
    val customer_name: String,
    val customer_phone: String,
    val customer_address: String,
    val shipping_method: String,
    val shipping_cost: Double,
    val subtotal: Double,
    val total_payment: Double,
    val payment_method: String?,

    @SerializedName("order_status")
    val status: String = "Processing"
)
