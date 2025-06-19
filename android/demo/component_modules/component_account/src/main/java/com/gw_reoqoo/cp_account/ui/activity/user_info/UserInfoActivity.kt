package com.gw_reoqoo.cp_account.ui.activity.user_info

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import androidx.core.util.valueIterator
import androidx.lifecycle.ViewModel
import com.gw_reoqoo.cp_account.R
import com.gw_reoqoo.cp_account.databinding.AccountActivityUserInfoBinding
import com.gw_reoqoo.cp_account.entity.ParamConstants
import com.gw_reoqoo.cp_account.ui.activity.user_info.vm.UserInfoVM
import com.gw_reoqoo.cp_account.ui.activity.user_info.vm.UserInfoVM.Companion.FINISH_ACTIVITY_CODE
import com.gw_reoqoo.cp_account.widget.dialog.AvatarSelectDialog
import com.gw_reoqoo.cp_account.widget.dialog.StringListInputDialog
import com.gw_reoqoo.lib_base_architecture.PageJumpData
import com.gw_reoqoo.lib_base_architecture.view.ABaseMVVMDBActivity
import com.gw_reoqoo.lib_router.ReoqooRouterPath
import com.gw_reoqoo.lib_utils.ktx.isEmptyOrZero
import com.gw_reoqoo.lib_utils.ktx.loadUrl
import com.gw_reoqoo.lib_widget.dialog.comm_dialog.CommDialog
import com.gw_reoqoo.lib_widget.dialog.comm_dialog.entity.CommDialogAction
import com.gw_reoqoo.lib_widget.dialog.comm_dialog.entity.InputContent
import com.gw_reoqoo.lib_widget.dialog.comm_dialog.entity.TextContent
import com.gw_reoqoo.lib_widget.dialog.comm_dialog.ext.showCommDialog
import com.gwell.loglibs.GwellLogUtils
import com.jwkj.base_utils.clipboard.ClipboardUtils
import com.therouter.TheRouter
import com.therouter.router.Route
import dagger.hilt.android.AndroidEntryPoint
import com.gw_reoqoo.resource.R as RR

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/9/13 14:52
 * Description: UserInfoActivity
 */
@AndroidEntryPoint
@Route(path = ReoqooRouterPath.AccountPath.ACTIVITY_USER_INFO)
class UserInfoActivity : ABaseMVVMDBActivity<AccountActivityUserInfoBinding, UserInfoVM>() {

    companion object {
        private const val TAG = "UserInfoActivity"
    }

    private var mAvatarDialog: AvatarSelectDialog? = null

    private var mReasonDialog: StringListInputDialog? = null

    /**
     * 昵称的输入弹框
     */
    private var commDialog: CommDialog? = null

    override fun initView() {

        mViewBinding.layoutTitle.run {
            leftIcon.setOnClickListener { finish() }
        }

        mAvatarDialog ?: let {
            mAvatarDialog = AvatarSelectDialog(this)
            mAvatarDialog?.window?.setGravity(Gravity.BOTTOM)
            mViewModel.defaultAvatar.value?.let {
                mAvatarDialog?.setData(it)
            }
            mAvatarDialog?.window?.setWindowAnimations(RR.style.dialog_bottom_anim)
            mAvatarDialog?.setOnAvatarClick(::avatarSelected)
        }
        mViewBinding.layoutAvatar.setOnClickListener {
            setDefaultAvatar(mViewModel.userInfo.value?.headUrl ?: "")
            mAvatarDialog?.show()
        }

        mViewBinding.layoutNickname.setOnClickListener {
            var nickName = mViewBinding.tvNickname.text.toString()
            commDialog = showCommDialog {
                location = Gravity.CENTER
                title = getString(RR.string.AA0286)
                content = InputContent(
                    text = nickName,
                    autoFocus = true,
                    isShowClear = true,
                    maxLength = 24,
                    onOverLimit = {
                        toast.show(RR.string.AA0289)
                    },
                    onTextChange = {
                        nickName = it
                    }
                )
                actions = listOf(
                    CommDialogAction(getString(RR.string.AA0059)),
                    CommDialogAction(
                        getString(RR.string.AA0058),
                        linkWithInput = true,
                        conformAutoHide = false,
                        onClick = {
                            val isConform = mViewModel.isConformNickName(nickName)
                            if (!isConform) {
                                toast.show(RR.string.AA0288)
                                return@CommDialogAction
                            }
                            commDialog?.dismiss()
                            mViewModel.changeNickName(nickName)
                        })
                )
            }
        }

        mViewBinding.tvUserIdCopy.setOnClickListener {
            // 要复制的文本
            val textToCopy = mViewBinding.tvUserId.text.toString()
            // 将文本复制到剪贴板
            ClipboardUtils.setClipboard(this@UserInfoActivity, textToCopy)
            // 提示用户已成功复制文本
            toast.show(RR.string.AA0597)
        }

        mViewBinding.layoutPwd.setOnClickListener {
            // 修改密码
            TheRouter.build(ReoqooRouterPath.AccountPath.ACTIVITY_MODIFY_PWD).navigation()
        }

        mViewBinding.layoutMobile.setOnClickListener {
            mViewModel.goBindMobile()
        }

        mViewBinding.layoutEmail.setOnClickListener {
            mViewModel.goBindEmail()
        }

        mViewBinding.layoutCancel.setOnClickListener {
            mReasonDialog ?: let {
                val reasonList = mViewModel.getReasonsTypeList()
                val reasonTypeStringList = arrayListOf<String>()
                for (reasonType in reasonList.valueIterator()) {
                    reasonTypeStringList.add(reasonType)
                }
                mReasonDialog = StringListInputDialog(this)
                mReasonDialog?.setData(reasonTypeStringList)
                mReasonDialog?.window?.setGravity(Gravity.BOTTOM)
                mReasonDialog?.window?.setWindowAnimations(RR.style.dialog_bottom_anim)
                mReasonDialog?.setItemClickListener(object :
                    StringListInputDialog.OnReasonItemClickListener {
                    override fun onItemClick(index: Int, value: String) {
                        GwellLogUtils.i(TAG, "selected question type:$value")
                        if (value.isEmptyOrZero()) {
                            toast.show(RR.string.AA0310)
                            return
                        }

                        mViewModel.pageJumpData.postValue(
                            PageJumpData(
                                TheRouter.build(ReoqooRouterPath.AccountPath.ACTIVITY_CLOSE_ACCOUNT)
                                    .withInt(ParamConstants.PARAM_CLOSE_REASON_TYPE, 0)
                                    .withString(ParamConstants.PARAM_CLOSE_REASON_VALUE, value)
                            )
                        )
                        mReasonDialog?.dismiss()
                    }
                })
            }
            mReasonDialog?.show()
        }

        mViewBinding.btnLogout.setOnClickListener {
            showCommDialog {
                content = TextContent(getString(RR.string.AA0309))
                actions = listOf(
                    CommDialogAction(getString(RR.string.AA0059)),
                    CommDialogAction(
                        getString(RR.string.AA0058),
                        isDestructiveAction = true,
                        onClick = {
                            mViewModel.userLogout()
                        })
                )
            }
        }

    }

    override fun initData(savedInstanceState: Bundle?) {
        super.initData(savedInstanceState)
        mViewModel.getUserDetail()

        mViewModel.getDefaultAvatar()
    }

    override fun initLiveData(viewModel: UserInfoVM, savedInstanceState: Bundle?) {
        super.initLiveData(viewModel, savedInstanceState)

        mViewModel.watchUserInfo(this)

        mViewModel.userInfo.observe(this) {
            GwellLogUtils.i(TAG, "userinfo $it")
            it?.let {
                val avatar = it.headUrl
                if (avatar?.isNotEmpty() == true) {
                    mViewBinding.ivAvatar.loadUrl(
                        avatar,
                        placeHolder = RR.drawable.gw_reoqoo_icon_default_avatar,
                        error = RR.drawable.gw_reoqoo_icon_default_avatar
                    )
                }

                mViewBinding.tvNickname.text = it.nickname ?: ""
                mViewBinding.tvUserId.text = it.showId ?: ""
                mViewBinding.tvMobile.text = if (it.phone.isEmptyOrZero()) {
                    getString(RR.string.AA0296)
                } else {
                    it.phone
                }
                mViewBinding.tvEmail.text = if (it.email.isNullOrEmpty()) {
                    getString(RR.string.AA0296)
                } else {
                    it.email
                }
                it.regRegion?.let { code ->
                    mViewModel.getDistrictByCode(code)
                } ?: ""
            } ?: GwellLogUtils.e(TAG, "userInfo is null")
        }

        mViewModel.getDistrictName().observe(this) {
            mViewBinding.tvArea.text = it
        }

        mViewModel.defaultAvatar.observe(this) {
            mAvatarDialog?.setData(it)
        }
    }

    private fun setDefaultAvatar(avatarUrl: String) {
        mAvatarDialog?.run {
            selectAvatar = avatarUrl
        }
    }

    private fun avatarSelected(avatarUrl: String) {
        GwellLogUtils.i(TAG, "avatarUrl: $avatarUrl")
        mViewModel.updateUserAvatar(avatarUrl)
        mAvatarDialog?.run {
            selectAvatar = avatarUrl
            dismiss()
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
                mViewModel.getUserDetail()
            }
        }
    }

    override fun getLayoutId() = R.layout.account_activity_user_info

    override fun <T : ViewModel?> loadViewModel() = UserInfoVM::class.java as Class<T>

    override fun getTitleView() = mViewBinding.layoutTitle

}