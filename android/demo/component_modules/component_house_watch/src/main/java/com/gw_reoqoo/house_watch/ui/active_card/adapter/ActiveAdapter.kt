package com.gw_reoqoo.house_watch.ui.active_card.adapter

import androidx.databinding.ViewDataBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.gw_reoqoo.component_house_watch.R
import com.gw.component_push.api.interfaces.AlarmEventType
import com.gw_reoqoo.house_watch.entities.ActiveType
import com.gw_reoqoo.lib_http.entities.ActiveBean
import com.gw_reoqoo.lib_utils.ktx.bitAt
import com.gw_reoqoo.lib_utils.ktx.dp
import com.gw_reoqoo.lib_utils.ktx.visible
import java.util.Calendar
import java.util.LinkedList
import com.gw_reoqoo.component_house_watch.databinding.HouseWatchItemActiveListBinding as ActiveBinding

import com.gw_reoqoo.resource.R as RR

/**
 * @Description: - 活动列表适配器
 * @Author: XIAOLEI
 * @Date: 2023/8/22
 *
 * @param list 列表数据
 * @param getDeviceName 根据设备ID获取对应的名称
 * @param showConfigDialog 显示配置弹窗
 * @param onClickActiveItem 点击某个事件item
 */
class ActiveAdapter(
    private val list: LinkedList<Any> = LinkedList(),
    private val getDeviceName: (devId: String) -> String,
    private val showConfigDialog: () -> Unit,
    private val onClickActiveItem: (ActiveBean) -> Unit
) : ActiveBaseAdapter(list, showConfigDialog, onClickActiveItem) {

    /**
     * 绑定活动视图
     */
    override fun onBindActiveViewHolder(binding: ViewDataBinding, position: Int, bean: ActiveBean) {
        val mBinding = binding as ActiveBinding
        mBinding.root.tag = bean
        val context = mBinding.root.context
        val calendar = Calendar.getInstance().apply {
            timeInMillis = bean.startTime * 1000L
        }
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val activeTypes = com.gw_reoqoo.house_watch.entities.ActiveType.values().filter {
            bean.alarmType.bitAt(it.bitOfIndex) == 1L
        }.take(4)
        val iconsAdapter = mBinding.rvActiveIcons.adapter
        if (iconsAdapter is ActiveIconsAdapter) {
            iconsAdapter.updateData(activeTypes, false)
        }

        mBinding.tvTime.text = "%02d:%02d".format(hour, minute)
        mBinding.tvDevName.text = getDeviceName("${bean.devId}")
        mBinding.tvDesc.text = when (activeTypes.size) {
            1 -> context.getString(RR.string.AA0582, context.getString(activeTypes.first().descRes))
            else -> null
        }

        val isVideo = bean.alarmType.bitAt(AlarmEventType.ALARM_VIDEO_BIT) == 1L
        if (isVideo) {
            val duration = bean.duration
            val durationMin = duration / 60
            val durationSec = duration % 60
            mBinding.tvVideoDuration.text = "%02d:%02d".format(durationMin, durationSec)
            mBinding.tvVideoDuration.visible(true)
        } else {
            mBinding.tvVideoDuration.visible(true)
        }
        Glide.with(mBinding.ivImage)
            .load(bean.imgUrl ?: bean.thumbUrlSuffix)
            .placeholder(R.drawable.house_watch_img_dev_empty)
            .error(R.drawable.house_watch_img_dev_empty)
            .transform(CenterCrop(), RoundedCorners(4.dp))
            .into(mBinding.ivImage)
    }
}