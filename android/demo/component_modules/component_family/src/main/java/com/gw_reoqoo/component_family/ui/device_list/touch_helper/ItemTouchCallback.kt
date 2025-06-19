package com.gw_reoqoo.component_family.ui.device_list.touch_helper

import android.util.Log
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.gw_reoqoo.component_family.ui.device_list.adapter.DeviceListAdapter
import com.gwell.loglibs.GwellLogUtils

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
    ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.START or ItemTouchHelper.END,
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
        val adapter = recyclerView.adapter as? DeviceListAdapter? ?: return false
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
        super.onSelectedChanged(viewHolder, actionState)
    }
}