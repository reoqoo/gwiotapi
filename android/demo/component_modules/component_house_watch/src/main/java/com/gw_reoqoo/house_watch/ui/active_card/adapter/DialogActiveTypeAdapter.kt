package com.gw_reoqoo.house_watch.ui.active_card.adapter

import com.gw_reoqoo.component_house_watch.R
import com.gw_reoqoo.house_watch.entities.ActiveTypeWrapper
import com.gw_reoqoo.lib_utils.ktx.draw
import com.gw_reoqoo.component_house_watch.databinding.HouseWatchItemDialogActiveConfigBinding as Binding
import com.gw_reoqoo.lib_utils.ktx.setSingleClickListener
import com.gw_reoqoo.lib_widget.adapter.AbsDiffBDAdapter
import com.gw_reoqoo.resource.R as RR

/**
 * @Description: - 弹窗选择选项的适配器
 * @Author: XIAOLEI
 * @Date: 2023/8/22
 */
class DialogActiveTypeAdapter : AbsDiffBDAdapter<Binding, com.gw_reoqoo.house_watch.entities.ActiveTypeWrapper>() {
    override val layoutId: Int get() = R.layout.house_watch_item_dialog_active_config

    /**
     * item点击事件
     */
    private var itemClick: ((com.gw_reoqoo.house_watch.entities.ActiveTypeWrapper) -> Unit)? = null

    /**
     * 设置item点击事件
     */
    fun setOnItemClick(block: (com.gw_reoqoo.house_watch.entities.ActiveTypeWrapper) -> Unit) {
        this.itemClick = block
    }

    override fun onCreateViewHolder(binding: Binding) {
        binding.root.setSingleClickListener { v ->
            val tag = v.tag
            if (tag is com.gw_reoqoo.house_watch.entities.ActiveTypeWrapper) {
                itemClick?.invoke(tag)
            }
        }
    }

    override fun onBindViewHolder(binding: Binding, position: Int) {
        val bean = getItemData(position)
        binding.root.tag = bean
        binding.tvSelect.run {
            text = context.getString(bean.type?.descRes ?: RR.string.AA0208)
            draw(start = bean.type?.iconRes)
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