package com.gw_reoqoo.component_device_share.ui.share_owner

import android.content.Intent
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import com.gw_reoqoo.component_device_share.R
import com.gw_reoqoo.component_device_share.api.DevShareApi.Companion.KEY_PARAM_DEVICE
import com.gw_reoqoo.component_device_share.api.DevShareApi.Companion.KEY_PARAM_PAGE_FROM
import com.gw_reoqoo.component_device_share.api.DevShareApi.Companion.PARAM_DEV_SHARE_ENTITY
import com.gw_reoqoo.component_device_share.databinding.DevShareActivityManagerOwnerBinding
import com.gw_reoqoo.component_device_share.ui.share_owner.adapter.VisitorsAdapter
import com.gw_reoqoo.component_device_share.ui.share_owner.vm.ShareOwnerVM
import com.gw_reoqoo.component_family.api.interfaces.FamilyModeApi
import com.gw_reoqoo.component_family.api.interfaces.IDevice
import com.gw.cp_config.api.IAppConfigApi
import com.gw_reoqoo.lib_base_architecture.view.ABaseMVVMDBActivity
import com.gw_reoqoo.lib_http.ResponseNotSuccessException
import com.gw_reoqoo.lib_http.error.ResponseCode
import com.gw_reoqoo.lib_router.ReoqooRouterPath
import com.gw_reoqoo.lib_router.navigation
import com.gw_reoqoo.lib_utils.ktx.launch
import com.gw_reoqoo.lib_utils.ktx.loadUrl
import com.gw_reoqoo.lib_utils.ktx.setSingleClickListener
import com.gw_reoqoo.lib_utils.ktx.visible
import com.gw_reoqoo.lib_widget.dialog.comm_dialog.entity.CommDialogAction
import com.gw_reoqoo.lib_widget.dialog.comm_dialog.entity.TextContent
import com.gw_reoqoo.lib_widget.dialog.comm_dialog.ext.showCommDialog
import com.gwell.loglibs.GwellLogUtils
import com.tencentcs.iotvideo.http.interceptor.flow.HttpAction
import com.therouter.router.Autowired
import com.therouter.router.Route
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.gw_reoqoo.resource.R as RR

/**
 * @Desc: 我的分享页面
 */
@AndroidEntryPoint
@Route(path = ReoqooRouterPath.DevShare.ACTIVITY_SHARE_MANAGER_OWNER_PATH)
class ShareOwnerActivity :
    ABaseMVVMDBActivity<DevShareActivityManagerOwnerBinding, ShareOwnerVM>() {

    companion object {
        private const val TAG = "ShareOwnerActivity"
    }

    @Autowired(name = PARAM_DEV_SHARE_ENTITY)
    lateinit var device: IDevice

    @Inject
    lateinit var familyModeApi: FamilyModeApi

    @Inject
    lateinit var iAppConfigApi: IAppConfigApi

    private val visitorsAdapter = VisitorsAdapter()

    override fun getLayoutId() = R.layout.dev_share_activity_manager_owner

    override fun <T : ViewModel?> loadViewModel(): Class<T> = ShareOwnerVM::class.java as Class<T>

    override fun getTitleView() = mViewBinding.layoutTitle

    override fun onParseParams(intent: Intent) {
        super.onParseParams(intent)
        mViewModel.setDevice(device)
    }

    override fun initView() {
        mViewBinding.layoutTitle.leftIcon.setOnClickListener { finish() }
        mViewBinding.tvPermissionSetting.visible(iAppConfigApi.getPermissionMode() == 1)
        mViewBinding.linePermission.visible(iAppConfigApi.getPermissionMode() == 1)
        mViewBinding.tvAddVisitor.setOnClickListener {
            val guestCount = mViewModel.getMaxGuestCount()
            if (visitorsAdapter.data.size >= guestCount) {
                showCommDialog {
                    content = TextContent(
                        getString(RR.string.AA0146, guestCount.toString())
                    )
                    actions = listOf(
                        CommDialogAction(getString(RR.string.AA0131))
                    )
                }
                return@setOnClickListener
            }
            ReoqooRouterPath
                .DevShare
                .FRAGMENT_SHARE_2_USER_PATH
                .navigation(
                    context = this,
                    with = mapOf(
                        KEY_PARAM_DEVICE to device,
                        KEY_PARAM_PAGE_FROM to ""
                    )
                )
        }
        mViewBinding.tvPermissionSetting.setOnClickListener {
            ReoqooRouterPath
                .DevShare
                .ACTIVITY_SHARE_MANAGER_PERMISSION_PATH
                .navigation(
                    context = this,
                    with = mapOf(KEY_PARAM_DEVICE to device)
                )
        }
        // 已经分享的好友列表
        mViewBinding.rvFriends.run {
            layoutManager = LinearLayoutManager(context)
            adapter = visitorsAdapter
            visitorsAdapter.onItemClick { guest ->
                val guestId = guest.guestId
                showCommDialog {
                    content = TextContent(text = getString(RR.string.AA0185))
                    actions = listOf(
                        CommDialogAction(text = getString(RR.string.AA0059)),
                        CommDialogAction(text = getString(RR.string.AA0058),
                            isDestructiveAction = true,
                            onClick = {
                                val flow = mViewModel.delGuest(visitorId = guestId)
                                launch {
                                    flow.collect { action ->
                                        when (action) {
                                            is HttpAction.Loading -> {}
                                            is HttpAction.Success -> {
                                                mViewModel.getGuestList()
                                                    .collect { guestListAction ->
                                                        when (guestListAction) {
                                                            is HttpAction.Loading -> {}
                                                            is HttpAction.Fail -> {
                                                                val t = guestListAction.t
                                                                if (t is ResponseNotSuccessException) {
                                                                    val respCode =
                                                                        ResponseCode.getRespCode(t.code)
                                                                    respCode?.msgRes?.let(toast::show)
                                                                }
                                                            }

                                                            is HttpAction.Success -> {
                                                                toast.show(RR.string.AA0186)
                                                                val isEmpty =
                                                                    guestListAction.data?.guestList.isNullOrEmpty()
                                                                if (isEmpty) {
                                                                    familyModeApi.refreshDevice()
                                                                    finish()
                                                                }
                                                            }
                                                        }
                                                    }
                                            }

                                            is HttpAction.Fail -> {
                                                toast.show(RR.string.AA0187)
                                            }
                                        }
                                    }
                                }
                            }
                        )
                    )
                }
            }
        }
        // 停止分享
        mViewBinding.btnShareStop.setSingleClickListener {
            showCommDialog {
                content = TextContent(text = getString(RR.string.AA0650))
                actions = listOf(
                    CommDialogAction(text = getString(RR.string.AA0059)),
                    CommDialogAction(text = getString(RR.string.AA0058),
                        isDestructiveAction = true,
                        onClick = {
                            launch {
                                val flow = mViewModel.delAllGuest()
                                flow.collect { action ->
                                    when (action) {
                                        is HttpAction.Loading -> {}
                                        is HttpAction.Success -> {
                                            toast.show(RR.string.AA0510)
                                            familyModeApi.refreshDevice()
                                            finish()
                                        }

                                        is HttpAction.Fail -> {
                                            val t = action.t
                                            if (t is ResponseNotSuccessException) {
                                                val respCode = ResponseCode.getRespCode(t.code)
                                                respCode?.msgRes?.let(toast::show)
                                            }
                                            // 暂时保留注释代码，方便以后恢复
                                            //toast.show(RR.string.delete_all_share_fail)
                                        }
                                    }
                                }
                            }
                        })
                )
            }
        }
    }

    override fun initLiveData(viewModel: ShareOwnerVM, savedInstanceState: Bundle?) {
        super.initLiveData(viewModel, savedInstanceState)
        viewModel.guestList.observe(this) {
            GwellLogUtils.i(TAG, "guest $it")
            mViewBinding.tvHadShareFriends.isVisible = it.isNotEmpty()
            mViewBinding.rvFriends.isVisible = it.isNotEmpty()
            visitorsAdapter.updateData(it)
        }
    }

    override fun initData(savedInstanceState: Bundle?) {
        super.initData(savedInstanceState)
        GwellLogUtils.i(TAG, "mEntity $device")

        val devImg = iAppConfigApi.getProductImgUrl(device.productId)
        GwellLogUtils.i(TAG, "devImg $devImg")
        mViewBinding.ivDeviceIcon.loadUrl(devImg)

        mViewBinding.tvDeviceName.text = device.remarkName

        val flow = mViewModel.getGuestList()
        launch {
            flow.collect { action ->
                when (action) {
                    is HttpAction.Loading -> {}
                    is HttpAction.Fail -> {
                        val t = action.t
                        if (t is ResponseNotSuccessException) {
                            val respCode = ResponseCode.getRespCode(t.code)
                            respCode?.msgRes?.let(toast::show)
                        }
                    }

                    is HttpAction.Success -> {}
                }
            }
        }
    }
}