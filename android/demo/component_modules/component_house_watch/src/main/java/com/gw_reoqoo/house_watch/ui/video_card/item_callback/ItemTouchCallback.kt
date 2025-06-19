package com.gw_reoqoo.house_watch.ui.video_card.item_callback

import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.gw_reoqoo.component_house_watch.R
import com.gw_reoqoo.house_watch.ui.video_card.adapter.VideoConfigAdapter
import com.gwell.loglibs.GwellLogUtils
import com.gw_reoqoo.resource.R as RR

/**
 * @Description: -
 * @Author: XIAOLEI
 * @Date: 2023/8/17
 * @param recyclerView 需要传入对应的recyclerview
 * @param onMove 当移动的时候的回调
 */
class ItemTouchCallback(
    private val recyclerView: RecyclerView,
    private val onMove: () -> Unit,
    private val afterMove: () -> Unit
) : ItemTouchHelper.SimpleCallback(
    ItemTouchHelper.UP or ItemTouchHelper.DOWN,
    0
) {
    companion object {
        private const val TAG = "ItemTouchCallback"
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        onMove.invoke()
        val adapter = recyclerView.adapter as? VideoConfigAdapter? ?: return false
        // 取出原位置的position
        val from = viewHolder.adapterPosition
        // 取出目标位置的position
        val to = target.adapterPosition
        GwellLogUtils.d(TAG, "onMove(from:$from,to:$to)")
        // 取出原位置的数据
        val list = adapter.data.toMutableList()
        val fromBean = list[from]
        // 删除原位置的数据
        list.removeAt(from)
        // 向目标位置添加原位置的数据
        list.add(to, fromBean)
        // 通知Adapter移动了Item
        adapter.updateData(list)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) = Unit

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        when (actionState) {
            ItemTouchHelper.ACTION_STATE_IDLE -> {
                afterMove.invoke()
                recyclerView.parent.requestDisallowInterceptTouchEvent(false)
            }

            ItemTouchHelper.ACTION_STATE_DRAG -> {
                recyclerView.parent.requestDisallowInterceptTouchEvent(true)
            }

            ItemTouchHelper.ACTION_STATE_SWIPE -> Unit
        }

        // 加阴影
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            if (viewHolder != null) {
                val itemContent: View? = viewHolder.itemView.findViewById(R.id.rl_item_content)
                itemContent?.setBackgroundResource(RR.color.color_f2f2f2)
            }
        }

        super.onSelectedChanged(viewHolder, actionState)
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        // 清除阴影
        val itemContent: View? = viewHolder.itemView.findViewById(R.id.rl_item_content)
        itemContent?.setBackgroundResource(RR.color.white_100)
        super.clearView(recyclerView, viewHolder)
    }
}