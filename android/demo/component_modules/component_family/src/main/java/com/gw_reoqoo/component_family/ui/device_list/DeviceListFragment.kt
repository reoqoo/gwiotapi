package com.gw_reoqoo.component_family.ui.device_list

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.gw_reoqoo.component_device_share.api.DevShareApi.Companion.KEY_PARAM_PAGE_FROM
import com.gw_reoqoo.component_device_share.api.DevShareApi.Companion.PARAM_DEV_SHARE_ENTITY
import com.gw_reoqoo.component_family.R
import com.gw_reoqoo.component_family.api.impl.DeviceImpl
import com.gw_reoqoo.component_family.databinding.FamilyFragmentDeviceBinding
import com.gw_reoqoo.component_family.ui.device_list.adapter.DeviceListAdapter
import com.gw_reoqoo.component_family.ui.device_list.popups.ItemMenuPopup
import com.gw_reoqoo.component_family.ui.device_list.touch_helper.OnRecyclerItemClickListener
import com.gw_reoqoo.component_family.ui.device_list.touch_helper.RecyItemTouchHelperCallback
import com.gw_reoqoo.component_family.ui.device_list.vm.DeviceListVM
import com.gw_reoqoo.component_family.ui.family.vm.FamilyVM
import com.gw.component_plugin_service.api.IPluginManager
import com.gw.cp_config.api.IAppConfigApi
import com.gw_reoqoo.lib_base_architecture.view.ABaseMVVMDBFragment
import com.gw_reoqoo.lib_http.ResponseNotSuccessException
import com.gw_reoqoo.lib_http.error.ResponseCode
import com.gw.lib_plugin_service.IResultCallback
import com.gw.lib_plugin_service.constant.PluginCodeConstants
import com.gw_reoqoo.component_family.api.interfaces.FamilyModeApi
import com.gw_reoqoo.component_iotapi_plugin.opt.api.IGWIotOpt
import com.gw_reoqoo.component_iotapi_plugin.opt.api.PluginSolution
import com.gw_reoqoo.lib_room.device.DeviceInfo
import com.gw_reoqoo.lib_room.ktx.isMaster
import com.gw_reoqoo.lib_router.ReoqooRouterPath
import com.gw_reoqoo.lib_router.navigation
import com.gw_reoqoo.lib_utils.ktx.launch
import com.gw_reoqoo.lib_widget.dialog.comm_dialog.entity.CommDialogAction
import com.gw_reoqoo.lib_widget.dialog.comm_dialog.entity.TextContent
import com.gw_reoqoo.lib_widget.dialog.comm_dialog.ext.showCommDialog
import com.gw_reoqoo.lib_widget.popups.GuidePopup
import com.gwell.loglibs.GwellLogUtils
import com.tencentcs.iotvideo.http.interceptor.flow.HttpAction
import com.therouter.router.Route
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.pow
import kotlin.math.sqrt
import com.gw_reoqoo.resource.R as RR


/**
 * @Description: - 设备列表界面
 * @Author: XIAOLEI
 * @Date: 2023/8/1
 */
@Route(path = ReoqooRouterPath.Family.FAMILY_FRAGMENT_DEVICE_LIST_PATH)
@AndroidEntryPoint
class DeviceListFragment : ABaseMVVMDBFragment<FamilyFragmentDeviceBinding, DeviceListVM>() {
    companion object {
        private const val TAG = "DeviceListFragment"
    }

    override fun getLayoutId() = R.layout.family_fragment_device

    override fun <T : ViewModel?> loadViewModel() = DeviceListVM::class.java as Class<T>

    /**
     * 公共配置信息
     */
    @Inject
    lateinit var configApi: IAppConfigApi

    @Inject
    lateinit var familyModeApi: FamilyModeApi

    @Inject
    lateinit var igwIotOpt: IGWIotOpt
    
    /**
     * 设备列表适配器
     */
    private val deviceListAdapter by lazy {
        DeviceListAdapter(configApi, familyModeApi, igwIotOpt, lifecycleScope)
    }

    /**
     * 插件管理器
     */
    @Inject
    lateinit var pluginManager: IPluginManager

    private var itemMenuPopup: ItemMenuPopup? = null

    // 获取FamilyFragment的ViewModel
    private val parentViewModel by lazy {
        ViewModelProvider(requireParentFragment())[FamilyVM::class.java]
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun initView(view: View, savedInstanceState: Bundle?) {
        super.initView(view, savedInstanceState)
        mFgViewModel.isDeviceGuidShow = false
        mViewBinding.deviceRv.run {
            // 布局方式
            layoutManager = GridLayoutManager(context, 2)
            // 适配器
            adapter = deviceListAdapter
            // 拖拽排序
            val callback = RecyItemTouchHelperCallback(this,
                onMove = {
                    itemMenuPopup?.dismiss()
                    GwellLogUtils.i(TAG, "onMove-itemMenuPopup?.dismiss()")
                    itemMenuPopup = null
                }, afterMove = {
                    GwellLogUtils.d(TAG, "afterMove")
                    mFgViewModel.updateDevice(deviceListAdapter.data)
                })
            val helper = ItemTouchHelper(callback)
            helper.attachToRecyclerView(this)
            addOnItemTouchListener(object : OnRecyclerItemClickListener(this) {
                override fun onItemClick(viewHolder: RecyclerView.ViewHolder?, position: Int) {
                    val devInfo = deviceListAdapter.data[position]
                    val pid = devInfo.productId ?: return
                    if (mFgViewModel.isReoqooDevice(pid)) {
                        pluginManager.startMonitorActivity(devInfo.deviceId)
                    } else {
                        lifecycleScope.launch {
                            igwIotOpt.openHome(devInfo.deviceId)
                        }
                    }
                }

                override fun onLongClick(viewHolder: RecyclerView.ViewHolder?, position: Int) {
                    val context = context ?: return
                    val info = deviceListAdapter.data[position]
                    itemMenuPopup = ItemMenuPopup(context, info,
                        onShareClick = {
                            if (familyModeApi.isReoqooDevice(info.productId ?: "")) {
                                val device = mapOf(
                                    KEY_PARAM_PAGE_FROM to "DeviceListActivity",
                                    PARAM_DEV_SHARE_ENTITY to DeviceImpl(info),
                                )
                                ReoqooRouterPath
                                    .DevShare
                                    .ACTIVITY_SHARE_MANAGER_OWNER_PATH
                                    .navigation(
                                        fragment = this@DeviceListFragment,
                                        with = device
                                    )
                            } else {
                                lifecycleScope.launch {
                                    igwIotOpt.openDevSharePage(info.deviceId, PluginSolution.YOOSEE)
                                }
                            }
                        },
                        onDeleteClick = {
                            showCommDialog {
                                content = TextContent(
                                    getString(RR.string.AA0173)
                                )
                                actions = listOf(
                                    CommDialogAction(getString(RR.string.AA0059)),
                                    CommDialogAction(
                                        getString(RR.string.AA0058),
                                        isDestructiveAction = true,
                                        onClick = {
                                            launch {
                                                parentViewModel.deleteDevice(info)
                                                    .collect { action ->
                                                        when (action) {
                                                            is HttpAction.Loading -> Unit
                                                            is HttpAction.Success -> Unit
                                                            is HttpAction.Fail -> {
                                                                when (val throwable = action.t) {
                                                                    is ResponseNotSuccessException -> {
                                                                        val respCode =
                                                                            ResponseCode.getRespCode(
                                                                                throwable.code
                                                                            )
                                                                        respCode?.msgRes?.let(toast::show)
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                            }
                                        }
                                    ),
                                )
                            }
                        }
                    ).apply {
                        setOnDismissListener {
                            itemMenuPopup = null
                        }
                        val anchorActivity = activity
                        if (anchorActivity != null && viewHolder != null) {
                            show(viewHolder.itemView, anchorActivity)
                        }
                    }
                }

            })
            // 触摸事件，抬起手指，父控件恢复
            this.setOnTouchListener(object : View.OnTouchListener {
                private var downEvent: MotionEvent? = null
                override fun onTouch(v: View?, event: MotionEvent): Boolean {
                    when (event.action) {
                        MotionEvent.ACTION_MOVE -> {
                            val downEvent = downEvent
                            if (downEvent == null) {
                                this.downEvent = MotionEvent.obtain(event)
                                return false
                            }
                            // d=√[(x2-x1)²+(y2-y1)²]
                            val x1 = downEvent.x
                            val y1 = downEvent.y
                            val x2 = event.x
                            val y2 = event.y
                            val distance = sqrt((x2 - x1).pow(2) + (y2 - y1).pow(2))
                            if (distance > ViewConfiguration.get(context).scaledTouchSlop) {
                                itemMenuPopup?.dismiss()
                                GwellLogUtils.i(TAG, "event-itemMenuPopup?.dismiss():$event")
                                itemMenuPopup = null
                            }
                        }

                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            downEvent?.recycle()
                            downEvent = null
                            parent.requestDisallowInterceptTouchEvent(false)
                        }
                    }
                    return false
                }
            })
            // 去掉刷新闪白动画
            (itemAnimator as? SimpleItemAnimator?)?.supportsChangeAnimations = false
        }
        // 点击开机/关机
        deviceListAdapter.setOnTurnOnOrOffClick { info, view, onConfirm ->
            val powerOn = info.powerOn ?: return@setOnTurnOnOrOffClick
            val textRes = if (powerOn) {
                RR.string.AA0060
            } else {
                RR.string.AA0061
            }
            showCommDialog {
                content = TextContent(getString(textRes))
                actions = listOf(
                    CommDialogAction(getString(RR.string.AA0059)),
                    CommDialogAction(
                        getString(RR.string.AA0058),
                        isDestructiveAction = true,
                        onClick = {
                            turnOnOrOffDevice(info, view)
                            onConfirm.invoke()
                        }
                    ),
                )
            }
        }
        // 设置根据设备ID查询是否开启云服务的回调
        deviceListAdapter.setCheckCloudOn(mFgViewModel::checkDeviceCloudOn)
        // 设置根据设备ID查询是否开启4g服务的回调
        deviceListAdapter.setCheck4gOn(mFgViewModel::checkDevice4gOn)
    }

    override fun initLiveData(viewModel: DeviceListVM, savedInstanceState: Bundle?) {
        super.initLiveData(viewModel, savedInstanceState)
        // 设备列表更新通知
        parentViewModel.deviceList.observe(this) { list ->
            deviceListAdapter.updateData(list)
            if (list.isNotEmpty()) {
                viewModel.loadFirstDeviceGuide()
            }
        }
        // 新手引导
        viewModel.firstDeviceGuide.observe(this) { shown ->
            if (shown == false) {
                val layoutManager = mViewBinding.deviceRv.layoutManager as? GridLayoutManager?
                // 第一个显示的子控件的position
                val firstPosition = layoutManager?.findFirstVisibleItemPosition()
                if (firstPosition != null && firstPosition >= 0) {
                    // 从适配器中获取对应的Device
                    val deviceInfo = deviceListAdapter.getItemData(firstPosition)
                    // 从RecyclerView中获取对应的View
                    val firstChild: View? = mViewBinding.deviceRv.getChildAt(firstPosition)
                    if (firstChild != null) {
                        // 主人还是访客，使用不同的文字提示
                        val contentRes = if (deviceInfo.isMaster) {
                            RR.string.AA0338
                        } else {
                            RR.string.AA0338
                        }
                        if (!viewModel.isDeviceGuidShow) {
                            context?.let {
                                GuidePopup(it, this, contentRes).apply {
                                    setOnIKnowClick {
                                        viewModel.iKnowFirstDeviceGuide()
                                    }
                                    setOnDismissListener {
                                        viewModel.isDeviceGuidShow = false
                                    }
                                }
                            }?.show(firstChild)
                            viewModel.isDeviceGuidShow = true
                        }
                    }
                }
            }
        }
    }


    /**
     * 启动或者关闭设备
     * @param device 对应的Device
     * @param view RecyclerView 对应的itemView
     */
    private fun turnOnOrOffDevice(device: DeviceInfo, view: View) {
        val powerOn = device.powerOn ?: return
        val newStatus = if (powerOn) {
            PluginCodeConstants.DevicePowerStatus.POWER_OFF
        } else {
            PluginCodeConstants.DevicePowerStatus.POWER_ON
        }
        GwellLogUtils.i(TAG, "turnOnOrOffDevice(${device.deviceId},$newStatus)")
        pluginManager.deviceEnOn(device.deviceId, newStatus, object : IResultCallback {
            override fun onSuccess(code: Int, responseInfo: String?) {
                view.isEnabled = true
                GwellLogUtils.i(
                    TAG,
                    "pluginManager.deviceEnOn(${device.deviceId},$newStatus).onSuccess($code,$responseInfo)"
                )
                parentViewModel.loadRemoteDeviceList()
            }

            override fun onFailed(code: Int, errorMsg: String?) {
                view.isEnabled = true
                GwellLogUtils.e(
                    TAG,
                    "pluginManager.deviceEnOn(${device.deviceId},$newStatus).onFailed($code,$errorMsg)"
                )
                parentViewModel.loadRemoteDeviceList()
            }
        })
    }

}