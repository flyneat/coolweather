package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;
/**
 * 依据天气情况，给出的日常生活建议
 */
public class Suggestion {
    /** 天气舒适度描述 */
    @SerializedName("comf")
    public Comfort comfort;

    /** 洗车建议 */
    @SerializedName("cw")
    public CarWash carWash;

    /** 运动建议 */
    public Sport sport;

    public class Comfort {
        @SerializedName("txt")
        public String info;
    }

    public class CarWash {
        @SerializedName("txt")
        public String info;
    }

    public class Sport {
        @SerializedName("txt")
        public String info;
    }
}
