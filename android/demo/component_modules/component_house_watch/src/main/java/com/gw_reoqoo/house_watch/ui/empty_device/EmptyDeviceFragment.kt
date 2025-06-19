package com.gw_reoqoo.house_watch.ui.empty_device

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModel
import com.gw_reoqoo.component_house_watch.R
import com.gw_reoqoo.component_house_watch.databinding.HouseWatchFragmentEmptyDeviceBinding as Binding
import com.gw_reoqoo.house_watch.ui.empty_device.vm.EmptyDeviceVM
import com.gw_reoqoo.lib_base_architecture.view.ABaseMVVMDBFragment
import com.gw_reoqoo.lib_router.ReoqooRouterPath
import com.gw_reoqoo.lib_router.navigation
import com.gw_reoqoo.lib_utils.ktx.setSingleClickListener
import com.therouter.router.Route
import dagger.hilt.android.AndroidEntryPoint

/**
 * @Description: - 没有设备显示的界面
 * @Author: XIAOLEI
 * @Date: 2023/8/18
 */
@Route(path = ReoqooRouterPath.HouseWatch.FRAGMENT_EMPTY_DEVICE)
@AndroidEntryPoint
class EmptyDeviceFragment : ABaseMVVMDBFragment<Binding, EmptyDeviceVM>() {
    override fun getLayoutId() = R.layout.house_watch_fragment_empty_device
    override fun <T : ViewModel?> loadViewModel() = EmptyDeviceVM::class.java as Class<T>

    override fun initView(view: View, savedInstanceState: Bundle?) {
        super.initView(view, savedInstanceState)
        mViewBinding.btAddDevice.setSingleClickListener {
            ReoqooRouterPath
                .ConfigPath
                .CONFIG_SCAN_ACTIVITY_PATH
                .navigation(this)
        }
    }
}