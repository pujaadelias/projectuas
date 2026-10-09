package com.example.ujera.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface OrderApi {

    @Headers(
        "Content-Type: application/json",
        "Prefer: return=representation"
    )
    @POST("orders")
    suspend fun createOrder(
        @Body order: OrderRequest
    ): List<OrderResponse>


    @Headers(
        "Content-Type: application/json",
        "Prefer: return=representation"
    )
    @POST("order_items")
    suspend fun createOrderItems(
        @Body items: List<OrderItemRequest>
    ): List<OrderItemRequest>


    @Headers(
        "Accept: application/json"
    )
    @GET("orders")
    suspend fun getOrders(
        @Query("order") order: String = "created_at.desc"
    ): List<OrderResponse>
}