package com.gw_reoqoo.house_watch.widgets

import android.content.Context
import android.util.AttributeSet
import com.airbnb.lottie.LottieAnimationView
import com.gw_reoqoo.component_house_watch.R
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.constant.RefreshState
import com.scwang.smart.refresh.layout.simple.SimpleComponent

/**
 * @Description: - 智能看家活动卡片的加载更多控件底部
 * @Author: XIAOLEI
 * @Date: 2023/9/6
 */
class ActiveRefreshFooter : SimpleComponent {
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
        inflate(context, R.layout.house_watch_layout_refresh_foot, this)
    }

    override fun onStateChanged(
        refreshLayout: RefreshLayout,
        oldState: RefreshState,
        newState: RefreshState
    ) {
        when (newState) {
            // 上拉开始加载
            RefreshState.PullUpToLoad -> Unit
            // 正在加载
            RefreshState.Loading -> {
                lavAnim.playAnimation()
            }
            // 释放立即加载
            RefreshState.ReleaseToLoad -> Unit
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
        lavAnim.progress = percent % 1.0f
    }

    override fun setNoMoreData(noMoreData: Boolean): Boolean {
        return super.setNoMoreData(noMoreData)
    }

    override fun isSupportHorizontalDrag() = false
}