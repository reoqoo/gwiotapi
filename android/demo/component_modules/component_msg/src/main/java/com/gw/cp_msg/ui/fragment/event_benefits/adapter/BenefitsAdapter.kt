package com.gw.cp_msg.ui.fragment.event_benefits.adapter

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.gw.cp_msg.R
import com.gw.cp_msg.entity.http.EventBenefitsEntity
import com.gw.cp_msg.entity.http.Notice
import com.gw_reoqoo.lib_utils.ktx.loadUrl
import com.gw_reoqoo.lib_widget.image.GwRoundImageView
import com.gwell.loglibs.GwellLogUtils
import com.jwkj.base_utils.time.GwTimeUtils
import com.jwkj.base_utils.ui.DensityUtil
import com.gw_reoqoo.resource.R as RR

/**
 *@message   ActiveMsgAdapter
 *@user      zouhuihai
 *@date      2022/8/8
 */
class BenefitsAdapter(resId: Int, datas: MutableList<Notice>) :
    BaseQuickAdapter<Notice, BaseViewHolder>(resId, datas) {

    companion object {
        private const val TAG = "BenefitsAdapter"
    }

    override fun convert(holder: BaseViewHolder, item: Notice) {
        GwellLogUtils.i(TAG, "Notice $item")
        val imageView = holder.getView<GwRoundImageView>(R.id.iv_event_image)
        imageView.loadUrl(item.picUrl)

        holder.setVisible(
            R.id.tv_event_expire,
            EventBenefitsEntity.STATUS_ACTIVE_EXPIRE == item.status
        )
        imageView.setMaskColorRes(if (EventBenefitsEntity.STATUS_ACTIVE_EXPIRE == item.status) RR.color.black_65 else RR.color.transparent)
        holder.setText(R.id.tv_event_expire_time, buildString {
            append(context.getString(RR.string.AA0602))
            item.expireTime.run {
                append(GwTimeUtils.dateFormatTime(this * 1000, GwTimeUtils.DEFAULT_DATE_FORMAT_TWO))
            }
        })
        val layoutParams: RecyclerView.LayoutParams =
            holder.getView<ConstraintLayout>(R.id.cl_item).layoutParams as RecyclerView.LayoutParams
        if (holder.layoutPosition > 0) {
            layoutParams.topMargin = DensityUtil.dip2px(context, 30)
        } else {
            layoutParams.topMargin = 0
        }
    }


}