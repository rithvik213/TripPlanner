package com.example.flightapitest

import com.google.gson.annotations.SerializedName

data class TokenResponse(
    @SerializedName("type") val type: String,
    @SerializedName("username") val username: String,
    @SerializedName("application_name") val applicationName: String,
    @SerializedName("client_id") val clientId: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("expires_in") val expiresIn: Int,
    @SerializedName("state") val state: String,
    @SerializedName("scope") val scope: String
)
