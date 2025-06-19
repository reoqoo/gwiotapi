package com.gw_reoqoo.cp_account.ui.activity.login

import android.os.Bundle
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.lifecycle.ViewModel
import com.gw.component_debug.api.interfaces.IAppEvnApi
import com.gw_reoqoo.cp_account.R
import com.gw_reoqoo.cp_account.databinding.AccountActivityLoginBinding
import com.gw_reoqoo.cp_account.entity.ParamConstants
import com.gw_reoqoo.lib_base_architecture.view.ABaseMVVMDBActivity
import com.gw_reoqoo.lib_router.ReoqooRouterPath
import com.gw_reoqoo.lib_router.RouterParam
import com.jwkj.base_utils.ui.DensityUtil
import com.therouter.router.Autowired
import com.therouter.router.Route
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

import com.gw_reoqoo.resource.R as RR


/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/7/28 11:05
 * Description: 登录页面
 */
@AndroidEntryPoint
@Route(
    path = ReoqooRouterPath.AccountPath.LOGIN_ACTIVITY_PATH,
    params = [RouterParam.PARAM_NEED_LOGIN, "false"]
)
class LoginActivity : ABaseMVVMDBActivity<AccountActivityLoginBinding, LoginVM>() {

    companion object {
        private const val TAG = "LoginActivity"
    }

    @Inject
    lateinit var appEnvApi: IAppEvnApi

    @JvmField
    @Autowired(name = ParamConstants.PARAM_BIND_TYPE)
    var entrance: Int = 1

    override fun getLayoutId(): Int {
        return R.layout.account_activity_login
    }

    override fun initView() {
        // 调试模式右上角增加角标
        if (appEnvApi.isDebugEnvMode()) {
            val img = ImageView(this)
            img.setImageResource(RR.drawable.gw_reoqoo_debug_icon)
            val params =
                FrameLayout.LayoutParams(DensityUtil.dip2px(this, 40), DensityUtil.dip2px(this, 40))
            params.gravity = Gravity.END
            addContentView(img, params)
        }
    }

    override fun initData(savedInstanceState: Bundle?) {
        super.initData(savedInstanceState)
    }

    override fun <T : ViewModel?> loadViewModel(): Class<T> {
        return LoginVM::class.java as Class<T>
    }

}