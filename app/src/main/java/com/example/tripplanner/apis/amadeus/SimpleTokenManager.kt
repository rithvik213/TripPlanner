package com.example.tripplanner.apis.amadeus

object SimpleTokenManager {

    private var accessToken: String? = "null"

    fun setToken(token: String) {
        accessToken = token
    }

    fun getToken(): String? {
        return accessToken
    }
}