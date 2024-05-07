package com.example.tripplanner.apis.amadeus

//This is just a simple object so that we can access the token from multiple places
object SimpleTokenManager {

    private var accessToken: String? = "null"

    fun setToken(token: String) {
        accessToken = token
    }

    fun getToken(): String? {
        return accessToken
    }
}