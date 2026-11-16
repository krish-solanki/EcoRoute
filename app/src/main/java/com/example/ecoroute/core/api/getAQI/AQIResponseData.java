package com.example.ecoroute.core.api.getAQI;

import com.google.gson.annotations.SerializedName;

public class AQIResponseData {

    @SerializedName("current")
    private CurrentData current;

    public CurrentData getCurrent() { return current; }

    public static class CurrentData {

        private String time;

        @SerializedName("european_aqi") private Double europeanAqi;
        @SerializedName("us_aqi") private Double usAqi;
        @SerializedName("pm2_5") private Double pm25;
        @SerializedName("pm10") private Double pm10;
        @SerializedName("carbon_monoxide") private Double carbonMonoxide;
        @SerializedName("nitrogen_dioxide") private Double nitrogenDioxide;
        @SerializedName("sulphur_dioxide") private Double sulphurDioxide;
        @SerializedName("ozone") private Double ozone;

        public String getTime() { return time; }
        public Double getEuropeanAqi() { return europeanAqi; }
        public Double getUsAqi() { return usAqi; }
        public Double getPm25() { return pm25; }
        public Double getPm10() { return pm10; }
        public Double getCarbonMonoxide() { return carbonMonoxide; }
        public Double getNitrogenDioxide() { return nitrogenDioxide; }
        public Double getSulphurDioxide() { return sulphurDioxide; }
        public Double getOzone() { return ozone; }
    }
}
