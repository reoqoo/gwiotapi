package com.gw_reoqoo.component_device_share.ui.share_manager.adapter

import androidx.core.view.isVisible
import com.gw_reoqoo.component_device_share.R
import com.gw_reoqoo.component_device_share.databinding.DevShareManagerItemContentBinding as Binding
import com.gw_reoqoo.component_family.api.interfaces.IDevice
import com.gw.cp_config.api.IAppConfigApi
import com.gw_reoqoo.lib_utils.ktx.loadUrl
import com.gw_reoqoo.lib_utils.ktx.setSingleClickListener
import com.gw_reoqoo.lib_widget.adapter.AbsDiffBDAdapter
import com.gwell.loglibs.GwellLogUtils

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/9/1 14:56
 * Description: DevShareAdapter
 */
class DevShareAdapter(
    private var configApi: IAppConfigApi
) : AbsDiffBDAdapter<Binding, IDevice>() {
    
    companion object {
        private const val TAG = "DevShareAdapter"
    }
    
    /**
     * 点击事件
     */
    private var onItemClick: ((IDevice) -> Unit)? = null
    
    override val layoutId: Int get() = R.layout.dev_share_manager_item_content
    
    /**
     * 设置点击事件
     */
    fun setOnItemClick(onItemClick: ((IDevice) -> Unit)?) {
        this.onItemClick = onItemClick
    }
    
    override fun onCreateViewHolder(binding: Binding) {
        binding.root.setSingleClickListener { v ->
            val device = v.tag
            if (device is IDevice) {
                onItemClick?.invoke(device)
            }
        }
    }
    
    override fun onBindViewHolder(binding: Binding, position: Int) {
        val device = getItemData(position)
        binding.root.tag = device
        
        binding.tvDevName.text = device.remarkName
        binding.splitLine.isVisible = position < itemCount - 1
        val imgUrl = configApi.getDevConfig()?.get(device.productId)?.productImageURL
        GwellLogUtils.i(TAG, "onBindViewHolder-pid:${device.productId},imgUrl:$imgUrl")
        binding.ivDevIcon.loadUrl(
            imgUrl,
            placeHolder = R.drawable.dev_share_icon_device_holder_place,
            error = R.drawable.dev_share_icon_device_holder_place,
        )
    }
}