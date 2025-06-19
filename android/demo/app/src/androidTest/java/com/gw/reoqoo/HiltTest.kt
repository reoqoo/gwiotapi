package com.gw.reoqoo

import com.gw_reoqoo.component_family.api.interfaces.FamilyModeApi
import com.gw_reoqoo.component_family.data_store.GuideDataStore
import com.gw.component_push.datastore.PushDataStore
import com.gw_reoqoo.cp_account.api.kapi.IAccountApi
import com.gw_reoqoo.cp_account.kits.AccountMgrKit
import com.gw_reoqoo.lib_utils.toast.IToast
import com.gw.reoqoo.app.AppCoreInitTask
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

/**
 * @Description: -
 * @Author: XIAOLEI
 * @Date: 2023/10/10
 */
@HiltAndroidTest
class HiltTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var toast: IToast

    @Inject
    lateinit var moduleApi: FamilyModeApi

    @Inject
    lateinit var guideDataStore: GuideDataStore

    @Inject
    lateinit var coreInitTask: AppCoreInitTask

    @Inject
    lateinit var iAccountApi: IAccountApi

    @Inject
    lateinit var pushDataStore: PushDataStore

    @Before
    fun init() = runBlocking {
        hiltRule.inject()
        async(Dispatchers.Main) {
            coreInitTask.run()
            val info = iAccountApi.getAsyncUserInfo()
            assertNotNull(info)
            if (info != null) {
                AccountMgrKit.setAccessInfo(info.accessId, info.accessToken)
            }
        }.await()
    }


    /**
     * 测试吐司
     */
    @Test
    fun testToast(): Unit = runBlocking {
        async(Dispatchers.Main) { toast.show("测试吐司") }.await()
    }

    /**
     * 测试获取设备列表
     */
    @Test
    fun testGetDeviceList() {
        val devices = moduleApi.getDeviceList("014")
        assertNotNull(devices)
        assertTrue(devices.isNotEmpty())
    }

    /**
     * 测试获取设备信息
     */
    @Test
    fun testGetDeviceInfo() {
        val deviceId = "12885119690"
        val deviceInfo = moduleApi.deviceInfo(deviceId)
        assertNotNull(deviceInfo)
    }

    /**
     * 测试添加按钮的新手引导
     */
    @Test
    fun testSetAddBtnGuide() = runBlocking {
        guideDataStore.setAddBtnGuide(false)
        assertFalse(guideDataStore.getAddBtnGuide())
        guideDataStore.setAddBtnGuide(true)
        assertTrue(guideDataStore.getAddBtnGuide())
    }

    /**
     * 测试从远程加载设备
     */
    @Test
    fun testLoadDeviceFromRemote() = runBlocking {
        assertNotNull(moduleApi)
        moduleApi.refreshDevice()
        val userId = iAccountApi.getAsyncUserId()
        assertNotNull(userId)
        if (userId == null) return@runBlocking
        val devices = moduleApi.getDeviceList(userId)
        assertNotNull(devices)
        assert(devices.isNotEmpty())
    }

    /**
     * 测试不存在的设备
     */
    @Test
    fun testNotExistDevice() = runBlocking {
        for (i in 0 until 100) {
            val device = moduleApi.deviceInfo("dev_id:$i")
            assertNull(device)
            delay(5)
        }
    }

    @Test
    fun testPushDataSTore() = runBlocking {
        val userId = "fake_userId"
        val token = pushDataStore.getToken(userId)
        assertNull(token)
        pushDataStore.saveToken(userId,"fake_token")
        assertEquals(pushDataStore.getToken(userId),"fake_token")
    }
}