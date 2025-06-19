package com.gw_reoqoo.house_watch.entities

import com.gw_reoqoo.component_house_watch.databinding.HouseWatchLayoutDeviceCamVideoViewBinding
import com.gw_reoqoo.house_watch.ui.video_page.adapter.MultiPageOtherVideoViewAdapter
import com.jwkj.iotvideo.player.LivePlayer

/**
 * 一个播放器对应的VideoView的集合
 * @param player 播放器
 * @param backVideoViewBinds 这个在单画面模式下，会把所有的画面放在里面然后由底部的缩略图切换，而多画面模式下，只会放第0个画面进去。
 * @param multiPageOtherVideoViewAdapter 多设备模下，播放器对应的其他镜头的View的适配器
 */
data class PlayerAndViewMap(
    val player: LivePlayer,
    val backVideoViewBinds: MutableList<HouseWatchLayoutDeviceCamVideoViewBinding> = mutableListOf(),
    val multiPageOtherVideoViewAdapter: MultiPageOtherVideoViewAdapter = MultiPageOtherVideoViewAdapter(player),
)
