package com.example.tripplanner.apis.serp;

import com.google.gson.JsonObject;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

// The SerpAPI request body as specified by its docs
public interface SerpApiService {
    @GET("search.json") // Body type
    Call<JsonObject> getEvents(@QueryMap Map<String, String> options);
}
