package com.gw_reoqoo.house_watch.ui.active_card.layout_manager

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager

/**
 * @Description: -
 * @Author: XIAOLEI
 * @Date: 2023/9/5
 */
class NonScrollableGridLayoutManager(
    context: Context, spanCount: Int
) : GridLayoutManager(context, spanCount) {
    override fun canScrollVertically(): Boolean {
        return false
    }
}