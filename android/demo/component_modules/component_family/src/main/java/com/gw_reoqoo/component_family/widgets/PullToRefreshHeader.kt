package com.gw_reoqoo.component_family.widgets

import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.lottie.LottieAnimationView
import com.gw_reoqoo.component_family.R
import com.gw_reoqoo.lib_utils.ktx.dp

/**
 * @Description: - 下拉刷新顶部控件
 * @Author: XIAOLEI
 * @Date: 2023/8/3
 */
class PullToRefreshHeader(context: Context) : ConstraintLayout(context), LoadingHandler {
    private var mHeight = 0
    private val animationView: LottieAnimationView by lazy { findViewById(R.id.animation_view) }
    private val minLoadingHeight = 72.dp

    init {
        layoutParams = LinearLayoutCompat.LayoutParams(
            LinearLayoutCompat.LayoutParams.MATCH_PARENT,
            0
        )
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.family_pull_2_refresh_header, null)
        val childParams = LayoutParams(0, 0)
        childParams.topToTop = LayoutParams.PARENT_ID
        childParams.startToStart = LayoutParams.PARENT_ID
        childParams.endToEnd = LayoutParams.PARENT_ID
        childParams.bottomToBottom = LayoutParams.PARENT_ID
        addView(view, childParams)
    }

    /**
     * 当高度改变时，调用的函数。
     * @param height 高度
     * @param percent 当前高度与最大高度的百分比
     * @see PullToRefreshLayout.MAX_HEIGHT
     */
    fun onUpdate(height: Int, percent: Float) {
        animationView.progress = percent
        this.mHeight = height
    }

    /**
     * 获取最小开始加载中的高度
     */
    fun getMinLoadingHeight(): Int {
        return minLoadingHeight
    }

    /**
     * 是否满足加载条件
     */
    fun canLoading(): Boolean {
        return this.mHeight >= minLoadingHeight
    }

    /**
     * 开始加载
     */
    override fun startLoading() {
        if (!isLoading()) {
            animationView.playAnimation()
        }
    }

    /**
     * 是否正在加载
     */
    override fun isLoading(): Boolean {
        return animationView.isAnimating
    }

    /**
     * 取消加载
     */
    override fun cancelLoading() {
        animationView.cancelAnimation()
    }
}