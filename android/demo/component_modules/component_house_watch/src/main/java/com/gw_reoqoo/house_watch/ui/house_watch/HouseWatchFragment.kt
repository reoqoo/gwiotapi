package com.gw_reoqoo.house_watch.ui.house_watch

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import com.gw_reoqoo.component_house_watch.R
import com.gw_reoqoo.house_watch.ui.empty_device.EmptyDeviceFragment
import com.gw_reoqoo.component_house_watch.databinding.HouseWatchFragmentMainBinding as Binding
import com.gw_reoqoo.house_watch.ui.house_watch.vm.HouseWatchVM
import com.gw_reoqoo.house_watch.ui.card_list.CardListFragment
import com.gw_reoqoo.lib_base_architecture.view.ABaseMVVMDBFragment
import com.gw_reoqoo.lib_router.ReoqooRouterPath
import com.gw_reoqoo.lib_router.createFragment
import com.therouter.router.Route
import dagger.hilt.android.AndroidEntryPoint

/**
 * @Description: - 看家主界面
 * @Author: XIAOLEI
 * @Date: 2023/8/18
 */
@Route(path = ReoqooRouterPath.HouseWatch.FRAGMENT_MAIN)
@AndroidEntryPoint
class HouseWatchFragment : ABaseMVVMDBFragment<Binding, HouseWatchVM>() {
    /**
     * 空界面
     */
    private var currentFragment: Fragment? = null

    override fun getLayoutId() = R.layout.house_watch_fragment_main
    override fun <T : ViewModel?> loadViewModel() = HouseWatchVM::class.java as Class<T>
    override fun initLiveData(viewModel: HouseWatchVM, savedInstanceState: Bundle?) {
        super.initLiveData(viewModel, savedInstanceState)
        mFgViewModel.watchDeviceList()?.observe(this) { devices ->
            if (devices.isEmpty()) {
                if (currentFragment !is EmptyDeviceFragment) {
                    val emptyDeviceFragment = ReoqooRouterPath
                        .HouseWatch
                        .FRAGMENT_EMPTY_DEVICE
                        .createFragment<Fragment>()
                    if (emptyDeviceFragment != null) {
                        currentFragment = emptyDeviceFragment
                        val transaction = childFragmentManager.beginTransaction()
                        transaction.replace(mViewBinding.fragmentLayout.id, emptyDeviceFragment)
                        transaction.commitNow()
                    }
                }
            } else {
                if (currentFragment !is CardListFragment) {
                    val watchListFragment = ReoqooRouterPath
                        .HouseWatch
                        .FRAGMENT_CARD_LIST
                        .createFragment<Fragment>()
                    if (watchListFragment != null) {
                        currentFragment = watchListFragment
                        val transaction = childFragmentManager.beginTransaction()
                        transaction.replace(mViewBinding.fragmentLayout.id, watchListFragment)
                        transaction.commitNow()
                    }
                }
            }
        }
    }
}