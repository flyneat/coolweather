package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

/**
 * 空气质量指数
 */
public class AQI {
    public City city;

    public class City {
        public String aqi;

        public String pm25;
    }

}
