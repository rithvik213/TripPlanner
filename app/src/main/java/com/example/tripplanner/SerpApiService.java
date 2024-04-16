package com.example.tripplanner;

import com.google.gson.JsonObject;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

// Assuming SerpApiService is an interface where you define your Retrofit API calls
public interface SerpApiService {
    @GET("search.json")
    Call<JsonObject> getEvents(@QueryMap Map<String, String> options);
}
