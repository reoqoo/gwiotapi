package com.gw_reoqoo.house_watch.ui.active_card

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gw_reoqoo.component_house_watch.R
import com.gw_reoqoo.house_watch.ui.active_card.adapter.ActiveAdapter
import com.gw_reoqoo.house_watch.ui.active_card.adapter.ActiveBaseAdapter
import com.gw_reoqoo.house_watch.ui.active_card.adapter.SingleActiveAdapter
import com.gw_reoqoo.house_watch.ui.active_card.dialog.ActiveConfigDialog
import com.gw_reoqoo.house_watch.ui.active_card.vm.ActiveCardVM
import com.gw_reoqoo.house_watch.ui.card_list.CardListFragment
import com.gw_reoqoo.house_watch.ui.card_list.vm.CardListVM
import com.gw_reoqoo.lib_base_architecture.view.ABaseMVVMDBFragment
import com.gw_reoqoo.lib_http.ResponseNotSuccessException
import com.gw_reoqoo.lib_http.error.ResponseCode
import com.gw_reoqoo.lib_router.ReoqooRouterPath
import com.gw_reoqoo.lib_utils.ktx.setSingleClickListener
import com.gw_reoqoo.lib_utils.ktx.visible
import com.gwell.loglibs.GwellLogUtils
import com.tencentcs.iotvideo.http.interceptor.flow.HttpAction
import com.therouter.router.Route
import dagger.hilt.android.AndroidEntryPoint
import com.gw_reoqoo.component_house_watch.databinding.HouseWatchFragmentActiveCardBinding as Binding
import com.gw_reoqoo.resource.R as RR


/**
 * @Description: - 活动卡片
 * @Author: XIAOLEI
 * @Date: 2023/8/18
 */
@AndroidEntryPoint
@Route(path = ReoqooRouterPath.HouseWatch.FRAGMENT_ACTIVE_CARD)
class ActiveCardFragment : ABaseMVVMDBFragment<Binding, ActiveCardVM>() {
    companion object {
        private const val TAG = "ActiveCardFragment"
    }

    /**
     * 多设备的事件列表适配器
     */
    private var activeAdapter: ActiveAdapter? = null

    /**
     * 单设备的事件列表适配器
     */
    private var singleActiveAdapter: SingleActiveAdapter? = null

    override fun getLayoutId() = R.layout.house_watch_fragment_active_card
    override fun <T : ViewModel?> loadViewModel() = ActiveCardVM::class.java as Class<T>

    private val cardListVm: CardListVM?
        get() {
            val parentFragment = parentFragment
            return if (parentFragment is CardListFragment) {
                ViewModelProvider(parentFragment)[CardListVM::class.java]
            } else {
                null
            }
        }

    private val linearLayoutManager by lazy {
        object : LinearLayoutManager(context, RecyclerView.VERTICAL, false) {
            var canScrollVertically = false
            override fun canScrollVertically(): Boolean {
                return if (canScrollVertically) {
                    super.canScrollVertically()
                } else {
                    false
                }
            }
        }
    }

    override fun initView(view: View, savedInstanceState: Bundle?) {
        super.initView(view, savedInstanceState)

        // 初始化RecyclerView
        mViewBinding.rvActives.run {
            layoutManager = linearLayoutManager
        }
        // 点击菜单
        mViewBinding.ivMenu.setSingleClickListener {
            context?.let {
                ActiveConfigDialog(it, toast, mFgViewModel)
            }?.show()
        }
        // 点击展开
        mViewBinding.tvExpand.setSingleClickListener {
            cardListVm?.switchExpand(true)
            mViewBinding.refreshLayout.setEnableLoadMore(true)
            mViewBinding.refreshLayout.setEnableRefresh(true)
        }
        // 点击收起
        mViewBinding.ivPullDown.setSingleClickListener {
            cardListVm?.switchExpand(false)
            mViewBinding.refreshLayout.setEnableLoadMore(false)
            mViewBinding.refreshLayout.setEnableRefresh(false)
        }
        // 下拉刷新，
        mViewBinding.refreshLayout.setOnRefreshListener { _ ->
            mFgViewModel.refreshActiveList()
        }
        // 上拉加载
        mViewBinding.refreshLayout.setOnLoadMoreListener { _ ->
            mFgViewModel.loadMoreActiveList()
        }
    }

    override fun initLiveData(viewModel: ActiveCardVM, savedInstanceState: Bundle?) {
        super.initLiveData(viewModel, savedInstanceState)
        viewModel.netWorkSate.observe(this) { action ->
            when (action) {
                is HttpAction.Loading -> {}
                is HttpAction.Success -> {
                    mViewBinding.refreshLayout.finishRefresh()
                    (this.parentFragment as? CardListFragment?)?.refreshFinish()
                    mViewBinding.refreshLayout.finishLoadMore()
                }

                is HttpAction.Fail -> {
                    (this.parentFragment as? CardListFragment?)?.refreshFinish()
                    mViewBinding.refreshLayout.finishRefresh()
                    mViewBinding.refreshLayout.finishLoadMore()
                    val t = action.t
                    if (t is ResponseNotSuccessException) {
                        when (val respCode = ResponseCode.getRespCode(t.code)) {
                            null -> Unit
                            ResponseCode.CODE_20001 -> {
                                toast.show(RR.string.AA0573)
                            }

                            else -> toast.show(respCode.msgRes)
                        }
                    }
                }
            }
        }
        // 监听展开状态
        cardListVm?.expand?.observe(this) { expand ->
            // 收起按钮是否显示
            mViewBinding.ivPullDown.isVisible = expand
            // 扩展按钮是否显示
            mViewBinding.tvExpand.isVisible = !expand
            // 扩展模式下，列表可以滚动，缩略模式不可以滚动
            linearLayoutManager.canScrollVertically = expand
            // 如果是收起，则滚动回列表顶部
            if (!expand) {
                mViewBinding.rvActives.scrollToPosition(0)
            }
        }
        // 事件
        viewModel.activeList.observe(this) { _ ->
            mFgViewModel.debounceRefreshData()
        }
        // 设备
        viewModel.deviceList.observe(this) { list ->
            val devList = list.mapNotNull { it.device }
            GwellLogUtils.i(TAG, "deviceList=$devList")
            mFgViewModel.getPreviewType(list)
            mFgViewModel.debounceRefreshData()
        }
        // 监听数据更新
        viewModel.activeBeanList.observe(this) { activeList ->
            GwellLogUtils.i(TAG, "activeBeanList=$activeList")
            val loadEndOfData = mFgViewModel.endOfEvent
            // 是否是展开
            val isExpand = cardListVm?.expand?.value == true
            // 空内容的图像
            mViewBinding.ivEmpty.isVisible = activeList.isEmpty()
            // 空内容的文字提示
            mViewBinding.tvEmpty.isVisible = activeList.isEmpty()
            // 展开按钮是否显示
            mViewBinding.tvExpand.isVisible = activeList.size > 3 && !isExpand
            // 是否启用上拉加载更多
            mViewBinding.refreshLayout.setEnableLoadMore(!loadEndOfData)

            val adapter = mViewBinding.rvActives.adapter
            if (adapter !is ActiveBaseAdapter) return@observe

            val allList = mFgViewModel.loadTimeType(activeList)
            GwellLogUtils.i(TAG, "allList=$allList")
            adapter.updateAll(allList)

            // 加载到数据结束，则加入一个调整区域的控件在底部
            GwellLogUtils.i(TAG, "loadEndOfData=$loadEndOfData")
            if (loadEndOfData) {
                adapter.add(Unit)
            }
        }
        viewModel.viewType.observe(this) { showType ->
            GwellLogUtils.i(TAG, "viewType=$showType")
            when (showType) {
                ActiveCardVM.PreviewType.SINGLE -> {
                    mViewBinding.tvSingleDevName.visible(true)
                    mViewBinding.tvSingleDevName.text = mFgViewModel.getCheckedDevName()
                    mViewBinding.rvActives.run {
                        GwellLogUtils.i(TAG, "singleActiveAdapter=$singleActiveAdapter")
                        adapter = singleActiveAdapter
                    }
                }

                ActiveCardVM.PreviewType.MULTI -> {
                    mViewBinding.tvSingleDevName.visible(false)
                    mViewBinding.rvActives.run {
                        GwellLogUtils.i(TAG, "activeAdapter=$activeAdapter")
                        adapter = activeAdapter
                    }
                }

                else -> {
                    mViewBinding.tvSingleDevName.visible(false)
                    mViewBinding.rvActives.run {
                        GwellLogUtils.i(TAG, "activeAdapter=$activeAdapter")
                        adapter = activeAdapter
                    }
                }
            }
        }
    }

    override fun initData() {
        super.initData()
        mFgViewModel.loadEventTypesAndDevices()

        activeAdapter = ActiveAdapter(
            getDeviceName = mFgViewModel::getDeviceName,
            showConfigDialog = {
                mViewBinding.ivMenu.performClick()
            },
            onClickActiveItem = { bean ->
                mFgViewModel.startPlaybackPage(bean)
            }
        )

        singleActiveAdapter = SingleActiveAdapter(
            getDeviceName = mFgViewModel::getDeviceName,
            showConfigDialog = {
                mViewBinding.ivMenu.performClick()
            },
            onClickActiveItem = { bean ->
                mFgViewModel.startPlaybackPage(bean)
            }
        )
    }

    override fun onHiddenChanged(hidden: Boolean) {
        GwellLogUtils.i(TAG, "onHiddenChanged=$hidden")
        if (!hidden) {
            val showTimes = mFgViewModel.showCount.addAndGet(1)
            GwellLogUtils.i(TAG, "onHiddenChanged-showTimes=$showTimes")
            if (showTimes >= 2) {
                mFgViewModel.refreshActiveList()
                GwellLogUtils.i(TAG, "onHiddenChanged-refreshActiveList")
            }
        }
        super.onHiddenChanged(hidden)
    }

    override fun onResume() {
        super.onResume()
        if (!isHidden) {
            val showTimes = mFgViewModel.showCount.addAndGet(1)
            GwellLogUtils.i(TAG, "onResume-showTimes=$showTimes")
            if (showTimes >= 2) {
                mFgViewModel.refreshActiveList()
                GwellLogUtils.i(TAG, "onResume-refreshActiveList")
            }
        }
    }
}