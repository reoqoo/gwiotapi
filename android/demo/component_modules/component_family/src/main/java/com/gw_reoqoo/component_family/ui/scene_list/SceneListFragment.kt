package com.gw_reoqoo.component_family.ui.scene_list

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModel
import com.gw_reoqoo.component_family.R
import com.gw_reoqoo.component_family.databinding.FamilyFragmentSceneBinding
import com.gw_reoqoo.component_family.ui.scene_list.vm.SceneListVM
import com.gw_reoqoo.lib_base_architecture.view.ABaseMVVMDBFragment
import com.gw_reoqoo.lib_router.ReoqooRouterPath
import com.therouter.router.Route
import dagger.hilt.android.AndroidEntryPoint

/**
 * @Description: - 场景列表界面
 * @Author: XIAOLEI
 * @Date: 2023/8/1
 */
@Route(path = ReoqooRouterPath.Family.FAMILY_FRAGMENT_SCENE_LIST_PATH)
@AndroidEntryPoint
class SceneListFragment : ABaseMVVMDBFragment<FamilyFragmentSceneBinding, SceneListVM>() {
    override fun getLayoutId() = R.layout.family_fragment_scene
    override fun <T : ViewModel?> loadViewModel() = SceneListVM::class.java as Class<T>

    override fun initView(view: View, savedInstanceState: Bundle?) {
        super.initView(view, savedInstanceState)
    }

    override fun initData() {
        super.initData()
    }
}