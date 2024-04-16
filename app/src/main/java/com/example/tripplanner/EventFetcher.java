package com.example.tripplanner;


import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventFetcher {

    public interface EventFetchListener {
        void onEventsFetched(List<EventResult> events);
        void onEventFetchFailed(String errorMessage);
    }
    //private RecyclerView recyclerView;
    private String cityName;
    private Context context;

    private EventFetchListener listener;

    public EventFetcher(String cityName, Context context, EventFetchListener listener) {
        //this.recyclerView = recyclerView;
        this.cityName = cityName;
        this.context = context;
        this.listener = listener;
    }

    public void fetchEvents() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://serpapi.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        SerpApiService service = retrofit.create(SerpApiService.class);

        Map<String, String> parameters = new HashMap<>();
        parameters.put("engine", "google_events");
        parameters.put("q", "Events in " + cityName);
        parameters.put("hl", "en");
        parameters.put("gl", "us");
        parameters.put("api_key", "9212fd8f821b387637eb1f6c78b9f36f11d8d0f13b321533022b0568d4b8392f");

        Call<JsonObject> call = service.getEvents(parameters);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<EventResult> events = new Gson().fromJson(response.body().getAsJsonArray("events_results"), new TypeToken<List<EventResult>>(){}.getType());
                    //updateUI(events);
                    listener.onEventsFetched(events);
                } else {
                    // Error handling
                    String errorMessage = "Failed to retrieve events";
                    if (response.errorBody() != null) {
                        try {
                            // Attempt to parse the error body if present
                            errorMessage += ": " + response.errorBody().string();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(context, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

/*
    private void updateUI(List<EventResult> events) {

        EventAdapter adapter = new EventAdapter(events);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);
    }*/

    public class EventResult {
        String title;
        Date date;

        public class Date {
            String when;
        }
    }
/*
    public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {
        private List<EventResult> events;

        public EventAdapter(List<EventResult> events) {
            this.events = events;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            EventResult event = events.get(position);
            holder.titleTextView.setText(event.title);
            holder.whenTextView.setText(event.date.when);
        }

        @Override
        public int getItemCount() {
            return events.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView titleTextView;
            public TextView whenTextView;

            public ViewHolder(View itemView) {
                super(itemView);
                titleTextView = itemView.findViewById(R.id.title);
                whenTextView = itemView.findViewById(R.id.when);
            }
        }
    }*/
}


