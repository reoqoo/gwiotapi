package com.gw_reoqoo.component_device_share.ui.permission_setting.vm

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.gw_reoqoo.component_device_share.ui.permission_setting.adapter.FunctionEntity
import com.gw_reoqoo.component_device_share.ui.permission_setting.adapter.FunctionKey
import com.gw_reoqoo.component_device_share.ui.permission_setting.adapter.PermissionEntity
import com.gw.component_plugin_service.api.IPluginManager
import com.gw.component_plugin_service.api.ModeResponse
import com.gw_reoqoo.lib_base_architecture.vm.ABaseVM
import com.gw_reoqoo.lib_http.jsonToEntity
import com.gw.lib_plugin_service.IResultCallback
import com.gw_reoqoo.lib_utils.ktx.bitAt
import com.gw_reoqoo.lib_utils.ktx.bitUpdate
import com.gwell.loglibs.GwellLogUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.gw_reoqoo.resource.R as RR

/**
 * Author: yanzheng@gwell.cc
 * Time: 2024/7/13 14:57
 * Description: PermissionSettingVM
 */
@HiltViewModel
class PermissionSettingVM @Inject constructor(
    private val app: Application,
    private val pluginApi: IPluginManager
) : ABaseVM() {

    companion object {
        private const val TAG = "PermissionSettingVM"
    }

    val list = MutableLiveData<List<PermissionEntity>>()

    var _bits: Int? = null

    fun getPermissionList() {
        // 设备监控
        val monitorEntity = FunctionEntity(
            functionName = app.getString(RR.string.AA0637),
            functionEnable = false,
            functionStatus = true
        )
        val talkEntity = FunctionEntity(
            functionKey = FunctionKey.TALK,
            functionName = app.getString(RR.string.AA0638),
            functionEnable = true,
            functionStatus = false
        )
        val ptzEntity = FunctionEntity(
            functionKey = FunctionKey.PTZ,
            functionName = app.getString(RR.string.AA0639),
            functionEnable = false,
            functionStatus = true
        )
        val devMonitorEntity =
            PermissionEntity(
                app.getString(RR.string.AA0643),
                listOf(monitorEntity, talkEntity, ptzEntity)
            )

        // 设备回放
        val playbackEntity = FunctionEntity(
            functionKey = FunctionKey.PLAYBACK,
            functionName = app.getString(RR.string.AA0640),
            functionEnable = false,
            functionStatus = false
        )
        val devPlaybackEntity =
            PermissionEntity(app.getString(RR.string.AA0644), listOf(playbackEntity))


        // 智能看家
        val watchEntity = FunctionEntity(
            functionKey = FunctionKey.WATCH,
            functionName = app.getString(RR.string.AA0192),
            functionEnable = false,
            functionStatus = false
        )
        // 设备配置
        val configEntity = FunctionEntity(
            functionKey = FunctionKey.CONFIG,
            functionName = app.getString(RR.string.AA0642),
            functionEnable = false,
            functionStatus = false
        )
        // 设备控制
        val devControlEntity =
            PermissionEntity(app.getString(RR.string.AA0645), listOf(watchEntity, configEntity))

        list.postValue(listOf(devMonitorEntity, devPlaybackEntity, devControlEntity))
    }

    suspend fun loadPermissionSettings(deviceId: String) {
        pluginApi.getPermissionBits(deviceId, object : IResultCallback {
            override fun onFailed(code: Int, errorMsg: String?) {
                GwellLogUtils.e(TAG, "getPermissionBits:$code, $errorMsg")
            }

            override fun onSuccess(code: Int, responseInfo: String?) {
                GwellLogUtils.i(TAG, "getPermissionBits:$code, $responseInfo")
                if (code == 0 && !responseInfo.isNullOrEmpty()) {
                    responseInfo.jsonToEntity<ModeResponse>()?.setVal?.let { _bits ->
                        this@PermissionSettingVM._bits = _bits
                        list.value?.map { _permission ->
                            _permission.function.map { _function ->
                                when (_function.functionKey) {
                                    FunctionKey.TALK -> {
                                        _function.functionStatus =
                                            _bits.bitAt(FunctionKey.TALK.key) == 1
                                        _function.functionEnable = true
                                    }

                                    FunctionKey.PTZ -> {
                                        _function.functionStatus =
                                            _bits.bitAt(FunctionKey.PTZ.key) == 1
                                        _function.functionEnable = true
                                    }

                                    FunctionKey.PLAYBACK -> {
                                        _function.functionStatus =
                                            _bits.bitAt(FunctionKey.PLAYBACK.key) == 1
                                        _function.functionEnable = true
                                    }

                                    FunctionKey.WATCH -> {
                                        _function.functionStatus =
                                            _bits.bitAt(FunctionKey.WATCH.key) == 1
                                        _function.functionEnable = true
                                    }

                                    FunctionKey.CONFIG -> {
                                        _function.functionStatus =
                                            _bits.bitAt(FunctionKey.CONFIG.key) == 1
                                        _function.functionEnable = true
                                    }

                                    else -> {}
                                }
                            }
                        }
                    }
                    list.postValue(list.value)
                }
            }
        })
    }

    fun setPermissionBit(deviceId: String, func: FunctionKey, has: Boolean) {
        this._bits?.let {
            this._bits = it.bitUpdate(func.key, has)
            GwellLogUtils.i(TAG, "setPermissionBit: $func, $has, $it, ${this._bits}")
            pluginApi.setPermissionBits(deviceId, this._bits ?: 0, object : IResultCallback {
                override fun onFailed(code: Int, errorMsg: String?) {
                    GwellLogUtils.e(TAG, "setPermissionBits:$code, $errorMsg")
                }

                override fun onSuccess(code: Int, responseInfo: String?) {
                    GwellLogUtils.i(TAG, "setPermissionBits:$code, $responseInfo")
                }
            })
        }
    }

}