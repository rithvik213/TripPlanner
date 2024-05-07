package com.example.tripplanner.apis.amadeus

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
        val token = SimpleTokenManager.getToken()

        val request = if (token != null) {
            val originalRequest = chain.request()
            val newRequestBuilder = originalRequest.newBuilder()

            //Use the bearer token from our SimpleTokenManager if it's a GET call
            //The only non-GET call is when we're trying to get another bearer token
            if (originalRequest.method() == "GET") {
                newRequestBuilder.header("Authorization", "Bearer $token")
            }

            newRequestBuilder.build()

        } else {
            chain.request()
        }

        var response = chain.proceed(request)

        //401 is the error code received if our token isn't valid anymore
        if (response.code() == 401) {

            //Get a new token
            val refreshTokenResponse = AmadeusApiClient.client.getToken().execute()

            if (refreshTokenResponse.isSuccessful) {
                val newToken = refreshTokenResponse.body()?.accessToken
                newToken?.let {
                    SimpleTokenManager.setToken(newToken)

                    //Resend the request that returned 401 with the new token
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

