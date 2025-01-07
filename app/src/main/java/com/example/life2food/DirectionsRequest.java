package com.example.life2food;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DirectionsRequest extends AsyncTask<Void, Void, String> {

    private static final String TAG = "DirectionsRequest"; // Etiqueta para los mensajes de depuración
    private final String url; // URL de la solicitud de direcciones
    private final GoogleMap googleMap; // Mapa de Google
    private final String mode; // Modo de transporte
    private final DirectionsRequestCallback callback; // Callback

    // Constructor
    public DirectionsRequest(MapsActivity activity, String url, GoogleMap googleMap, String mode, DirectionsRequestCallback callback) {
        this.url = url;
        this.googleMap = googleMap;
        this.mode = mode;
        this.callback = callback;
    }

    // makes an HTTP GET request, reads the response, and returns it as a string.
    @Override
    protected String doInBackground(Void... voids) {
        try {
            URL url = new URL(this.url);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            return result.toString();
        } catch (Exception e) {
            Log.e(TAG, "Error en la solicitud de direcciones: " + e.getMessage());
            return null;
        }
    }

    // takes the result of a directions request (in JSON format), processes it to extract the route and duration information, decodes the route points, creates a line on the map with these points, and then reports the route duration.
    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            try {
                JSONObject json = new JSONObject(result);
                JSONArray routes = json.getJSONArray("routes");
                if (routes.length() > 0) {
                    JSONArray legs = routes.getJSONObject(0).getJSONArray("legs");
                    JSONObject leg = legs.getJSONObject(0);
                    JSONObject duration = leg.getJSONObject("duration");
                    String durationText = duration.getString("text");

                    JSONArray steps = leg.getJSONArray("steps");
                    List<LatLng> path = new ArrayList<>();
                    for (int i = 0; i < steps.length(); i++) {
                        String polyline = steps.getJSONObject(i).getJSONObject("polyline").getString("points");
                        path.addAll(DecodePoly.decodePolyline(polyline));
                    }

                    PolylineOptions polylineOptions = new PolylineOptions().addAll(path);
                    if ("driving".equals(mode)) {
                        polylineOptions.color(0xFF3BC1BF);
                    } else if ("walking".equals(mode)) {
                        polylineOptions.color(0xFF3BC16E);
                    }

                    googleMap.addPolyline(polylineOptions);
                    Log.d(TAG, "Ruta trazada con éxito.");

                    if (callback != null) {
                        callback.onDurationRetrieved(mode, durationText);
                    }
                } else {
                    Log.e(TAG, "No se encontraron rutas.");
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error al analizar la respuesta JSON: " + e.getMessage());
            }
        }
    }

    // Callback interface
    public interface DirectionsRequestCallback {
        void onDurationRetrieved(String mode, String duration);
    }
}