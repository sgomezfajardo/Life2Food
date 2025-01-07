package com.example.life2food;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class GeocodingService {

    private static final String GEOCODING_API_URL = "https://maps.googleapis.com/maps/api/geocode/json";
    private String apiKey;

    public GeocodingService(String apiKey) {
        this.apiKey = apiKey;
    }

    // takes an address as input, makes a request to the Geocoding API to get the coordinates (latitude and longitude) of that address, and returns a Task<LatLng> containing the coordinates if the request is successful
    public Task<LatLng> getLocationFromAddress(String address) {
        TaskCompletionSource<LatLng> taskCompletionSource = new TaskCompletionSource<>();

        new Thread(() -> {
            try {
                String encodedAddress = URLEncoder.encode(address, "UTF-8");
                String requestUrl = GEOCODING_API_URL + "?address=" + encodedAddress + "&key=" + apiKey;

                HttpURLConnection connection = (HttpURLConnection) new URL(requestUrl).openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Parsear la respuesta JSON
                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray results = jsonResponse.getJSONArray("results");

                if (results.length() > 0) {
                    JSONObject location = results.getJSONObject(0).getJSONObject("geometry").getJSONObject("location");
                    double lat = location.getDouble("lat");
                    double lng = location.getDouble("lng");
                    LatLng latLng = new LatLng(lat, lng);
                    taskCompletionSource.setResult(latLng);
                } else {
                    taskCompletionSource.setException(new Exception("No se encontraron resultados"));
                }

            } catch (Exception e) {
                e.printStackTrace();
                taskCompletionSource.setException(e);
            }
        }).start();

        return taskCompletionSource.getTask();
    }
}
