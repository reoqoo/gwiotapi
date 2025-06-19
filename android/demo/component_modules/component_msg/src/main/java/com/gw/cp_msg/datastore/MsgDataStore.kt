package com.gw.cp_msg.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.gw_reoqoo.lib_datastore.DataStoreUtils
import dagger.*
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject

/**
 * Author: yanzheng@gwell.cc
 * Time: 2023/7/25 10:39
 * Description: AccountDataStore
 */
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "msg")

class MsgDataStore @Inject constructor(@ApplicationContext context: Context) :
    IMsgDataStoreApi {

    companion object {

        private const val TAG = "MsgDataStore"

        /**
         * 已读的报警事件(设备id+报警Id+报警类型)
         */
        private const val KEY_VALUE_ALARM_EVENT_READ = "key_value_alarm_event_read"

        /**
         * 已读的APP升级事件(用户id+升级版本号)
         */
        const val KEY_VALUE_APP_UPDATE_READ = "key_value_app_update_read"

        /**
         * 已读的设备升级事件(设备id+升级版本号)
         */
        const val KEY_VALUE_DEVICE_UPDATE_READ = "key_value_device_update_read"

    }

    private val dataStore = context.dataStore

    override fun getAlarmEventRead(): String {
        return DataStoreUtils.getData(dataStore, KEY_VALUE_ALARM_EVENT_READ, "")
    }

    override fun setAlarmEventRead(alarmEvent: String) {
        DataStoreUtils.putData(dataStore, KEY_VALUE_ALARM_EVENT_READ, alarmEvent)
    }

    override fun getAppUpgradeRead(): String? {
        return DataStoreUtils.getData(dataStore, KEY_VALUE_APP_UPDATE_READ, "")
    }

    override fun setAppUpgradeRead(upgrade: String) {
        DataStoreUtils.putData(dataStore, KEY_VALUE_APP_UPDATE_READ, upgrade)
    }

    override fun getDevUpgradeRead(deviceId: String): String? {
        return DataStoreUtils.getData(dataStore, KEY_VALUE_DEVICE_UPDATE_READ, "")
    }

    override fun setDevUpgradeRead(deviceId: String, upgrade: String) {
        DataStoreUtils.putData(dataStore, KEY_VALUE_DEVICE_UPDATE_READ, "$deviceId$upgrade")
    }

}

interface IMsgDataStoreApi {

    /**
     * 获取 已读的报警事件
     *
     * @return String?
     */
    fun getAlarmEventRead(): String?

    /**
     * 设置 已读的报警事件
     *
     * @param alarmEvent String
     */
    fun setAlarmEventRead(alarmEvent: String)

    /**
     * 获取 已读的app升级
     *
     * @return String?
     */
    fun getAppUpgradeRead(): String?

    /**
     * 设置 已读的app升级
     *
     * @param upgrade String
     */
    fun setAppUpgradeRead(upgrade: String)

    /**
     * 获取 已读的设备升级
     *
     * @return String?
     */
    fun getDevUpgradeRead(deviceId: String): String?

    /**
     * 设置 已读的设备升级
     *
     * @param upgrade String 升级信息
     */
    fun setDevUpgradeRead(deviceId: String, upgrade: String)

}

@InstallIn(SingletonComponent::class)
@Module
abstract class IMsgDataApi {
    @Binds
    abstract fun api(impl: MsgDataStore): IMsgDataStoreApi

}