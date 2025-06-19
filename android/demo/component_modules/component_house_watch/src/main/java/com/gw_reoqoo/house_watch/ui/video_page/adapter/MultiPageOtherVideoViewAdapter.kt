package com.gw_reoqoo.house_watch.ui.video_page.adapter

import com.gw_reoqoo.component_house_watch.R
import com.gw_reoqoo.component_house_watch.databinding.HouseWatchItemMultiPageOtherCamBinding as Binding
import com.gw_reoqoo.lib_widget.adapter.AbsDiffBDAdapter
import com.jwkj.iotvideo.player.LivePlayer

class MultiPageOtherVideoViewAdapter(
    private val player: LivePlayer
) : AbsDiffBDAdapter<Binding, Int>() {
    override val layoutId: Int = R.layout.house_watch_item_multi_page_other_cam
    override fun onCreateViewHolder(binding: Binding) {
        player.addVideoView(binding.videoView.videoView)
    }
}