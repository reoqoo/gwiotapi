package com.gw_reoqoo.cp_account.ui.activity.bind_account

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.NavHostFragment
import com.gw_reoqoo.cp_account.R
import com.gw_reoqoo.cp_account.databinding.AccountActivityBindBinding
import com.gw_reoqoo.cp_account.entity.ParamConstants
import com.gw_reoqoo.cp_account.entity.ParamConstants.PARAM_BIND_TYPE
import com.gw_reoqoo.cp_account.ui.activity.bind_account.vm.BindAccountVM
import com.gw_reoqoo.lib_base_architecture.view.ABaseMVVMDBActivity
import com.gw_reoqoo.lib_router.ReoqooRouterPath
import com.therouter.router.Autowired
import com.therouter.router.Route
import dagger.hilt.android.AndroidEntryPoint

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/9/14 10:24
 * Description: BindAcountActivty
 */
@AndroidEntryPoint
@Route(path = ReoqooRouterPath.AccountPath.ACTIVITY_BIND_ACCOUNT)
class BindAccountActivity : ABaseMVVMDBActivity<AccountActivityBindBinding, BindAccountVM>() {

    companion object {
        private const val TAG = "BindAccountActivity"
    }

    @JvmField
    @Autowired(name = PARAM_BIND_TYPE)
    var accountType = ParamConstants.TYPE_MOBILE

    override fun initView() {
    }

    override fun initData(savedInstanceState: Bundle?) {
        super.initData(savedInstanceState)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment?
        val bundle = Bundle()
        bundle.putInt(PARAM_BIND_TYPE, accountType)
        navHostFragment?.navController?.setGraph(R.navigation.gw_reoqoo_account_bind_graph, bundle)
    }

    override fun getLayoutId() = R.layout.account_activity_bind

    override fun <T : ViewModel?> loadViewModel() = BindAccountVM::class.java as Class<T>

}