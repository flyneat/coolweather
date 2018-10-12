package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;
/**
 * 当天的综合天气情况
 */
public class Now {
    /** 温度 */
    @SerializedName("tmp")
    public String temperature;

    /** 天气情况 */
    @SerializedName("cond")
    public More more;

    public class More {
        /** 天气描述 */
        @SerializedName("txt")
        public String info;
    }

}
