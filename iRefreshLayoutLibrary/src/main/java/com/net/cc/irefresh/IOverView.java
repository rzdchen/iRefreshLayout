package com.net.cc.irefresh;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * @Description 下拉刷新的Overlay视图，可以重载这个类来定义自己的Overlay
 * @Author CC
 * @Date 2021/9/15 下午10:55
 */
public abstract class IOverView extends FrameLayout {
    /**
     * 刷新的状态
     */
    protected IRefreshState refreshState = IRefreshState.STATE_INIT;
    /**
     * 触发下拉刷新的最小高度
     */
    public int pullRefreshHeight;

    /**
     * 最小阻尼
     */
    public float minDamp = 1.6f;
    /**
     * 最大阻尼
     */
    public float maxDamp = 2.2f;


    public IOverView(@NonNull Context context) {
        this(context,null);
    }

    public IOverView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public IOverView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        preInit();
    }

    protected void preInit() {
        pullRefreshHeight = IDisplayUtil.dp2px(66, getResources());
        init();
    }


    /**
     * 初始化
     */
    public abstract void init();

    protected abstract void onScroll(int scrollY, int pullRefreshHeight);

    /**
     * 显示overlay
     */
    protected abstract void onVisible();

    /**
     * 超过overlay，释放就会加载
     */
    public abstract void onOver();

    /**
     * 开始刷新
     */
    public abstract void onRefresh();

    /**
     * 加载完成
     */
    public abstract void onFinish();

    /**
     * 设置刷新状态
     * @param refreshState 状态
     */
    public void setRefreshState(IRefreshState refreshState){
        this.refreshState = refreshState;
    }

    /**
     * 获取刷新状态
     * @return 刷新状态
     */
    public IRefreshState getRefreshState(){
        return refreshState;
    }

    /**
     * 定义刷新时状态
     */
    public enum IRefreshState {
        /**
         * 初始状态
         */
        STATE_INIT,
        /**
         * Header展示的状态
         */
        STATE_VISIBLE,
        /**
         * 超出可刷新距离的状态
         */
        STATE_REFRESH,
        /**
         * 超出刷新位置松开手后的状态
         */
        STATE_OVER_RELEASE,
        STATE_OVER
    }
}
