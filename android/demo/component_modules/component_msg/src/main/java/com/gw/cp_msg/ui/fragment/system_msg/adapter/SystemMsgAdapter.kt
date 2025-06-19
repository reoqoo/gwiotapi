package com.gw.cp_msg.ui.fragment.system_msg.adapter

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.gw.cp_msg.R
import com.gw.cp_msg.entity.http.MsgDetailEntity
import com.jwkj.base_utils.time.GwTimeUtils
import com.jwkj.base_utils.ui.DensityUtil
import com.gw_reoqoo.resource.R as RR

/**
 *@message   SystemMsgAdapter
 *@user      zouhuihai
 *@date      2022/8/5
 */
class SystemMsgAdapter(resId: Int, msgs: MutableList<MsgDetailEntity>) :
    BaseQuickAdapter<MsgDetailEntity, BaseViewHolder>(resId, msgs) {

    companion object {
        private const val TAG = "SystemMsgAdapter"
    }

    override fun convert(holder: BaseViewHolder, item: MsgDetailEntity) {
        setMsgIcon(
            holder.getView(R.id.iv_msg_icon),
            holder.getView(R.id.tv_msg_unread_count),
            holder.getView(R.id.view_oval),
            item.tag,
            item.unreadCnt
        )
        holder.setText(R.id.tv_msg_title, item.title)
        holder.setText(R.id.tv_msg_summary, item.summary)
        setTime(item, holder)
    }

    private fun setMsgIcon(
        iconImg: AppCompatImageView?,
        tvUnRead: AppCompatTextView?,
        viewOval: View?,
        tag: String?,
        unReadCount: Int?
    ) {
        when (tag) {
            MsgDetailEntity.TAG_MSG_CENTER_COUPON_REMIND -> {
                iconImg?.setImageResource(R.drawable.icon_coupon_msg)
                setUnReadViewVisible(tvUnRead, viewOval, unReadCount)
            }

            MsgDetailEntity.TAG_MSG_CENTER_VSS -> {
                iconImg?.setImageResource(R.drawable.icon_vas_msg)
                setUnReadViewVisible(tvUnRead, viewOval, unReadCount)
            }

            MsgDetailEntity.TAG_MSG_CENTER_FEEDBACK -> {
                iconImg?.setImageResource(R.drawable.icon_feedback_msg)
                setUnReadViewVisible(tvUnRead, viewOval, unReadCount)
            }

            MsgDetailEntity.TAG_MSG_CENTER_SHARE_GUEST -> {
                iconImg?.setImageResource(R.drawable.icon_dev_share)
                setUnReadViewVisible(tvUnRead, viewOval, unReadCount)
            }

            MsgDetailEntity.TAG_MSG_CENTER_APP_UPGRADE -> {
                iconImg?.setImageResource(R.drawable.icon_app_update_msg)
                setUnReadViewVisible(tvUnRead, viewOval, unReadCount)
            }

            MsgDetailEntity.TAG_MSG_CENTER_FIRMWARE_UPDATE -> {
                iconImg?.setImageResource(R.drawable.icon_device_update_msg)
                setUnReadViewVisible(tvUnRead, viewOval, unReadCount)
            }

            // TODO 这里的消息类型是后面会需要用到的，所以我暂时就没有删除
//            MsgListEntity.TAG_MSG_CENTER_FCS -> {
//                iconImg?.setImageResource(R.drawable.icon_4g_msg)
//                setUnReadTvVisible(tvUnRead, viewOval, unReadCount)
//            }
//
//            MsgListEntity.TAG_MSG_CENTER_CUSTOMER_SRV -> {
//                iconImg?.setImageResource(R.drawable.icon_customer_msg)
//                setUnReadTvVisible(tvUnRead, viewOval, unReadCount)
//            }
//
//            MsgListEntity.TAG_MSG_CENTER_COINS_REMIND -> {
//                iconImg?.setImageResource(R.drawable.icon_coins_msg)
//                setUnReadViewVisible(tvUnRead, viewOval, unReadCount)
//            }
//
//            MsgListEntity.TAG_MSG_CENTER_ALARM_EVENT -> {
//                GwellLogUtils.i(TAG, "TAG_MSG_CENTER_ALARM_EVENT unReadCount:$unReadCount")
//                iconImg?.setImageResource(R.drawable.icon_device_msg)
//                setUnReadViewVisible(tvUnRead, viewOval, unReadCount)
//            }
//
//            else -> {
//                iconImg?.setImageResource(R.drawable.icon_defeault_msg)
//                setUnReadViewVisible(tvUnRead, viewOval, unReadCount)
//            }
        }
    }

    private fun setTime(item: MsgDetailEntity?, helper: BaseViewHolder?) {

        item?.msgTime?.run {
            val timeMillis = this * 1000L
            when {
                GwTimeUtils.isToday(timeMillis) -> {
                    helper?.setText(R.id.tv_msg_time, buildString {
                        append(context.resources.getString(RR.string.AA0416))
                        append(GwTimeUtils.dateFormatTime(timeMillis, "HH:mm"))
                    })
                }

                GwTimeUtils.isYesterday(timeMillis) -> {
                    helper?.setText(R.id.tv_msg_time, buildString {
                        append(context.resources.getString(RR.string.AA0417))
                        append(GwTimeUtils.dateFormatTime(timeMillis, "HH:mm"))
                    })
                }

                GwTimeUtils.isThisYear(timeMillis) -> {
                    helper?.setText(
                        R.id.tv_msg_time,
                        GwTimeUtils.dateFormatTime(timeMillis, "yyyy-MM-dd HH:mm")
                    )
                }

                else -> {
                    helper?.setText(
                        R.id.tv_msg_time,
                        GwTimeUtils.dateFormatTime(timeMillis, "yyyy-MM-dd HH:mm")
                    )
                }
            }
        }
    }

    /**
     * 展示文字未读消息提示
     *
     * @param tvUnRead      文字消息提示
     * @param viewOval      红点提示
     * @param unReadCount   未读消息
     */
    private fun setUnReadTvVisible(
        tvUnRead: AppCompatTextView?,
        viewOval: View?,
        unReadCount: Int?
    ) {
        viewOval?.visibility = View.GONE
        unReadCount?.run {
            tvUnRead?.visibility = if (this > 0) View.VISIBLE else View.GONE
            val layoutParams = tvUnRead?.layoutParams
            if (this < 10) {
                layoutParams?.width = DensityUtil.dip2px(context, 19)
                layoutParams?.height = DensityUtil.dip2px(context, 19)
                tvUnRead?.setPadding(0, 0, 0, 0)
                tvUnRead?.layoutParams = layoutParams
                tvUnRead?.setBackgroundResource(R.drawable.oval_11_fa2a2d)
            } else {
                layoutParams?.width = ViewGroup.LayoutParams.WRAP_CONTENT
                layoutParams?.height = ViewGroup.LayoutParams.WRAP_CONTENT
                tvUnRead?.setPadding(
                    DensityUtil.dip2px(context, 6),
                    0,
                    DensityUtil.dip2px(context, 6),
                    0
                )
                tvUnRead?.layoutParams = layoutParams
                tvUnRead?.setBackgroundResource(R.drawable.shape_9_eb5a51)
            }
            tvUnRead?.text = if (this < 100) this.toString() else "99+"
        } ?: let {
            tvUnRead?.visibility = View.GONE
        }

    }

    /**
     * 展示红点未读消息提示
     *
     * @param tvUnRead      文字消息提示
     * @param viewOval      红点提示
     * @param unReadCount   未读消息
     */
    private fun setUnReadViewVisible(
        tvUnRead: AppCompatTextView?,
        viewOval: View?,
        unReadCount: Int?
    ) {
        tvUnRead?.visibility = View.GONE
        if (unReadCount == null) {
            viewOval?.visibility = View.GONE
        } else {
            viewOval?.visibility = if (unReadCount > 0) View.VISIBLE else View.GONE
        }
    }

}