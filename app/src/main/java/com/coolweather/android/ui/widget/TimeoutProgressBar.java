package com.coolweather.android.ui.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.coolweather.android.R;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 自定义超时进度条，用于界面操作超时，返回到上一步Activity 或 Fragment
 **/
public class TimeoutProgressBar extends FrameLayout {
    /**
     * 超时时长（单位：秒）
     */
    private int mTimeOut;

    public boolean isTimeOuted() {
        return isTimeOuted;
    }

    private boolean isTimeOuted;

    /**
     * 屏幕水平方向尺寸（单位：pixel）
     */
    private int mWidthSize;

    /** 重置进度条位置状态的标志 */
    private volatile boolean isReset;

    private Context mContext;

    private ImageView mProgressBarIv;

    private View view;

    ValueAnimator mValueAnimator;

    public TimeoutProgressBar(@NonNull Context context, int timeOut) {
        super(context);
        Log.d("在超时进度条", "构造方法1被执行");
        init(context, timeOut);
    }

    public TimeoutProgressBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        Log.d("在超时进度条", "构造方法2被执行");
        init(context, 60);
    }

    public TimeoutProgressBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Log.d("在超时进度条", "构造方法3被执行");
        init(context, 60);
    }

    private void init(Context context, int timeOut) {
        mContext = context;
        mTimeOut = timeOut;
        Display display = ((Activity) mContext).getWindowManager().getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        // 屏幕水平方向尺寸（单位：pixel）
        mWidthSize = displayMetrics.widthPixels;
        isReset = false;
        initView();
    }

    private void initView() {
        view = LayoutInflater.from(mContext).inflate(R.layout.timeout_progress_bar, this, true);
        Log.d("View是", view.toString());
        mProgressBarIv = view.findViewById(R.id.progress_bar_image);
        Log.d("on超时进度条initView()方法", mProgressBarIv.toString());
    }

    /**
     * 开始超时等待任务
     */
    public void startTimeOut() {
        Log.d("mWidthSize值：", mWidthSize + "");
        /* 每秒宽度增长的步长 *//*
        int speed = mWidthSize / mTimeOut;
        Log.d("speed值：", "" + speed);*/
        defineValueAnimator();
        // 启动属性动画
        mValueAnimator.start();
    }

    /**
     * 属性动画,实现进度条随时间自增长
     */
    private void defineValueAnimator() {
        mValueAnimator = ValueAnimator.ofInt(1, mWidthSize);
        mValueAnimator.setDuration(mTimeOut * 1000);
        // 设置动画重复次数：设定值 + 1
        mValueAnimator.setRepeatCount(0);
        // 设置重复播放动画模式
        mValueAnimator.setRepeatMode(ValueAnimator.RESTART);

        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int width = (Integer) valueAnimator.getAnimatedValue();
                mProgressBarIv.getLayoutParams().width = mWidthSize - width;
                mProgressBarIv.requestLayout();
            }
        });

        mValueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                isTimeOuted = false;
                isReset = false;
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (isReset) {
                    Log.d("onTimeOutProgressBar","执行属性动画结束回调方法");
                    return;
                }
                Log.d("onTimeOutProgressBar","执行属性动画结束回调方法（动画结束）");
                isTimeOuted = true;
                setVisibility(INVISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                Log.d("onTimeOutProgressBar","执行属性动画取消回调方法");
                isReset = true;
                //mProgressBarIv.requestLayout();
                //mValueAnimator.start();
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }


    /**
     * 自定义属性动画,实现进度条随时间自增长
     */
    public void customValueAnimator() {
        /* 每秒宽度增长的步长 */
        int speed = mWidthSize / mTimeOut;
        Log.d("speed值：", "" + speed);
        Runnable updateProgressTask = new Runnable() {
            @Override
            public void run() {
                try {
                    int curWidth = mWidthSize;
                    isTimeOuted = false;
                    // 当curWidth == 0 时，表明进度条长度已达最大值，长度变化结束
                    while (curWidth > 0) {
                        if (isReset) {
                            isReset = false;
                            curWidth = mWidthSize;
                            mProgressBarIv.getLayoutParams().width = mWidthSize;
                            ((Activity) mContext).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // 指定控件的属性值发送改变后，需要请求重绘布局
                                    mProgressBarIv.requestLayout();
                                }
                            });
                            continue;
                        }
                        // 每秒curWidth减小speed,即进度条每秒增长speed大小的像素
                        Thread.sleep(1000);
                        curWidth -= speed;
                        if (curWidth < 0) {
                            curWidth = 0;
                        }
                        mProgressBarIv.getLayoutParams().width = curWidth;
                        ((Activity) mContext).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // 指定控件的属性值发送改变后，需要请求重绘布局
                                mProgressBarIv.requestLayout();
                                if (mProgressBarIv.getLayoutParams().width == 0) {
                                    isTimeOuted = true;
                                    setVisibility(INVISIBLE);
                                }
                            }
                        });
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        // 使用线程池来管理线程的生命周期和系统资源的占用
        ExecutorService singleThreadPool = new ThreadPoolExecutor(1,1,0,
                TimeUnit.MILLISECONDS,new LinkedBlockingQueue<Runnable>());
        singleThreadPool.execute(updateProgressTask);
        singleThreadPool.shutdown();
      // new Thread(updateProgressTask).start();
    }

    public int getTimeOut() {
        return mTimeOut;
    }

    public void setTimeOut(int mTimeOut) {
        this.mTimeOut = mTimeOut;
    }

    /** 重置属性动画状态 */
    public void resetProgressBar() {
        if (mValueAnimator.isRunning()) {
            Log.d("onTimeOutProgressBar","执行重置动画");
            mValueAnimator.cancel();
            mValueAnimator.start();
        }

    }

    /**
     * 重置进度条状态
     * @param isReset true:重置 false:不重置
     **/
     public void resetProgressBar(Boolean isReset) {
        this.isReset = isReset;
    }


}