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

    private static final String TAG = "DirectionsRequest";
    private final String url;
    private final GoogleMap googleMap;

    public DirectionsRequest(MapsActivity activity, String url, GoogleMap googleMap) {
        this.url = url;
        this.googleMap = googleMap;
    }

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

    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            try {
                JSONObject json = new JSONObject(result);
                JSONArray routes = json.getJSONArray("routes");
                if (routes.length() > 0) {
                    JSONArray steps = routes.getJSONObject(0).getJSONArray("legs")
                            .getJSONObject(0).getJSONArray("steps");
                    List<LatLng> path = new ArrayList<>();
                    for (int i = 0; i < steps.length(); i++) {
                        String polyline = steps.getJSONObject(i).getJSONObject("polyline").getString("points");
                        path.addAll(DecodePoly.decodePolyline(polyline));
                    }
                    googleMap.addPolyline(new PolylineOptions().addAll(path));
                    Log.d(TAG, "Ruta trazada con éxito.");
                } else {
                    Log.e(TAG, "No se encontraron rutas.");
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error al analizar la respuesta JSON: " + e.getMessage());
            }
        }
    }
}
