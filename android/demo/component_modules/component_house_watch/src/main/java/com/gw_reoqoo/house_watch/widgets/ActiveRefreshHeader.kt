package com.gw_reoqoo.house_watch.widgets

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import com.airbnb.lottie.LottieAnimationView
import com.gw_reoqoo.component_house_watch.R
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.constant.RefreshState
import com.scwang.smart.refresh.layout.simple.SimpleComponent

/**
 * @Description: - 智能看家活动卡片的刷新控件头部
 * @Author: XIAOLEI
 * @Date: 2023/9/5
 */
class ActiveRefreshHeader : SimpleComponent {
    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : super(context, attrs, 0)

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr)

    // 动画控件
    private val lavAnim by lazy { findViewById<LottieAnimationView>(R.id.lav_anim) }

    init {
        inflate(context, R.layout.house_watch_layout_refresh_head, this)
    }


    override fun onStateChanged(
        refreshLayout: RefreshLayout,
        oldState: RefreshState,
        newState: RefreshState
    ) {
        when (newState) {
            // 下拉开始刷新
            RefreshState.PullDownToRefresh -> Unit
            // 正在刷新
            RefreshState.Refreshing -> {
                lavAnim.playAnimation()
            }
            // 释放立即刷新
            RefreshState.ReleaseToRefresh -> Unit
            else -> Unit
        }
    }

    override fun onFinish(refreshLayout: RefreshLayout, success: Boolean): Int {
        lavAnim.cancelAnimation()
        return 0
    }

    override fun onMoving(
        isDragging: Boolean,
        percent: Float,
        offset: Int,
        height: Int,
        maxDragHeight: Int
    ) {
        //Log.e("XIAOLEI", "percent:$percent")
        lavAnim.progress = percent % 1.0f
    }

    override fun isSupportHorizontalDrag() = false
}