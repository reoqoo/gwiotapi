package com.gw_reoqoo.cp_account.ui.activity.account_close

import android.text.method.ScrollingMovementMethod
import androidx.lifecycle.ViewModel
import com.gw_reoqoo.cp_account.R
import com.gw_reoqoo.cp_account.databinding.AccountActivityAccountCloseBinding
import com.gw_reoqoo.cp_account.entity.ParamConstants.PARAM_CLOSE_REASON_TYPE
import com.gw_reoqoo.cp_account.entity.ParamConstants.PARAM_CLOSE_REASON_VALUE
import com.gw_reoqoo.cp_account.ui.activity.account_close.vm.AccountCloseVM
import com.gw_reoqoo.lib_base_architecture.PageJumpData
import com.gw_reoqoo.lib_base_architecture.view.ABaseMVVMDBActivity
import com.gw_reoqoo.lib_router.ReoqooRouterPath
import com.gwell.loglibs.GwellLogUtils
import com.therouter.TheRouter
import com.therouter.router.Autowired
import com.therouter.router.Route

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/9/14 20:38
 * Description: AccountCloseActivity
 */
@Route(path = ReoqooRouterPath.AccountPath.ACTIVITY_CLOSE_ACCOUNT)
class AccountCloseActivity :
    ABaseMVVMDBActivity<AccountActivityAccountCloseBinding, AccountCloseVM>() {

    companion object {
        private const val TAG = "AccountCloseActivity"
    }

    @JvmField
    @Autowired(name = PARAM_CLOSE_REASON_TYPE)
    var reasonType: Int? = null

    @JvmField
    @Autowired(name = PARAM_CLOSE_REASON_VALUE)
    var reasonValue: String? = null

    override fun initView() {

        mViewBinding.tvContent.movementMethod = ScrollingMovementMethod()

        mViewBinding.btnNext.setOnClickListener {
            GwellLogUtils.i(TAG, "reasonType $reasonType, reasonValue $reasonValue")
            reasonType?.let {
                mViewModel.pageJumpData.postValue(
                    PageJumpData(
                        TheRouter.build(ReoqooRouterPath.AccountPath.ACTIVITY_CLOSE_VERIFY)
                            .withInt(PARAM_CLOSE_REASON_TYPE, it)
                            .withString(PARAM_CLOSE_REASON_VALUE, reasonValue)
                    )
                )
            }
        }
    }

    override fun getLayoutId() = R.layout.account_activity_account_close

    override fun <T : ViewModel?> loadViewModel() = AccountCloseVM::class.java as Class<T>

    override fun getTitleView() = mViewBinding.layoutTitle
}