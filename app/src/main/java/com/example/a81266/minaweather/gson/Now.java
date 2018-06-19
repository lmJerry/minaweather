package com.example.a81266.minaweather.gson;

import com.google.gson.annotations.SerializedName;

public class Now {
    @SerializedName("tmp")
    public String temperature;
    @SerializedName("wind_dir")
    public String windDirection;
    @SerializedName("wind_sc")
    public String windScale;
    @SerializedName("fl")
    public String feel;
    @SerializedName("pres")
    public String pressure;
    @SerializedName("hum")
    public String humidity;
    @SerializedName("cond")
    public More more;
    public class More{
        @SerializedName("txt")
        public String info;
    }
}
