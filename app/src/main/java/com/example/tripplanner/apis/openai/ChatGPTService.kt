package com.example.tripplanner.apis.openai

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Response
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


/* Retrofit Instance of the OpenAIService class using our apiKey
* and specifies the parameters needed to make our requests
*/

class ChatGPTService(private val apiKey: String) {
    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $apiKey")
                .build()
            chain.proceed(request)
        }.build()

    // Retrofit Instance
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.openai.com/v1/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    private val apiService = retrofit.create(OpenAIService::class.java)

    // We generate the exact prompt in our OpenAIService class
    // Create Completion is the endpoint we used and is one of the most
    // popular offered by OpenAI
    // The price per request can be limited by maxTokens which for '1000'
    // tokens is roughly $.01, so we had to limit the length of the itinerary
    // in order to not get our response cut off
    suspend fun generateResponse(prompt: String): String = withContext(Dispatchers.IO) {
        //More tokens than this and it ends up being expensive for little gain
        val requestBody = CompletionRequest(
            prompt = prompt,
            maxTokens = 1000,
            temperature = 0.0
        )

        try {
            val response: Response<ApiResponse> = apiService.createCompletion(requestBody)
            if (response.isSuccessful) {
                response.body()?.choices?.firstOrNull()?.text?.trim() ?: "No response generated"
            } else {
                "Failed to generate response: ${response.errorBody()?.string()}"
            }
        } catch (e: Exception) {
            "Failed to generate response: ${e.localizedMessage}"
        }
    }
}

