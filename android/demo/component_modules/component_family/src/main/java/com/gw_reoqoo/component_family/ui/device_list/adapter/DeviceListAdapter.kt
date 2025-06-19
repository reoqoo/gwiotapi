package com.gw_reoqoo.component_family.ui.device_list.adapter

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.view.View
import androidx.core.view.isVisible
import com.gw_reoqoo.component_family.R
import com.gw_reoqoo.component_family.databinding.FamilyItemDeviceListBinding
import com.gw.cp_config.api.IAppConfigApi
import com.gw_reoqoo.component_family.api.interfaces.FamilyModeApi
import com.gw_reoqoo.component_iotapi_plugin.opt.api.IGWIotOpt
import com.gw_reoqoo.component_iotapi_plugin.opt.api.PluginSolution
import com.gw_reoqoo.lib_room.device.DeviceInfo
import com.gw_reoqoo.lib_room.ktx.isMaster
import com.gw_reoqoo.lib_room.ktx.isOffline
import com.gw_reoqoo.lib_room.ktx.isOnline
import com.gw_reoqoo.lib_utils.ktx.draw
import com.gw_reoqoo.lib_utils.ktx.loadUrl
import com.gw_reoqoo.lib_utils.ktx.setSingleClickListener
import com.gw_reoqoo.lib_utils.ktx.visible
import com.gw_reoqoo.lib_widget.adapter.AbsDiffBDAdapter
import com.gwell.loglibs.GwellLogUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import com.gw_reoqoo.resource.R as RS

/**
 * @Description: - 设备列表适配器
 * @Author: XIAOLEI
 * @Date: 2023/8/1
 */
class DeviceListAdapter(
    private var configApi: IAppConfigApi,
    private val familyModeApi: FamilyModeApi,
    private var igwIotOpt: IGWIotOpt,
    private val scope: CoroutineScope
) : AbsDiffBDAdapter<FamilyItemDeviceListBinding, DeviceInfo>() {

    companion object {
        private const val TAG = "DeviceListAdapter"
    }

    /**
     * 点击item的事件
     */
    private var onItemClick: ((DeviceInfo) -> Unit)? = null

    /**
     * item长按事件
     */
    private var onItemLongClick: ((v: View, info: DeviceInfo) -> Unit)? = null

    /**
     * 点击关机/开机事件
     */
    private var onTurnOnOrOffClick: ((DeviceInfo, View, onClickConfirm: () -> Unit) -> Unit)? = null

    /**
     * 根据ID查询云服务是否开启
     */
    private var checkCloudOn: ((deviceId: String) -> Boolean?)? = null

    /**
     * 根据ID查询4G服务是否开启
     */
    private var check4gOn: ((deviceId: String) -> Boolean?)? = null

    /**
     * 设置Item长按
     */
    fun setOnItemLongClick(onItemLongClick: ((v: View, info: DeviceInfo) -> Unit)) {
        this.onItemLongClick = onItemLongClick
    }

    /**
     * 设置Item点击事件
     */
    fun setOnItemClick(onItemClick: ((DeviceInfo) -> Unit)) {
        this.onItemClick = onItemClick
    }

    /**
     * 设置点击开机/关机按钮事件
     */
    fun setOnTurnOnOrOffClick(onTurnOnOrOffClick: ((DeviceInfo, View, onClickConfirm: () -> Unit) -> Unit)) {
        this.onTurnOnOrOffClick = onTurnOnOrOffClick
    }

    /**
     * 设置根据设备ID，查询云服务是否开启的函数
     */
    fun setCheckCloudOn(block: (deviceId: String) -> Boolean?) {
        this.checkCloudOn = block
    }

    /**
     * 设置根据设备ID，查询4g服务是否开启的函数
     */
    fun setCheck4gOn(block: (deviceId: String) -> Boolean?) {
        this.check4gOn = block
    }

    override val layoutId: Int get() = R.layout.family_item_device_list

    override fun onCreateViewHolder(binding: FamilyItemDeviceListBinding) {
        // 点击设备
        binding.root.setSingleClickListener { v ->
            val tag = v.tag
            if (tag is DeviceInfo) {
                this.onItemClick?.invoke(tag)
            }
        }
        // 设备长按事件
        binding.root.setOnLongClickListener { v ->
            val tag = v.tag
            if (tag is DeviceInfo) {
                this.onItemLongClick?.invoke(v, tag)
            }
            true
        }
        // 点击关机/开机
        binding.btTurnOffOrOn.setSingleClickListener { view ->
            val tag = binding.root.tag
            if (tag is DeviceInfo) {
                if (tag.online == 0) {
                    // 设备离线状态，不可点击
                    return@setSingleClickListener
                }
                val onClickConfirm: () -> Unit = {
                    view.playAnimation()
                }
                this.onTurnOnOrOffClick?.invoke(tag, view, onClickConfirm)
            }
        }
        binding.btTurnOffOrOn.addAnimatorListener(object : AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                binding.btTurnOffOrOn.isEnabled = false
            }

            override fun onAnimationEnd(animation: Animator) {
                binding.btTurnOffOrOn.isEnabled = true
            }

            override fun onAnimationCancel(animation: Animator) {
                binding.btTurnOffOrOn.isEnabled = true
            }

            override fun onAnimationRepeat(animation: Animator) = Unit
        })
        binding.ivProduct.post {
            GwellLogUtils.i(TAG, "onCreateViewHolder ivProduct: width=${binding.ivProduct.width}, height=${binding.ivProduct.height}")
        }
    }

    override fun onBindViewHolder(binding: FamilyItemDeviceListBinding, position: Int) {
        val info = this.getItemData(position)
        binding.root.tag = info
        binding.tvDevName.text = info.remarkName
        binding.btTurnOffOrOn.cancelAnimation()
        val pid = info.productId ?: ""
        when {
            info.isOnline -> when (info.powerOn) {
                true -> {
                    binding.tvOnline.setText(RS.string.AA0053)
                    binding.tvOnline.draw(start = R.drawable.family_shape_online_point_online)
                    binding.btTurnOffOrOn.setAnimation(R.raw.family_dev_turnon)
                }

                false -> {
                    binding.tvOnline.setText(RS.string.AA0502)
                    binding.tvOnline.draw(start = R.drawable.family_shape_online_point_power_off)
                    binding.btTurnOffOrOn.setAnimation(R.raw.family_dev_turnoff)
                }

                else -> {

                }
            }

            info.isOffline -> {
                binding.tvOnline.setText(RS.string.AA0054)
                binding.tvOnline.draw(start = R.drawable.family_shape_online_point_offline)
                binding.btTurnOffOrOn.setImageResource(R.drawable.family_icon_btn_status_offline)
            }
        }

        binding.tvShare.isVisible = !info.isMaster
        binding.btTurnOffOrOn.isVisible = info.isMaster
        binding.btTurnOffOrOn.progress = 0f
        if (check4gOn?.invoke(info.deviceId) == null) {
            binding.iv4g.visible(false)
            binding.vSplit.visible(false)
        } else {
            binding.iv4g.visible(true)
            binding.vSplit.visible(true)
            val is4gOn = check4gOn?.invoke(info.deviceId) ?: false
            binding.iv4g.setImageResource(
                if (is4gOn) {
                    R.drawable.family_icon_4g_on
                } else {
                    R.drawable.family_icon_4g_off
                }
            )
        }
        if (checkCloudOn?.invoke(info.deviceId) == null) {
            binding.ivCloud.visible(false)
            binding.vSplitLine.visible(false)
        } else {
            binding.ivCloud.visible(true)
            binding.vSplitLine.visible(true)
            val isCloudOn = checkCloudOn?.invoke(info.deviceId) ?: false
            binding.ivCloud.setImageResource(
                if (isCloudOn) {
                    R.drawable.family_icon_cloud_on
                } else {
                    R.drawable.family_icon_cloud_off
                }
            )
        }
        val productImg = configApi.getProductImgUrl(info.productId)
        binding.ivProduct.loadUrl(productImg)

        // 判断是否支持开关机,如果不支持，则隐藏按钮
        if (!familyModeApi.isReoqooDevice(pid)) {
            scope.launch {
                binding.btTurnOffOrOn.isVisible =
                    igwIotOpt.supportPowerOnOff(info.deviceId, PluginSolution.YOOSEE)
            }
        }
    }


    override fun areIdSame(old: DeviceInfo, new: DeviceInfo) = old.deviceId == new.deviceId

    override fun areAllSame(old: DeviceInfo, new: DeviceInfo): Boolean {
        return old.deviceId == new.deviceId &&
                old.remarkName == new.remarkName &&
                old.online == new.online &&
                old.powerOn == new.powerOn
    }
}