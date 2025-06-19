package com.gw_reoqoo.component_family.api.impl

import android.app.Activity
import android.content.Context
import android.os.Looper
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import com.gw_reoqoo.component_family.api.interfaces.IShareDeviceApi
import com.gw_reoqoo.component_family.databinding.FamilyDialogDevShareDetailBinding
import com.gw_reoqoo.component_family.datasource.RemoteUserMsgDataSource
import com.gw_reoqoo.component_family.repository.UserMsgRepository
import com.gw.component_plugin_service.api.IPluginManager
import com.gw.cp_config.api.IAppConfigApi
import com.gw.cp_config.api.ProductImgType
import com.gw_reoqoo.component_iotapi_plugin.opt.api.IGWIotOpt
import com.gw_reoqoo.component_iotapi_plugin.opt.api.PluginSolution
import com.gw_reoqoo.lib_base_architecture.SingleLiveEvent
import com.gw_reoqoo.lib_http.ResponseNotSuccessException
import com.gw_reoqoo.lib_http.entities.DeviceShareDetail
import com.gw_reoqoo.lib_http.error.ResponseCode
import com.tencentcs.iotvideo.http.interceptor.flow.HttpAction
import com.gw_reoqoo.lib_utils.ktx.loadUrl
import com.gw_reoqoo.lib_utils.toast.IToast
import com.gw_reoqoo.lib_widget.dialog.comm_dialog.entity.CommDialogAction
import com.gw_reoqoo.lib_widget.dialog.comm_dialog.entity.CustomContent
import com.gw_reoqoo.lib_widget.dialog.comm_dialog.entity.TextContent
import com.gw_reoqoo.lib_widget.dialog.comm_dialog.ext.showCommDialog
import com.gw_reoqoo.resource.R
import com.gwell.loglibs.GwellLogUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @Description: - 用户扫码添加别人二维码分享的设备
 * @Author: XIAOLEI
 * @Date: 2023/9/12
 */
@Singleton
class ShareDeviceImpl @Inject constructor(
    private val userMsgRepository: UserMsgRepository,
    private val userMsgDataSource: RemoteUserMsgDataSource,
    private val toast: IToast,
    private val configApi: IAppConfigApi,
    private val pluginManager: IPluginManager,
    private val igwIotOpt: IGWIotOpt,
) : IShareDeviceApi {

    companion object {
        private const val TAG = "ShareDeviceImpl"
    }

    /**
     * onScanShareDevice 的真实实现类
     */
    private val _onScanShareQRCode = SingleLiveEvent<Map<String, String>>()

    /**
     * 当扫码成功，则把参数解析成 key-value，调用此函数
     * @param params 把url解析成key-value的形式传入
     */
    override fun scanShareDevice(params: Map<String, String>) {
        if (Thread.currentThread() == Looper.getMainLooper().thread) {
            _onScanShareQRCode.value = params
        } else {
            _onScanShareQRCode.postValue(params)
        }
    }

    /**
     * 当扫描成功，并且解析的livedata
     */
    override val onScanShareDevice: LiveData<Map<String, String>> get() = _onScanShareQRCode


    /**
     * 显示分享详情的弹窗
     * @param owner 上下文 fragment 或者 activity
     * @param inviteToken 分享token
     * @param deviceId 设备ID
     * @param sharerName 设备主人名称
     * @param onClickAccept 当点击同意
     */
    override fun showShareDetailDialog(
        owner: LifecycleOwner,
        inviteToken: String,
        deviceId: String,
        sharerName: String?,
        onClickAccept: () -> Unit,
    ) {
        require(owner is Fragment || owner is Activity) { "required owner is fragment or activity" }
        val scope = owner.lifecycleScope
        val context = if (owner is Fragment) {
            owner.context
        } else {
            owner as Activity
        }
        if (context == null) return

        scope.launch {
            val flow = userMsgRepository.loadDeviceShareDetail(inviteToken, deviceId)
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

                    is HttpAction.Success -> {
                        val detail = action.data ?: return@collect
                        showInviteDetail(
                            context,
                            scope,
                            detail,
                            inviteToken,
                            deviceId,
                            onClickAccept
                        )
                    }
                }
            }

        }
    }

    /**
     * 显示邀请详情
     *
     * @param inviteToken 分享token
     * @param deviceId 设备ID
     * @param onClickAccept 当点击同意
     */
    private fun showInviteDetail(
        context: Context,
        scope: CoroutineScope,
        detail: DeviceShareDetail,
        inviteToken: String,
        deviceId: String,
        onClickAccept: () -> Unit,
    ) {
        val layoutInflater = LayoutInflater.from(context)
        val now = Calendar.getInstance().timeInMillis
        if ((detail.longExpireTime * 1000) < now) {
            toast.show(R.string.AA0168)
            return
        }

        context.showCommDialog {
            content = CustomContent(
                binding = FamilyDialogDevShareDetailBinding.inflate(layoutInflater),
                initView = { binding ->
                    val productName = configApi.getProductName("${detail.pid}")
                    binding.tvShareContent.text = context.getString(
                        R.string.AA0164,
                        detail.nickName
                    )
                    binding.tvDevName.text = productName
                    val imgUrl =
                        configApi.getProductImgUrl_D("${detail.pid}", ProductImgType.INTRODUCTION)
                    GwellLogUtils.i(TAG, "imgUrl $imgUrl")
                    binding.ivDevImg.loadUrl(
                        imgUrl
                    )
                    GwellLogUtils.i(
                        TAG,
                        "devShareDetail,pid:${detail.pid},productName:$productName,productImg:$imgUrl"
                    )
                }
            )
            actions = listOf(
                CommDialogAction(context.getString(R.string.AA0067), onClick = {
                    val flow = userMsgDataSource.rejectDeviceShare(deviceId, inviteToken)
                    scope.launch {
                        flow.collect { action ->
                            if (action is HttpAction.Fail) {
                                val t = action.t
                                if (t is ResponseNotSuccessException) {
                                    val respCode = ResponseCode.getRespCode(t.code)
                                    respCode?.msgRes?.let(toast::show)
                                }
                            }
                        }
                    }
                }),
                CommDialogAction(context.getString(R.string.AA0165), onClick = {
                    scope.launch {
                        val productName = configApi.getProductName("${detail.pid}")
                        GwellLogUtils.i(
                            TAG,
                            "configApi.getProductName(${detail.pid})->$productName"
                        )
                        val result = userMsgRepository.acceptDeviceShare(
                            inviteToken,
                            productName ?: deviceId
                        )
                        if (result == true) {
                            onClickAccept.invoke()
                            igwIotOpt.refreshDevices()
                            context.showCommDialog {
                                content = TextContent(
                                    context.getString(R.string.AA0089)
                                )
                                actions = listOf(
                                    CommDialogAction(context.getString(R.string.AA0059)),
                                    CommDialogAction(
                                        context.getString(R.string.AA0166),
                                        onClick = {
                                            scope.launch {
                                                igwIotOpt.openHome(deviceId)
                                            }
                                        }),
                                )
                            }
                        }
                    }
                }),
            )
        }
    }
}