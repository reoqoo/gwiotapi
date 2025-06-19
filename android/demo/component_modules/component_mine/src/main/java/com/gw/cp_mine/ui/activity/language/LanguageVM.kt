package com.gw.cp_mine.ui.activity.language

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gw.cp_mine.entity.Language
import com.gw.cp_mine.entity.LanguageEntity
import com.gw.cp_mine.kits.LanguageMgr
import com.gw_reoqoo.lib_base_architecture.vm.ABaseVM
import com.gwell.loglibs.GwellLogUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


/**
@author: xuhaoyuan
@date: 2023/9/4
description:
1. 语言页面的VM，存入一个list数据，用于适配器使用；
 */
@HiltViewModel
class LanguageVM @Inject constructor(
    private val languageMgr: LanguageMgr
) : ABaseVM() {

    companion object {
        private const val TAG = "LanguageVM"
    }

    /**
     * 语言列表
     */
    private val _languageList = MutableLiveData<List<LanguageEntity>>()

    /**
     * 语言列表
     */
    val languageList: LiveData<List<LanguageEntity>> get() = _languageList

    /**
     * 获取当前语言
     */
    val currentLanguage: Language get() = languageMgr.getAppLanguage()

    init {
        loadLanguageList()
    }

    /**
     * 保存用户选择的语言状态
     * 在app加载的时候会第一时间读取语言类型
     */
    fun saveLanguageSettings(language: Language) {
        GwellLogUtils.i(TAG, "saveLanguageSettings: itemName = $language")
        languageMgr.saveAppLocaleLanguage(language)
    }


    /**
     * 加载语言列表
     */
    private fun loadLanguageList() {
        val entities = Language.values().map {
            LanguageEntity(it, it == currentLanguage)
        }
        _languageList.postValue(entities)
    }
}