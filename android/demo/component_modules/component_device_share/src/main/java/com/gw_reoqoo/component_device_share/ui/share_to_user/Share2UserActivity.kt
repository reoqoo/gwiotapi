package com.gw_reoqoo.component_device_share.ui.share_to_user

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gw_reoqoo.component_device_share.R
import com.gw_reoqoo.component_device_share.api.DevShareApi.Companion.KEY_PARAM_DEVICE
import com.gw_reoqoo.component_device_share.databinding.DevShareFragmentShare2UserBinding
import com.gw_reoqoo.component_device_share.sa.SaEvent
import com.gw_reoqoo.component_device_share.sa.SaEvent.Attr.DEVICE_ID
import com.gw_reoqoo.component_device_share.sa.SaEvent.Attr.PAGE_TITLE
import com.gw_reoqoo.component_device_share.sa.SaEvent.Attr.SHARE_MODE
import com.gw_reoqoo.component_device_share.ui.share_to_user.adapter.RecentlyShareAdapter
import com.gw_reoqoo.component_device_share.ui.share_to_user.dialog.AccountListDialog
import com.gw_reoqoo.component_device_share.ui.share_to_user.vm.Share2UserVM
import com.gw_reoqoo.component_family.api.interfaces.IDevice
import com.gw_reoqoo.lib_base_architecture.view.ABaseMVVMDBActivity
import com.gw_reoqoo.lib_http.ResponseNotSuccessException
import com.gw_reoqoo.lib_http.error.ResponseCode
import com.gw_reoqoo.lib_router.ReoqooRouterPath
import com.gw_reoqoo.lib_router.navigation
import com.gw_reoqoo.lib_utils.ktx.launch
import com.gw_reoqoo.lib_utils.ktx.setSingleClickListener
import com.gw_reoqoo.lib_widget.dialog.comm_dialog.entity.CommDialogAction
import com.gw_reoqoo.lib_widget.dialog.comm_dialog.entity.TextContent
import com.gw_reoqoo.lib_widget.dialog.comm_dialog.ext.showCommDialog
import com.jwkj.base_statistics.sa.kits.SA
import com.jwkj.base_utils.str_utils.GwStringUtils
import com.tencentcs.iotvideo.http.interceptor.flow.HttpAction
import com.therouter.router.Autowired
import com.therouter.router.Route
import dagger.hilt.android.AndroidEntryPoint

import com.gw_reoqoo.resource.R as RR

/**
 * Author: yanzheng@gwell.cc
 * Time: 2024/8/7 11:47
 * Description: Share2UserActivity
 */
@Route(path = ReoqooRouterPath.DevShare.FRAGMENT_SHARE_2_USER_PATH)
@AndroidEntryPoint
class Share2UserActivity : ABaseMVVMDBActivity<DevShareFragmentShare2UserBinding, Share2UserVM>() {

    @Autowired
    lateinit var device: IDevice

    @Autowired
    lateinit var pageFrom: String

    override fun initView() {
        mViewBinding.layoutTitle.leftIcon.setOnClickListener { finish() }

        // 初始化最近分享列表
        mViewBinding.nearShareList.run {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = com.gw_reoqoo.component_device_share.ui.share_to_user.adapter.RecentlyShareAdapter()
                .apply {
                // item点击，直接把账号填充到输入框里
                // 然后拿到内容之后，再更新adapter的勾
                onItemClick { bean ->
                    mViewBinding.etAccount.setText(bean.guestAccount)
                    updateChecked(bean.guestId)
                    mViewBinding.btnShare.isEnabled = true
                }
            }
        }
        // 输入框内文字改变事件
        mViewBinding.etAccount.doAfterTextChanged { text: Editable? ->
            val userName = text?.toString() ?: ""
            val adapter = mViewBinding.nearShareList.adapter as? com.gw_reoqoo.component_device_share.ui.share_to_user.adapter.RecentlyShareAdapter?
            adapter?.clearCheck()
            mViewBinding.btnShare.isEnabled = userName.isNotEmpty()
        }
        // 点击分享
        mViewBinding.btnShare.setSingleClickListener {
            val adapter = mViewBinding.nearShareList.adapter
            if (adapter is com.gw_reoqoo.component_device_share.ui.share_to_user.adapter.RecentlyShareAdapter) {
                val userName = mViewBinding.etAccount.text?.toString()
                val checkGuest = adapter.getCheckedGuest()
                if (checkGuest != null) {
                    confirmShare(
                        remarkName = checkGuest.remarkName ?: "",
                        account = checkGuest.guestAccount,
                        userId = checkGuest.guestId
                    )
                } else {
                    if (userName.isNullOrEmpty()) return@setSingleClickListener
                    mViewModel.queryGuestListInfoExits(device.deviceId, userName)
                }
            }
        }
        // 面对面分享
        mViewBinding.tvFaceToFace.setSingleClickListener {
            SA.track(
                SaEvent.RETURN_RESULT,
                mapOf(
                    DEVICE_ID to device.deviceId,
                    PAGE_TITLE to pageFrom,
                    SHARE_MODE to "面对面分享"
                )
            )
            ReoqooRouterPath
                .DevShare
                .ACTIVITY_QRCODE_SHARE_PATH
                .navigation(context = null, with = mapOf(KEY_PARAM_DEVICE to device))
        }
    }

    override fun initLiveData(viewModel: Share2UserVM, savedInstanceState: Bundle?) {
        super.initLiveData(viewModel, savedInstanceState)
        // 最近分享列表
        viewModel.recentlyShareUser.observe(this) { list ->
            val adapter = mViewBinding.nearShareList.adapter as? com.gw_reoqoo.component_device_share.ui.share_to_user.adapter.RecentlyShareAdapter?
            if (list.isEmpty()) {
                mViewBinding.nearShareList.setBackgroundColor(Color.TRANSPARENT)
            } else {
                mViewBinding.nearShareList.setBackgroundResource(com.gw_reoqoo.lib_widget.R.drawable.gw_reoqoo_widget_bg_card_12)
            }
            adapter?.updateData(list)
        }
        // 根据访客ID获取访客账号
        // viewModel.guestAccount.observe(this) { pair ->
        //     val guestId = pair?.first
        //     val account = pair?.second
        //     mViewBinding.etAccount.setText(account)
        //     val adapter = mViewBinding.nearShareList.adapter as? RecentlyShareAdapter?
        //     adapter?.updateChecked(guestId ?: "")
        // }
        // 点击分享按钮，检查账号存在，多个账号弹窗选择
        viewModel.guestListInfo.observe(this) { action ->
            when (action) {
                is HttpAction.Loading -> Unit
                is HttpAction.Fail -> {
                    val e = action.t
                    if (e is ResponseNotSuccessException) {
                        when (val respCode = ResponseCode.getRespCode(e.code)) {
                            ResponseCode.CODE_10905023 -> {
                                toast.show(com.gw_reoqoo.resource.R.string.AA0168)
                            }

                            else -> {
                                respCode?.msgRes?.let(toast::show)
                            }
                        }
                    }
                }

                is HttpAction.Success -> {
                    val accountList = action.data?.userList ?: emptyList()
                    when {
                        accountList.isEmpty() -> toast.show(com.gw_reoqoo.resource.R.string.AA0153)
                        accountList.size == 1 -> {
                            val guestInfo = accountList.first()
                            confirmShare(
                                remarkName = guestInfo.remarkName,
                                account = guestInfo.account,
                                userId = guestInfo.userId
                            )
                        }

                        else -> {
                            AccountListDialog(this, accountList) { guestInfo ->
                                confirmShare(
                                    remarkName = guestInfo.remarkName,
                                    account = guestInfo.account,
                                    userId = guestInfo.userId
                                )
                            }.show()
                        }
                    }
                }
            }
        }
    }

    override fun initData(savedInstanceState: Bundle?) {
        super.initData(savedInstanceState)
        mViewModel.loadRecentlyShareUser()
    }

    /**
     * 弹出确认分享的弹窗
     */
    private fun confirmShare(remarkName: String, account: String?, userId: String) {
        showCommDialog {
            content = TextContent(
                getString(RR.string.AA0154, "$remarkName($account)")
            )
            actions = listOf(
                CommDialogAction(getString(RR.string.AA0059)),
                CommDialogAction(getString(RR.string.AA0058), onClick = {
                    val shareMode =
                        if (GwStringUtils.isEmailValid(account)) "账号分享_邮箱" else "账号分享_手机号"
                    SA.track(
                        SaEvent.RETURN_RESULT,
                        mapOf(
                            DEVICE_ID to device.deviceId,
                            PAGE_TITLE to pageFrom,
                            SHARE_MODE to shareMode
                        )
                    )
                    launch {
                        mViewModel.shareGuest(
                            device.deviceId,
                            userId
                        ).collect { action ->
                            when (action) {
                                is HttpAction.Loading -> {}
                                is HttpAction.Fail -> {
                                    val e = action.t
                                    if (e is ResponseNotSuccessException) {
                                        when (val respCode = ResponseCode.getRespCode(e.code)) {
                                            null -> Unit
                                            else -> {
                                                respCode.msgRes.let(toast::show)
                                            }
                                        }
                                    }
                                }

                                is HttpAction.Success -> {
                                    toast.show(com.gw_reoqoo.resource.R.string.AA0156)
                                    this@Share2UserActivity.finish()
                                }
                            }
                        }
                    }
                }),
            )
        }
    }

    override fun getTitleView() = mViewBinding.layoutTitle

    override fun getLayoutId() = R.layout.dev_share_fragment_share_2_user

    override fun <T : ViewModel?> loadViewModel() = Share2UserVM::class.java as Class<T>

}