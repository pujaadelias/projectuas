
package com.example.ujera.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface ProductApi {

    @Headers("Accept: application/json")
    @GET("products")
    suspend fun getProducts(): List<Product>

    @Headers("Accept: application/json")
    @GET("products")
    suspend fun getProductsResponse(
        @Query("select") select: String = "*",
        @Query("order") order: String = "id.asc"
    ): Response<List<Product>>
}
