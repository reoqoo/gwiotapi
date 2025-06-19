package com.gw_reoqoo.component_device_share.ui.permission_setting

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import com.gw_reoqoo.component_device_share.R
import com.gw_reoqoo.component_device_share.databinding.DevShareActivityPermissionSettingBinding
import com.gw_reoqoo.component_device_share.ui.permission_setting.adapter.FunctionEntity
import com.gw_reoqoo.component_device_share.ui.permission_setting.adapter.PermissionAdapter
import com.gw_reoqoo.component_device_share.ui.permission_setting.vm.PermissionSettingVM
import com.gw_reoqoo.component_family.api.interfaces.IDevice
import com.gw.component_plugin_service.api.IPluginManager
import com.gw_reoqoo.lib_base_architecture.view.ABaseMVVMDBActivity
import com.gw_reoqoo.lib_router.ReoqooRouterPath
import com.gw_reoqoo.lib_utils.ktx.launch
import com.gwell.loglibs.GwellLogUtils
import com.therouter.router.Autowired
import com.therouter.router.Route
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Dispatcher
import javax.inject.Inject
import com.gw_reoqoo.component_device_share.ui.permission_setting.vm.PermissionSettingVM as VM

/**
 * Author: yanzheng@gwell.cc
 * Time: 2024/7/13 14:57
 * Description: PermissionSettingActivity
 */
@Route(path = ReoqooRouterPath.DevShare.ACTIVITY_SHARE_MANAGER_PERMISSION_PATH)
@AndroidEntryPoint
class PermissionSettingActivity :
    ABaseMVVMDBActivity<DevShareActivityPermissionSettingBinding, VM>() {

    companion object {
        private const val TAG = "PermissionSettingActivity"
    }

    @Autowired
    lateinit var device: IDevice

    private var adapter: PermissionAdapter? = null

    override fun initView() {
        mViewBinding.layoutTitle.leftIcon.setOnClickListener {
            finish()
        }
        adapter = PermissionAdapter()
        val manager = LinearLayoutManager(this)
        mViewBinding.rvSetting.layoutManager = manager
        mViewBinding.rvSetting.setAdapter(adapter)
        adapter?.setListener(object : PermissionAdapter.OnSwitchChangeListener {
            override fun onSwitchChange(item: FunctionEntity, isChecked: Boolean) {
                item.functionKey?.let {
                    mViewModel.setPermissionBit(device.deviceId, item.functionKey, isChecked)
                }
            }

        })
    }

    override fun initData(savedInstanceState: Bundle?) {
        super.initData(savedInstanceState)
        mViewModel.getPermissionList()
        launch(Dispatchers.IO) {
            mViewModel.loadPermissionSettings(device.deviceId)
        }
    }

    override fun initLiveData(viewModel: PermissionSettingVM, savedInstanceState: Bundle?) {
        super.initLiveData(viewModel, savedInstanceState)
        mViewModel.list.observe(this) {
            GwellLogUtils.i(TAG, "list:$it")
            adapter?.setData(it)
        }
    }

    override fun getLayoutId() = R.layout.dev_share_activity_permission_setting

    override fun <T : ViewModel?> loadViewModel() = VM::class.java as Class<T>

    override fun onViewLoadFinish() {
        setStatusBarColor()
    }

}