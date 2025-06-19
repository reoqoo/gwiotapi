package com.gw.cp_msg.ui.activity.msg_dev_upgrade

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import com.gw.cp_msg.R
import com.gw.cp_msg.entity.ParamConstant
import com.gw.cp_msg.entity.http.MsgDetailEntity
import com.gw.cp_msg.databinding.MsgActivityMsgDevUpgradeBinding
import com.gw.cp_msg.ui.activity.msg_dev_upgrade.adapter.MsgDevUpgradeAdapter
import com.gw.cp_msg.ui.activity.msg_dev_upgrade.vm.MsgDevUpgradeVM
import com.gw_reoqoo.lib_base_architecture.PageJumpData
import com.gw_reoqoo.lib_base_architecture.view.ABaseMVVMDBActivity
import com.gw_reoqoo.lib_http.jsonToEntity
import com.gw_reoqoo.lib_router.ReoqooRouterPath
import com.therouter.TheRouter
import com.therouter.router.Autowired
import com.therouter.router.Route
import dagger.hilt.android.AndroidEntryPoint

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/11/3 13:57
 * Description: 消息中心 - 设备升级的列表
 */
@AndroidEntryPoint
@Route(path = ReoqooRouterPath.MsgCenterPath.ACTIVITY_MSG_DEV_UPGRADE)
class MsgDevUpgradeActivity :
    ABaseMVVMDBActivity<MsgActivityMsgDevUpgradeBinding, MsgDevUpgradeVM>() {

    companion object {
        private const val TAG = "MsgDevUpgradeActivity"
    }

    @JvmField
    @Autowired(name = ParamConstant.KEY_CURRENT_MSG_LIST)
    var msgJson: String? = null

    private var adapter: MsgDevUpgradeAdapter? = null

    /**
     * 获取布局id
     * @return Int
     */
    override fun getLayoutId(): Int = R.layout.msg_activity_msg_dev_upgrade

    /**
     * 初始化view
     */
    override fun initView() {
        mViewBinding.rvMsgInfo.layoutManager = LinearLayoutManager(this)
        adapter = MsgDevUpgradeAdapter(R.layout.msg_fragment_list_item, ArrayList())
        mViewBinding.rvMsgInfo.adapter = adapter
        adapter?.setEmptyView(R.layout.msg_fragment_list_empty)
        adapter?.isUseEmpty = true
        adapter?.setOnItemClickListener { adapter, _, position ->
            mViewModel.pageJumpData.postValue(PageJumpData(TheRouter.build(ReoqooRouterPath.Family.FAMILY_ACTIVITY_DEVICE_UPDATE)))
        }
    }

    override fun initData(savedInstanceState: Bundle?) {
        super.initData(savedInstanceState)
        msgJson?.let {
            if (it.isNotEmpty()) {
                val msgList = it.jsonToEntity<List<MsgDetailEntity>>()
                adapter?.setNewInstance(msgList?.toMutableList())
            }
        }
    }

    /**
     * 子类可以通过实现该抽象函数提供需要实例化的VideModel实例的Class 对象
     * @return Class<T> 需要实例化ViewModel的Class对象
     */
    override fun <T : ViewModel?> loadViewModel() = MsgDevUpgradeVM::class.java as Class<T>

    override fun getTitleView() = mViewBinding.layoutTitle

}