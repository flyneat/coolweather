package com.coolweather.android.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.AlarmManagerCompat;
import android.util.Log;

import com.coolweather.android.gson.Weather;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateWeatherService extends Service {
    private static final long orignalTime = SystemClock.elapsedRealtime();
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        long currentTime = SystemClock.elapsedRealtime();
        Log.d("onAutoUpdateService", "开始进行一次定时任务,时间间隔："+((currentTime-orignalTime)/1000.0)+"s");
        updateWeather();
        updateBingPic();
        setAlarmTask();
        return super.onStartCommand(intent, flags, startId);
    }

    /** 设置定时任务 */
    private void setAlarmTask() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        // 定时任务触发事件间隔，暂设定时间间隔3小时
        int intervalTime = 3 * 3600 * 1000;
        long triggerAtTime = SystemClock.elapsedRealtime() + intervalTime;
        Intent intent = new Intent(this, AutoUpdateWeatherService.class);
        PendingIntent pi = PendingIntent.getService(this,0,intent,0);
        alarmManager.cancel(pi);
        // 设定定时任务
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi);
    }

    private void updateBingPic() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherId = sp.getString("bing_pic", null);
        if (weatherId != null) {
            String bingPicUrl = "http://guolin.tech/api/bing_pic";
            HttpUtil.sendOkHttpRequest(bingPicUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("onUpdateBingPic", "网络连接异常");
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String bingPicLink = response.body().string();
                    // 缓存Bing每日一图片的下载链接
                    sp.edit().putString("bing_pic",bingPicLink).apply();
                }
            });
        }
    }

    private void updateWeather() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherId = sp.getString("weather_id", null);
        if (weatherId != null) {
            String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId
                    + "&key=c72d9f8149ac436da648ac0e43211edd";
            HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("onUpdateWeather", "网络连接异常");
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String weatherInfo = response.body().string();
                    Weather weather = Utility.handleWeatherResponse(weatherInfo);
                    if (weather != null & "ok".equals(weather.status)) {
                        // 缓存天气信息
                        sp.edit().putString("heWeather", weatherInfo).apply();
                    }
                }
            });
        }
    }
}
