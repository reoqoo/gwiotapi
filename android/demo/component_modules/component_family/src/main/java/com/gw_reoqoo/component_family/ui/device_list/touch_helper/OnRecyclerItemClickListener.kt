package com.gw_reoqoo.component_family.ui.device_list.touch_helper

import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import androidx.core.view.GestureDetectorCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener
import com.gw_reoqoo.component_family.databinding.FamilyItemDeviceListBinding
import com.gw_reoqoo.lib_room.device.DeviceInfo
import com.gw_reoqoo.lib_widget.adapter.AbsDiffBDAdapter
import com.gwell.loglibs.GwellLogUtils

/**
 * @author yanzheng@gwell.cc
 * @desc 自定义列表的点击事件
 */
abstract class OnRecyclerItemClickListener(private val mRecyclerView: RecyclerView) :
    OnItemTouchListener {

    private val TAG = "OnRecyclerItemClickListener"

    /**
     * 手势检测工具类
     */
    private val mGestureDetectorCompat: GestureDetectorCompat

    /**
     * 适配器
     */
    private val mAdapter: RecyclerView.Adapter<*>?

    init {
        mGestureDetectorCompat = GestureDetectorCompat(
            mRecyclerView.context,
            ItemTouchHelperGestureListener()
        )

        mAdapter = mRecyclerView.adapter
    }

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        mGestureDetectorCompat.onTouchEvent(e)
        return false
    }

    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
        mGestureDetectorCompat.onTouchEvent(e)
    }

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
    }

    /**
     * 点击回调
     *
     * @param viewHolder ViewHolder?  点击的view
     * @param position Int            点击view的位置
     */
    abstract fun onItemClick(viewHolder: RecyclerView.ViewHolder?, position: Int)

    /**
     * 长按回调
     *
     * @param viewHolder ViewHolder?  点击的view
     * @param position Int            点击view的位置
     */
    abstract fun onLongClick(viewHolder: RecyclerView.ViewHolder?, position: Int)

    /**
     * 手势监听器
     */
    private inner class ItemTouchHelperGestureListener : SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            val childViewUnder = mRecyclerView.findChildViewUnder(e.x, e.y)
            GwellLogUtils.i(TAG, "childViewUnder: $childViewUnder")
            if (childViewUnder != null) {
                val viewHolder =
                    mRecyclerView.getChildViewHolder(childViewUnder) as? AbsDiffBDAdapter<FamilyItemDeviceListBinding, DeviceInfo>.ViewHolder<FamilyItemDeviceListBinding>
                if (viewHolder == null) {
                    val childViewPosition = mRecyclerView.getChildAdapterPosition(childViewUnder)
                    val childViewHolder = mRecyclerView.getChildViewHolder(childViewUnder)
                    onItemClick(childViewHolder, childViewPosition)
                } else {
                    val isPositionInside = isPointInsideView(e, viewHolder.binding.btTurnOffOrOn)
                    GwellLogUtils.i(TAG, "isPointInsideView: $isPositionInside")
                    if (!isPositionInside) {
                        val childViewPosition =
                            mRecyclerView.getChildAdapterPosition(childViewUnder)
                        val childViewHolder = mRecyclerView.getChildViewHolder(childViewUnder)
                        onItemClick(childViewHolder, childViewPosition)
                    }
                }
            }
            return true
        }

        override fun onLongPress(e: MotionEvent) {
            val childViewUnder = mRecyclerView.findChildViewUnder(e.x, e.y)
            if (childViewUnder != null) {
                val childViewHolder = mRecyclerView.getChildViewHolder(childViewUnder)
                val childViewPosition = mRecyclerView.getChildAdapterPosition(childViewUnder)
                onLongClick(childViewHolder, childViewPosition)
            }
        }
    }

    /**
     * 判断点击点是否在指定 View 的范围内
     *
     * @param event MotionEvent 点击事件event
     * @param view View         需要判断的view
     * @return Boolean          是否点击在view上
     */
    private fun isPointInsideView(event: MotionEvent, view: View): Boolean {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val x = location[0]
        val y = location[1]
        val width = view.width
        val height = view.height

        GwellLogUtils.i(TAG, "x: $x, y: $y, width: $width, height: $height")

        return event.rawX >= x && event.rawX <= x + width &&
                event.rawY >= y && event.rawY <= y + height
    }
}
