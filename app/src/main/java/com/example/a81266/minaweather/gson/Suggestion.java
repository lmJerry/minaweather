package com.example.a81266.minaweather.gson;

import com.google.gson.annotations.SerializedName;

public class Suggestion {
    @SerializedName("comf")
    public Comfort comfort;
    @SerializedName("cw")
    public CarWash carWash;
    public Sport sport;
    public class Comfort{
        @SerializedName("txt")
        public String info;
        public String brf;
    }
    public class CarWash{
        @SerializedName("txt")
        public String info;
        public String brf;
    }
    public class Sport{
        @SerializedName("txt")
        public String info;
        public String brf;
    }
}
