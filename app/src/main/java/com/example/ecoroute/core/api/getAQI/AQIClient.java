package com.example.ecoroute.core.api.getAQI;

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.ecoroute.core.constants.ApiBaseUrl;
import com.google.gson.Gson;

public class AQIClient {

    private static RequestQueue requestQueue;
    private static final Gson gson = new Gson();

    public interface AQICallback {
        void onSuccess(AQIResponseData response);
        void onError(String error);
    }

    public static void getAQI(Context context, double latitude, double longitude, AQICallback callback) {

        if (requestQueue == null) requestQueue = Volley.newRequestQueue(context.getApplicationContext());

        String url = ApiBaseUrl.AQI_GET_BASE_URL + "?latitude=" + latitude + "&longitude=" + longitude + "&current=european_aqi,us_aqi,pm2_5,pm10,carbon_monoxide,nitrogen_dioxide,sulphur_dioxide,ozone&timezone=Asia/Kolkata";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    AQIResponseData data = gson.fromJson(response.toString(), AQIResponseData.class);
                    if (data != null && data.getCurrent() != null) callback.onSuccess(data);
                    else callback.onError("AQI data unavailable");
                },
                error -> callback.onError(error.toString()));

        requestQueue.add(request);
    }
}
