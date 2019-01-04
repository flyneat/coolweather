package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.concurrent.BlockingDeque;

/**
 * 天气情况实体类
 **/
public class Weather {
    /**
     * 和风天气接口返回数据的状态码：
     * ok	数据正常
     * invalid key	错误的key，请检查你的key是否输入以及是否输入有误
     * unknown location	未知或错误城市/地区
     * no data for this location	该城市/地区没有你所请求的数据
     * no more requests	超过访问次数，需要等到当月最后一天24点（免费用户为当天24点）后进行访问次数的重置或升级你的访问量
     * param invalid	参数错误，请检查你传递的参数是否正确
     * too fast	超过限定的QPM，请参考QPM说明
     * dead	无响应或超时，接口服务异常请联系我们
     * permission denied	无访问权限，你没有购买你所访问的这部分服务
     * sign error	签名错误，请参考签名算法
     **/
    public String status;

    public Basic basic;

    public AQI aqi;

    public Now now;

    public Suggestion suggestion;

    /** 未来几天天气情况预测 */
    @SerializedName("daily_forecast")
    public List<DailyForecast> dailyForecastList;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("heWeather:[{\n");
        builder.append("status:" +status+"\n");
        builder.append("basic:{\n   cityName:"+basic.cityName+"\n   weather_id:"+basic.weatherId+
                "\n   updateTime:"+basic.update.updateTime+"\n}\n");
        builder.append("aqi:{\n"+"  aqi:"+aqi.city.aqi+"\n  pm2.5:"+aqi.city.pm25+"\n}\n");
        builder.append("now:{\n    温度："+now.temperature+"℃"+"\n"
                +"    天气："+ now.more.info + "\n"
                +"    天气代码："+ now.more.code +"\n}\n");

        builder.append("daily_forecast:[\n");
        for (DailyForecast dailyForecast : dailyForecastList) {
            builder.append("    {\n");
            builder.append("    date:" + dailyForecast.date+"\n");
            builder.append("    天气：" + dailyForecast.more.info + "\n");
            builder.append("    最高温度：" + dailyForecast.temperature.max + "℃\n");
            builder.append("    最低温度：" + dailyForecast.temperature.min + "℃\n");
            builder.append("    }\n");
        }
        builder.append("}\n");

        builder.append("suggestion:{\n");
        builder.append("    舒适度："+suggestion.comfort.info+"\n");
        builder.append("    运动建议："+suggestion.sport.info+"\n");
        builder.append("    洗车建议："+suggestion.carWash.info+"\n");
        builder.append("}\n");

        builder.append("}]");

        return builder.toString();
    }
}
