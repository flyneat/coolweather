package com.coolweather.android.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.coolweather.android.R;
import com.coolweather.android.gson.DailyForecast;
import com.coolweather.android.gson.Weather;
import com.coolweather.android.service.AutoUpdateWeatherService;
import com.coolweather.android.ui.fragment.ChooseAreaFragment;
import com.coolweather.android.ui.widget.TimeoutProgressBar;
import com.coolweather.android.util.BitmapHelper;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import java.io.IOException;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {
    public final Context mContext = WeatherActivity.this;
    public final String TAG = "onWeatherActivity";

    private String mWeatherId;

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

    private static final String BingPicAddress = "http://guolin.tech/api/bing_pic";

    public DrawerLayout drawerLayout;
    public FrameLayout sideLayout;
    public NavigationView mNavView;

    private TimeoutProgressBar mProgressBar;

    private int picResId;
    private Bitmap weatherPic;

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

        //loadBgPic();

        loadWeatherInfo();

        weatherLayout.setVisibility(View.VISIBLE);

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            Log.w(TAG, "获得焦点");
            setTemperatureTextColor();
        }
    }


    /**
     * 设置显示温度文本颜色
     */
    private void setTemperatureTextColor() {
        new Thread(() -> {
            // 默认字体颜色为纯白色
            int color = 0xffffffff;

            int[] coordinate = new int[10];
            coordinate = getViewCoordinate(temperatureTv);

            if (weatherPic == null) {
                weatherPic = BitmapFactory.decodeResource(getResources(), picResId);
                View decorview = getWindow().getDecorView();
                int picWidth = decorview.getWidth();
                int picHeight = decorview.getHeight();
                Log.d(TAG, "图片缩放宽度:" + picWidth + " 图片缩放高度：" + picHeight);
                // 设定缩放图片的宽高和手机屏幕宽高尺寸一致
                weatherPic = BitmapHelper.resizePicture(weatherPic, picWidth, picHeight);
            }
            if (weatherPic != null) {
                // 分别获取view左上角、右上角、右下角、左下角、中心点的坐标对应的颜色值
                int ltColor = weatherPic.getPixel(coordinate[0], coordinate[1]);

                int rtColor = weatherPic.getPixel(coordinate[2], coordinate[3]);

                int rbColor = weatherPic.getPixel(coordinate[4], coordinate[5]);

                int lbColor = weatherPic.getPixel(coordinate[6], coordinate[7]);

                int centerColor = weatherPic.getPixel(coordinate[8], coordinate[9]);

                // 又5个点对应的颜色值，这里取均值权重 weight = 0.2f
                float weight = 0.15f;

                int blue = (int) ((ltColor & 0xff) * weight + (rtColor & 0xff) * weight
                        + (rbColor & 0xff) * weight + (lbColor & 0xff) * weight + (centerColor & 0xff) * 0.4f + 0.5f);

                int green = (int) (((ltColor >> 8) & 0xff) * weight + ((rtColor >> 8) & 0xff) * weight
                        + ((rbColor >> 8) & 0xff) * weight + ((lbColor >> 8) & 0xff) * weight
                        + ((centerColor >> 8) & 0xff) * 0.4f + 0.5f);

                int red = (int) (((ltColor >> 16) & 0xff) * weight + ((rtColor >> 16) & 0xff) * weight
                        + ((rbColor >> 16) & 0xff) * weight + ((lbColor >> 16) & 0xff) * weight
                        + ((centerColor >> 16) & 0xff) * 0.4f + 0.5f);

                int alpha = (int) ((ltColor >>> 24) * weight + (rtColor >>> 24) * weight
                        + (rbColor >>> 24) * weight + (lbColor >>> 24) * weight
                        + (centerColor >>> 24) * 0.4f + 0.5f);
                Log.d(TAG, "取色：alpha:" + alpha + " red:" + red + " green:" + green + " blue:" + blue);

                // 计算补色
                blue = 255 - blue;
                green = 255 - green;
                red = 255 - red;
                // alpha = 255 - alpha;
                Log.d(TAG, "补色：alpha:" + alpha + " red:" + red + " green:" + green + " blue:" + blue);
                // 合并颜色值
                color = (alpha << 24) | (red << 16) | (green << 8) | blue;
                Log.d(TAG, "合并的颜色值：" + Integer.toHexString(color));
            } else {
                Log.w(TAG, "bitmap为null,默认设置view文本颜色为纯白色");
            }
            // 设置显示温度文本字体颜色
            int finalColor = color;
            runOnUiThread(() -> {
                temperatureTv.setTextColor(finalColor);
            });
        }).start();
    }

    private int[] getViewCoordinate(View view) {
        int[] coordinate = new int[10];
        int[] topLeftCorner = new int[2];
        view.getLocationOnScreen(topLeftCorner);
        int width = view.getWidth();
        int height = view.getHeight();
        // 保存view的左上角坐标
        coordinate[0] = topLeftCorner[0];
        coordinate[1] = topLeftCorner[1];

        // 保存view的右上角的坐标
        coordinate[2] = coordinate[0] + width;
        coordinate[3] = coordinate[1];

        // 保存view的右下角坐标
        coordinate[4] = coordinate[2];
        coordinate[5] = coordinate[3] + height;

        // 保存view的左下角坐标
        coordinate[6] = coordinate[0];
        coordinate[7] = coordinate[5];

        // 保存view的中心点的坐标
        coordinate[8] = coordinate[0] + width / 2;
        coordinate[9] = coordinate[1] + height / 2;
        StringBuilder builder = new StringBuilder();
        builder.append("左上角坐标：(" + coordinate[0] + "," + coordinate[1] + ")\n");
        builder.append("右上角坐标：(" + coordinate[2] + "," + coordinate[3] + ")\n");
        builder.append("右下角坐标：(" + coordinate[4] + "," + coordinate[5] + ")\n");
        builder.append("左下角坐标：(" + coordinate[6] + "," + coordinate[7] + ")\n");
        builder.append("中心点坐标：(" + coordinate[8] + "," + coordinate[9] + ")");
        Log.i(TAG, builder.toString());
        return coordinate;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.d("onWeatherActivity", "执行dispatchTouchEvent方法的手指按下");
                Log.d(TAG, "点击点坐标：(" + ev.getX() + "," + ev.getY() + ")");
                //mProgressBar.resetProgressBar();
                mProgressBar.resetProgressBar(true);
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d("onWeatherActivity", "执行dispatchTouchEvent方法的手指滑动");
                break;
            default:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 加载天气信息
     */
    private void loadWeatherInfo() {
        String weatherInfo = sp.getString("heWeather", null);
        if (weatherInfo != null) {
            // sp里有缓存的天气信息数据
            Weather weather = Utility.handleWeatherResponse(weatherInfo);
            if (mWeatherId.equals(weather.basic.weatherId)) {
                //且请求的weatherId与sp里缓存的一致，则直接显示缓存的天气信息
                // 加载天气背景图片
                loadWeatherBgPic(weather.now.more.code);
                // 显示天气情况
                showWeatherInfo(weather);
            } else {
                // 请求的天气id和缓存里的不一致，则需重新请求网络来更新天气信息
                requestWeather(mWeatherId);
            }
        } else {
            // sp里没有缓存的天气信息数据，需请求网络来获取天气信息
            requestWeather(mWeatherId);
        }
    }

    /**
     * 根据天气情况加载对应天气背景图片
     *
     * @param code 天气代码<br/>
     *             天气代码对照 <br/>
     *             100       晴天 <br/>
     *             101~103   多云 <br/>
     *             104       阴天 <br/>
     *             300~399   雨天 <br/>
     *             400~499   雪天 <br/>
     *             其他   非上述天气情况 <br/>
     */
    private void loadWeatherBgPic(String code) {

        String picResName = getPicResName(code);
        if ("other".equals(picResName)) {
            // 非上述天气情况，则加载默认天气背景图片：Bing每日一图
            loadDefaultBgPic();
        } else {
            // 加载对应的天气背景图片
            picResId = getResources().getIdentifier(picResName, "drawable", getPackageName());
            Log.d("天气背景图片资源id", "" + picResId);
            if (picResId != 0) {
                Glide.with(mContext).load(picResId).into(bingPicImg);
            }
        }
    }

    private String getPicResName(String code) {
        // 初始默认设定为非上述天气情况
        String name = "other";
        if (code.startsWith("1")) {
            if ("100".equals(code)) {
                name = "sunny" + getPicRandomIndex(1, 2);
            } else if ("104".equals(code)) {
                // 加载阴天天气背景图片
                name = "overcast" + getPicRandomIndex(1, 1);
            } else {
                // 加载多云天气背景图片
                name = "cloudy" + getPicRandomIndex(1, 3);
            }
        } else if (code.startsWith("3")) {
            // 加载雨天天气背景图片
            name = "rain" + getPicRandomIndex(1, 2);
        } else if (code.startsWith("4")) {
            // 加载雪天天气背景图片
            name = "snow" + getPicRandomIndex(1, 2);
        }
        Log.d("天气背景图片名称", name);
        return name;
    }

    private int getPicRandomIndex(int startIndex, int endIndex) {
        return new Random().nextInt(endIndex - startIndex + 1) + startIndex;
    }

    /**
     * 加载缓存的背景图片
     */
    private void loadDefaultBgPic() {
        String bingPicLink = sp.getString("bing_pic", null);
        Log.d("onLoadBgPic", "bing每日背景图链接地址:" + bingPicLink);
        if (bingPicLink != null) {
            // 有图片链接缓存，就直接读取缓存并加载图片显示出来
            Log.d("onLoadBgPic", "有图片链接缓存");
            Glide.with(mContext).load(bingPicLink).into(bingPicImg);
        } else {
            requestBingPic(BingPicAddress);
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
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorPrimary));

        drawerLayout = findViewById(R.id.drawer_layout);
        mNavView = findViewById(R.id.nav_view);
        sideLayout = findViewById(R.id.side_layout);
        mProgressBar = findViewById(R.id.timeout_progress_bar);
        sp = PreferenceManager.getDefaultSharedPreferences(this);


        mWeatherId = getIntent().getStringExtra("weather_id");
        Log.d("onInitData", "天气id：" + mWeatherId);

    }

    private void initEvent() {
        addListenerEvent();
        Log.d("onInitEvent()方法", "开始启动超时等待任务");
        // 开始超时等待任务
        Log.d(TAG, "开始属性动画");
        mProgressBar.setTimeOut(10);
        // mProgressBar.startTimeOut();
        mProgressBar.customValueAnimator();
    }

    /**
     * 事件监听方法
     */
    private void addListenerEvent() {
        /* 添加标题栏展开侧滑栏按钮的点击事件 */
        Button switchDrawerBtn = findViewById(R.id.drawer_switch);
        switchDrawerBtn.setOnClickListener(view -> {
            // lambda表达式
            drawerLayout.openDrawer(Gravity.START, true);
        });

        /* 添加下拉刷新事件监听 */
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d("onRefresh", "天气id：" + mWeatherId);
                // 手动下拉刷新天气信息
                requestWeather(mWeatherId);
                // 数据加载完毕，下拉刷新结束
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        /* 添加点击系统后退按钮事件监听 */


        /* 设置滑动菜单的item点击事件响应 */
        mNavView.setCheckedItem(R.id.nav_location);
        mNavView.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                // 点击"选择地址"item
                case R.id.nav_location:
                    // 隐藏滑动菜单
                    mNavView.setVisibility(View.INVISIBLE);
                    if (sideLayout.getChildCount() < 2) {
                        // 添加区域选择界面（fragment view）
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        FragmentTransaction transaction = fragmentManager.beginTransaction();
                        transaction.add(R.id.side_layout, new ChooseAreaFragment(), "frag_view");
                        transaction.commit();
                    } else {
                        // 显示区域选择界面
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        FragmentTransaction transaction = fragmentManager.beginTransaction();
                        transaction.show(fragmentManager.findFragmentByTag("frag_view"));
                        transaction.commit();
                    }
                    return true;

                // 点击"设置"item
                case R.id.nav_settings:
                    Toast.makeText(WeatherActivity.this,
                            "点击设置菜单选项", Toast.LENGTH_SHORT)
                            .show();
                    return true;
                default:
                    return false;
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
                // 缓存bing每日一图链接地址
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("bing_pic", bingPicLink);
                editor.apply();
                // 加载Bing背景图片
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(mContext).load(bingPicLink).into(bingPicImg);
                    }
                });
            }
        });

    }

    /**
     * 根据天气 id 请求城市天气信息
     */
    public void requestWeather(String weatherId) {
        // requestBingPic(BingPicAddress);
        ProgressDialog progressDialog = Utility.showProgressDialog(this);
        String address = "http://guolin.tech/api/weather?cityid=" + weatherId
                + "&key=c72d9f8149ac436da648ac0e43211edd";
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.w("onWeatherActivity", "网络通讯异常");
                runOnUiThread(() -> {
                    Utility.closeProgressDialog(progressDialog);
                    Toast.makeText(mContext, "网络通讯异常，获取天气信息失败",
                            Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                if (responseText == null) {
                    throw new NullPointerException("请求天气信息的响应消息为空");
                }
                Log.d("onRequestWeather", "天气信息：" + responseText);
                // 当前WeatherActivity界面也要保存weatherId
                mWeatherId = weatherId;
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
                            // 加载天气背景图片
                            loadWeatherBgPic(weather.now.more.code);
                            showWeatherInfo(weather);
                        } else {
                            Log.w("onWeatherActivity", "天气信息json数据异常");
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
        // weather.basic.update.updateTime 格式：2018-10-29 19:46
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
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

        /*Palette palette = Palette.from(BitmapFactory.decodeResource(getResources(), picResId)).generate();
        // 获取输入图片中亮丽的颜色，获取失败则默认颜色为纯白色
        int bgColor1 = palette.getVibrantColor(0xffffffff);
        int bgColor2 = palette.getLightMutedColor(0xffffffff);
        int bgColor3 = palette.getLightVibrantColor(0xffffffff);*/

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
            // infoTv.setTextColor(bgColor1);

            // 显示当天最高气温
            tmpMaxTv.setText("最高气温\n" + dailyForecast.temperature.max + "℃");
            // tmpMaxTv.setTextColor(bgColor2);
            // 显示当天最低气温
            tmpMinTv.setText("最低气温\n" + dailyForecast.temperature.min + "℃");
            // tmpMaxTv.setTextColor(bgColor3);

            // 添加天气预报信息的 view 到天气预报界面容器内
            forecastLayout.addView(view);
        }
    }

}
