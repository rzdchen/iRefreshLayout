package com.net.cc.irefresh;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.Scroller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * @Description 刷新布局
 * @Author CC
 * @Date 2021/9/15 下午11:22
 */
public class IRefreshLayout extends FrameLayout implements IRefresh {

    private IOverView.IRefreshState refreshState;
    private GestureDetector gestureDetector;
    private IRefreshListener iRefreshListener;
    protected IOverView iOverView;
    private int lastY;
    //刷新时是否禁止滚动
    private boolean disableRefreshScroll;

    private AutoScroller autoScroller;

    public IRefreshLayout(@NonNull Context context) {
        this(context,null);
    }

    public IRefreshLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public IRefreshLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        gestureDetector = new GestureDetector(getContext(), iGestureDetectorListener);
        autoScroller = new AutoScroller(getContext());
    }

    @Override
    public void setDisableRefreshScroll(boolean disableRefreshScroll) {
        this.disableRefreshScroll = disableRefreshScroll;
    }

    @Override
    public void refreshFinished() {
        final View head = getChildAt(0);
//        HiLog.i(this.getClass().getSimpleName(), "refreshFinished head-bottom:" + head.getBottom());
        iOverView.onFinish();
        iOverView.setRefreshState(IOverView.IRefreshState.STATE_INIT);
        final int bottom = head.getBottom();
        if (bottom > 0) {
            //下over pull 200，height 100
            //  bottom  =100 ,height 100
            recover(bottom);
        }
        refreshState = IOverView.IRefreshState.STATE_INIT;

    }

    @Override
    public void setRefreshListener(IRefreshListener refreshListener) {
        this.iRefreshListener = refreshListener;
    }

    /**
     * 设置下拉刷新的视图
     */
    @Override
    public void setRefreshOverView(IOverView iOverView) {
        if (this.iOverView != null) {
            removeView(iOverView);
        }
        this.iOverView = iOverView;
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView(iOverView, 0, params);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        //事件分发处理
        if (!autoScroller.isFinished()) {
            return false;
        }
        View head = getChildAt(0);
        int action = ev.getAction();
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_POINTER_INDEX_MASK) {
            //松开手
            if (head.getBottom() > 0) {
                if (refreshState != IOverView.IRefreshState.STATE_REFRESH) {
                    recover(head.getBottom());
                    return false;
                }
            }
            lastY = 0;
        }
        boolean consumed = gestureDetector.onTouchEvent(ev);
        if ((consumed || (refreshState != IOverView.IRefreshState.STATE_INIT && refreshState != IOverView.IRefreshState.STATE_REFRESH)) && head.getBottom() != 0) {
            ev.setAction(MotionEvent.ACTION_CANCEL);//让父类接受不到真实到事件
            return super.dispatchTouchEvent(ev);
        }
        if (consumed) {
            return false;
        } else {
            return super.dispatchTouchEvent(ev);
        }
    }

    /**
     * 恢复成原样
     *
     * @param bottom 拉下的距离
     */
    private void recover(int bottom) {
        if (iRefreshListener != null && bottom > iOverView.pullRefreshHeight) {
            //滚动到指定位置
            autoScroller.recover(bottom - iOverView.pullRefreshHeight);
            refreshState = IOverView.IRefreshState.STATE_OVER_RELEASE;
        } else {
            autoScroller.recover(bottom);
        }
    }

    /**
     * OnGestureListener实例
     */
    IGestureDetectorListener iGestureDetectorListener = new IGestureDetectorListener() {
        @Override
        public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float disX, float disY) {
            if (Math.abs(disX) > Math.abs(disY) || iRefreshListener != null && !iRefreshListener.enableRefresh()) {
                //横向滑动，或刷新被禁止则不处理
                return false;
            }
            if (disableRefreshScroll && refreshState == IOverView.IRefreshState.STATE_REFRESH) {//刷新时是否禁止滑动
                return true;
            }

            View head = getChildAt(0);
            View child = IScrollUtil.findScrollableChild(IRefreshLayout.this);
            if (IScrollUtil.childScrolled(child)) {
                //如果列表发生了滚动则不处理
                return false;
            }

            //没有刷新或没有达到可以刷新的距离，且头部已经划出或下拉
            if ((refreshState != IOverView.IRefreshState.STATE_REFRESH || head.getBottom() <= iOverView.pullRefreshHeight) && (head.getBottom() > 0 || disY <= 0.0F)) {
                //还在滑动中
                if (refreshState != IOverView.IRefreshState.STATE_OVER_RELEASE) {
                    int speed;
                    //阻尼计算
                    if (child.getTop() < iOverView.pullRefreshHeight) {
                        speed = (int) (lastY / iOverView.minDamp);
                    } else {
                        speed = (int) (lastY / iOverView.maxDamp);
                    }
                    //如果是正在刷新状态，则不允许在滑动的时候改变状态
                    boolean bool = moveDown(speed, true);
                    lastY = (int) (-disY);
                    return bool;
                } else {
                    return false;
                }
            } else {
                return false;
            }

        }
    };
    //刷新
    private void refresh(){
        if (iRefreshListener != null) {
            refreshState = IOverView.IRefreshState.STATE_REFRESH;
            iOverView.onRefresh();
            iOverView.setRefreshState(IOverView.IRefreshState.STATE_REFRESH);
            iRefreshListener.onRefresh();
        }

    }

    /**
     * 根据偏移量移动header与child
     *
     * @param offsetY 偏移量
     * @param nonAuto 是否非自动滚动触发
     * @return
     */
    private boolean moveDown(int offsetY, boolean nonAuto) {
//        HiLog.i("111", "changeState:" + nonAuto);
        View head = getChildAt(0);
        View child = getChildAt(1);
        int childTop = child.getTop() + offsetY;

//        HiLog.i("-----", "moveDown head-bottom:" + head.getBottom() + ",child.getTop():" + child.getTop() + ",offsetY:" + offsetY);
        if (childTop <= 0) {//异常情况的补充
//            HiLog.i(TAG, "childTop<=0,mState" + mState);
            offsetY = -child.getTop();
            //移动head与child的位置，到原始位置
            head.offsetTopAndBottom(offsetY);
            child.offsetTopAndBottom(offsetY);
            if (refreshState != IOverView.IRefreshState.STATE_REFRESH) {
                refreshState = IOverView.IRefreshState.STATE_INIT;
            }
        } else if (refreshState == IOverView.IRefreshState.STATE_REFRESH && childTop > iOverView.pullRefreshHeight) {
            //如果正在下拉刷新中，禁止继续下拉
            return false;
        } else if (childTop <= iOverView.pullRefreshHeight) {//还没超出设定的刷新距离
            if (iOverView.getRefreshState() != IOverView.IRefreshState.STATE_VISIBLE && nonAuto) {//头部开始显示
                iOverView.onVisible();
                iOverView.setRefreshState(IOverView.IRefreshState.STATE_VISIBLE);
                refreshState = IOverView.IRefreshState.STATE_VISIBLE;
            }
            head.offsetTopAndBottom(offsetY);
            child.offsetTopAndBottom(offsetY);
            if (childTop == iOverView.pullRefreshHeight && refreshState == IOverView.IRefreshState.STATE_OVER_RELEASE) {
//                HiLog.i(TAG, "refresh，childTop：" + childTop);
                refresh();
            }
        } else {
            if (iOverView.getRefreshState() != IOverView.IRefreshState.STATE_OVER && nonAuto) {
                //超出刷新位置
                iOverView.onOver();
                iOverView.setRefreshState(IOverView.IRefreshState.STATE_OVER);
            }
            head.offsetTopAndBottom(offsetY);
            child.offsetTopAndBottom(offsetY);
        }
        if (iOverView != null) {
            iOverView.onScroll(head.getBottom(), iOverView.pullRefreshHeight);
        }
        return true;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        //定义head和child的排列位置
        View head = getChildAt(0);
        View child = getChildAt(1);
        if (head != null && child != null) {
//            HiLog.i(TAG, "onLayout head-height:" + head.getMeasuredHeight());
            int childTop = child.getTop();
            if (refreshState == IOverView.IRefreshState.STATE_REFRESH) {
                head.layout(0, iOverView.pullRefreshHeight - head.getMeasuredHeight(), right, iOverView.pullRefreshHeight);
                child.layout(0, iOverView.pullRefreshHeight, right, iOverView.pullRefreshHeight + child.getMeasuredHeight());
            } else {
                //left,top,right,bottom
                head.layout(0, childTop - head.getMeasuredHeight(), right, childTop);
                child.layout(0, childTop, right, childTop + child.getMeasuredHeight());
            }

            View other;
            for (int i = 2; i < getChildCount(); ++i) {
                other = getChildAt(i);
                other.layout(0, top, right, bottom);
            }
//            HiLog.i(TAG, "onLayout head-bottom:" + head.getBottom());
        }

    }

    /**
     * 视图自动滚动
     */
    public class AutoScroller implements Runnable {

        private Scroller scroller;
        private boolean isFinished;
        private int lastY;

        public AutoScroller(Context context) {
            scroller = new Scroller(context, new LinearInterpolator());
            isFinished = true;
        }

        @Override
        public void run() {
            if (scroller.computeScrollOffset()) {//还未滚动完成
                moveDown(lastY - scroller.getCurrY(), false);
                lastY = scroller.getCurrY();
                post(this);
            } else {
                removeCallbacks(this);
                isFinished = true;
            }
        }

        void recover(int dis) {
            if (dis <= 0) return;
            removeCallbacks(this);
            lastY = 0;
            isFinished = false;
            scroller.startScroll(0, 0, 0, dis, 300);
            post(this);
        }

        boolean isFinished() {
            return isFinished;
        }
    }
}
