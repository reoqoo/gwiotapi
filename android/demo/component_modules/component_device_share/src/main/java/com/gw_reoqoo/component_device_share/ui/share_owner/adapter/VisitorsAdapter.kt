package com.gw_reoqoo.component_device_share.ui.share_owner.adapter

import androidx.core.view.isVisible
import com.gw_reoqoo.component_device_share.R
import com.gw_reoqoo.lib_http.entities.Guest
import com.gw_reoqoo.resource.R as RR
import com.gw_reoqoo.component_device_share.databinding.DevShareVisitorItemBinding as Binding
import com.gw_reoqoo.lib_utils.ktx.loadUrl
import com.gw_reoqoo.lib_utils.ktx.setSingleClickListener
import com.gw_reoqoo.lib_widget.adapter.AbsDiffBDAdapter

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/9/4 0:37
 * Description: VisitorsAdapter
 */
class VisitorsAdapter : AbsDiffBDAdapter<Binding, Guest>() {
    override val layoutId: Int get() = R.layout.dev_share_visitor_item
    
    /**
     * 点击事件
     */
    private var onItemClick: ((Guest) -> Unit)? = null
    
    /**
     * 点击事件
     */
    fun onItemClick(onItemClick: ((Guest) -> Unit)) {
        this.onItemClick = onItemClick
    }
    
    override fun onCreateViewHolder(binding: Binding) {
        binding.btnVisitorDel.setSingleClickListener { v ->
            val tag = v.tag
            if (tag is Guest) {
                onItemClick?.invoke(tag)
            }
        }
    }
    
    override fun onBindViewHolder(binding: Binding, position: Int) {
        val guest = getItemData(position)
        binding.root.tag = guest
        binding.btnVisitorDel.tag = guest
        binding.tvVisitorName.text = guest.remarkName ?: guest.guestAccount
        binding.tvVisitorAccount.text = guest.guestAccount
        binding.ivAvatar.loadUrl(
            guest.headUrl,
            placeHolder = RR.drawable.gw_reoqoo_icon_default_avatar,
            error = RR.drawable.gw_reoqoo_icon_default_avatar
        )
        binding.splitLine.isVisible = position < itemCount - 1
    }
}