package com.gw_reoqoo.cp_account.ui.fragment.account_input

import android.os.Bundle
import android.text.Editable
import android.text.TextPaint
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import com.gw.component_website.api.interfaces.IWebsiteApi
import com.gw.component_webview.api.interfaces.IWebViewApi
import com.gw_reoqoo.cp_account.R
import com.gw_reoqoo.cp_account.databinding.AccountFragmentAccountInputBinding
import com.gw_reoqoo.cp_account.entity.AccountInputType
import com.gw_reoqoo.cp_account.kits.PrivatePolicyStringKit
import com.gw_reoqoo.cp_account.ui.fragment.ShareVM
import com.gw.cp_config.api.AppChannelName
import com.gw.cp_config.api.IAppParamApi
import com.gw_reoqoo.lib_base_architecture.view.ABaseMVVMDBFragment
import com.gw_reoqoo.lib_utils.ktx.dp
import com.gw_reoqoo.lib_utils.ktx.invisible
import com.gw_reoqoo.lib_utils.ktx.visible
import com.gw_reoqoo.lib_widget.dialog.CommonDialog
import com.gwell.loglibs.GwellLogUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.gw_reoqoo.resource.R as RR

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/8/1 16:54
 * Description: 账号输入页
 */
@AndroidEntryPoint
class AccountInputFragment :
    ABaseMVVMDBFragment<AccountFragmentAccountInputBinding, AccountInputVM>() {

    companion object {
        private const val TAG = "AccountInputFragment"
    }

    @Inject
    lateinit var webViewApi: IWebViewApi

    @Inject
    lateinit var websiteApi: IWebsiteApi

    @Inject
    lateinit var appParamApi: IAppParamApi

    /**
     * 共享VM，主要处理数据回传的问题
     */
    private val shareVM: ShareVM by activityViewModels()

    override fun initView(view: View, savedInstanceState: Bundle?) {
        super.initView(view, savedInstanceState)
        mViewBinding.appTitle.run {
            leftIcon.setOnClickListener {
                findNavController().navigateUp()
            }
        }

        mViewBinding.tvAgree.movementMethod = LinkMovementMethod.getInstance()
        mViewBinding.tvAgree.text =
            PrivatePolicyStringKit.getAgreement(requireActivity(), false, showUserProtocol = {
                val userProtocol = websiteApi.getUserProtocolUrl()
                webViewApi.openWebView(userProtocol, getString(RR.string.AA0044))
            }, showPrivacyProtocol = {
                val userPrivacyUrl = websiteApi.getUserPrivacyUrl()
                webViewApi.openWebView(userPrivacyUrl, getString(RR.string.AA0368))
            })

        mViewBinding.tvArea.setOnClickListener {
            val action =
                AccountInputFragmentDirections.accountActionAccountAccountinputfragmentToAccountArealistfragment(
                    shareVM.districtLD.value
                )
            findNavController().navigate(action)
        }

        mViewBinding.etAccountNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                mViewBinding.btnAccountNext.isEnabled = !s.isNullOrEmpty()
                mViewBinding.tvErrNotice.text = ""
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        mViewBinding.imgAgree.setOnCheckedChangeListener { _, isChecked ->
            mFgViewModel.agreeBtnClick(isChecked)
        }

        mViewBinding.btnAccountNext.setOnClickListener {
            val accountString = mViewBinding.etAccountNumber.text.toString()
            if (accountString.isEmpty()) {
                toast.show(com.gw_reoqoo.resource.R.string.AA0369)
                return@setOnClickListener
            }
            if (mViewBinding.imgAgree.visibility == View.VISIBLE && mFgViewModel.isAgree.value == false) {
                context?.let {
                    val content = PrivatePolicyStringKit.getAgreement(
                        it,
                        false,
                        showUserProtocol = {
                            val userProtocol = websiteApi.getUserProtocolUrl()
                            GwellLogUtils.i(TAG, "userProtocol $userProtocol")
                            webViewApi.openWebView(userProtocol, getString(RR.string.AA0044))
                        },
                        showPrivacyProtocol = {
                            val userPrivacyUrl = websiteApi.getUserPrivacyUrl()
                            GwellLogUtils.i(TAG, "userPrivacyUrl $userPrivacyUrl")
                            webViewApi.openWebView(
                                userPrivacyUrl,
                                getString(RR.string.AA0368)
                            )
                        })
                    val dialog = CommonDialog.Builder(it)
                        .spannable(content)
                        .cancelTxt(getString(com.gw_reoqoo.resource.R.string.AA0011))
                        .sureTxt(getString(com.gw_reoqoo.resource.R.string.AA0010))
                        .build()
                    dialog.hideTitle()
                    dialog.setButtonListener(onSureClick = {
                        mFgViewModel.agreeTxtClick(true)
                        mFgViewModel.getVerifyCode(accountString)
                    })
                    dialog.show()
                    return@setOnClickListener
                }
            }
            mFgViewModel.getVerifyCode(accountString)
        }
    }

    override fun initData() {
        super.initData()

        arguments?.let {
            val entity = AccountInputFragmentArgs.fromBundle(it).keyDistrictBean
            mFgViewModel.mDistrictBean = entity
            val fromPage = AccountInputFragmentArgs.fromBundle(it).keyFromPage
            mFgViewModel.inputFromPage = fromPage

            GwellLogUtils.i(TAG, "entity: $entity, fromPage: $fromPage")
        }

        when (mFgViewModel.inputFromPage) {
            AccountInputType.ACCOUNT_REGISTER -> {
                mViewBinding.tvAccountTitle.text = getString(com.gw_reoqoo.resource.R.string.AA0016)
                mViewBinding.tvArea.visibility = View.GONE
                mViewBinding.tvAgree.visibility = View.VISIBLE
                mViewBinding.imgAgree.visibility = View.VISIBLE
                mFgViewModel.mDistrictBean?.districtCode?.let {
                    mFgViewModel.isSupportPhoneRegister(it)
                }
                mViewBinding.tvAccountNotice.setText(RR.string.AA0019)
            }

            AccountInputType.ACCOUNT_FORGET -> {
                mViewBinding.tvAccountTitle.text = getString(com.gw_reoqoo.resource.R.string.AA0031)
                mViewBinding.tvArea.invisible(AppChannelName.isXiaotunApp(appParamApi.getAppName()))
                mViewBinding.tvAgree.visibility = View.GONE
                mViewBinding.imgAgree.visibility = View.GONE
                mFgViewModel.mDistrictBean?.districtCode?.let {
                    mFgViewModel.isSupportPhoneRegister(it)
                }
                mViewBinding.tvAccountNotice.setText(RR.string.AA0032)
            }

            else -> {
                mViewBinding.tvAccountTitle.text = getString(com.gw_reoqoo.resource.R.string.AA0016)
                mViewBinding.tvAgree.visibility = View.VISIBLE
                mViewBinding.imgAgree.visibility = View.VISIBLE

                mFgViewModel.mDistrictBean?.districtCode?.let {
                    mFgViewModel.isSupportPhoneRegister(it)
                }
            }
        }

    }

    override fun initLiveData(viewModel: AccountInputVM, savedInstanceState: Bundle?) {
        super.initLiveData(viewModel, savedInstanceState)

        mFgViewModel.isAgree.observe(this) {
            mViewBinding.imgAgree.isChecked = it
        }

        mFgViewModel.isSupport.observe(this) {
            GwellLogUtils.i(TAG, "isSupport = $it")
            // 获取 TextView 使用的 Paint 对象
            val textPaint: TextPaint = mViewBinding.tvAreaId.paint
            // 使用 Paint 测量文本的宽度
            val textWidth = textPaint.measureText(mViewBinding.tvAreaId.text.toString())
            mViewBinding.llAccount.visible(it)
            if (it) {
                mViewBinding.etAccountNumber.hint = getString(com.gw_reoqoo.resource.R.string.AA0002)
                mViewBinding.etAccountNumber.setPadding(textWidth.toInt() + 30.dp, 0, 13.dp, 0)
            } else {
                mViewBinding.etAccountNumber.hint = getString(com.gw_reoqoo.resource.R.string.AA0280)
                mViewBinding.etAccountNumber.setPadding(0, 0, 13.dp, 0)
            }
        }

        shareVM.districtLD.observe(viewLifecycleOwner) {
            GwellLogUtils.i(TAG, "codeEntity = $it")
            mViewBinding.tvArea.text = it.districtName
            val areaCode = buildString {
                append("+")
                append(it.districtCode)
            }
            mViewBinding.tvAreaId.text = areaCode
            mFgViewModel.isSupportPhoneRegister(it.districtCode)
        }

        mFgViewModel.errorNotice.observe(this) {
            GwellLogUtils.i(TAG, "errorNotice: it = $it")
            mViewBinding.tvErrNotice.text = getString(it)
        }

        mFgViewModel.jumpVerifyFragment.observe(this) {
            if (it) {
                val account = mViewBinding.etAccountNumber.text?.toString()
                val district = mFgViewModel.mDistrictBean
                val registerType = mFgViewModel.registerType!!
                val inputType = mFgViewModel.inputFromPage!!
                val action =
                    AccountInputFragmentDirections.accountActionAccountAccountinputfragmentToAccountVerifycodefragment(
                        account!!,
                        district!!,
                        registerType,
                        inputType
                    )
                findNavController().navigate(action)
                mFgViewModel.jumpVerifyFragment.value = false
            }
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.account_fragment_account_input
    }

    override fun <T : ViewModel?> loadViewModel(): Class<T> {
        return AccountInputVM::class.java as Class<T>
    }

}