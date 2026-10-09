
package com.example.ujera.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

data class AuthRequest(
    val email: String,
    val password: String,
    val data: Map<String, String>? = null
)

data class AuthUser(
    val id: String?,
    val email: String?
)

data class AuthResponse(
    val access_token: String?,
    val token_type: String?,
    val expires_in: Long?,
    val user: AuthUser?,
    val msg: String?,
    val message: String?
)

interface AuthApi {
    @Headers("Content-Type: application/json")
    @POST("signup")
    suspend fun signUp(
        @Body request: AuthRequest
    ): Response<AuthResponse>

    @Headers("Content-Type: application/json")
    @POST("token")
    suspend fun signIn(
        @Query("grant_type") grantType: String = "password",
        @Body request: AuthRequest
    ): Response<AuthResponse>
}
