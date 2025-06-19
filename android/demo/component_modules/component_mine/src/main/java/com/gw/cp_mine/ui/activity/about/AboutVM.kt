package com.gw.cp_mine.ui.activity.about

import android.os.SystemClock
import androidx.lifecycle.MutableLiveData
import com.gw_reoqoo.lib_base_architecture.vm.ABaseVM
import com.gw_reoqoo.resource.R
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
@author: xuhaoyuan
@date: 2023/8/30
description:
1.
 */
@HiltViewModel
class AboutVM @Inject constructor() : ABaseVM() {

    val aboutMenuList = mutableListOf<AboutEnum>()

    val showDebugDialog = MutableLiveData<Boolean>()

    init {
        aboutMenuList.clear()
        aboutMenuList.add(AboutEnum.VERSION_UPDATE)
        aboutMenuList.add(AboutEnum.USER_PROTOCOL)
        aboutMenuList.add(AboutEnum.PRIVACY_POLICY)
    }

    private var mHits = LongArray(6)

    /**
     * 记录次数，点击6次展示设置debug弹窗
     *
     */
    fun jump2Debug() {
        // 每点击一次 实现左移一格数据
        System.arraycopy(mHits, 1, mHits, 0, mHits.size - 1)
        // 给数组的最后赋当前时钟值
        mHits[mHits.size - 1] = SystemClock.uptimeMillis()
        if (mHits[0] > SystemClock.uptimeMillis() - 2000) {
            mHits = LongArray(6)
            showDebugDialog.postValue(true)
        }
    }

}

enum class AboutEnum(val strRes: Int) {
    VERSION_UPDATE(R.string.AA0274),
    USER_PROTOCOL(R.string.AA0044),
    PRIVACY_POLICY(R.string.AA0368)
}