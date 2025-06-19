package com.gw_reoqoo.cp_account.ui.activity.close_success

import androidx.lifecycle.ViewModel
import com.gw_reoqoo.cp_account.R
import com.gw_reoqoo.cp_account.databinding.AccountActivityCloseSuccessBinding
import com.gw_reoqoo.cp_account.ui.activity.close_success.vm.CloseSuccessVM
import com.gw_reoqoo.lib_base_architecture.view.ABaseMVVMDBActivity
import com.gw_reoqoo.lib_router.ReoqooRouterPath
import com.therouter.router.Route

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/9/15 0:19
 * Description: CloseSuccessActivity
 */
@Route(path = ReoqooRouterPath.AccountPath.ACTIVITY_CLOSE_SUCCESS)
class CloseSuccessActivity :
    ABaseMVVMDBActivity<AccountActivityCloseSuccessBinding, CloseSuccessVM>() {
    override fun initView() {
        mViewBinding.btnNext.setOnClickListener {
            mViewModel.userLogout()
        }
    }

    override fun getLayoutId() = R.layout.account_activity_close_success

    override fun <T : ViewModel?> loadViewModel() = CloseSuccessVM::class.java as Class<T>
}