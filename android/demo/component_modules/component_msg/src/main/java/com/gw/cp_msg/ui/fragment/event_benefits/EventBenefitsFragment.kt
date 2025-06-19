package com.gw.cp_msg.ui.fragment.event_benefits

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gw.component_webview.api.interfaces.IWebViewApi
import com.gw.cp_msg.R
import com.gw.cp_msg.databinding.MsgFragmentEventBenefitsBinding
import com.gw.cp_msg.entity.http.EventBenefitsEntity
import com.gw.cp_msg.entity.http.Notice
import com.gw.cp_msg.ui.fragment.event_benefits.adapter.BenefitsAdapter
import com.gw.cp_msg.ui.fragment.event_benefits.vm.EventBenefitsVM
import com.gw_reoqoo.lib_base_architecture.view.ABaseMVVMDBFragment
import com.gw_reoqoo.lib_base_architecture.widget.LoadingDialog
import com.gwell.loglibs.GwellLogUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 *@message   ActiveMsgFragment
 *@user      zouhuihai
 *@date      2022/8/4
 */
@AndroidEntryPoint
class EventBenefitsFragment : ABaseMVVMDBFragment<MsgFragmentEventBenefitsBinding, EventBenefitsVM>() {

    @Inject
    lateinit var webViewApi: IWebViewApi

    private var adapter: BenefitsAdapter? = null

    private var mLoadingDialog: com.gw_reoqoo.lib_base_architecture.widget.LoadingDialog? = null

    companion object {
        private const val TAG = "ActiveMsgFragment"

        fun newInstance(): EventBenefitsFragment {
            val args = Bundle()
            val fragment = EventBenefitsFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun getLayoutId(): Int = R.layout.msg_fragment_event_benefits

    override fun <T : ViewModel?> loadViewModel(): Class<T> = EventBenefitsVM::class.java as Class<T>

    override fun initView(view: View, savedInstanceState: Bundle?) {
        super.initView(view, savedInstanceState)
        GwellLogUtils.i(TAG, "initView")
        mViewBinding.recycleView.layoutManager = LinearLayoutManager(context)
        adapter = BenefitsAdapter(R.layout.msg_fragment_item_event_benefits, ArrayList())
        mViewBinding.recycleView.adapter = adapter
        adapter?.setEmptyView(R.layout.msg_fragment_list_empty)
        adapter?.isUseEmpty = true
        adapter?.setOnItemClickListener { adapter, _, position ->
            (adapter.getItem(position) as? Notice)?.let { _item ->
                when (_item.status) {
                    EventBenefitsEntity.STATUS_ACTIVE_EXPIRE -> {
                        toast.show(com.gw_reoqoo.resource.R.string.AA0603)
                    }

                    EventBenefitsEntity.STATUS_HAVE_READ -> {
                        activity?.let {
                            if (!it.isFinishing) {
                                webViewApi.openWebView(_item.url, "")
                            } else {
                                GwellLogUtils.e(
                                    TAG,
                                    "initView: startWebActivity failed, activity is finishing"
                                )
                            }
                        } ?: let {
                            GwellLogUtils.i(
                                TAG,
                                "initView: startWebActivity failed, activity is null"
                            )
                        }
                    }
                }
            }
        }
        mViewBinding.recycleView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val manager = recyclerView.layoutManager
                    if (manager is LinearLayoutManager) {
                        val firstVisibleItem = manager.findFirstVisibleItemPosition()
                        val lastVisibleItem = manager.findLastVisibleItemPosition()
                        GwellLogUtils.i(
                            TAG,
                            "onScrollStateChanged firstVisibleItem = $firstVisibleItem, lastVisibleItem = $lastVisibleItem"
                        )
                        if (firstVisibleItem != lastVisibleItem) {
                            for (i in firstVisibleItem..lastVisibleItem) {
                                if (i >= (adapter?.data?.size ?: 0)) {
                                    GwellLogUtils.e(
                                        TAG,
                                        "IndexOutOfBoundsException i:$i, sizeOfList:${adapter?.data?.size}"
                                    )
                                    break
                                }
                                GwellLogUtils.i(TAG, "item = ${adapter?.data?.get(i)}")
                                val item = adapter?.data?.get(i)
                                if (item?.isReport == false) {
                                    item.isReport = true
                                }
                            }
                        }
                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dx == 0 && dy == 0) {
                    val manager = recyclerView.layoutManager
                    if (manager is LinearLayoutManager) {
                        val firstVisibleItem = manager.findFirstVisibleItemPosition()
                        val lastVisibleItem = manager.findLastVisibleItemPosition()
                        GwellLogUtils.i(
                            TAG,
                            "onScrolled firstVisibleItem = $firstVisibleItem, lastVisibleItem = $lastVisibleItem"
                        )
                        if (firstVisibleItem != lastVisibleItem) {
                            for (i in firstVisibleItem..lastVisibleItem) {
                                GwellLogUtils.i(TAG, "item = ${adapter?.data?.get(i)}")
                                val item = adapter?.data?.get(i)
                                if (item?.isReport == false) {
                                    item.isReport = true
                                }
                            }
                        }
                    }
                }
            }
        })
    }

    override fun initData() {
        super.initData()
        mFgViewModel.loadActiveMsg()
    }

    override fun initLiveData(viewModel: EventBenefitsVM, savedInstanceState: Bundle?) {
        super.initLiveData(viewModel, savedInstanceState)
        mFgViewModel.noticeEvent.observe(this) {
            it?.let { notices ->
                notices.sortBy { notice ->
                    notice.status
                }
                adapter?.isUseEmpty = false
                adapter?.setList(notices)
            } ?: let {
                adapter?.isUseEmpty = true
                adapter?.setNewInstance(null)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mLoadingDialog?.dismiss()
    }

}