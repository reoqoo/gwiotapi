package com.gw_reoqoo.component_device_share.ui.device_list

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gw_reoqoo.component_device_share.R
import com.gw_reoqoo.component_device_share.api.DevShareApi.Companion.KEY_PARAM_PAGE_FROM
import com.gw_reoqoo.component_device_share.api.DevShareApi.Companion.PARAM_DEV_SHARE_ENTITY
import com.gw_reoqoo.component_device_share.entities.DeviceWrapper
import com.gw_reoqoo.component_device_share.sa.SaEvent
import com.gw_reoqoo.component_device_share.sa.SaEvent.Attr.DEVICE_ID
import com.gw_reoqoo.component_device_share.sa.SaEvent.Attr.PAGE_TITLE
import com.gw_reoqoo.component_device_share.ui.device_list.adapter.DeviceListAdapter
import com.gw_reoqoo.component_device_share.ui.device_list.vm.DeviceListVM
import com.gw_reoqoo.component_device_share.ui.main_activity.vm.ShareDeviceVM
import com.gw_reoqoo.component_family.api.interfaces.IDevice
import com.gw.cp_config.api.IAppConfigApi
import com.gw_reoqoo.lib_base_architecture.view.ABaseMVVMDBFragment
import com.gw_reoqoo.lib_http.ResponseNotSuccessException
import com.gw_reoqoo.lib_http.error.ResponseCode
import com.gw_reoqoo.lib_router.ReoqooRouterPath
import com.gw_reoqoo.lib_router.navigation
import com.gw_reoqoo.lib_utils.ktx.getDrawableAndBounds
import com.gw_reoqoo.lib_utils.ktx.launch
import com.gw_reoqoo.lib_utils.ktx.setSingleClickListener
import com.gw_reoqoo.lib_widget.dialog.comm_dialog.entity.CommDialogAction
import com.gw_reoqoo.lib_widget.dialog.comm_dialog.entity.TextContent
import com.gw_reoqoo.lib_widget.dialog.comm_dialog.ext.showCommDialog
import com.gwell.loglibs.GwellLogUtils
import com.jwkj.base_statistics.sa.kits.SA
import com.tencentcs.iotvideo.http.interceptor.flow.HttpAction
import com.therouter.router.Autowired
import com.therouter.router.Route
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.gw_reoqoo.component_device_share.databinding.DevShareFragmentDevListBinding as Binding
import com.gw_reoqoo.resource.R as RR

/**
 * @Description: - 显示设备列表供选择分享的fragment
 * @Author: XIAOLEI
 * @Date: 2023/8/9
 */
@Route(path = ReoqooRouterPath.DevShare.FRAGMENT_DEVICE_LIST)
@AndroidEntryPoint
class DeviceListFragment : ABaseMVVMDBFragment<Binding, DeviceListVM>() {

    companion object {
        private const val TAG = "DeviceListFragment"
    }

    override fun getLayoutId() = R.layout.dev_share_fragment_dev_list
    override fun <T : ViewModel?> loadViewModel() = DeviceListVM::class.java as Class<T>

    @Autowired
    lateinit var userId: String

    @Autowired
    lateinit var initDevice: IDevice

    @Autowired
    lateinit var pageFrom: String

    @Inject
    lateinit var iAppConfigApi: IAppConfigApi

    private val shareDeviceVM: ShareDeviceVM by activityViewModels()
    override fun initView(view: View, savedInstanceState: Bundle?) {
        super.initView(view, savedInstanceState)
        // 设备列表的初始化
        mViewBinding.recyclerView.run {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            // 设置适配器
            adapter = DeviceListAdapter(iAppConfigApi).apply {
                // 点击更新勾选状态
                onItemClick { wrapper, _ ->
                    if (wrapper.checked) return@onItemClick
                    for (deviceWrapper in data) {
                        deviceWrapper.checked = false
                    }
                    wrapper.checked = true
                    notifyDataSetChanged()
                    val checkedDevices = data.filter { it.checked }
                    mViewBinding.btnNext.isEnabled = checkedDevices.isNotEmpty()
                }
            }
            val drawable = context.getDrawableAndBounds(R.drawable.dev_share_dev_list_decoration)
            val decoration = DividerItemDecoration(context, RecyclerView.VERTICAL)
            if (drawable != null) {
                decoration.setDrawable(drawable)
            }
            addItemDecoration(decoration)
        }
        // 点击下一步
        mViewBinding.btnNext.setSingleClickListener {
            val adapter = mViewBinding.recyclerView.adapter as? DeviceListAdapter?
            val device = adapter?.data?.firstOrNull { it.checked }?.device
            if (device != null) {
                launch {
                    val flow = mFgViewModel.listGuest(device.deviceId)
                    GwellLogUtils.i(TAG, "pageFrom $pageFrom")
                    SA.track(
                        SaEvent.BUTTON_CLICK,
                        mapOf(DEVICE_ID to device.deviceId, PAGE_TITLE to pageFrom)
                    )
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
                                val guestContent = action.data
                                if (guestContent != null) {
                                    if (guestContent.guestList.size >= guestContent.guestCount) {
                                        showCommDialog {
                                            content = TextContent(
                                                getString(
                                                    RR.string.AA0146,
                                                    guestContent.guestCount.toString()
                                                )
                                            )
                                            actions = listOf(
                                                CommDialogAction(getString(RR.string.AA0445)),
                                                CommDialogAction(
                                                    getString(RR.string.AA0147),
                                                    onClick = {
                                                        ReoqooRouterPath
                                                            .DevShare
                                                            .ACTIVITY_SHARE_MANAGER_PATH
                                                            .navigation(fragment = null)
                                                    }),
                                            )
                                        }
                                    } else {
                                        ReoqooRouterPath
                                            .DevShare
                                            .ACTIVITY_SHARE_MANAGER_OWNER_PATH
                                            .navigation(
                                                fragment = this@DeviceListFragment,
                                                with = mapOf(
                                                    PARAM_DEV_SHARE_ENTITY to device,
                                                    KEY_PARAM_PAGE_FROM to pageFrom
                                                )
                                            )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun initLiveData(viewModel: DeviceListVM, savedInstanceState: Bundle?) {
        super.initLiveData(viewModel, savedInstanceState)
        // 监听设备列表改变
        viewModel.deviceList.observe(this) { devList ->
            val adapter = mViewBinding.recyclerView.adapter as? DeviceListAdapter?
            if (adapter !is DeviceListAdapter) return@observe

            val currentDevice = if (::initDevice.isInitialized) initDevice else null
            adapter.updateData(devList.map {
                DeviceWrapper(it, checked = it.deviceId == currentDevice?.deviceId)
            })
            // 更新按钮的选中状态
            val checkedDevices = adapter.data.filter { it.checked }
            mViewBinding.btnNext.isEnabled = checkedDevices.isNotEmpty()
        }
    }

    override fun initData() {
        super.initData()
        // 加载设备列表
        mFgViewModel.loadDeviceList(userId)
    }
}