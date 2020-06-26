package com.sammilward.neighbourhood.ui.login;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.AddressComponent;
import com.google.maps.model.AddressComponentType;
import com.google.maps.model.GeocodingResult;
import com.google.type.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static android.media.CamcorderProfile.get;
import static com.sammilward.neighbourhood.ui.login.Constants.GOOGLEAPIKEY;

public class Geocoder {

    private final String TAG = "Geocoder";
    private String requestURL = "https://maps.googleapis.com/maps/api/geocode/json?{QUERY}&key={APIKEY}";

    private URL url;

    public Geocoder()
    {
        requestURL = requestURL.replace("{APIKEY}", GOOGLEAPIKEY);
    }

    public String GetCityFromPostcode(String postcode)
    {
        requestURL = requestURL.replace("{QUERY}", "address=" + postcode.replace(" ", ""));
        UpdateURL();
        return GetCityFromResponse(MakeRequest());
    }

    public String GetCityFromLatLon(com.google.android.gms.maps.model.LatLng latLng)
    {
        requestURL = requestURL.replace("{QUERY}", "latlng=" + latLng.latitude + "," + latLng.longitude);
        UpdateURL();
        return GetCityFromResponse(MakeRequest());
    }

    private void UpdateURL()
    {
        try {
            url = new URL(requestURL);
        } catch (MalformedURLException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private String GetCityFromResponse(String response)
    {
        String city = "";
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray addressComponents = ((JSONArray)jsonObject.get("results")).getJSONObject(0).getJSONArray("address_components");
            addressComponentsLoop:
            for (int i = 0; i < addressComponents.length(); i++)
            {
                JSONObject component = (JSONObject) addressComponents.get(i);
                JSONArray addressTypes = ((JSONArray)component.getJSONArray("types"));
                for (int j = 0; j < addressTypes.length(); j++)
                {
                    String type = addressTypes.get(j).toString();
                    if (type.equals("postal_town"))
                    {
                        city = component.get("long_name").toString();
                        break addressComponentsLoop;
                    }
                }
            }
        } catch (Exception e) {
            String message = e.getMessage();
            e.printStackTrace();
            Log.e(TAG, message);
        }
        finally {
            return city;
        }
    }

    private String MakeRequest()
    {
        String response = "";
        try {
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("GET");
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK)
            {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = br.readLine()) != null)
                {
                    response+=line;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        finally {
            return response;
        }
    }
}
