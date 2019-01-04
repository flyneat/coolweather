package com.coolweather.android.util.listener;

public interface TimeOutListener {
    /** 超时操作处理 */
    void timeOut();

    /** 未超时操作处理 */
    void unTimeOut();
}
