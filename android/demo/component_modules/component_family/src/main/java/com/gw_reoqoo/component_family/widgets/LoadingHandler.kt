package com.gw_reoqoo.component_family.widgets

/**
 * @Description: - 加载的控制句柄
 * @Author: XIAOLEI
 * @Date: 2023/8/15
 */
interface LoadingHandler {
    /**
     * 开始加载
     */
    fun startLoading()

    /**
     * 是否正在加载
     */
    fun isLoading(): Boolean

    /**
     * 取消加载
     */
    fun cancelLoading()
}