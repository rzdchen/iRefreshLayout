package com.net.cc.irefresh;

/**
 * @Description IRefreshLayout 对外的接口
 * @Author CC
 * @Date 2021/9/15 下午10:55
 */
public interface IRefresh {

    /**
     * 刷新时是否禁止滚动
     *
     * @param disableRefreshScroll 是否禁止滚动
     */
    void setDisableRefreshScroll(boolean disableRefreshScroll);

    /**
     * 刷新完成
     */
    void refreshFinished();

    void setRefreshListener(IRefreshListener refreshListener);

    void setRefreshOverView(IOverView iOverView);

    /**
     * 刷新回调接口
     */
    interface IRefreshListener {
        void onRefresh();

        boolean enableRefresh();
    }
}
