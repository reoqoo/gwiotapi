package com.gw.cp_msg.ui.activity.msg_dev_upgrade.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.gw.cp_msg.R
import com.gw.cp_msg.entity.http.MsgDetailEntity
import com.jwkj.base_utils.time.GwTimeUtils

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/11/3 14:04
 * Description: MsgDevUpgradeAdapter
 */
class MsgDevUpgradeAdapter(resId: Int, msgInfoList: MutableList<MsgDetailEntity>) :
    BaseQuickAdapter<MsgDetailEntity, BaseViewHolder>(resId, msgInfoList) {

    override fun convert(holder: BaseViewHolder, item: MsgDetailEntity) {
        holder.setText(R.id.tv_msg_info_title, item.title)
        holder.setText(R.id.tv_msg_info_body, item.summary)
        item.msgTime.run {
            val timeMillis = this * 1000L
            when {
                GwTimeUtils.isToday(timeMillis) -> {
                    holder.setText(R.id.tv_msg_info_time, buildString {
                        append(context.getString(com.gw_reoqoo.resource.R.string.AA0416))
                        append(GwTimeUtils.dateFormatTime(timeMillis, "HH:mm"))
                    })
                }

                GwTimeUtils.isYesterday(timeMillis) -> {
                    holder.setText(R.id.tv_msg_info_time, buildString {
                        append(context.getString(com.gw_reoqoo.resource.R.string.AA0417))
                        append(GwTimeUtils.dateFormatTime(timeMillis, "HH:mm"))
                    })
                }

                GwTimeUtils.isThisYear(timeMillis) -> {
                    holder.setText(
                        R.id.tv_msg_info_time,
                        GwTimeUtils.dateFormatTime(timeMillis, "MM/dd HH:mm")
                    )
                }

                else -> {
                    holder.setText(
                        R.id.tv_msg_info_time,
                        GwTimeUtils.dateFormatTime(timeMillis, "yyyy/MM/dd HH:mm")
                    )
                }
            }
        }
    }
}