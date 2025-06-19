package com.gw_reoqoo.component_family.ui.family.adapter

import android.graphics.Color
import com.gw_reoqoo.component_family.R
import com.gw_reoqoo.component_family.databinding.FamilyTitleListItemBinding
import com.gw_reoqoo.component_family.ui.family.bean.FragmentBeanWrapper
import com.gw_reoqoo.lib_widget.adapter.AbsDiffBDAdapter

/**
 * @Description: - 标题适配器
 * @Author: XIAOLEI
 * @Date: 2023/8/1
 */
class TitleAdapter : AbsDiffBDAdapter<FamilyTitleListItemBinding, FragmentBeanWrapper>() {

    private var current = 0
    override val layoutId: Int get() = R.layout.family_title_list_item

    override fun onBindViewHolder(binding: FamilyTitleListItemBinding, position: Int) {
        val context = binding.root.context
        val wrapper = getItemData(position)
        val text = context.getString(wrapper.textSrc)
        binding.titleTv.text = text
        if (current == position) {
            binding.titleTv.textSize = 20f
            val color = Color.BLACK
            binding.titleTv.setTextColor(color)
        } else {
            binding.titleTv.textSize = 17f
            val color = Color.parseColor("#BF000000")
            binding.titleTv.setTextColor(color)
        }
    }

    fun setData(datas: List<FragmentBeanWrapper>, current: Int) {
        this.current = current
        this.updateData(datas)
    }

    fun setCurrent(current: Int) {
        this.current = current
        this.notifyDataSetChanged()
    }
}