package com.example.tripplanner.apis.openai

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Response
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChatGPTService(private val apiKey: String) {
    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $apiKey")
                .build()
            chain.proceed(request)
        }.build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.openai.com/v1/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    private val apiService = retrofit.create(OpenAIService::class.java)

    suspend fun generateResponse(prompt: String): String = withContext(Dispatchers.IO) {
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

