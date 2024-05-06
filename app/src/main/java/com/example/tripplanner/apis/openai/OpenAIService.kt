package com.example.tripplanner.apis.openai

import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.Response

import com.google.gson.annotations.SerializedName

data class CompletionRequest(
    @SerializedName("prompt") val prompt: String,
    @SerializedName("max_tokens") val maxTokens: Int,
    @SerializedName("temperature") val temperature: Double
)

interface OpenAIService {
    @POST("engines/gpt-3.5-turbo-instruct/completions")
    @Headers("Content-Type: application/json")
    suspend fun createCompletion(@Body request: CompletionRequest): Response<ApiResponse>
}

data class ApiResponse(val choices: List<Choice>)
data class Choice(val text: String)

