package com.gw.cp_msg.ui.activity.msg_info

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import com.gw_reoqoo.component_family.api.interfaces.IShareDeviceApi
import com.gw.cp_msg.R
import com.gw.cp_msg.entity.ParamConstant.KEY_CURRENT_MSG
import com.gw.cp_msg.entity.http.MsgDetailEntity
import com.gw.cp_msg.entity.http.MsgInfoListEntity
import com.gw.cp_msg.entity.http.MsgInfoListEntity.MSGInfo.MsgInfoType.SHARE_GUEST_ARRIVE
import com.gw.cp_msg.entity.http.MsgInfoListEntity.MSGInfo.MsgInfoType.SHARE_GUEST_CONFIRM
import com.gw.cp_msg.databinding.MsgActivityMsgInfoBinding
import com.gw.cp_msg.ui.activity.msg_info.adapter.MsgInfoAdapter
import com.gw.cp_msg.ui.activity.msg_info.vm.MsgInfoVM
import com.gw_reoqoo.lib_base_architecture.view.ABaseMVVMDBActivity
import com.gw_reoqoo.lib_router.ReoqooRouterPath
import com.gwell.loglibs.GwellLogUtils
import com.therouter.router.Autowired
import com.therouter.router.Route
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/9/20 9:38
 * Description: 消息中心 - 消息二级页面
 */
@AndroidEntryPoint
@Route(path = ReoqooRouterPath.MsgCenterPath.ACTIVITY_MSG_INFO)
class MsgInfoActivity : ABaseMVVMDBActivity<MsgActivityMsgInfoBinding, MsgInfoVM>() {

    companion object {
        private const val TAG = "MsgInfoActivity"
    }

    @JvmField
    @Autowired(name = KEY_CURRENT_MSG)
    var msg: MsgDetailEntity? = null

    @Inject
    lateinit var shareDevApi: IShareDeviceApi

    private var adapter: MsgInfoAdapter? = null

    override fun initView() {
        msg?.run {
            mViewBinding.appTitle.mainTitle.setText(title)
            mViewBinding.appTitle.leftIcon.setOnClickListener {
                finish()
            }
        } ?: let {
            GwellLogUtils.e(TAG, "msg is null")
            finish()
        }

        mViewBinding.rvMsgInfo.layoutManager = LinearLayoutManager(this)
        adapter = MsgInfoAdapter(R.layout.msg_fragment_list_item, ArrayList())
        mViewBinding.rvMsgInfo.adapter = adapter
        adapter?.setEmptyView(R.layout.msg_fragment_list_empty)
        adapter?.isUseEmpty = true
        adapter?.setOnItemClickListener { adapter, _, position ->
            val mMsgInfo = adapter.data[position] as MsgInfoListEntity.MSGInfo?
            GwellLogUtils.i(TAG, "position = $position item is clicked. msgInfo = $mMsgInfo")
            mMsgInfo?.let { msgInfo ->
                // MsgCenterSA.clickMsgType(msgInfo.tag, msgInfo.type.toString())
                when (msgInfo.type) {
                    SHARE_GUEST_ARRIVE -> {
                        val redirectUrl = msgInfo.redirectUrl
                        if (redirectUrl.isEmpty()) {
                            GwellLogUtils.e(TAG, "SHARE_GUEST_ARRIVE: redirectUrl is null")
                        } else {
                            mViewModel.devShareMsg(redirectUrl)?.run {
                                val inviteCode = this["inviteCode"]
                                val deviceID = this["deviceID"]
                                val sharerName = this["sharerName"]
                                if (inviteCode.isNullOrEmpty() || deviceID.isNullOrEmpty()) {
                                    GwellLogUtils.e(
                                        TAG,
                                        "devShare fail, inviteCode $inviteCode, deviceID $deviceID is null"
                                    )
                                    return@run
                                }
                                shareDevApi.showShareDetailDialog(
                                    this@MsgInfoActivity,
                                    inviteCode,
                                    deviceID,
                                    sharerName,
                                    onClickAccept = {}
                                )
                            }
                        }
                    }

                    SHARE_GUEST_CONFIRM -> {
                        val deviceId = msgInfo.deviceId
                        mViewModel.goDevManagerPage(deviceId.toString())
                    }

                    else -> {
                        val redirectUrl = msgInfo.redirectUrl
                        if (redirectUrl.isEmpty()) {
                            GwellLogUtils.e(TAG, "type ${msgInfo.type}: redirectUrl is null")
                        } else {
                            mViewModel.openWebView(msgInfo.redirectUrl, msgInfo.title)
                        }
                    }
                }
            }
        }
    }

    override fun initData(savedInstanceState: Bundle?) {
        super.initData(savedInstanceState)
        msg?.run {
            if (MsgDetailEntity.TAG_MSG_CENTER_CUSTOMER_SRV == tag) {
                customerMsg?.run {
                    adapter?.setNewInstance(this)
                }
            } else {
                showLoadDialog()
                mViewModel.loadMsgInfo(this, 0)
            }
        } ?: let {
            GwellLogUtils.e(TAG, "msg is null")
            finish()
        }
    }

    override fun initLiveData(viewModel: MsgInfoVM, savedInstanceState: Bundle?) {
        super.initLiveData(viewModel, savedInstanceState)
        mViewModel.msgInfoEvent.observe(this) { msgInfos ->
            dismissLoadDialog()
            msgInfos?.let {
                if (msgInfos.isNotEmpty()) {
                    msgInfos.sortByDescending {
                        it.time
                    }
                    GwellLogUtils.i(TAG, "set msg info list:${msgInfos.size}")
                    adapter?.setNewInstance(msgInfos)
                    adapter?.isUseEmpty = false
                } else {
                    adapter?.setNewInstance(null)
                    adapter?.isUseEmpty = (true)
                }
            } ?: let {
                adapter?.setNewInstance(null)
                adapter?.isUseEmpty = (true)
            }
        }
    }

    override fun getLayoutId() = R.layout.msg_activity_msg_info

    override fun <T : ViewModel?> loadViewModel() = MsgInfoVM::class.java as Class<T>

    override fun getTitleView() = mViewBinding.appTitle
}