package com.gw.cp_mine.ui.activity.language

import androidx.core.view.isVisible
import com.gw.cp_mine.R
import com.gw_reoqoo.resource.R as RR
import com.gw.cp_mine.databinding.MineRvLanguageItemBinding as Binding
import com.gw.cp_mine.entity.LanguageEntity
import com.gw_reoqoo.lib_utils.ktx.draw
import com.gw_reoqoo.lib_utils.ktx.setSingleClickListener
import com.gw_reoqoo.lib_widget.adapter.AbsDiffBDAdapter

/**
@author: xuhaoyuan
@date: 2023/9/5
description:
1.
 */
class LanguageListAdapter : AbsDiffBDAdapter<Binding, LanguageEntity>() {
    /**
     * 点击事件
     */
    private var onLanClick: ((LanguageEntity) -> Unit)? = null

    /**
     * Item对应的layoutId
     */
    override val layoutId: Int get() = R.layout.mine_rv_language_item

    override fun onCreateViewHolder(binding: Binding) {
        binding.root.setSingleClickListener {
            val tag = it.tag
            if (tag is LanguageEntity) {
                onLanClick?.invoke(tag)
            }
        }
    }

    override fun onBindViewHolder(binding: Binding, position: Int) {
        val entity = getItemData(position)
        binding.root.tag = entity
        binding.tvLangName.setText(entity.language.strRes)
        if (entity.selected) {
            binding.tvLangName.draw(end = RR.drawable.gw_reoqoo_dev_share_icon_user_check)
        } else {
            binding.tvLangName.draw(end = R.drawable.mine_shape_check_none)
        }
        binding.vLine.isVisible = position < itemCount - 1
    }


    /**
     * 设置Item点击事件
     */
    fun setOnItemClickListener(onLanClick: ((LanguageEntity) -> Unit)) {
        this.onLanClick = onLanClick
    }
}