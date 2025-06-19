package com.gw_reoqoo.house_watch.ui.active_card.adapter

import com.gw_reoqoo.component_house_watch.R
import com.gw_reoqoo.component_house_watch.databinding.HouseWatchActiveIconListItemBinding as Binding
import com.gw_reoqoo.house_watch.entities.ActiveType
import com.gw_reoqoo.lib_widget.adapter.AbsDiffBDAdapter

/**
 * @Description: - 事件列表里，每个item中的图标列表的adapter
 * @Author: XIAOLEI
 * @Date: 2023/10/11
 */
class ActiveIconsAdapter : AbsDiffBDAdapter<Binding, com.gw_reoqoo.house_watch.entities.ActiveType>() {

    override val layoutId: Int get() = R.layout.house_watch_active_icon_list_item

    override fun onBindViewHolder(binding: Binding, position: Int) {
        val type = getItemData(position)
        binding.ivActive.setImageResource(type.iconRes)
    }

}