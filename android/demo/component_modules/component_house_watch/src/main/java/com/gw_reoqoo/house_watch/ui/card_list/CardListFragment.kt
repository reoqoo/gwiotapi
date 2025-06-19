package com.gw_reoqoo.house_watch.ui.card_list

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.ViewModel
import com.gw_reoqoo.component_house_watch.R
import com.gw_reoqoo.house_watch.ui.active_card.ActiveCardFragment
import com.gw_reoqoo.house_watch.ui.card_list.vm.CardListVM
import com.gw_reoqoo.house_watch.ui.video_card.VideoCardFragment
import com.gw_reoqoo.lib_base_architecture.view.ABaseMVVMDBFragment
import com.gw_reoqoo.lib_router.ReoqooRouterPath
import com.gw_reoqoo.lib_utils.ktx.dp
import com.gwell.loglibs.GwellLogUtils
import com.therouter.router.Route
import dagger.hilt.android.AndroidEntryPoint
import com.gw_reoqoo.component_house_watch.databinding.HouseWatchFragmentCardListBinding as Binding


/**
 * @Description: - 看家卡片列表页
 * @Author: XIAOLEI
 * @Date: 2023/8/18
 */
@AndroidEntryPoint
@Route(path = ReoqooRouterPath.HouseWatch.FRAGMENT_CARD_LIST)
class CardListFragment : ABaseMVVMDBFragment<Binding, CardListVM>() {
    companion object {
        private const val TAG = "CardListFragment"
    }

    private var videoCardFragment: VideoCardFragment? = null

    private var activeCardFragment: ActiveCardFragment? = null

    override fun initView(view: View, savedInstanceState: Bundle?) {
        super.initView(view, savedInstanceState)

        mViewBinding.refreshLayout.setOnRefreshListener { _ ->
            videoCardFragment?.mFgViewModel?.refreshDeviceListFromRemote()
            activeCardFragment?.mFgViewModel?.refreshActiveList()
        }
    }

    override fun initData() {
        super.initData()
        videoCardFragment =
            childFragmentManager.findFragmentById(mViewBinding.videoCardFragment.id) as VideoCardFragment?

        activeCardFragment =
            childFragmentManager.findFragmentById(mViewBinding.activeCardFragment.id) as ActiveCardFragment?
        GwellLogUtils.i(
            TAG,
            "videoCardFragment=$videoCardFragment, activeCardFragment=$activeCardFragment"
        )
    }

    override fun initLiveData(viewModel: CardListVM, savedInstanceState: Bundle?) {
        super.initLiveData(viewModel, savedInstanceState)
        viewModel.expand.observe(this) { expand ->
            mViewBinding.videoCardFragment.isVisible = !expand
            mViewBinding.refreshLayout.setEnableRefresh(!expand)
            mViewBinding.activeCardFragment.updateLayoutParams<LinearLayout.LayoutParams> {
                height = if (expand) LinearLayout.LayoutParams.MATCH_PARENT else 321.dp
            }
        }
    }

    /**
     * 刷新完成
     */
    fun refreshFinish() {
        if (mViewBinding.refreshLayout.isRefreshing) {
            mViewBinding.refreshLayout.finishRefresh()
        }
    }

    override fun getLayoutId() = R.layout.house_watch_fragment_card_list

    override fun <T : ViewModel?> loadViewModel() = CardListVM::class.java as Class<T>
}