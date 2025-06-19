package com.gw_reoqoo.component_family.ui.scene_empty

import androidx.lifecycle.ViewModel
import com.gw_reoqoo.component_family.R
import com.gw_reoqoo.component_family.databinding.FamilyFragmentSceneEmptyBinding
import com.gw_reoqoo.component_family.ui.scene_empty.vm.EmptySceneVM
import com.gw_reoqoo.lib_base_architecture.view.ABaseMVVMDBFragment
import com.gw_reoqoo.lib_router.ReoqooRouterPath
import com.therouter.router.Route
import dagger.hilt.android.AndroidEntryPoint

/**
 * @Description: - 当场景为空时，则用这个界面代替
 * @Author: XIAOLEI
 * @Date: 2023/8/1
 */
@Route(path = ReoqooRouterPath.Family.FAMILY_FRAGMENT_SCENE_EMPTY_PATH)
@AndroidEntryPoint
class EmptySceneFragment : ABaseMVVMDBFragment<FamilyFragmentSceneEmptyBinding, EmptySceneVM>() {
    override fun getLayoutId() = R.layout.family_fragment_scene_empty
    override fun <T : ViewModel?> loadViewModel() = EmptySceneVM::class.java as Class<T>
}