package com.gw_reoqoo.component_device_share.ui.device_list.adapter

import com.gw_reoqoo.component_device_share.R
import com.gw_reoqoo.component_device_share.entities.DeviceWrapper
import com.gw.cp_config.api.IAppConfigApi
import com.gw_reoqoo.lib_utils.ktx.loadUrl
import com.gw_reoqoo.component_device_share.databinding.DevShareItemDeviceListBinding as Binding
import com.gw_reoqoo.lib_utils.ktx.setSingleClickListener
import com.gw_reoqoo.lib_widget.adapter.AbsDiffBDAdapter

/**
 * @Description: - 设备列表的适配器
 * @Author: XIAOLEI
 * @Date: 2023/8/10
 */
class DeviceListAdapter(
    private val iAppConfigApi: IAppConfigApi
) : AbsDiffBDAdapter<Binding, DeviceWrapper>() {
    override val layoutId: Int get() = R.layout.dev_share_item_device_list
    private var onItemClick: ((wrapper: DeviceWrapper, index: Int) -> Unit)? = null
    override fun onCreateViewHolder(binding: Binding) {
        binding.root.setSingleClickListener { v ->
            val tag = v.tag
            if (tag is DeviceWrapper) {
                val index = indexOf(tag)
                onItemClick?.invoke(tag, index)
            }
        }
    }

    override fun onBindViewHolder(binding: Binding, position: Int) {
        val wrapper = getItemData(position)
        val device = wrapper.device
        binding.ivDevImg.loadUrl(iAppConfigApi.getProductImgUrl(device.productId))
        binding.root.tag = wrapper
        binding.tvDevName.text = device.remarkName
        binding.checkbox.isChecked = wrapper.checked
    }

    override fun areIdSame(old: DeviceWrapper, new: DeviceWrapper): Boolean {
        return old.device.deviceId == new.device.deviceId
    }

    override fun areAllSame(old: DeviceWrapper, new: DeviceWrapper): Boolean {
        return old == new
    }

    /**
     * item点击事件
     */
    fun onItemClick(block: ((wrapper: DeviceWrapper, index: Int) -> Unit)) {
        this.onItemClick = block
    }
}