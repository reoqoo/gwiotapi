package com.gw.cp_msg.ui.activity.msg_info.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.gw.cp_msg.R
import com.gw.cp_msg.entity.http.MsgInfoListEntity
import com.jwkj.base_utils.time.GwTimeUtils
import com.gw_reoqoo.resource.R as RR

/**
 *@message   MsgInfoAdapter
 *@user      zouhuihai
 *@date      2022/8/9
 */
class MsgInfoAdapter(resId: Int, msgInfoList: MutableList<MsgInfoListEntity.MSGInfo>) :
    BaseQuickAdapter<MsgInfoListEntity.MSGInfo, BaseViewHolder>(resId, msgInfoList) {

    override fun convert(holder: BaseViewHolder, item: MsgInfoListEntity.MSGInfo) {
        holder.setText(R.id.tv_msg_info_title, item.title)
        holder.setText(R.id.tv_msg_info_body, item.body)
        item.time.run {
            val timeMillis = this * 1000
            when {
                GwTimeUtils.isToday(timeMillis) -> {
                    holder.setText(R.id.tv_msg_info_time, buildString {
                        append(context.getString(RR.string.AA0416))
                        append(GwTimeUtils.dateFormatTime(timeMillis, "HH:mm"))
                    })
                }

                GwTimeUtils.isYesterday(timeMillis) -> {
                    holder.setText(R.id.tv_msg_info_time, buildString {
                        append(context.getString(RR.string.AA0417))
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