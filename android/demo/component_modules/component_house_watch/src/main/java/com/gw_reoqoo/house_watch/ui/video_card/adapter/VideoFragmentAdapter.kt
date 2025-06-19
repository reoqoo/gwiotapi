package com.gw_reoqoo.house_watch.ui.video_card.adapter

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.gw_reoqoo.house_watch.entities.DevicePack
import com.gw_reoqoo.house_watch.entities.ViewTypeModel
import com.gw_reoqoo.house_watch.ui.video_page.VideoPageFragment
import com.gwell.loglibs.GwellLogUtils
import java.util.LinkedList

/**
 * @Description: -
 * @Author: XIAOLEI
 * @Date: 2023/10/25
 */
class VideoFragmentAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    companion object {
        private const val TAG = "VideoFragmentAdapter"
    }

    /**
     * 设备列表
     */
    private val list = LinkedList<VideoPage>()

    val data: List<VideoPage> get() = LinkedList<VideoPage>().apply { addAll(list) }

    override fun getItemCount() = list.size

    override fun createFragment(position: Int): Fragment {
        val fragment = VideoPageFragment()
        val videoPage = list[position]
        fragment.arguments = bundleOf(
            "videoPage" to videoPage
        )
        return fragment
    }

    override fun getItemId(position: Int): Long {
        return list[position].getId.toLong()
    }

    override fun containsItem(itemId: Long): Boolean {
        return list.any { it.getId.toLong() == itemId }
    }

    /**
     * 更新数据
     *
     * @param data 设备
     * @param model 显示模式 单个模式/4个模式
     */
    fun updateData(data: List<DevicePack>, model: ViewTypeModel = ViewTypeModel.SINGLE) {
        val packList = if (model == ViewTypeModel.SINGLE) {
            data.map { VideoPage.SinglePage(it) }
        } else {
            data.chunked(4) { partList ->
                VideoPage.MultiPage(
                    partList.first(),
                    partList.getOrNull(1),
                    partList.getOrNull(2),
                    partList.getOrNull(3)
                )
            }
        }
        this.list.clear()
        this.list.addAll(packList)
        notifyDataSetChanged()
    }

    /**
     * 设置显示模式
     * @param model 单个模式/4个模式
     */
    fun setViewType(model: ViewTypeModel) {
        val deviceList = this.list.map { it.devices }.flatten()
        updateData(deviceList, model)
    }
}