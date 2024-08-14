package com.example.tripplanner.apis.amadeus

import android.content.Context
import com.example.tripplanner.apis.ApiKeyProvider
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object AmadeusApiClient {

    private const val BASE_URL = "https://test.api.amadeus.com"

    val client: AmadeusApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(createOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AmadeusApiService::class.java)
    }

    //Use custom timeout for Amadeus because calls can sometimes be long, also add TokenInterceptor
    private fun createOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(TokenInterceptor())
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .build()
    }

}


class TokenInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val clientId = ApiKeyProvider.getAmadeusClientId()
        val clientSecret = ApiKeyProvider.getAmadeusClientSecret()
        val token = SimpleTokenManager.getToken()

        val request = chain.request().newBuilder()
            .apply {
                if (token != null) {
                    header("Authorization", "Bearer $token")
                }
            }
            .build()

        var response = chain.proceed(request)

        if (response.code() == 401) {
            val refreshTokenResponse = AmadeusApiClient.client.getToken(
                clientId = clientId,
                clientSecret = clientSecret,
                grantType = "client_credentials"
            ).execute()

            if (refreshTokenResponse.isSuccessful) {
                val newToken = refreshTokenResponse.body()?.accessToken
                newToken?.let {
                    SimpleTokenManager.setToken(newToken)

                    val newRequest = request.newBuilder()
                        .header("Authorization", "Bearer $newToken")
                        .build()

                    response.close()
                    response = chain.proceed(newRequest)
                }
            }
        }

        return response
    }
}

