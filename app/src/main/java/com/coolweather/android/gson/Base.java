package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;
/**
 * 城市基本信息
 */
public class Base {
    /** 城市名 */
    @SerializedName("city")
    public String cityName;

    /** 天气ID */
    @SerializedName("id")
    public String weatherId;

    public class Update {
        /** 天气的更新时间（当地时间） */
        @SerializedName("loc")
        public String updateTime;
    }
}
