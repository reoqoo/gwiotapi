package com.gw_reoqoo.component_family.ui.device_list.touch_helper

import android.util.Log
import android.view.ViewGroup
import android.view.ViewParent
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.gw_reoqoo.component_family.ui.device_list.adapter.DeviceListAdapter
import com.gw_reoqoo.component_family.widgets.PullToRefreshLayout as FamilyPullToRefreshLayout
import com.gw_reoqoo.lib_widget.layout.pulltorefresh.PullToRefreshLayout as WidgetPullToRefreshLayout
import com.gwell.loglibs.GwellLogUtils

/**
 * @author yanzheng@gwell.cc
 * @desc Android 提供的一个工具类，用于实现 RecyclerView 的拖动和滑动功能
 */
class RecyItemTouchHelperCallback(
    private val recyclerView: RecyclerView,
    private val onMove: () -> Unit,
    private val afterMove: () -> Unit
) : ItemTouchHelper.Callback() {

    companion object {
        private const val TAG = "RecyItemTouchHelperCall"
        private const val ANIMATE_DURATION = 250L
        private const val SCALE_UP = 1.05f
        private const val SCALE_BACK = 1.0f
    }

    /**
     * 拖拽排序
     */
    private var mAdapter: DeviceListAdapter? = null

    /**
     * 是否开启侧滑删除
     */
    private var isSwipeEnable: Boolean = true

    /**
     * 是否禁止第一项拖动
     */
    private var isFirstDragUnable: Boolean = false

    /**
     * 拖拽的ViewMode
     */
    private var dragViewHolder: RecyclerView.ViewHolder? = null

    init {
        mAdapter = recyclerView.adapter as? DeviceListAdapter?
    }

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        if (recyclerView.layoutManager is GridLayoutManager) {
            val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN or
                    ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            val swipeFlags = 0
            return makeMovementFlags(dragFlags, swipeFlags)
        } else {
            val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
            val swipeFlags = ItemTouchHelper.START or ItemTouchHelper.END
            return makeMovementFlags(dragFlags, swipeFlags)
        }
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        onMove.invoke()
        val adapter = mAdapter ?: return false
        //取出原位置的position
        val from = viewHolder.adapterPosition
        //取出目标位置的position
        val to = target.adapterPosition
        GwellLogUtils.d(TAG, "onMove(from:$from,to:$to)")
        //取出原位置的数据
        val list = adapter.data.toMutableList()

        val fromBean = list[from]

        if (from > to) {
            val subList = list.subList(to, from)
            subList.forEach { info ->
                val index = info.index
                if (index != null) {
                    info.index = index + 1
                }
            }
        } else {
            val subList = list.subList((from + 1), to + 1)
            subList.forEach { info ->
                val index = info.index
                if (index != null) {
                    info.index = index - 1
                }
            }
        }
        fromBean.index = to.toLong()
        //删除原位置的数据
        list.removeAt(from)
        //向目标位置添加原位置的数据
        list.add(to, fromBean)
        //通知Adapter移动了Item
        adapter.updateData(list)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        when (actionState) {
            ItemTouchHelper.ACTION_STATE_IDLE -> {
                afterMove.invoke()
                this.requestDisallowInterceptTouchEvent(false)
                val oldDragVideHolder = this.dragViewHolder
                if (oldDragVideHolder != null) {
                    this.dragViewHolder = null
                    val animate = oldDragVideHolder.itemView.animate()
                    animate.setDuration(ANIMATE_DURATION)
                    animate.scaleX(SCALE_BACK)
                    animate.scaleY(SCALE_BACK)
                    animate.start()
                }
            }

            ItemTouchHelper.ACTION_STATE_DRAG -> {
                this.requestDisallowInterceptTouchEvent(true)
                val oldDragVideHolder = this.dragViewHolder
                if (oldDragVideHolder != null) {
                    this.dragViewHolder = null
                    oldDragVideHolder.itemView.scaleY = SCALE_BACK
                    oldDragVideHolder.itemView.scaleX = SCALE_BACK
                }
                if (viewHolder != null) {
                    this.dragViewHolder = viewHolder
                    val animate = viewHolder.itemView.animate()
                    animate.setDuration(ANIMATE_DURATION)
                    animate.scaleX(SCALE_UP)
                    animate.scaleY(SCALE_UP)
                    animate.start()
                }
            }

            ItemTouchHelper.ACTION_STATE_SWIPE -> Unit
        }
        super.onSelectedChanged(viewHolder, actionState)
    }

    /**
     * 通知父控件不要拦截了
     */
    private fun requestDisallowInterceptTouchEvent(disAllowIntercept: Boolean) {
        var parent: ViewParent? = recyclerView.parent
        while (true) {
            if (parent == null) {
                break
            }
            if (parent !is ViewGroup) {
                break
            }
            parent.requestDisallowInterceptTouchEvent(disAllowIntercept)
            // 如果是我自己写的下拉刷新组件，则直接跳出，不再往上找了。
            if (parent is WidgetPullToRefreshLayout) {
                break
            }
            if (parent is FamilyPullToRefreshLayout) {
                break
            }
            parent = parent.parent
        }
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
    }

    override fun isLongPressDragEnabled(): Boolean {
        return !isFirstDragUnable
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return isSwipeEnable
    }
}
