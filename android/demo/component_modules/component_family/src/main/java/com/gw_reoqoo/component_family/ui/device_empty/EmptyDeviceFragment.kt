package com.gw_reoqoo.component_family.ui.device_empty

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModel
import com.gw_reoqoo.component_family.R
import com.gw_reoqoo.component_family.databinding.FamilyFragmentDeviceEmptyBinding
import com.gw_reoqoo.component_family.ui.device_empty.vm.EmptyDeviceVM
import com.gw.cp_config_net.api.interfaces.ISaEventApi
import com.gw_reoqoo.lib_router.ReoqooRouterPath
import com.gw_reoqoo.lib_base_architecture.view.ABaseMVVMDBFragment
import com.gw_reoqoo.lib_router.navigation
import com.therouter.router.Autowired
import com.therouter.router.Route
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * @Description: - 当设备列表为空的时候，则以此页面替换
 * @Author: XIAOLEI
 * @Date: 2023/8/1
 */
@Route(path = ReoqooRouterPath.Family.FAMILY_FRAGMENT_DEVICE_EMPTY_PATH)
@AndroidEntryPoint
class EmptyDeviceFragment : ABaseMVVMDBFragment<FamilyFragmentDeviceEmptyBinding, EmptyDeviceVM>() {

    @Autowired
    lateinit var userId: String

    @Inject
    lateinit var saEventApi: ISaEventApi

    override fun getLayoutId() = R.layout.family_fragment_device_empty

    override fun <T : ViewModel?> loadViewModel() = EmptyDeviceVM::class.java as Class<T>

    override fun initView(view: View, savedInstanceState: Bundle?) {
        super.initView(view, savedInstanceState)
        mViewBinding.addBtn.setOnClickListener {
            saEventApi.addEvent(ISaEventApi.BUTTON_CLICK)
            ReoqooRouterPath
                .ConfigPath
                .CONFIG_SCAN_ACTIVITY_PATH
                .navigation(fragment = null)
        }
    }
}