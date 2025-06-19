package com.gw_reoqoo.cp_account.ui.fragment.area_list

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.gw_reoqoo.cp_account.R
import com.gw_reoqoo.cp_account.databinding.AccountFragmentAreaListBinding
import com.gw_reoqoo.cp_account.ui.fragment.ShareVM
import com.gw_reoqoo.cp_account.widget.SearchAreaInputLayout
import com.gw_reoqoo.lib_base_architecture.view.ABaseMVVMDBFragment
import com.gw_reoqoo.lib_http.entities.DistrictEntity
import com.gw_reoqoo.lib_widget.title.WidgetCommonTitleView
import com.gwell.loglibs.GwellLogUtils
import dagger.hilt.android.AndroidEntryPoint

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/8/1 16:51
 * Description: 地区列表页
 */
@AndroidEntryPoint
class AreaListFragment : ABaseMVVMDBFragment<AccountFragmentAreaListBinding, AreaListVM>() {

    companion object {
        private const val TAG = "AreaListFragment"
    }

    private val shareVM: ShareVM by activityViewModels()

    /**
     * 地区码adapter
     */
    private lateinit var adapter: DistrictAreaListAdapter

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

        // 初始化地区码列表
        val layoutManager = LinearLayoutManager(context)
        mViewBinding.rvAreas.layoutManager = layoutManager
        adapter = DistrictAreaListAdapter(mFgViewModel.districtList.value)
        mViewBinding.rvAreas.adapter = adapter
        adapter.setOnDistrictItemClickListener(object :
            DistrictAreaListAdapter.OnDistrictItemClickListener {
            override fun onItemClicked(bean: DistrictEntity) {
                GwellLogUtils.i(TAG, "onItemClicked: bean = $bean")
                mFgViewModel.saveSelectedDistrict(bean)
                shareVM.districtLD.postValue(bean)
                findNavController().navigateUp()
            }
        })

        mViewBinding.ilSearchArea.setTextChangeCallback(object :
            SearchAreaInputLayout.TextChangeCallback {
            override fun onTextChange(content: String) {
                GwellLogUtils.i(TAG, "onTextChange: s = $content")
                adapter.filter.filter(content)
            }
        })

    }

    override fun initData() {
        super.initData()

        arguments?.let {
            val entity = AreaListFragmentArgs.fromBundle(it).keyDistrictBean
            mFgViewModel.mDistrictBean.postValue(entity)
        }

        mFgViewModel.getDistrictAreaAll()
    }

    override fun initLiveData(viewModel: AreaListVM, savedInstanceState: Bundle?) {
        super.initLiveData(viewModel, savedInstanceState)

        mFgViewModel.districtList.observe(this) {
            GwellLogUtils.i(TAG, "districtList: list = $it")
            adapter.updateDistrictList(it)
        }

        mFgViewModel.mDistrictBean.observe(this) {
            GwellLogUtils.i(TAG, "initLiveData: mDistrictBean = $it")
            it?.let {
                mViewBinding.tvDefaultArea.text = buildString {
                    append(it.districtName)
                    append(" +")
                    append(it.districtCode)
                }
            }
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.account_fragment_area_list
    }

    override fun <T : ViewModel?> loadViewModel(): Class<T> {
        return AreaListVM::class.java as Class<T>
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mViewBinding.ilSearchArea.onDestroy()
    }

}