package com.example.ecoroute.core.api.getAQI;

import com.example.ecoroute.core.constants.ApiBaseUrl;

public class AQIApi {
    public static String getAQIUrl(AQIRequestData data) {
        return ApiBaseUrl.AQI_GET_BASE_URL
                + "?latitude=" + data.getLatitude()
                + "&longitude=" + data.getLongitude()
                + "&current=european_aqi,us_aqi,pm2_5,pm10,carbon_monoxide,nitrogen_dioxide,sulphur_dioxide,ozone"
                + "&timezone=Asia/Kolkata";
    }
}