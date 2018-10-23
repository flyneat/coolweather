package com.coolweather.android.ui.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.coolweather.android.R;
import com.coolweather.android.db.City;
import com.coolweather.android.db.County;
import com.coolweather.android.db.Province;
import com.coolweather.android.ui.MainActivity;
import com.coolweather.android.ui.WeatherActivity;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import org.litepal.LitePal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 显示省、市、县区域信息
 */
public class ChooseAreaFragment extends Fragment {
    private Activity mActivity;

    private Button backBtn;

    private TextView titleTv;

    private ListView areaListView;

    private ArrayAdapter<String> adapter;

    /**
     * 省级列表数据model
     */
    private List<Province> provinceList;
    /**
     * 市级列表数据model
     */
    private List<City> cityList;
    /**
     * 县级列表数据model
     */
    private List<County> countyList;

    List<String> dataList;

    /**
     * 选中的省份
     */
    private Province selectedProvince;

    /**
     * 选中的城市
     */
    private City selectedCity;

    /**
     * 当前区域列表的层级
     */
    private int currentLevel;

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        initData(view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initEvent();
        queryProvinces();
    }

    private void initData(View view) {
        mActivity = getActivity();

        backBtn = view.findViewById(R.id.back_button);
        titleTv = view.findViewById(R.id.title_text);
        areaListView = view.findViewById(R.id.area_list_view);
        dataList = new ArrayList<>();
        adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, dataList);
        areaListView.setAdapter(adapter);
    }

    private void initEvent() {
        /* 区域列表item的点击事件处理 */
        areaListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                switch (currentLevel) {
                    case LEVEL_PROVINCE:
                        selectedProvince = provinceList.get(position);
                        queryCities();
                        break;
                    case LEVEL_CITY:
                        selectedCity = cityList.get(position);
                        queryCounties();
                        break;
                    case LEVEL_COUNTY:
                        Activity activity = getActivity();
                        String weatheId = countyList.get(position).getWeatherId();
                        if (activity instanceof MainActivity) {
                            /* 启动weather activity ，显示选定的区域的天气情况 */
                            Intent intent = new Intent(getActivity(), WeatherActivity.class);
                            intent.putExtra("weather_id", weatheId);
                            mActivity.startActivity(intent);
                            // mActivity.finish();
                        } else if (activity instanceof WeatherActivity) {
                            WeatherActivity weatherActivity = (WeatherActivity) activity;

                            // 移除选择地址的fragment view
                            weatherActivity.sideLayout.removeViewAt(1);
                            // 恢复显示滑动菜单
                            weatherActivity.mNavView.setVisibility(View.VISIBLE);
                            // 关闭侧边栏
                            weatherActivity.drawerLayout.closeDrawers();

                            // 显示新选择的地区的天气信息
                            weatherActivity.swipeRefreshLayout.setRefreshing(true);
                            weatherActivity.requestWeather(weatheId);
                            weatherActivity.swipeRefreshLayout.setRefreshing(false);
                        }
                        break;
                    default:
                        break;
                }
            }
        });

        /* 标题栏 返回按钮 的点击事件处理 */
        backBtn.setOnClickListener((view) -> {
            /*
             * TODO：每次返回到上级区域列表都需要重新查询数据库，影响性能，
             * TODO: 可用一数据结构保存各区域列表信息，减少查询数据库的次数
             */
            if (currentLevel == LEVEL_COUNTY) {
                // 返回到市级列表
                queryCities();
            } else if (currentLevel == LEVEL_CITY) {
                // 返回省级列表
                queryProvinces();
            } else if (currentLevel == LEVEL_PROVINCE) {
                WeatherActivity weatherActivity = (WeatherActivity) getActivity();
                // 隐藏区域选择fragment view 界面
                FragmentManager fragmentManager = weatherActivity.getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.hide(fragmentManager.findFragmentByTag("frag_view"));
                transaction.commit();
                // 返回到滑动菜单
                weatherActivity.mNavView.setVisibility(View.VISIBLE);
            }
        });

    }

    /**
     * 查询全国所有的省，优先从数据库中查询，如果没有查询到再去服务器上查询
     */
    private void queryProvinces() {
        titleTv.setText(R.string.province_title);
        if (getActivity() instanceof MainActivity) {
            backBtn.setVisibility(View.GONE);
        }
        provinceList = LitePal.findAll(Province.class);
        if (provinceList.size() > 0) {
            // 本地数据库有省份数据
            dataList.clear();
            for (Province province : provinceList) {
                dataList.add(province.getProvinceName());
            }
            // underlying data 发生改变，则通知刷新ListView列表信息
            adapter.notifyDataSetChanged();
            // 选中item初始化置为第一项
            areaListView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        } else {
            // 本地数据库无省份数据，则网络请求服务器获取省份数据

            String address = "http://guolin.tech/api/china";  // 远程服务器省份区域的接口
            queryFromServer(address, "province");

        }

    }

    /**
     * 查询选中的省内的所有市，优先从数据库中查询，如果没有查询到再去服务器上查询
     */
    private void queryCities() {
        titleTv.setText(selectedProvince.getProvinceName());
        backBtn.setVisibility(View.VISIBLE);
        int provinceCode = selectedProvince.getProvinceCode();
        cityList = LitePal.where("provinceCode = ?", "" + provinceCode)
                .find(City.class);
        if (cityList.size() > 0) {
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            areaListView.setSelection(0);
            currentLevel = LEVEL_CITY;
        } else {
            String address = "http://guolin.tech/api/china/" + provinceCode;
            queryFromServer(address, "city");
        }
    }

    /**
     * 查询选中的市内所有的县，优先从数据库中查询，如果没有查询到再去服务器上查询
     */
    private void queryCounties() {
        titleTv.setText(selectedCity.getCityName());
        int cityCode = selectedCity.getCityCode();
        countyList = LitePal.where("cityCode = ?", "" + cityCode)
                .find(County.class);
        if (countyList.size() > 0) {
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            areaListView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        } else {
            int provinceCode = selectedCity.getProvinceCode();
            String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
            queryFromServer(address, "county");
        }
    }

    /**
     * 根据传入的地址和类型从服务器上查询省市县的数据
     */
    private void queryFromServer(String address, final String type) {
        final ProgressDialog progressDialog = Utility.showProgressDialog(mActivity);
        // 发送网络请求
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("onChooseAreaFragment", "网络请求失败");
                // UI的变化，需要在UI线程上进行处理
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utility.closeProgressDialog(progressDialog);
                        Toast.makeText(getContext(), "网络请求失败", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("onChooseAreaFragment", "当前线程：" + Thread.currentThread().toString());
                String responseText = response.body().string();   // json格式的省份数据
                boolean result = false;
                switch (type) {
                    case "province":
                        result = Utility.handleProvinceResponse(responseText);
                        break;
                    case "city":
                        result = Utility.handleCityResponse(responseText, selectedProvince.getProvinceCode());
                        break;
                    case "county":
                        result = Utility.handleCountyResponse(responseText, selectedCity.getCityCode());
                        break;
                    default:
                        break;
                }
                if (!result) {
                    Log.d("onChooseAreaFragment", "解析json数据异常");
                    // UI的变化，需要在UI线程上进行处理
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utility.closeProgressDialog(progressDialog);
                            Toast.makeText(mActivity, "解析数据异常", Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    Log.d("onChooseAreaFragment", "解析json数据成功");
                    Log.d("onChooseAreaFragment", "mActivity信息:" + mActivity.toString());
                    mActivity.runOnUiThread(new Runnable() {
                        // 查询区域信息涉及到UI操作，需要从子线程切换到主线程上来处理
                        @Override
                        public void run() {
                            Utility.closeProgressDialog(progressDialog);
                            switch (type) {
                                case "province":
                                    queryProvinces();
                                    break;
                                case "city":
                                    queryCities();
                                    break;
                                case "county":
                                    queryCounties();
                                    break;
                                default:
                                    break;
                            }
                        }
                    });
                }
            }
        });
    }

}
