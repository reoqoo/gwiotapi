package com.gw_reoqoo.component_device_share.ui.share_to_user.dialog

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.gw_reoqoo.component_device_share.ui.share_to_user.adapter.AccountListAdapter
import com.gw.cp_config.api.AppChannelName
import com.gw.cp_config.api.IAppParamApi
import com.gw_reoqoo.lib_http.entities.GuestInfoContent
import com.gw_reoqoo.lib_utils.ktx.setSingleClickListener
import javax.inject.Inject
import com.gw_reoqoo.component_device_share.databinding.DevShareDialogUserListBinding as Binding
import com.gw_reoqoo.resource.R as RR

/**
 * @Description: - 查询账号列表的弹窗
 * @Author: XIAOLEI
 * @Date: 2023/9/21
 */
class AccountListDialog(
    context: Context,
    private val accounts: List<GuestInfoContent>,
    private val onItemClick: (GuestInfoContent) -> Unit
) : Dialog(context, RR.style.commonDialog) {
    private val binding = Binding.inflate(layoutInflater)
    private val accountListAdapter = AccountListAdapter()

    @Inject
    lateinit var appParamApi: IAppParamApi

    init {
        setContentView(binding.root)
        val layoutParams = WindowManager.LayoutParams()
        window?.attributes?.let(layoutParams::copyFrom)
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        layoutParams.gravity = Gravity.BOTTOM
        layoutParams.dimAmount = 0.3f
        this.setCancelable(false)
        window?.attributes = layoutParams
        window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        binding.initView()
        binding.initData()
    }
    
    private fun Binding.initView() {
        rvAccountList.run {
            layoutManager = LinearLayoutManager(context)
            adapter = accountListAdapter
            accountListAdapter.onItemClick {
                dismiss()
                onItemClick.invoke(it)
            }
        }
        tvCancel.setSingleClickListener { 
            dismiss()
        }
        if (AppChannelName.isXiaotunApp(appParamApi.getAppName())) {
            tvCancel.setTextColor(context.resources.getColor(RR.color.color_000000_90 ))
        }
    }
    
    private fun Binding.initData() {
        accountListAdapter.updateData(accounts)
    }
}