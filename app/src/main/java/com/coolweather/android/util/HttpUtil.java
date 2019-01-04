package com.coolweather.android.util;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class HttpUtil {
    /** 连接超时时长 */
    public final static int CONNECT_TIMEOUT =5;
    /** 读超时时长 */
    public final static int READ_TIMEOUT=10;
    /** 写超时时长 */
    public final static int WRITE_TIMEOUT=6;

    /** 发送一条网络请求，并返回响应数据 */
    public static void sendOkHttpRequest(String address, okhttp3.Callback callback) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT,TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT,TimeUnit.SECONDS)
                .build();
        Request request = new Request.Builder()
                .url(address)
                .build();
        client.newCall(request).enqueue(callback);
    }
}
