package com.gw_reoqoo.cp_account.kits

import android.content.Context
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import androidx.core.content.ContextCompat
import com.gwell.loglibs.GwellLogUtils
import com.gw_reoqoo.resource.R as RR

/**
 * 生成底部用户协议文案工具类
 *
 */
object PrivatePolicyStringKit {

    private const val TAG = "PrivatePolicyStringKit"

    /**
     * 获取底部用户协议文案
     * @param context    上下文
     * @param dialogTxt  是否弹窗文案
     * @return 用户协议文案
     */
    fun getAgreement(
        context: Context,
        dialogTxt: Boolean,
        showUserProtocol: (() -> Unit),
        showPrivacyProtocol: (() -> Unit)
    ): SpannableStringBuilder {
        val gwUserAgreement = context.getString(RR.string.AA0570)
        val gwPolicy = context.getString(RR.string.AA0408)
        val normalStr = context.getString(
            RR.string.AA0574,
            gwUserAgreement,
            gwPolicy
        )
        val gwUserAgreementIndex = normalStr.lastIndexOf(gwUserAgreement, ignoreCase = true)
        val gwPolicyIndex = normalStr.lastIndexOf(gwPolicy, ignoreCase = true)
        GwellLogUtils.i(
            TAG,
            "gwUserAgreementIndex $gwUserAgreementIndex, gwPolicyIndex $gwPolicyIndex"
        )

        val builder = SpannableStringBuilder(normalStr)
        if (gwUserAgreementIndex != -1) {
            builder.setSpan(
                object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        GwellLogUtils.i(TAG, "gotoUserProtocolPage")
                        showUserProtocol.invoke()
                    }

                    override fun updateDrawState(ds: TextPaint) {
                        // 弹窗文案与界面文案颜色不一样，且弹窗文案需要加粗
                        ds.color = ContextCompat.getColor(context, RR.color.color_4a68a6)
                        ds.isFakeBoldText = dialogTxt
                        ds.isUnderlineText = false
                    }

                }, gwUserAgreementIndex,
                gwUserAgreementIndex + gwUserAgreement.length,
                SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        if (gwPolicyIndex != -1) {
            builder.setSpan(
                object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        GwellLogUtils.i(TAG, "gotoPrivacyPolicyPage")
                        showPrivacyProtocol.invoke()
                    }

                    override fun updateDrawState(ds: TextPaint) {
                        // 弹窗文案与界面文案颜色不一样，且弹窗文案需要加粗
                        ds.color = ContextCompat.getColor(context, RR.color.color_4a68a6)
                        ds.isFakeBoldText = dialogTxt
                        ds.isUnderlineText = false
                    }

                }, gwPolicyIndex,
                gwPolicyIndex + gwPolicy.length,
                SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        return builder
    }

    /**
     * 获取隐私协议的Spannable
     *
     * @param context Context 上下文
     * @param dialogTxt Boolean 是否弹框文字
     * @param showUserProtocol Function0<Unit> 用户协议的回调
     * @param showPrivacyProtocol Function0<Unit> 隐私协议的回调
     * @return SpannableStringBuilder
     */
    fun showProtocolDetail(
        context: Context,
        dialogTxt: Boolean,
        showUserProtocol: ((String?) -> Unit),
        showPrivacyProtocol: ((String?) -> Unit)
    ): SpannableStringBuilder {
        val gwUserAgreement = context.getString(RR.string.AA0570)
        val gwPolicy = context.getString(RR.string.AA0408)
        val normalStr =
            String.format(context.getString(RR.string.AA0046), gwUserAgreement, gwPolicy)
        val gwUserAgreementIndex = normalStr.lastIndexOf(gwUserAgreement, ignoreCase = true)
        val gwPolicyIndex = normalStr.lastIndexOf(gwPolicy, ignoreCase = true)
        GwellLogUtils.i(
            TAG,
            "gwUserAgreementIndex $gwUserAgreementIndex, gwPolicyIndex $gwPolicyIndex"
        )

        val builder = SpannableStringBuilder(normalStr)
        if (gwUserAgreementIndex != -1) {
            builder.setSpan(
                object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        GwellLogUtils.i(TAG, "gotoUserProtocolPage")
                        showUserProtocol.invoke(context.getString(RR.string.AA0044))
                    }

                    override fun updateDrawState(ds: TextPaint) {
                        ds.color = ContextCompat.getColor(context, RR.color.color_4a68a6)
                        ds.isFakeBoldText = dialogTxt
                        ds.isUnderlineText = false
                    }

                }, gwUserAgreementIndex,
                gwUserAgreementIndex + gwUserAgreement.length,
                SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        if (gwPolicyIndex != -1) {
            builder.setSpan(
                object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        GwellLogUtils.i(TAG, "gotoPrivacyPolicyPage")
                        showPrivacyProtocol.invoke(context.getString(RR.string.AA0408))
                    }

                    override fun updateDrawState(ds: TextPaint) {
                        ds.color = ContextCompat.getColor(context, RR.color.color_4a68a6)
                        ds.isFakeBoldText = dialogTxt
                        ds.isUnderlineText = false
                    }

                }, gwPolicyIndex,
                gwPolicyIndex + gwPolicy.length,
                SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        return builder
    }

}