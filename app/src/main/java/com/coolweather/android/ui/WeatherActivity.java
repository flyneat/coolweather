package com.coolweather.android.ui;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.coolweather.android.R;
import com.coolweather.android.gson.DailyForecast;
import com.coolweather.android.gson.Weather;
import com.coolweather.android.service.AutoUpdateWeatherService;
import com.coolweather.android.ui.fragment.ChooseAreaFragment;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {
    public final Context context = WeatherActivity.this;

    private String weatherId;

    private LinearLayout weatherLayout;

    TextView areaTitleTv;

    TextView nowTimeTv;

    TextView temperatureTv;

    TextView weatherInfoTv;

    TextView aqiTv;

    TextView pm25Tv;

    TextView comfortTv;

    TextView carWashTv;

    TextView sportTv;

    LinearLayout forecastLayout;

    public SwipeRefreshLayout swipeRefreshLayout;

    private SharedPreferences sp;

    private ImageView bingPicImg;

    private static final String bingPicAddress = "http://guolin.tech/api/bing_pic";

    public DrawerLayout drawerLayout;
    public FrameLayout sideLayout;
    public NavigationView mNavView;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            // 将背景图和状态栏融合到一起
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);

        initData();

        initEvent();

        // 加载天气信息前，先隐藏天气界面布局，优化视觉效果
        weatherLayout.setVisibility(View.INVISIBLE);

        loadBgPic();

        loadWeatherInfo();


    }

    /**
     * 加载天气信息
     */
    private void loadWeatherInfo() {
        String weatherInfo = sp.getString("heWeather", null);
        Weather weather = Utility.handleWeatherResponse(weatherInfo);
        if (weatherInfo != null && weatherId.equals(weather.basic.weatherId)) {
            // sp里有缓存的天气信息数据，且请求的weatherId与sp里缓存的一致，则直接显示缓存的天气信息，
            showWeatherInfo(weather);
        } else {
            // 否则，需请求网络，更新天气信息
            requestWeather(weatherId);
        }
    }

    /**
     * 加载缓存的背景图片
     */
    private void loadBgPic() {
        String bingPicLink = sp.getString("bing_pic", null);
        Log.d("onLoadBgPic", "bing每日背景图链接地址:" + bingPicLink);
        if (bingPicLink != null) {
            // 有图片链接缓存，就直接读取缓存并加载图片显示出来
            Log.d("onLoadBgPic", "有图片链接缓存");
            Glide.with(context).load(bingPicLink).into(bingPicImg);
        }
    }

    private void initData() {
        weatherLayout = findViewById(R.id.weather_layout);
        areaTitleTv = findViewById(R.id.title_area);
        nowTimeTv = findViewById(R.id.now_time);
        temperatureTv = findViewById(R.id.degree_text);
        weatherInfoTv = findViewById(R.id.weather_info_text);
        aqiTv = findViewById(R.id.aqi);
        pm25Tv = findViewById(R.id.pm25);
        forecastLayout = findViewById(R.id.forecast_layout);
        comfortTv = findViewById(R.id.comfort_text);
        carWashTv = findViewById(R.id.car_wash_text);
        sportTv = findViewById(R.id.sport_text);

        bingPicImg = findViewById(R.id.bing_pic_img);

        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this,R.color.colorPrimary));

        drawerLayout = findViewById(R.id.drawer_layout);
        mNavView = findViewById(R.id.nav_view);
        sideLayout = findViewById(R.id.side_layout);

        sp = PreferenceManager.getDefaultSharedPreferences(this);


        weatherId = getIntent().getStringExtra("weather_id");
        Log.d("onInitData", "天气id：" + weatherId);

    }

    private void initEvent() {
        addListenerEvent();
    }

    /* 事件监听方法 */
    private void addListenerEvent() {
        /* 添加下拉刷新事件监听 */
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d("onRefresh", "天气id：" + weatherId);
                // 手动下拉刷新天气信息
                requestWeather(weatherId);
                // 数据加载完毕，下拉刷新结束
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        /* 设置滑动菜单的item点击事件响应 */
        mNavView.setCheckedItem(R.id.nav_location);
        mNavView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    // 点击"选择地址"item
                    case R.id.nav_location:
                        // 显示区域选择界面（fragment view）
                        if (sideLayout.getChildCount() < 2) {
                            // 隐藏滑动菜单
                            mNavView.setVisibility(View.INVISIBLE);
                            FragmentManager fragmentManager = getSupportFragmentManager();
                            FragmentTransaction transaction = fragmentManager.beginTransaction();
                            transaction.add(R.id.side_layout, new ChooseAreaFragment(), "frag_view");
                            transaction.commit();
                        } else {
                            // 隐藏滑动菜单
                            mNavView.setVisibility(View.INVISIBLE);
                            FragmentManager fragmentManager = getSupportFragmentManager();
                            FragmentTransaction transaction = fragmentManager.beginTransaction();
                            transaction.show(fragmentManager.findFragmentByTag("frag_view"));
                            transaction.commit();
                        }
                        return true;

                    // 点击"设置"item
                    case R.id.nav_settings:
                        Toast.makeText(WeatherActivity.this,
                                "点击设置菜单选项",Toast.LENGTH_SHORT)
                                .show();
                        return true;
                    default:
                        return false;
                }

            }
        });

    }

    /**
     * 请求网络获取背景图片链接地址
     **/
    private void requestBingPic(String address) {
        // 通过网络请求图片链接地址
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.w("onLoadBingPic", "请求bing每日背景图片失败:");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String bingPicLink = response.body().string();
                Log.d("onLoadBingPic", "请求的bing每日背景图链接地址:" + bingPicLink);
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("bing_pic", bingPicLink);
                editor.apply();
                // 加载Bing背景图片
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(context).load(bingPicLink).into(bingPicImg);
                    }
                });
            }
        });

    }

    /**
     * 根据天气 id 请求城市天气信息
     */
    public void requestWeather(String weatherId) {
        requestBingPic(bingPicAddress);
        ProgressDialog progressDialog = Utility.showProgressDialog(this);
        String address = "http://guolin.tech/api/weather?cityid=" + weatherId
                + "&key=c72d9f8149ac436da648ac0e43211edd";
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("onWeatherActivity", "网络通讯异常");
                runOnUiThread(() -> {
                    Utility.closeProgressDialog(progressDialog);
                    Toast.makeText(WeatherActivity.this, "网络通讯异常，获取天气信息失败",
                            Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                Log.d("onRequestWeather", "天气信息：" + responseText);
                // 使用SharedPreferences来缓存天气信息数据
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("heWeather", responseText);
                editor.putString("weather_id", weatherId);
                editor.apply();

                Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utility.closeProgressDialog(progressDialog);
                        if (weather != null && "ok".equals(weather.status)) {
                            showWeatherInfo(weather);
                        } else {
                            Log.d("onWeatherActivity", "天气信息json数据异常");
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

    }

    /**
     * 处理并展示Weather实体类中的数据
     */
    private void showWeatherInfo(Weather weather) {

        Log.d("onWeatherActivity", "天气实体类：\n" + weather.toString());
        // 显示区域标题
        areaTitleTv.setText(weather.basic.cityName);

        // 显示当前天气更新的时间(本地时间)
        String updateTime = weather.basic.update.updateTime;
        nowTimeTv.setText("更新时间\n" + updateTime);

        // 显示当前天气的气温
        temperatureTv.setText(weather.now.temperature + "℃");

        // 显示当前天气情况
        weatherInfoTv.setText(weather.now.more.info);

        // 显示未来几天天气情况
        showForecastInfo(weather);

        if (weather.aqi != null) {
            /* 显示空气质量情况 */
            aqiTv.setText(weather.aqi.city.aqi);    // 空气质量指数
            pm25Tv.setText(weather.aqi.city.pm25);  // 显示PM2.5指数
        }

        /* 显示生活建议 */
        comfortTv.setText("【舒适度】：" + weather.suggestion.comfort.info);    // 天气舒适度
        carWashTv.setText("【洗车建议】：" + weather.suggestion.carWash.info);  // 洗车建议
        sportTv.setText("【运动建议】：" + weather.suggestion.sport.info);        // 运动建议

        // 加载完背景图片后，再显示天气界面布局
        weatherLayout.setVisibility(View.VISIBLE);

        Intent intent = new Intent(this, AutoUpdateWeatherService.class);
        startService(intent);
    }

    /**
     * 显示天气预报信息
     **/
    private void showForecastInfo(Weather weather) {
        forecastLayout.removeAllViews();
        for (DailyForecast dailyForecast : weather.dailyForecastList) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dateTv = view.findViewById(R.id.date_text);
            TextView infoTv = view.findViewById(R.id.info_text);
            TextView tmpMaxTv = view.findViewById(R.id.tmp_max_text);
            TextView tmpMinTv = view.findViewById(R.id.tmp_min_text);

            // 显示天气预报的预报日期
            dateTv.setText(dailyForecast.date);
            // 显示预报天气情况
            String info = dailyForecast.more.info;
            infoTv.setText(info);
            // 显示当天最高气温
            tmpMaxTv.setText("最高气温\n" + dailyForecast.temperature.max + "℃");
            // 显示当天最低气温
            tmpMinTv.setText("最低气温\n" + dailyForecast.temperature.min + "℃");
            // 添加天气预报信息的 view 到天气预报界面容器内
            forecastLayout.addView(view);
        }
    }


}
