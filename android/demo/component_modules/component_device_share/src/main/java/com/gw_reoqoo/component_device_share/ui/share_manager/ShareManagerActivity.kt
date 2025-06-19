package com.gw_reoqoo.component_device_share.ui.share_manager

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.gw_reoqoo.component_device_share.R
import com.gw_reoqoo.component_device_share.api.DevShareApi.Companion.PARAM_DEV_SHARE_ENTITY
import com.gw_reoqoo.component_device_share.databinding.DevShareActivityManagerBinding
import com.gw_reoqoo.component_device_share.ui.share_manager.adapter.DevShareAdapter
import com.gw_reoqoo.component_device_share.ui.share_manager.vm.ShareManagerVM
import com.gw_reoqoo.component_family.api.interfaces.IDevice
import com.gw.cp_config.api.IAppConfigApi
import com.gw_reoqoo.component_iotapi_plugin.opt.api.IGWIotOpt
import com.gw_reoqoo.lib_base_architecture.view.ABaseMVVMDBActivity
import com.gw_reoqoo.lib_router.ReoqooRouterPath
import com.gw_reoqoo.lib_router.navigation
import com.therouter.router.Route
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 共享管理页面
 */
@AndroidEntryPoint
@Route(path = ReoqooRouterPath.DevShare.ACTIVITY_SHARE_MANAGER_PATH)
class ShareManagerActivity : ABaseMVVMDBActivity<DevShareActivityManagerBinding, ShareManagerVM>() {

    companion object {
        private const val TAG = "ShareManagerActivity"

        /**
         * 是返回activity是否需要刷新页面数据
         */
        private const val RESULT_CALLBACK_REFRESH = 1000
    }

    override fun <T : ViewModel?> loadViewModel() = ShareManagerVM::class.java as Class<T>

    override fun getLayoutId() = R.layout.dev_share_activity_manager

    override fun getTitleView(): View = mViewBinding.appTitle

    @Inject
    lateinit var configApi: IAppConfigApi

    @Inject
    lateinit var iotOpt: IGWIotOpt

    /**
     * 我分享的设备适配器
     */
    private val sharedAdapter by lazy {
        DevShareAdapter(configApi)
    }

    /**
     * 来自分享的设备适配器
     */
    private val fromSharedAdapter by lazy {
        DevShareAdapter(configApi)
    }

    override fun initView() {
        mViewBinding.appTitle.leftIcon.setOnClickListener { finish() }

        mViewBinding.rvDeviceShared.run {
            layoutManager = LinearLayoutManager(context)
            adapter = sharedAdapter
            sharedAdapter.setOnItemClick(::onDeviceClick)
        }
        mViewBinding.rvDeviceFromShared.run {
            layoutManager = LinearLayoutManager(context)
            adapter = fromSharedAdapter
            fromSharedAdapter.setOnItemClick(::onDeviceClick)
        }
    }

    override fun initLiveData(viewModel: ShareManagerVM, savedInstanceState: Bundle?) {
        super.initLiveData(viewModel, savedInstanceState)
        // 全部设备
        viewModel.allDevice.observe(this) { list ->
            mViewBinding.llNoShared.isVisible = list.isEmpty()
        }
        // 我分享的设备
        viewModel.sharedDeviceList.observe(this) { list ->
            sharedAdapter.updateData(list, false)
            mViewBinding.llShared.isVisible = list.isNotEmpty()
        }
        // 来自分享的设备
        viewModel.fromSharedDeviceList.observe(this) { list ->
            fromSharedAdapter.updateData(list, false)
            mViewBinding.llFromShared.isVisible = list.isNotEmpty()
        }
    }

    override fun initData(savedInstanceState: Bundle?) {
        super.initData(savedInstanceState)
        // 监听设备信息
        mViewModel.watchDeviceList(this)
        // 刷新一次设备信息
        mViewModel.refreshDeviceFromRemote()
    }

    /**
     * 当设备被点击
     */
    private fun onDeviceClick(device: IDevice) {
        if (device.isMaster) {
            lifecycleScope.launch {
                iotOpt.openMySharedPage(device.deviceId)
            }
        } else {
            ReoqooRouterPath
                .DevShare
                .ACTIVITY_SHARE_MANAGER_VISITOR_PATH
                .navigation(
                    context = this,
                    with = mapOf(PARAM_DEV_SHARE_ENTITY to device)
                )
        }
    }
}