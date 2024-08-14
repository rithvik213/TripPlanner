package com.example.tripplanner.apis.tripadvisor;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.content.Context;
import com.example.tripplanner.apis.ApiKeyProvider;

import com.example.tripplanner.apis.serp.SerpApiService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Defines the class for using TripAdvisor
public class EventFetcher {

    // Callback for once events are fetched
    public interface EventFetchListener {
        void onEventsFetched(List<EventResult> events);
        void onEventFetchFailed(String errorMessage);
    }
    // Variables necessary to store for simultaneous calls to different endpoints
    private String cityName;
    private Context context;
    private EventFetchListener listener;
    private String returnDate;
    private String departDate;
    public EventFetcher(String cityName, Context context, String returnDate, String departDate, EventFetchListener listener) {
        this.cityName = cityName;
        this.context = context;
        this.listener = listener;
        this.returnDate = returnDate;
        this.departDate = departDate;
    }
    // Retrofit Instance
    public void fetchEvents() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://serpapi.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        SerpApiService service = retrofit.create(SerpApiService.class);

        // Parameters for the API body and specifying the search parameters
        Map<String, String> parameters = new HashMap<>();
        parameters.put("engine", "google_events"); // To specify the data we want back
        parameters.put("q", "Events in " + cityName + " " + departDate + " to " + returnDate); // Can take any length string input, similar to Google Search
        parameters.put("hl", "en");
        parameters.put("gl", "us");
        //parameters.put("api_key", "SERP_API_KEY");
        parameters.put("api_key", ApiKeyProvider.INSTANCE.getSerpAPIKey());

        Call<JsonObject> call = service.getEvents(parameters);
        call.enqueue(new Callback<JsonObject>() {
            // callback for response to API
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<EventResult> events = new Gson().fromJson(response.body().getAsJsonArray("events_results"), new TypeToken<List<EventResult>>(){}.getType());
                    listener.onEventsFetched(events);
                } else {
                    String errorMessage = "Failed to retrieve events";
                    if (response.errorBody() != null) {
                        try {
                            errorMessage += ": " + response.errorBody().string();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    listener.onEventFetchFailed(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                listener.onEventFetchFailed("Error: " + t.getMessage());
            }
        });
    }

    // Specifies Event Results data class to just get the title and date (which includes exact time) from SerpAPI
    public class EventResult {
        public String title;
        public Date date;

        public class Date {
            public String when;
        }
    }

}


