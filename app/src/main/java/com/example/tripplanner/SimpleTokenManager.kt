package com.example.flightapitest

object SimpleTokenManager {

    private var accessToken: String? = "null"

    fun setToken(token: String) {
        this.accessToken = token
    }

    fun getToken(): String? {
        return accessToken
    }
}