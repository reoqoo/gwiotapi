package com.gw.cp_mine.ui.activity.language

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import com.gw.cp_mine.R
import com.gw.cp_mine.databinding.MineActivityLanguageBinding as Binding
import com.gw_reoqoo.lib_base_architecture.view.ABaseMVVMDBActivity
import com.gw_reoqoo.lib_router.ReoqooRouterPath
import com.gwell.loglibs.GwellLogUtils
import com.jakewharton.processphoenix.ProcessPhoenix
import com.therouter.router.Route
import dagger.hilt.android.AndroidEntryPoint


/**
 * 选择语言页面，可以修改app的语言至简体中文、繁体中文、英语等
 */
@Route(path = ReoqooRouterPath.MinePath.ACTIVITY_LANGUAGE)
@AndroidEntryPoint
class LanguageActivity : ABaseMVVMDBActivity<Binding, LanguageVM>() {

    companion object {
        private const val TAG = "LanguageActivity"
    }

    private val languageAdapter = LanguageListAdapter()

    override fun getLayoutId(): Int = R.layout.mine_activity_language
    override fun <T : ViewModel?> loadViewModel() = LanguageVM::class.java as Class<T>
    override fun onViewLoadFinish() {
        setStatusBarColor()
    }

    override fun initView() {
        mViewBinding.appTitle.leftIcon.setOnClickListener { finish() }
        mViewBinding.appTitle.rightText.setOnClickListener {
            val entity = languageAdapter.data.firstOrNull { it.selected }
            if (entity != null) {
                // 存储
                mViewModel.saveLanguageSettings(entity.language)
                // 重启
                ProcessPhoenix.triggerRebirth(this)
            }
        }

        mViewBinding.rvLanguageList.run {
            layoutManager = LinearLayoutManager(context)
            adapter = languageAdapter.apply {
                setOnItemClickListener { entity ->
                    val currentLanguage = mViewModel.currentLanguage
                    GwellLogUtils.i(TAG, "setOnItemClickListener:$entity")
                    val newData = languageAdapter.data
                    for (newDatum in newData) {
                        newDatum.selected = entity == newDatum
                    }
                    languageAdapter.updateData(newData, false)
                    mViewBinding.appTitle.rightText.visible = entity.language != currentLanguage
                }
            }
        }
    }


    override fun initLiveData(viewModel: LanguageVM, savedInstanceState: Bundle?) {
        super.initLiveData(viewModel, savedInstanceState)
        mViewModel.languageList.observe(this) {
            languageAdapter.updateData(it, false)
        }
    }
}