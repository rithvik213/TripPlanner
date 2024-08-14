package com.example.tripplanner.apis

import android.content.Context
import com.example.tripplanner.R
import com.example.tripplanner.database.MyApp

object ApiKeyProvider {

    private var appContext: Context? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }
    private fun getContext(): Context {
        return appContext ?: throw IllegalStateException("ApiKeyProvider is not initialized")
    }

    fun getAmadeusClientId(): String {
        return getContext().getString(R.string.amadeus_client_id)
    }

    fun getAmadeusClientSecret(): String {
        return getContext().getString(R.string.amadeus_client_secret)
    }

    fun getSerpAPIKey(): String {
        return getContext().getString(R.string.serp_api_key)
    }

    fun getTripAdvisorApiKey(): String {
        return getContext().getString(R.string.trip_advisor_api_key)
    }

    fun getOpenAiApiKey(): String {
        return getContext().getString(R.string.open_ai_api_key)
    }
}
