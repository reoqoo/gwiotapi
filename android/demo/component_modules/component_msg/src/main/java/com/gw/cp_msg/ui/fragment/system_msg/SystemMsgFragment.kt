package com.gw.cp_msg.ui.fragment.system_msg

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import com.gw.cp_msg.R
import com.gw.cp_msg.databinding.MsgFragmentSystemMsgBinding
import com.gw.cp_msg.entity.http.MsgDetailEntity
import com.gw.cp_msg.ui.fragment.system_msg.adapter.SystemMsgAdapter
import com.gw.cp_msg.ui.fragment.system_msg.vm.SystemMsgVM
import com.gw.cp_msg.utils.PushUtils
import com.gw_reoqoo.lib_base_architecture.protocol.IGwBaseVm
import com.gw_reoqoo.lib_base_architecture.view.ABaseMVVMDBFragment
import com.gw_reoqoo.lib_utils.ktx.visible
import com.gwell.loglibs.GwellLogUtils
import dagger.hilt.android.AndroidEntryPoint

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/9/18 11:15
 * Description: SystemMsgFragment
 */
@AndroidEntryPoint
class SystemMsgFragment :
    ABaseMVVMDBFragment<MsgFragmentSystemMsgBinding, SystemMsgVM>() {

    companion object {
        private const val TAG = "SystemMsgFragment"

        fun newInstance(): SystemMsgFragment {
            val args = Bundle()
            val fragment = SystemMsgFragment()
            fragment.arguments = args
            return fragment
        }

    }

    private var mLoadUnReadMsgCount: ((Int) -> Unit?)? = null

    private var mReadMsgCount: ((Int) -> Unit?)? = null

    private var mMsgSize: ((Int) -> Unit?)? = null

    private var mIsMsgEmpty: ((Boolean) -> Unit?)? = null

    fun addSystemMsgCallback(
        loadUnReadMsgCount: ((Int) -> Unit)? = null,
        readMsgCount: ((Int) -> Unit)? = null,
        msgSize: ((Int) -> Unit)? = null,
        isMsgEmpty: ((Boolean) -> Unit)? = null
    ) {
        mLoadUnReadMsgCount = loadUnReadMsgCount
        mReadMsgCount = readMsgCount
        mMsgSize = msgSize
        mIsMsgEmpty = isMsgEmpty
    }

    private var adapter: SystemMsgAdapter? = null

    override fun initView(view: View, savedInstanceState: Bundle?) {
        super.initView(view, savedInstanceState)
        mViewBinding.clPushTip.setOnClickListener {
            context?.let {
                PushUtils.openNotification(it)
            }
        }

        mViewBinding.rvMsg.layoutManager = LinearLayoutManager(context)
        adapter = SystemMsgAdapter(R.layout.msg_fragment_system_msg_item, mutableListOf())
        mViewBinding.rvMsg.adapter = adapter

        mViewBinding.llEmpty.visible(true)
        adapter?.isUseEmpty = false
        adapter?.setOnItemClickListener { adapter, _, position ->
            val item = adapter.getItem(position) as MsgDetailEntity
            when (item.tag) {
                MsgDetailEntity.TAG_MSG_CENTER_APP_UPGRADE -> {
                    // app升级
                    if (item.unreadCnt > 0) {
                        mReadMsgCount?.invoke(item.unreadCnt)
                        item.unreadCnt = 0
                        adapter.notifyItemChanged(position)
                    }
                    mFgViewModel.itemAppUpgradeClick(item)
                }

                MsgDetailEntity.TAG_MSG_CENTER_FIRMWARE_UPDATE -> {
                    // 固件升级
                    if (item.unreadCnt > 0) {
                        mReadMsgCount?.invoke(item.unreadCnt)
                        item.unreadCnt = 0
                        adapter.notifyItemChanged(position)
                    }
                    mFgViewModel.itemDevUpgradeClick(item)
                }

                MsgDetailEntity.TAG_MSG_CENTER_ALARM_EVENT -> {
                    // TODO 报警事件为本地事件，不需要上报服务器已读，后期再来处理报警事件，现在还没有约定跳转逻辑
                    if (item.unreadCnt > 0) {
                        mReadMsgCount?.invoke(item.unreadCnt)
                        item.unreadCnt = 0
                        adapter.notifyItemChanged(position)
                    }
                }

                else -> {
                    // 统一为服务器消息，如果有未读消息，先上报已读，在继续下一步操作
                    if (item.unreadCnt > 0) {
                        mFgViewModel.readSystemMsg(item, position)
                    }
                    mFgViewModel.itemSystemMsgClick(item)
                }
            }
        }
    }

    override fun initData() {
        super.initData()
        mFgViewModel.loadSystemMsgList()
    }

    override fun initLiveData(viewModel: SystemMsgVM, savedInstanceState: Bundle?) {
        super.initLiveData(viewModel, savedInstanceState)
        viewModel.notificationStatus.observe(this) {
            if (!it) {
                mViewBinding.clPushTip.visibility = View.VISIBLE
            } else {
                mViewBinding.clPushTip.visibility = View.GONE
            }
        }

        mFgViewModel.msgListEvent.observe(this) { msgList ->
            GwellLogUtils.i(TAG, "msgList: $msgList")
            if (msgList.isNullOrEmpty()) {
                mViewBinding.llEmpty.visible(true)
                adapter?.setNewInstance(null)
                mMsgSize?.invoke(0)
            } else {
                msgList.sortByDescending {
                    it.msgTime
                }
                mViewBinding.llEmpty.visible(false)
                adapter?.setList(msgList)
            }
            mIsMsgEmpty?.invoke(msgList.isNullOrEmpty())
        }

        mFgViewModel.itemUpdateFromPos.observe(this) {
            adapter?.notifyItemChanged(it)
        }

        mFgViewModel.readMsgEvent.observe(this) {
            val readMsg = it[SystemMsgVM.KEY_READ_MSG] as MsgDetailEntity?
            val readMsgPos = it[SystemMsgVM.KEY_READ_MSG_POSITION] as Int
            readMsg?.run {
                mReadMsgCount?.invoke(unreadCnt)
                unreadCnt = 0
                adapter?.notifyItemChanged(readMsgPos)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mFgViewModel.isNotificationEnabled()
    }

    /**
     * 清除未读消息
     */
    fun cleanUnreadMsg() {
        mFgViewModel.loadDialogState.postValue(IGwBaseVm.LOAD_DIALOG_STATE_OPEN)
        // 一键已读时，先延迟300ms loading，再去清除未读消息
        mViewBinding.llEmpty.postDelayed({
            mFgViewModel.cleanUnreadMsg()
            mFgViewModel.loadDialogState.postValue(IGwBaseVm.LOAD_DIALOG_STATE_CLOSE)
        }, 300)
    }

    override fun getLayoutId(): Int = R.layout.msg_fragment_system_msg

    override fun <T : ViewModel?> loadViewModel() = SystemMsgVM::class.java as Class<T>

}