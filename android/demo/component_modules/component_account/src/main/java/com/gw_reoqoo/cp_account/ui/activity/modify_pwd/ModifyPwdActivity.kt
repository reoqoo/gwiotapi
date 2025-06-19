package com.gw_reoqoo.cp_account.ui.activity.modify_pwd

import android.text.Editable
import android.text.TextWatcher
import androidx.lifecycle.ViewModel
import com.gw_reoqoo.cp_account.R
import com.gw_reoqoo.cp_account.databinding.AccountActivityModifyPwdBinding
import com.gw_reoqoo.cp_account.ui.activity.modify_pwd.vm.ModifyPwdVM
import com.gw_reoqoo.lib_base_architecture.view.ABaseMVVMDBActivity
import com.gw_reoqoo.lib_router.ReoqooRouterPath
import com.therouter.router.Route
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

import com.gw_reoqoo.resource.R as RR

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/9/14 1:55
 * Description: ModifyPwdActivity
 */
@AndroidEntryPoint
@Route(path = ReoqooRouterPath.AccountPath.ACTIVITY_MODIFY_PWD)
class ModifyPwdActivity @Inject constructor() :
    ABaseMVVMDBActivity<AccountActivityModifyPwdBinding, ModifyPwdVM>() {


    override fun initView() {
        mViewBinding.appTitle.run {
            leftIcon.setOnClickListener { finish() }
        }

        mViewBinding.layoutOldPwd.addTextWatcher(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                val oldPwdSize = s?.toString()?.length ?: 0
                val isNewPwdRight = mViewBinding.layoutSetPwd.getText().length >= 8
                val isConfirmPwdRight = mViewBinding.layoutPwdConfirm.getText().length >= 8
                mViewBinding.btnFinish.isEnabled =
                    isNewPwdRight && isConfirmPwdRight && oldPwdSize >= 8
            }
        })
        mViewBinding.layoutSetPwd.addTextWatcher(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                val newPwdSize = s?.toString()?.length ?: 0
                val isOldPwdRight = mViewBinding.layoutOldPwd.getText().length >= 8
                val isConfirmPwdRight = mViewBinding.layoutPwdConfirm.getText().length >= 8
                mViewBinding.btnFinish.isEnabled =
                    isOldPwdRight && isConfirmPwdRight && newPwdSize >= 8
            }
        })
        mViewBinding.layoutPwdConfirm.addTextWatcher(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                val confirmPwd = s?.toString()?.length ?: 0
                val oldPwd = mViewBinding.layoutOldPwd.getText().length >= 8
                val newPwd = mViewBinding.layoutPwdConfirm.getText().length >= 8
                mViewBinding.btnFinish.isEnabled = oldPwd && newPwd && confirmPwd >= 8
            }
        })

        mViewBinding.btnFinish.setOnClickListener {
            val oldPwd = mViewBinding.layoutOldPwd.getText()
            val newPwd = mViewBinding.layoutSetPwd.getText()
            val confirmPwd = mViewBinding.layoutPwdConfirm.getText()
            if (newPwd != confirmPwd) {
                toast.show(RR.string.AA0373)
                return@setOnClickListener
            }
            mViewModel.modifyPwd(oldPwd, newPwd)
        }

    }

    override fun getLayoutId() = R.layout.account_activity_modify_pwd

    override fun getTitleView() = mViewBinding.appTitle

    override fun <T : ViewModel?> loadViewModel() = ModifyPwdVM::class.java as Class<T>
}