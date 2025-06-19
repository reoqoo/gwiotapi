package com.gw.reoqoo.ui.logo

import android.os.Bundle
import androidx.core.view.isInvisible
import androidx.lifecycle.ViewModel
import com.gw_reoqoo.cp_account.api.kapi.IAccountApi
import com.gw.cp_config.api.AppChannelName
import com.gw.cp_config.api.IAppConfigApi
import com.gw.cp_config.api.IAppParamApi
import com.gw_reoqoo.lib_base_architecture.view.ABaseMVVMDBActivity
import com.gw_reoqoo.lib_router.ReoqooRouterPath
import com.gw_reoqoo.lib_router.RouterParam
import com.gw_reoqoo.lib_utils.ktx.launch
import com.gw_reoqoo.lib_utils.ktx.visible
import com.gw.reoqoo.BuildConfig
import com.gw.reoqoo.R
import com.gw.reoqoo.databinding.AppActivityLogoBinding
import com.gwell.loglibs.GwellLogUtils
import com.jwkj.base_lifecycle.activity_lifecycle.ActivityLifecycleManager
import com.therouter.router.Route
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import java.util.Locale
import javax.inject.Inject


/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/7/27 13:55
 * Description: LogoActivity
 */
@Route(
    path = ReoqooRouterPath.AppPath.LOGO_ACTIVITY_PATH,
    params = [RouterParam.PARAM_NEED_LOGIN, "false"]
)
@AndroidEntryPoint
class LogoActivity : ABaseMVVMDBActivity<AppActivityLogoBinding, LogoVM>() {
    companion object {
        private const val TAG = "LogoActivity"

    }

    @Inject
    lateinit var accountApi: IAccountApi

    @Inject
    lateinit var appParamApi: IAppParamApi

    override fun getLayoutId() = R.layout.app_activity_logo

    override fun <T : ViewModel?> loadViewModel() = LogoVM::class.java as Class<T>

    override fun initView() {
        GwellLogUtils.i(TAG, "countryCode ${Locale.getDefault().country}")
        mViewBinding.ivAppLogo.visible(!AppChannelName.isIpTimeApp(appParamApi.getAppName()))
        launch {
            delay(1000)
            if (ActivityLifecycleManager.getMainActivity() == null) {
                mViewModel.goMainAction()
            }
            finish()
        }
    }

    override fun initData(savedInstanceState: Bundle?) {
        super.initData(savedInstanceState)
        mViewModel.initData(intent)
    }

    override fun initLiveData(viewModel: LogoVM, savedInstanceState: Bundle?) {
        super.initLiveData(viewModel, savedInstanceState)
        accountApi.watchUserInfo().observe(this) {
            mViewModel.iotVideoRegister(it)
        }
    }

    override fun onViewLoadFinish() {
        setStatusBarColor()
    }

}