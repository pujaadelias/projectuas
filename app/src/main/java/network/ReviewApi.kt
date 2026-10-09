
package com.example.ujera.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

data class ReviewRequest(
    val order_number: String,
    val rating: Int,
    val review_text: String
)

interface ReviewApi {

    @Headers(
        "Content-Type: application/json",
        "Prefer: return=minimal"
    )
    @POST("reviews")
    suspend fun addReview(
        @Body review: ReviewRequest
    ): Response<Unit>
}
