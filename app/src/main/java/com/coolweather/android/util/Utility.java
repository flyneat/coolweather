package com.coolweather.android.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.coolweather.android.db.City;
import com.coolweather.android.db.County;
import com.coolweather.android.db.Province;
import com.coolweather.android.gson.Weather;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Utility {
    /**
     * 解析服务器返回的省级数据(JSON格式)，并保持到数据库
     */
    public static boolean handleProvinceResponse(String response) {
        if (!TextUtils.isEmpty(response)) {
            Log.d(Utility.class.getSimpleName(), "返回的省级信息：" + response);
            try {
                /* 解析JSON格式数据 */
                JSONArray allProvinces = new JSONArray(response);
                JSONObject provinceObject;
                Province province;
                for (int i = 0; i < allProvinces.length(); i++) {
                    provinceObject = allProvinces.getJSONObject(i);
                    province = new Province();
                    province.setProvinceName(provinceObject.getString("name"));
                    province.setProvinceCode(provinceObject.getInt("id"));
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析服务器返回的市级数据(JSON格式)
     */
    public static boolean handleCityResponse(String response, int provinceCode) {
        if (!TextUtils.isEmpty(response)) {
            try {
                Log.d(Utility.class.getSimpleName(), "返回的市级信息：" + response);
                /* 解析JSON格式数据 */
                JSONArray allCities = new JSONArray(response);
                JSONObject cityObject;
                City city;
                for (int i = 0; i < allCities.length(); i++) {
                    cityObject = allCities.getJSONObject(i);
                    city = new City();
                    city.setCityCode(cityObject.getInt("id"));
                    city.setCityName(cityObject.getString("name"));
                    city.setProvinceCode(provinceCode);
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析服务器返回的县级数据(JSON格式)
     */
    public static boolean handleCountyResponse(String response, int cityCode) {
        if (!TextUtils.isEmpty(response)) {
            try {
                Log.d(Utility.class.getSimpleName(), "返回的县级信息：" + response);
                JSONObject countyObject;
                County county;
                /* 解析JSON格式数据 */
                JSONArray allCounties = new JSONArray(response);
                for (int i = 0; i < allCounties.length(); i++) {
                    countyObject = allCounties.getJSONObject(i);
                    county = new County();
                    county.setCountyCode(countyObject.getInt("id"));
                    county.setCountyName(countyObject.getString("name"));
                    county.setWeatherId(countyObject.getString("weather_id"));
                    county.setCityCode(cityCode);
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析服务器返回的天气信息数据(JSON格式)
     */
    public static Weather handleWeatherResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather");
            String weatherContent = jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent, Weather.class);
        } catch (JSONException e) {
            Log.e("onUtility", "解析weather json 失败");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 关闭进度对话框
     */
    public static void closeProgressDialog(ProgressDialog dialog) {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    /**
     * 网络请求数据时显示进度对话框
     */
    public static ProgressDialog showProgressDialog(Context context) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("网络请求");
        progressDialog.setMessage("正在加载数据...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        return progressDialog;
    }

}
