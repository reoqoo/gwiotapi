package com.gw_reoqoo.component_device_share.ui.share_to_user.adapter

import com.gw_reoqoo.component_device_share.R
import com.gw_reoqoo.lib_http.entities.GuestInfoContent
import com.gw_reoqoo.lib_utils.ktx.setSingleClickListener
import com.gw_reoqoo.component_device_share.databinding.DevShareItemUserListDialogBinding as Binding
import com.gw_reoqoo.lib_widget.adapter.AbsDiffBDAdapter

/**
 * @Description: - 用户列表的适配器
 * @Author: XIAOLEI
 * @Date: 2023/9/21
 */
class AccountListAdapter : AbsDiffBDAdapter<Binding, GuestInfoContent>() {
    override val layoutId: Int get() = R.layout.dev_share_item_user_list_dialog
    
    private var onItemClick: ((GuestInfoContent) -> Unit)? = null
    
    fun onItemClick(onItemClick: ((GuestInfoContent) -> Unit)) {
        this.onItemClick = onItemClick
    }
    
    override fun onCreateViewHolder(binding: Binding) {
        binding.root.setSingleClickListener { v ->
            val tag = v.tag
            if (tag is GuestInfoContent) {
                onItemClick?.invoke(tag)
            }
        }
    }
    
    override fun onBindViewHolder(binding: Binding, position: Int) {
        val content = getItemData(position)
        binding.root.tag = content
        binding.tvUserName.text = content.remarkName
        binding.tvAreaAndPhone.text = "（${content.account}）"
    }
}