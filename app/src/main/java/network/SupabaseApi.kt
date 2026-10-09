
package com.example.ujera.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object SupabaseApi {

    @Volatile
    private var accessToken: String? = null

    fun setAccessToken(token: String?) {
        accessToken = token?.takeIf { it.isNotBlank() }
    }

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(
                Interceptor { chain ->
                    val original = chain.request()
                    val path = original.url().encodedPath()
                    val isAuthRequest = path.contains("/auth/v1/")

                    val bearerToken = if (isAuthRequest) {
                        SupabaseConfig.SUPABASE_KEY
                    } else {
                        accessToken ?: SupabaseConfig.SUPABASE_KEY
                    }

                    val request = original.newBuilder()
                        .header(
                            "apikey",
                            SupabaseConfig.SUPABASE_KEY
                        )
                        .header(
                            "Authorization",
                            "Bearer $bearerToken"
                        )
                        .header(
                            "Accept",
                            "application/json"
                        )
                        .build()

                    chain.proceed(request)
                }
            )
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(
                "${SupabaseConfig.SUPABASE_URL.trimEnd('/')}/rest/v1/"
            )
            .client(client)
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
    }

    private val authRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(
                "${SupabaseConfig.SUPABASE_URL.trimEnd('/')}/auth/v1/"
            )
            .client(client)
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
    }

    val productApi: ProductApi by lazy {
        retrofit.create(ProductApi::class.java)
    }

    val cartApi: CartApi by lazy {
        retrofit.create(CartApi::class.java)
    }

    val orderApi: OrderApi by lazy {
        retrofit.create(OrderApi::class.java)
    }

    val reviewApi: ReviewApi by lazy {
        retrofit.create(ReviewApi::class.java)
    }

    val authApi: AuthApi by lazy {
        authRetrofit.create(AuthApi::class.java)
    }
}
