package com.gw_reoqoo.component_device_share.ui.main_activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import com.gw_reoqoo.component_device_share.R
import com.gw_reoqoo.component_family.api.interfaces.IDevice
import com.gw_reoqoo.lib_base_architecture.view.ABaseMVVMDBActivity
import com.gw_reoqoo.lib_router.ReoqooRouterPath
import com.gw_reoqoo.lib_router.createFragment
import com.gw_reoqoo.component_device_share.databinding.DevShareActivityBinding as Binding
import com.gw_reoqoo.component_device_share.ui.main_activity.vm.ShareDeviceVM as VM
import com.therouter.router.Autowired
import com.therouter.router.Route
import dagger.hilt.android.AndroidEntryPoint

/**
 * @Description: - 选择设备分享界面
 * @Author: XIAOLEI
 * @Date: 2023/8/9
 */
@Route(path = ReoqooRouterPath.DevShare.ACTIVITY_SHARE_DEVICE)
@AndroidEntryPoint
class ShareDeviceActivity : ABaseMVVMDBActivity<Binding, VM>() {
    override fun getLayoutId() = R.layout.dev_share_activity
    override fun <T : ViewModel?> loadViewModel() = VM::class.java as Class<T>

    @Autowired
    lateinit var device: IDevice

    @Autowired(name = "pageFrom")
    lateinit var pageFrom: String
    
    override fun onViewLoadFinish() {
        setStatusBarColor()
    }

    override fun initView() {
        mViewBinding.appTitle.leftIcon.setOnClickListener {
            finish()
        }
    }

    override fun initLiveData(viewModel: VM, savedInstanceState: Bundle?) {
        super.initLiveData(viewModel, savedInstanceState)
        viewModel.fragmentData.observe(this) { (url, args) ->
            val fragment = url.createFragment<Fragment>(with = args)
            fragment?.let {
                val transaction = supportFragmentManager.beginTransaction()
                transaction.replace(mViewBinding.fragmentLayout.id, fragment)
                transaction.commitAllowingStateLoss()
            }
        }
    }

    override fun initData(savedInstanceState: Bundle?) {
        super.initData(savedInstanceState)
        mViewModel.loadDeviceListFragment(if(::device.isInitialized) device else null, pageFrom)
    }
}