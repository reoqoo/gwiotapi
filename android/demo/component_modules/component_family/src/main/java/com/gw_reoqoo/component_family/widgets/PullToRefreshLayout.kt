package com.gw_reoqoo.component_family.widgets

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.LinearInterpolator
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.animation.doOnEnd
import androidx.core.view.updateLayoutParams
import androidx.core.widget.NestedScrollView
import com.gw_reoqoo.lib_utils.ktx.dp
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * @Description: - 下拉刷新控件
 * @Author: XIAOLEI
 * @Date: 2023/8/3
 */
class PullToRefreshLayout : LinearLayoutCompat, LoadingHandler {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    )

    /**
     * 顶部提示控件
     */
    private val header by lazy {
        PullToRefreshHeader(context)
    }

    /**
     * 记录按下的位置
     */
    private var downEvent: MotionEvent? = null

    /**
     * 手指滑动方向
     */
    private var direction: Direction? = null

    /**
     * 记录开始拖拽的位置
     */
    private var startPullEvent: MotionEvent? = null

    /**
     * 判断滑动方向需要至少滑动的距离
     */
    private val scrollDistance = 50

    /**
     * 标记是否进入拖拽
     */
    private var canPull = false

    /**
     * 最大可拖拽的距离
     */
    private var MAX_HEIGHT = (72 * 3).dp

    /**
     * 加载的回调默认实现，3秒后复原
     */
    private var onLoading: ((LoadingHandler) -> Unit) = {
        handler.postDelayed({
            cancelLoading()
        }, 3000)
    }

    init {
        this.orientation = VERTICAL
        this.addView(header, 0)
    }

    /**
     * 重置动画
     */
    private val resetAnim by lazy {
        ValueAnimator.ofInt(0, 0).apply {
            duration = 200
            interpolator = LinearInterpolator()
            addUpdateListener { anim ->
                val height = anim.animatedValue as Int
                updateHeaderHeight(height)
            }
        }
    }

    /**
     * 重置到最小高度的动画，并且在动画结束后，回调onLoading
     */
    private val resetToMinHeightAnim by lazy {
        ValueAnimator.ofInt(0, 0).apply {
            duration = 100
            interpolator = LinearInterpolator()
            addUpdateListener { anim ->
                val height = anim.animatedValue as Int
                updateHeaderHeight(height)
            }
            doOnEnd {
                onLoading.invoke(this@PullToRefreshLayout)
            }
        }
    }
    private var disallowIntercept = false

    override fun requestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        this.disallowIntercept = disallowIntercept
        super.requestDisallowInterceptTouchEvent(disallowIntercept)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (disallowIntercept) return super.dispatchTouchEvent(ev)
        if (isLoading()) return false
        when (ev) {
            null -> return super.dispatchTouchEvent(null)
            else -> when (ev.action) {
                MotionEvent.ACTION_DOWN -> {
                    downEvent = MotionEvent.obtain(ev)
                    direction = null
                    canPull = false
                    startPullEvent?.recycle()
                    startPullEvent = null
                }

                MotionEvent.ACTION_MOVE -> {
                    val downEvent = downEvent ?: ev
                    val distance = distance(downEvent, ev)
                    val direction = this.direction
                    // 先判断手指滑动方向
                    if (direction == null) {
                        if (distance > scrollDistance) {
                            this.direction = getDirection(downEvent, ev)
                        }
                    }
                    // 再判断 可以拖拽标志 是否为false,
                    if (!canPull) {
                        // 再判断之前是否已经计算出来了手指滑动方向
                        if (direction != null) {
                            // 如果有方向，就判断是否达到可以拖拽的条件，并且保存标志
                            canPull = canPull(direction)
                            if (canPull) {
                                // 如果已经达到可以拖拽的的条件，则手动向下分发一个“手指抬起”的假事件，
                                // 并且把符合拖拽的点的事件保存起来，下次计算距离就使用这个事件来计算
                                startPullEvent = MotionEvent.obtain(ev)
                                val fakeEvent = MotionEvent.obtain(ev)
                                fakeEvent.action = MotionEvent.ACTION_UP
                                return super.dispatchTouchEvent(fakeEvent)
                            }
                        }
                    } else {
                        // 这里已经是满足可以拖拽的条件，获取已经保存的符合拖拽条件的坐标，用来计算垂直方向的距离
                        val startPullEvent = this.startPullEvent
                        if (startPullEvent != null) {
                            // 当距离为负数时，则把距离设置为0
                            // 当距离超过最大值时，则把距离设置在最大值
                            pull(min(max(distance(startPullEvent, ev), 0f), MAX_HEIGHT.toFloat()))
                            return true
                        }
                    }
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    downEvent?.recycle()
                    downEvent = null
                    startPullEvent?.recycle()
                    startPullEvent = null
                    direction = null
                    canPull = false
                    if (header.canLoading()) {
                        startLoading()
                        resetToMinHeight()
                    } else {
                        resetHeader()
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    /**
     * 设置正在加载的回调
     */
    fun onLoading(block: (LoadingHandler) -> Unit) {
        this.onLoading = block
    }


    /**
     * 判断是否滚动到顶部，是否可以开启拖拽
     */
    private fun canPull(direction: Direction): Boolean {
        // Log.e("XIAOLEI", "direction:$direction")
        val viewScroll: NestedScrollView = findViewWithTag("view_scroll") ?: return true

        return when (direction) {
            Direction.Vertical -> {
                val canScroll = viewScroll.canScrollVertically(-1)
                !canScroll
            }
        }
    }


    /**
     * 拖拽
     */
    private fun pull(distance: Float) {
        val distanceInt = distance.roundToInt()
        if (distanceInt > header.height) {
            val range = header.height..distanceInt
            range.forEach(::updateHeaderHeight)
        } else {
            val range = header.height downTo distanceInt
            range.forEach(::updateHeaderHeight)
        }
    }

    /**
     * 重置到最小高度
     */
    private fun resetToMinHeight() {
        resetToMinHeightAnim.cancel()
        resetToMinHeightAnim.setIntValues(header.measuredHeight, header.getMinLoadingHeight())
        resetToMinHeightAnim.start()
    }

    /**
     * 恢复顶部控件位置
     */
    private fun resetHeader() {
        resetAnim.cancel()
        resetAnim.setIntValues(header.measuredHeight, 0)
        resetAnim.start()
    }

    /**
     * 两点之间的距离
     * d=√[(x2-x1)²+(y2-y1)²]
     */
    private fun distance(start: MotionEvent, end: MotionEvent): Float {
        val y1 = start.y
        val y2 = end.getY(0)
        return y2 - y1
    }

    /**
     * 获取方向
     */
    private fun getDirection(start: MotionEvent, end: MotionEvent): Direction? {
        val x1 = start.x
        val y1 = start.y
        val x2 = end.x
        val y2 = end.y
        if (abs(x2 - x1) > abs(y2 - y1)) {
            return null
        }
        return Direction.Vertical
    }

    /**
     * 更新头部的高度
     */
    private fun updateHeaderHeight(height: Int) {
        header.updateLayoutParams {
            this.height = height
        }
        header.onUpdate(height, height / MAX_HEIGHT.toFloat())
    }

    /**
     * 手指滑动方向
     */
    sealed interface Direction {
        /**
         * 竖向
         */
        object Vertical : Direction {
            override fun toString(): String = this.javaClass.simpleName
        }
    }

    /**
     * 开始播放加载中的动画
     */
    override fun startLoading() = header.startLoading()

    /**
     * 加载中的动画是否播放
     */
    override fun isLoading() = header.isLoading()

    /**
     * 取消加载中的动画
     */
    override fun cancelLoading() {
        header.cancelLoading()
        resetHeader()
    }
}