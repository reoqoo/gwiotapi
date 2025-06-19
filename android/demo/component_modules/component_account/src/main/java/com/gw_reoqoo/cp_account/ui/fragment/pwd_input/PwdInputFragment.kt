package com.gw_reoqoo.cp_account.ui.fragment.pwd_input

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import com.gw_reoqoo.cp_account.R
import com.gw_reoqoo.cp_account.databinding.AccountFragmentPwdInputBinding
import com.gw_reoqoo.cp_account.entity.AccountInputType
import com.gw_reoqoo.cp_account.sa.AccountSaEvent
import com.gw_reoqoo.cp_account.utils.AccountUtils
import com.gw_reoqoo.lib_base_architecture.view.ABaseMVVMDBFragment
import com.gw_reoqoo.lib_utils.ktx.enable
import com.gw_reoqoo.lib_utils.ktx.visible
import com.gw_reoqoo.lib_widget.title.WidgetCommonTitleView
import com.gwell.loglibs.GwellLogUtils
import com.jwkj.base_statistics.sa.kits.SA
import dagger.hilt.android.AndroidEntryPoint
import com.gw_reoqoo.resource.R as RR

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/8/1 16:56
 * Description: 密码输入页
 */
@AndroidEntryPoint
class PwdInputFragment : ABaseMVVMDBFragment<AccountFragmentPwdInputBinding, PwdInputVM>() {

    companion object {
        private const val TAG = "PwdInputFragment"
    }

    override fun initView(view: View, savedInstanceState: Bundle?) {
        super.initView(view, savedInstanceState)

        mViewBinding.layoutTitle.setTitleListener(object :
            WidgetCommonTitleView.OnTitleClickListener {
            override fun onLeftClick() {
                findNavController().navigateUp()
            }

            override fun onRightClick() {

            }
        })
        // 监听输入框变化,校验两次密码是否一致，并且控制密码不一致，以及完成按钮的显隐
        fun afterChange() {
            val firstPasswd = mViewBinding.layoutSetPwd.getText()
            val rePasswd = mViewBinding.layoutPwdConfirm.getText()
            if (firstPasswd.isEmpty()) {
                mViewBinding.tvPwdLevel.visible(false)
                mViewBinding.tvInputReminder.visible(false)
                mViewBinding.btnFinish.enable(false)
                return
            }
            val pwdFormat = AccountUtils.checkPasswordFormat(firstPasswd)
            mViewBinding.tvPwdLevel.visible(!pwdFormat)
            if (rePasswd.isEmpty()) {
                mViewBinding.tvInputReminder.visible(false)
                mViewBinding.btnFinish.enable(false)
                return
            }
            mViewBinding.tvInputReminder.isVisible = firstPasswd != rePasswd
            val pwdSame = firstPasswd == rePasswd
            mViewBinding.btnFinish.isEnabled = pwdSame && pwdFormat
        }

        mViewBinding.layoutSetPwd.addTextWatcher(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                val confirmPwd = mViewBinding.layoutPwdConfirm.getText()
                GwellLogUtils.i(TAG, "etPwd: pwd = ${s?.toString()}, confirmPwd = $confirmPwd")
                afterChange()
            }
        })

        // 确认密码输入栏的功能
        mViewBinding.layoutPwdConfirm.addTextWatcher(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                afterChange()
            }
        })

        mViewBinding.btnFinish.setOnClickListener {
            val pwd = mViewBinding.layoutSetPwd.getText().trim()
            val pwdConfirm = mViewBinding.layoutPwdConfirm.getText().trim()
            // 校验密码和确认密码是否一致
            if (pwd != pwdConfirm) {
                GwellLogUtils.i(TAG, "btnFinish click, but pwd is not equal")
                toast.show(RR.string.AA0373)
                return@setOnClickListener
            }
            // 校验密码格式
            if (!AccountUtils.checkPasswordFormat(pwd)) {
                toast.show(RR.string.AA0396)
                return@setOnClickListener
            }
            SA.track(
                AccountSaEvent.SET_PASSWORD,
                mapOf(AccountSaEvent.EventAttr.SET_PASSWORDRESULT to "1")
            )
            // 密码校验通过，根据入口进行相应处理
            when (mFgViewModel.inputType) {
                AccountInputType.ACCOUNT_REGISTER -> {
                    // 账号注册
                    mFgViewModel.setPwdFromAccount(pwd)
                }

                AccountInputType.ACCOUNT_FORGET -> {
                    // 忘记密码
                    mFgViewModel.setPwdFromForgetPwd(pwd)
                }

                else -> {}
            }
        }
    }

    override fun initData() {
        super.initData()

        arguments?.let {
            PwdInputFragmentArgs.fromBundle(it).run {
                mFgViewModel.account = this.keyAccount
                mFgViewModel.vCode = this.keyVerifyCode
                mFgViewModel.districtEntity = this.keyDistrictBean
                mFgViewModel.registerType = this.keyRegisterType
                mFgViewModel.inputType = this.keyFromPage
            }
        } ?: let {
            GwellLogUtils.i(TAG, "arguments has no data")
            findNavController().navigateUp()
        }

        when (mFgViewModel.inputType) {
            AccountInputType.ACCOUNT_REGISTER -> {
                // 账号注册
                mViewBinding.tvSetPwdTitle.text = getString(RR.string.AA0024)
            }

            AccountInputType.ACCOUNT_FORGET -> {
                // 忘记密码
                mViewBinding.tvSetPwdTitle.text = getString(RR.string.AA0024)
            }

            else -> {}
        }
    }

    override fun initLiveData(viewModel: PwdInputVM, savedInstanceState: Bundle?) {
        super.initLiveData(viewModel, savedInstanceState)

        mFgViewModel.jumpLogin.observe(this) {
            if (it) {
                findNavController().popBackStack(R.id.account_loginfragment, false)
            }
        }

        mFgViewModel.jumpFindSuccess.observe(this) {
            if (it) {
                findNavController().navigate(R.id.account_retrievepwdresultfragment)
            }
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.account_fragment_pwd_input
    }

    override fun <T : ViewModel?> loadViewModel(): Class<T> {
        return PwdInputVM::class.java as Class<T>
    }

}