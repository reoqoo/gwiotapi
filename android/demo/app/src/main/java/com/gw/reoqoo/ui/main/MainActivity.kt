package com.gw.reoqoo.ui.main

import android.app.Application
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import com.gw.component_push.api.interfaces.IPushApi
import com.gw.reoqoo.R
import com.gw.reoqoo.databinding.AppActivityMainBinding
import com.gw.reoqoo.ui.fragment.MineFragment
import com.gw.reoqoosdk.dev_monitor.IMonitorService
import com.gw_reoqoo.component_family.ui.family.FamilyFragment
import com.gw_reoqoo.house_watch.receivers.api.INetworkStatusApi
import com.gw_reoqoo.lib_base_architecture.ToolBarLoadStrategy
import com.gw_reoqoo.lib_base_architecture.view.ABaseMVVMDBActivity
import com.gw_reoqoo.lib_router.ReoqooRouterPath
import com.gw_reoqoo.lib_router.RouterParam.PARAM_NEED_LOGIN
import com.gw_reoqoo.lib_router.createFragment
import com.gwell.loglibs.GwellLogUtils
import com.jwkj.iotvideo.init.IoTVideoInitializerState
import com.therouter.router.Route
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
@Route(path = ReoqooRouterPath.AppPath.MAIN_ACTIVITY_PATH, params = [PARAM_NEED_LOGIN, "true"])
class MainActivity : ABaseMVVMDBActivity<AppActivityMainBinding, MainVM>() {
    companion object {
        private const val TAG = "MainActivity"
        private const val FRAGMENT_KEY = "FRAGMENT_KEY_%d"
        private const val CURRENT_ITEM_KEY = "CURRENT_ITEM_ID"
    }

    override fun getLayoutId() = R.layout.app_activity_main
    override fun <T : ViewModel?> loadViewModel() = MainVM::class.java as Class<T>

    @Inject
    lateinit var app: Application

    @Inject
    lateinit var pushApi: IPushApi

    @Inject
    lateinit var networkStatusApi: INetworkStatusApi

    @Inject
    lateinit var iMonitorService: IMonitorService

    private val fragmentsMap = LinkedHashMap<Int, Fragment?>()
    private var currentItemId: Int = R.id.navigation_family

    override fun initView() {
        mToolBarConfig.toolBarLoadStrategy = ToolBarLoadStrategy.NO_TOOLBAR
        mViewBinding.navBtnMenu.itemIconTintList = null
        mViewBinding.navBtnMenu.setOnItemSelectedListener {
            setFragment(it.itemId)
            true
        }
    }

    override fun initData(savedInstanceState: Bundle?) {
        super.initData(savedInstanceState)
        // 启动监听网络状态的广播
        networkStatusApi.startReceiver()
        // 刷新用户信息
        mViewModel.refreshUserInfo()
        if (savedInstanceState != null) {
            val familyFragment = supportFragmentManager.getFragment(
                savedInstanceState,
                FRAGMENT_KEY.format(R.id.navigation_family)
            )
            fragmentsMap[R.id.navigation_family] = familyFragment
            val mineFragment = supportFragmentManager.getFragment(
                savedInstanceState,
                FRAGMENT_KEY.format(R.id.navigation_mine)
            )
            fragmentsMap[R.id.navigation_mine] = mineFragment
            currentItemId = savedInstanceState.getInt(CURRENT_ITEM_KEY, R.id.navigation_family)
        }

        setFragment(currentItemId)
    }

    override fun initLiveData(viewModel: MainVM, savedInstanceState: Bundle?) {
        super.initLiveData(viewModel, savedInstanceState)
        mViewModel.accountApi.watchUserInfo().observeForever {
            GwellLogUtils.i(TAG, "watchUserInfo: $it")
            // 由于在插件OOM时，可能会导致app crash然后application重启，但是不会重新拉起logo页，所以这里增加了IoTSDK的注册逻辑
            if (it != null) {
                mViewModel.iotSdkInitMgr.registerSdk(it.accessId, it.accessToken)
                iMonitorService.register(app)
            } else {
                mViewModel.iotSdkInitMgr.unregisterSdk()
            }
        }
        mViewModel.accountApi.getIotSdkState()?.observe(this) {
            GwellLogUtils.i(TAG, "getIotSdkState: $it")
            when (it) {
                IoTVideoInitializerState.ONLINE -> {
                    pushApi.registerPushServer()
                }
                else -> {}
            }
        }
    }


    override fun onViewLoadFinish() {
        setStatusBarColor()
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
        //super.onBackPressed()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        for ((id, fragment) in fragmentsMap) {
            if (fragment == null) continue
            supportFragmentManager.putFragment(outState, FRAGMENT_KEY.format(id), fragment)
        }
        outState.putInt(CURRENT_ITEM_KEY, currentItemId)
        super.onSaveInstanceState(outState)
    }

    /**
     * 跳转到指定的fragment
     */
    private fun setFragment(itemId: Int) {
        this.currentItemId = itemId
        var fragment = fragmentsMap[itemId]
        if (fragment == null) {
            fragment = when (itemId) {
                R.id.navigation_family -> {
                    val familyFragment: Fragment = ReoqooRouterPath
                        .Family
                        .FAMILY_FRAGMENT_PATH
                        .createFragment<Fragment>()
                        ?: FamilyFragment()
                    familyFragment
                }

                R.id.navigation_mine -> {
                    val mineFragment: Fragment = ReoqooRouterPath
                        .MinePath
                        .FRAGMENT_MAIN
                        .createFragment<Fragment>()
                        ?: MineFragment()
                    mineFragment
                }

                else -> return
            }
            fragmentsMap[itemId] = fragment
        }
        val fm = supportFragmentManager
        val transaction = fm.beginTransaction()
        if (fragment.isAdded) {
            transaction.show(fragment)
        } else {
            transaction.add(mViewBinding.menuFrame.id, fragment)
        }
        fragmentsMap
            .values
            .filterNotNull()
            .filter { it != fragment }
            .forEach(transaction::hide)
        transaction.commitNowAllowingStateLoss()
    }

}