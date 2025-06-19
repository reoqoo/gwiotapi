package com.gw_reoqoo.house_watch.ui.video_card.adapter

import com.gw_reoqoo.component_house_watch.R
import com.gw_reoqoo.component_house_watch.databinding.HouseWatchVideoItemInstructBinding as Binding
import com.gw_reoqoo.lib_widget.adapter.AbsDiffBDAdapter

/**
 * @Description: - 页面页码指示器的适配器
 * @Author: XIAOLEI
 * @Date: 2023/8/30
 */
class PageInstructAdapter : AbsDiffBDAdapter<Binding, VideoPage>() {
    private var currentIndex: Int = 0
    override val layoutId: Int get() = R.layout.house_watch_video_item_instruct


    override fun onBindViewHolder(binding: Binding, position: Int) {
        if (currentIndex == position) {
            binding.view.setBackgroundResource(R.drawable.house_watch_shape_bg_instruct_enable)
        } else {
            binding.view.setBackgroundResource(R.drawable.house_watch_shape_bg_instruct_unable)
        }
    }

    /**
     * 设置当前下标
     * 
     * @param current 当前下标
     */
    fun setCurrent(current: Int) {
        this.currentIndex = current
        this.notifyDataSetChanged()
    }
}