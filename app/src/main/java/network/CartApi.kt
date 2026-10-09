package com.example.ujera.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

interface CartApi {

    // =========================================================
    // GET CART
    // =========================================================

    @Headers(
        "Accept: application/json"
    )
    @GET("carts")
    suspend fun getCart(): List<CartResponse>


    // =========================================================
    // ADD TO CART
    // =========================================================

    @Headers(
        "Content-Type: application/json",
        "Prefer: return=representation"
    )
    @POST("carts")
    suspend fun addToCart(
        @Body cart: CartRequest
    ): List<CartResponse>


    // =========================================================
    // UPDATE QUANTITY
    // =========================================================

    @Headers(
        "Content-Type: application/json",
        "Prefer: return=representation"
    )
    @PATCH("carts")
    suspend fun updateQuantity(
        @Query("id") id: String,
        @Body body: CartUpdateRequest
    ): List<CartResponse>


    // =========================================================
    // DELETE CART ITEM
    // =========================================================

    @DELETE("carts")
    suspend fun deleteCartItem(
        @Query("id") id: String
    ): Response<Unit>
}