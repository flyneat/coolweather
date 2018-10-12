package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;
/**
 * 天气情况预测
 */
public class DailyForecast {
    /** 日期 */
    public String date;

    /** 温度情况 */
    @SerializedName("tmp")
    public Temperature temperature;

    /** 天气情况 */
    @SerializedName("cond")
    public Now.More more;

    public class More {
        /** 天气描述 */
        @SerializedName("txt")
        public String info;
    }

    public class Temperature {
        public String max;
        public String min;
    }
}
