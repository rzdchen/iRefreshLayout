package com.net.cc.irefresh;

import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * @Description 手势监听器
 * 为了不让RefreshLayout过于冗余，定义监听器子类
 * @Author CC
 * @Date 2021/9/16 上午12:36
 */
public class IGestureDetectorListener implements GestureDetector.OnGestureListener {
    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }
}
