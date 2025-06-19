package com.gw_reoqoo.house_watch.ui.card_list.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gw_reoqoo.house_watch.entities.ViewTypeModel
import com.gw_reoqoo.lib_base_architecture.vm.ABaseVM
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * @Description: - 看家卡片列表页VM
 * @Author: XIAOLEI
 * @Date: 2023/8/18
 */
@HiltViewModel
class CardListVM @Inject constructor() : ABaseVM() {

    /**
     * 是否展开
     */
    private val _expand = MutableLiveData<Boolean>()

    /**
     * 是否展开
     */
    val expand: LiveData<Boolean> get() = _expand

    fun switchExpand(expand: Boolean) {
        if (_expand.value != expand) {
            _expand.postValue(expand)
        }
    }
}