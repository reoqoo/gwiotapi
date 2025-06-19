package com.gw_reoqoo.cp_account.ui.fragment.area_list

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.gw_reoqoo.cp_account.datastore.AccountDataStoreApi
import com.gw_reoqoo.cp_account.sa.AccountSaEvent
import com.gw_reoqoo.cp_account.utils.DistrictCodeListManager
import com.gw.cp_config.api.IAppConfigApi
import com.gw_reoqoo.lib_base_architecture.vm.ABaseVM
import com.gw_reoqoo.lib_http.entities.DistrictEntity
import com.gwell.loglibs.GwellLogUtils
import com.jwkj.base_statistics.sa.kits.SA
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.Collator
import javax.inject.Inject

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/8/1 16:52
 * Description: AreaListVm
 */
@HiltViewModel
class AreaListVM @Inject constructor(
    private val configApi: IAppConfigApi, private val api: AccountDataStoreApi
) : ABaseVM() {

    companion object {
        private const val TAG = "AreaListVM"
    }

    @Inject
    lateinit var app: Application

    @Inject
    lateinit var manager: DistrictCodeListManager

    /**
     * 当前的地区码
     */
    var mDistrictBean: MutableLiveData<DistrictEntity> = MutableLiveData()

    /**
     * 地区码列表数据
     */
    val districtList: MutableLiveData<List<DistrictEntity>> = MutableLiveData()

    /**
     * 获取全部的地区码
     */
    fun getDistrictAreaAll() {
        viewModelScope.launch(Dispatchers.IO) {
            val districtCodeList = manager.getCodeList()
            GwellLogUtils.i(TAG, "districtCodeList = $districtCodeList")
            val collator = Collator.getInstance()
            val sortedStrings =
                districtCodeList?.sortedWith(compareBy { collator.getCollationKey(it.districtName) })
            sortedStrings?.let {
                districtList.postValue(it)
            }
        }
    }

    /**
     * 获取服务器支持的地区码
     */
    suspend fun getDistrictAreaByServer() {
        val districtCodeList = getSupportPhoneRegisterAreaList()
        val collator = Collator.getInstance()
        val sortedStrings =
            districtCodeList?.sortedWith(compareBy { collator.getCollationKey(it.districtName) })
        sortedStrings?.let {
            districtList.postValue(it)
        }
    }

    /**
     * 通过手机地区码获取国家地区码对象
     */
    suspend fun getDistrictBeanByCode(districtCode: String) {
        val districtCodeEntity = manager.getDistrictCodeInfoByCode(districtCode)
        districtCodeEntity?.let {
            mDistrictBean.postValue(it)
        }
    }

    /**
     * 根据输入的地区码判断是否支持手机注册
     */
    fun isSupportPhoneRegister(districtCode: String): Boolean {
        val districtCodeList = configApi.getCountryCodeList()
        for (i in districtCodeList.indices) {
            if (districtCodeList[i] == districtCode) {
                return true
            }
        }
        return false
    }

    /**
     * 获取 `所有的 支持使用手机号注册app帐号的地区 构成的 数组`
     * 注意: 这里的逻辑 是:
     * 1) app 在 第一次启动时 或者 每隔24小时, 会:
     * 从 服务器 拉取 `一个 所有的 支持使用手机号注册app帐号的地区 构成的 数组`
     * 并将 `这个 所有的 支持使用手机号注册app帐号的地区 构成的 数组` 保存到 `sp` 中
     * 2) 在 assets 下, 有 一个 district_code目录, 其中 保存着: app支持的所有的地区
     * 3) 而 这个函数, 会 返回 `所有的 支持使用手机号注册app帐号的地区 构成的 数组`
     * 其实, 就是 取 `sp 中 保存的 所有的 支持使用手机号注册app帐号的地区 构成的 数组` 和 `assets中的district_code目录中的所有的地区` 的 `交集`
     */
    private suspend fun getSupportPhoneRegisterAreaList(): List<DistrictEntity>? {
        val mDistrictCodeList = manager.getCodeList()
        if (mDistrictCodeList.isNullOrEmpty()) {
            return null
        } else {
            // 取得 `sp 中 保存的 所有的 支持使用手机号注册app帐号的地区 构成的 数组`
            val districtCodeList = configApi.getCountryCodeList()
            val supportList: MutableList<DistrictEntity> = ArrayList()
            for (districtCodeBean in mDistrictCodeList) {
                val districtCode: String = districtCodeBean.districtCode
                for (countryCode in districtCodeList) {
                    // 取: `sp 中 保存的 所有的 支持使用手机号注册app帐号的地区 构成的 数组` 和 `assets中的district_code目录中的所有的地区` 的 `交集`
                    if (countryCode == districtCode) {
                        supportList.add(districtCodeBean)
                    }
                }
            }
            return supportList
        }
    }

    /**
     * 获取支持获取手机验证码的国家地区
     */
    suspend fun getSupportPhoneVerCode() {
        val mDistrictCodeList = manager.getCodeList()
        if (mDistrictCodeList.isNullOrEmpty()) {
            return
        }
        val mSupportList = mutableListOf<DistrictEntity>()
        val supportAreas = configApi.getCountryCodeList()
        for (i in supportAreas.indices) {
            for (j in mDistrictCodeList.indices) {
                if (supportAreas[i] == mDistrictCodeList[j].districtCode) {
                    mSupportList.add(mDistrictCodeList[j])
                }
            }
        }
    }

    fun saveSelectedDistrict(entity: DistrictEntity) {
        api.setUserDistrictEntity(entity)
        // 事件上报
        SA.track(
            AccountSaEvent.REGION_GETNAME,
            mapOf(AccountSaEvent.EventAttr.REGION_NAME to entity.districtName)
        )
        mDistrictBean.postValue(entity)
    }

}