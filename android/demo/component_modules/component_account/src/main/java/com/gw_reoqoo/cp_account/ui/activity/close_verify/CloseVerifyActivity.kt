package com.gw_reoqoo.cp_account.ui.activity.close_verify

import android.text.Editable
import android.text.TextWatcher
import androidx.lifecycle.ViewModel
import com.gw_reoqoo.cp_account.R
import com.gw_reoqoo.cp_account.databinding.AccountActivityCloseVerifyBinding
import com.gw_reoqoo.cp_account.entity.ParamConstants
import com.gw_reoqoo.cp_account.ui.activity.close_verify.vm.CloseVerifyVM
import com.gw_reoqoo.lib_base_architecture.view.ABaseMVVMDBActivity
import com.gw_reoqoo.lib_router.ReoqooRouterPath
import com.gw_reoqoo.lib_widget.dialog.comm_dialog.entity.CommDialogAction
import com.gw_reoqoo.lib_widget.dialog.comm_dialog.entity.TextContent
import com.gw_reoqoo.lib_widget.dialog.comm_dialog.ext.showCommDialog
import com.therouter.router.Autowired
import com.therouter.router.Route
import dagger.hilt.android.AndroidEntryPoint

import com.gw_reoqoo.resource.R as RR

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/9/14 21:14
 * Description: CloseVerifyActivity
 */
@AndroidEntryPoint
@Route(path = ReoqooRouterPath.AccountPath.ACTIVITY_CLOSE_VERIFY)
class CloseVerifyActivity :
    ABaseMVVMDBActivity<AccountActivityCloseVerifyBinding, CloseVerifyVM>() {

    companion object {
        private const val TAG = "CloseVerifyActivity"
    }

    @JvmField
    @Autowired(name = ParamConstants.PARAM_CLOSE_REASON_TYPE)
    var reasonType: Int? = null

    @Autowired(name = ParamConstants.PARAM_CLOSE_REASON_VALUE)
    lateinit var reasonValue: String

    override fun initView() {
        mViewBinding.layoutSetPwd.addTextWatcher(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                s?.toString()?.run {
                    mViewBinding.btnFinish.isEnabled = length in 8..29
                }
            }

        })

        mViewBinding.btnFinish.setOnClickListener {
            val pwd = mViewBinding.layoutSetPwd.getText()
            showCommDialog {
                title = getString(RR.string.AA0328)
                content = TextContent(text = getString(RR.string.AA0329))
                actions = listOf(
                    CommDialogAction(getString(RR.string.AA0059)),
                    CommDialogAction(
                        getString(RR.string.AA0058),
                        isDestructiveAction = true,
                        onClick = {
                            mViewModel.closeAccount(
                                pwd,
                                0,
                                reasonType ?: 0,
                                reasonValue
                            )
                        })
                )
            }
        }
    }

    override fun getLayoutId(): Int = R.layout.account_activity_close_verify

    override fun <T : ViewModel?> loadViewModel() = CloseVerifyVM::class.java as Class<T>

    override fun getTitleView() = mViewBinding.layoutTitle
}