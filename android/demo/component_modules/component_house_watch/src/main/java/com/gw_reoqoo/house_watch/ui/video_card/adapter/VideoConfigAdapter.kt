package com.gw_reoqoo.house_watch.ui.video_card.adapter

import com.gw_reoqoo.component_house_watch.R
import com.gw_reoqoo.component_house_watch.databinding.HouseWatchItemDialogVideoDevListBinding as Binding
import com.gw_reoqoo.house_watch.entities.DevicePack
import com.gw_reoqoo.lib_utils.ktx.setSingleClickListener
import com.gw_reoqoo.lib_widget.adapter.AbsDiffBDAdapter

/**
 * @Description: -
 * @Author: XIAOLEI
 * @Date: 2023/8/23
 *
 * @param onEyeChange 当点击眼睛改变的时候，进行的回调
 */
class VideoConfigAdapter(
    private val onEyeChange: (VideoConfigAdapter) -> Unit
) : AbsDiffBDAdapter<Binding, DevicePack>() {
    override val layoutId: Int get() = R.layout.house_watch_item_dialog_video_dev_list

    override fun onCreateViewHolder(binding: Binding) {
        binding.ivEnableView.setSingleClickListener { v ->
            val bean = v.tag
            if (bean is DevicePack) {
                bean.offView = !bean.offView
                onEyeChange.invoke(this)
                this.notifyDataSetChanged()
            }
        }
    }

    override fun onBindViewHolder(binding: Binding, position: Int) {
        val bean = getItemData(position)
        binding.ivEnableView.tag = bean

        binding.root.tag = bean
        binding.tvDevName.text = bean.device.remarkName
        binding.ivEnableView.setImageResource(
            if (bean.offView) {
                R.drawable.house_watch_icon_eyes_off
            } else {
                R.drawable.house_watch_icon_eyes_on
            }
        )
    }
}