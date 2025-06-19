package com.gw_reoqoo.house_watch.ui.active_card.adapter

import com.gw_reoqoo.component_house_watch.R
import com.gw_reoqoo.component_house_watch.databinding.HouseWatchItemDialogActiveDeviceConfigBinding as Binding
import com.gw_reoqoo.house_watch.entities.DeviceWrapper
import com.gw_reoqoo.lib_utils.ktx.setSingleClickListener
import com.gw_reoqoo.lib_widget.adapter.AbsDiffBDAdapter
import com.gw_reoqoo.resource.R as RR

/**
 * @Description: - 弹窗选择选项的适配器
 * @Author: XIAOLEI
 * @Date: 2023/8/22
 */
class DialogDeviceAdapter : AbsDiffBDAdapter<Binding, com.gw_reoqoo.house_watch.entities.DeviceWrapper>() {

    override val layoutId: Int get() = R.layout.house_watch_item_dialog_active_device_config

    /**
     * 是否全部显示，如果是否，则默认显示数3个
     */
    private var isShowAll = false

    /**
     * item点击事件
     */
    private var itemClick: ((com.gw_reoqoo.house_watch.entities.DeviceWrapper) -> Unit)? = null

    /**
     * 设置item点击事件
     */
    fun setOnItemClick(block: (com.gw_reoqoo.house_watch.entities.DeviceWrapper) -> Unit) {
        this.itemClick = block
    }

    /**
     * 设置列表的count
     *
     * @return Int 总数
     */
    override fun getItemCount(): Int {
        if (data.isEmpty()) {
            return 0
        }
        return if (isShowAll) {
            data.size
        } else {
            if (data.size > 3) 3 else data.size
        }
    }

    /**
     * 设置显示方式
     *
     * @param showAll Boolean 是否全部显示
     */
    fun setShowAll(showAll: Boolean) {
        isShowAll = showAll
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(binding: Binding) {
        binding.root.setSingleClickListener { v ->
            val tag = v.tag
            if (tag is com.gw_reoqoo.house_watch.entities.DeviceWrapper) {
                itemClick?.invoke(tag)
            }
        }
    }

    override fun onBindViewHolder(binding: Binding, position: Int) {
        val bean = getItemData(position)
        binding.root.tag = bean
        binding.tvSelect.run {
            text = (bean.device?.remarkName) ?: context.getString(RR.string.AA0208)
            setBackgroundResource(
                if (bean.checked) {
                    R.drawable.house_watch_shape_bg_viewtype_checked
                } else {
                    R.drawable.house_watch_shape_bg_viewtype_normal
                }
            )
        }
    }
}