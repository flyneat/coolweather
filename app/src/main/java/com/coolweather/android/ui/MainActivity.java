package com.coolweather.android.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.coolweather.android.R;
import com.coolweather.android.gson.Weather;
import com.coolweather.android.util.Utility;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        // 有缓存的天气信息，则直接跳转到天气显示界面
        String weatherInfo = sp.getString("heWeather", null);
        if ( weatherInfo != null) {
            Weather weather = Utility.handleWeatherResponse(weatherInfo);
            if (weather != null) {
                Intent intent = new Intent(this, WeatherActivity.class);
                intent.putExtra("weather_id", weather.basic.weatherId);
                startActivity(intent);
            }
        }
    }
}
