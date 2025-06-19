package com.gw_reoqoo.cp_account.ui.activity.show_bind_account

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModel
import com.gw_reoqoo.cp_account.R
import com.gw_reoqoo.cp_account.databinding.AccountActivityShowAccountBinding
import com.gw_reoqoo.cp_account.entity.ParamConstants
import com.gw_reoqoo.cp_account.entity.ParamConstants.PARAM_BIND_TYPE
import com.gw_reoqoo.cp_account.entity.ParamConstants.TYPE_MOBILE
import com.gw_reoqoo.cp_account.ui.activity.show_bind_account.vm.ShowAccountVM
import com.gw_reoqoo.cp_account.ui.activity.user_info.vm.UserInfoVM.Companion.FINISH_ACTIVITY_CODE
import com.gw_reoqoo.lib_base_architecture.PageJumpData
import com.gw_reoqoo.lib_base_architecture.view.ABaseMVVMDBActivity
import com.gw_reoqoo.lib_router.ReoqooRouterPath
import com.gwell.loglibs.GwellLogUtils
import com.therouter.TheRouter
import com.therouter.router.Autowired
import com.therouter.router.Route
import com.gw_reoqoo.resource.R as RR

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/9/14 3:12
 * Description: ShowAccountActivity
 */
@Route(path = ReoqooRouterPath.AccountPath.ACTIVITY_SHOW_ACCOUNT)
class ShowAccountActivity :
    ABaseMVVMDBActivity<AccountActivityShowAccountBinding, ShowAccountVM>() {

    companion object {
        private const val TAG = "ShowAccountActivity"
    }

    @JvmField
    @Autowired(name = PARAM_BIND_TYPE)
    var accountType: Int = TYPE_MOBILE

    @Autowired(name = ParamConstants.PARAM_ACCOUNT_INFO)
    lateinit var account: String

    override fun initView() {
        mViewBinding.btnSure.setOnClickListener {
            mViewModel.pageJumpData.postValue(
                PageJumpData(
                    TheRouter.build(ReoqooRouterPath.AccountPath.ACTIVITY_BIND_ACCOUNT)
                        .withInt(PARAM_BIND_TYPE, accountType),
                    requestCode = FINISH_ACTIVITY_CODE
                )
            )
        }
    }

    override fun initData(savedInstanceState: Bundle?) {
        super.initData(savedInstanceState)
        when (accountType) {
            TYPE_MOBILE -> {
                mViewBinding.layoutTitle.setTitle(getString(RR.string.AA0580))
                mViewBinding.ivIcon.setImageResource(R.drawable.gw_reoqoo_icon_bind_mobile)
                mViewBinding.btnSure.text = getString(RR.string.AA0298)
            }

            ParamConstants.TYPE_EMAIL -> {
                mViewBinding.layoutTitle.setTitle(getString(RR.string.AA0581))
                mViewBinding.ivIcon.setImageResource(R.drawable.gw_reoqoo_icon_bind_email)
                mViewBinding.btnSure.text = getString(RR.string.AA0304)
            }
        }

        mViewBinding.tvAccount.text = buildString {
            append(getString(RR.string.AA0297))
            append(account)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        GwellLogUtils.i(
            TAG,
            "onActivityResult: requestCode $requestCode, resultCode $resultCode, data $data"
        )
        if (requestCode == FINISH_ACTIVITY_CODE) {
            if (resultCode == RESULT_OK) {
                setResult(RESULT_OK)
                finish()
            }
        }
    }

    override fun getTitleView(): View? {
        return mViewBinding.layoutTitle
    }

    override fun getLayoutId() = R.layout.account_activity_show_account

    override fun <T : ViewModel?> loadViewModel() = ShowAccountVM::class.java as Class<T>
}