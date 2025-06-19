package com.gw_reoqoo.component_device_share.ui.share_visitor

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModel
import com.gw_reoqoo.component_device_share.R
import com.gw_reoqoo.component_device_share.api.DevShareApi.Companion.PARAM_DEV_SHARE_ENTITY
import com.gw_reoqoo.component_device_share.databinding.DevShareActivityManagerVisitorBinding
import com.gw_reoqoo.component_device_share.ui.share_visitor.vm.ShareVisitorVM
import com.gw_reoqoo.component_family.api.interfaces.IDevice
import com.gw.cp_config.api.IAppConfigApi
import com.gw.cp_config.api.ProductImgType
import com.gw_reoqoo.lib_base_architecture.view.ABaseMVVMDBActivity
import com.gw_reoqoo.lib_http.ResponseNotSuccessException
import com.gw_reoqoo.lib_http.error.ResponseCode
import com.tencentcs.iotvideo.http.interceptor.flow.HttpAction
import com.gw_reoqoo.lib_router.ReoqooRouterPath
import com.gw_reoqoo.lib_utils.ktx.loadUrl
import com.gw_reoqoo.lib_widget.dialog.comm_dialog.entity.CommDialogAction
import com.gw_reoqoo.lib_widget.dialog.comm_dialog.entity.TextContent
import com.gw_reoqoo.lib_widget.dialog.comm_dialog.ext.showCommDialog
import com.gwell.loglibs.GwellLogUtils
import com.therouter.router.Autowired
import com.therouter.router.Route
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.gw_reoqoo.resource.R as RR

@AndroidEntryPoint
@Route(path = ReoqooRouterPath.DevShare.ACTIVITY_SHARE_MANAGER_VISITOR_PATH)
class ShareVisitorActivity :
    ABaseMVVMDBActivity<DevShareActivityManagerVisitorBinding, ShareVisitorVM>() {

    companion object {
        private const val TAG = "ShareInfoActivity"
    }

    /**
     * 分享设备信息实体数据
     */
    @Autowired(name = PARAM_DEV_SHARE_ENTITY)
    lateinit var device: IDevice

    @Inject
    lateinit var configApi: IAppConfigApi

    override fun getLayoutId() = R.layout.dev_share_activity_manager_visitor

    override fun <T : ViewModel?> loadViewModel(): Class<T> = ShareVisitorVM::class.java as Class<T>

    override fun getTitleView() = mViewBinding.layoutTitle

    override fun onParseParams(intent: Intent) {
        super.onParseParams(intent)
        mViewModel.setDevEntity(device)
    }

    override fun initView() {
        mViewBinding.layoutTitle.leftIcon.setOnClickListener { finish() }

        mViewBinding.btnDeleteDev.setOnClickListener {
            showCommDialog {
                content = TextContent(text = getString(RR.string.AA0561))
                actions = listOf(
                    CommDialogAction(text = getString(RR.string.AA0059)),
                    CommDialogAction(text = getString(RR.string.AA0058),
                        isDestructiveAction = true,
                        onClick = {
                            mViewModel.cancelDevice()
                        })
                )
            }
        }
        val imgUrl = configApi.getProductImgUrl(
            pid = device.productId,
            imgType = ProductImgType.INTRODUCTION
        )
        mViewBinding.ivDeviceImg.loadUrl(
            imgUrl,
            placeHolder = R.drawable.share_manager_ic_device_img,
            error = R.drawable.share_manager_ic_device_img,
        )
        mViewBinding.tvDeviceName.text = device.remarkName
    }

    override fun initLiveData(viewModel: ShareVisitorVM, savedInstanceState: Bundle?) {
        super.initLiveData(viewModel, savedInstanceState)
        // 主人信息
        viewModel.ownerInfo.observe(this) { action ->
            when (action) {
                is HttpAction.Loading -> Unit
                is HttpAction.Success -> {
                    val ownerInfo = action.data
                    mViewBinding.tvOwnerInfo.text = buildString {
                        append(getString(RR.string.AA0191)).append(":").append(ownerInfo?.nickName)
                            .append("(")
                            .append(ownerInfo?.ownerAccount).append(")")
                    }
                }

                is HttpAction.Fail -> {
                    val t = action.t
                    if(t is ResponseNotSuccessException){
                        val respCode = ResponseCode.getRespCode(t.code)
                        respCode?.msgRes?.let(toast::show)
                    }
                }
            }
        }
        // 删除操作
        viewModel.cancelShareResult.observe(this) { success ->
            if (success == true) {
                finish()
            }
        }
    }

    override fun initData(savedInstanceState: Bundle?) {
        super.initData(savedInstanceState)
        mViewModel.queryOwnerInfo()

        val devImg = configApi.getProductImgUrl(device.productId)
        GwellLogUtils.i(TAG, "devImg $devImg")
        mViewBinding.ivDeviceImg.loadUrl(devImg)

        mViewBinding.tvDeviceName.text = device.remarkName
    }
}