package com.gw_reoqoo.house_watch.ui.video_page.adapter

import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.gw_reoqoo.component_house_watch.R
import com.gw_reoqoo.component_house_watch.databinding.HouseWatchSinglePanelItemMultiCamBinding as Binding
import com.gw_reoqoo.house_watch.entities.MultiCamThumbBean
import com.gw_reoqoo.lib_utils.ktx.dp
import com.gw_reoqoo.lib_widget.adapter.AbsDiffBDAdapter

/**
 * 单设备下，底部镜头的缩略图的适配器。
 */
class SinglePageMultiCamThumbAdapter : AbsDiffBDAdapter<Binding, MultiCamThumbBean>() {
    override val layoutId: Int get() = R.layout.house_watch_single_panel_item_multi_cam
    override fun onBindViewHolder(binding: Binding, position: Int) {
        val bean = getItemData(position)
        binding.cbCheckStatus.isChecked = bean.checked
        Glide.with(binding.ivImage)
            .load(bean.thumbImgUrl)
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .placeholder(R.drawable.house_watch_img_dev_empty)
            .error(R.drawable.house_watch_img_dev_empty)
            .transform(CenterCrop(), RoundedCorners(7.dp))
            .into(binding.ivImage)
    }

    override fun areIdSame(old: MultiCamThumbBean, new: MultiCamThumbBean): Boolean {
        return old.camIndex == new.camIndex
    }

    override fun areAllSame(old: MultiCamThumbBean, new: MultiCamThumbBean): Boolean {
        return old == new
    }
}