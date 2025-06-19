package com.gw.reoqoo

import com.gw.reoqoo.ClickChildAction.clickChildWithId
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.rule.ActivityTestRule
import androidx.test.uiautomator.UiDevice
import com.gw_reoqoo.component_family.data_store.GuideDataStore
import com.gw_reoqoo.lib_utils.toast.ToastImpl
import com.gw.reoqoo.ui.main.MainActivity
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * @Description: -
 * @Author: XIAOLEI
 * @Date: 2023/10/8
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class FamilyFragmentTest {
//    @get:Rule
//    val logoActivityRule = ActivityScenarioRule(LogoActivity::class.java)

    @get:Rule
    val mainActivityRule = ActivityTestRule(MainActivity::class.java)


    private var oldVideoCardGuide: Boolean = true
    private var oldAddBtnGuide: Boolean = true
    private var oldFirstDeviceGuide: Boolean = true

    @Before
    fun beforeTest() = runBlocking {
        println("测试之前初始化一些数据")
        val context = getInstrumentation().targetContext
        val dataStore = GuideDataStore(context)
        // 暂存
        oldVideoCardGuide = dataStore.getVideoCardGuide()
        oldAddBtnGuide = dataStore.getAddBtnGuide()
        oldFirstDeviceGuide = dataStore.getFirstDeviceGuide()

        // 先暂时隐藏
        dataStore.setVideoCardGuide(true)
        dataStore.setAddBtnGuide(true)
        dataStore.setFirstDeviceGuide(true)
    }

    @After
    fun afterTest() = runBlocking {
        val context = getInstrumentation().targetContext
        val dataStore = GuideDataStore(context)
        // 恢复
        dataStore.setVideoCardGuide(oldVideoCardGuide)
        dataStore.setAddBtnGuide(oldAddBtnGuide)
        dataStore.setFirstDeviceGuide(oldFirstDeviceGuide)
    }

    /**
     * 测试吐司
     */
    @Test
    fun testToast(): Unit = runBlocking {
        val context = getInstrumentation().targetContext
        val toast = ToastImpl(context)
        for (i in 0 until 100) {
            async(Dispatchers.Main) { toast.show("测试吐司$i") }.await()
            delay(1000)
            onView(withText("测试吐司$i"))
                .inRoot(withDecorView(not(mainActivityRule.activity.window.decorView)))
                .check(matches(ViewMatchers.isDisplayed()))
            delay(10)
        }
    }

    /**
     * 测试设备开机/关机
     */
    @Test
    fun testDevicePowerOnOrOff() = runBlocking {
        for (i in 0 until 10) {
            onView(withId(com.gw_reoqoo.component_family.R.id.device_rv))
                .perform(
                    actionOnItemAtPosition<RecyclerView.ViewHolder>(
                        0,
                        clickChildWithId(com.gw_reoqoo.component_family.R.id.bt_turn_off_or_on)
                    )
                )
            println("点击列表中第一个item中的开关机按钮")
            onView(withText("确定"))
                .check(matches(ViewMatchers.isDisplayed()))
                .inRoot(isDialog())
                .perform(click())
            println("点击弹出的确定按钮存在dialog中")
            delay(100)
        }
    }

    /**
     * 测试新手引导的DataStore
     */
    @Test
    fun testGuideDataStore() = runBlocking {
        val context = getInstrumentation().targetContext
        val dataStore = GuideDataStore(context)

        dataStore.setAddBtnGuide(false)
        assertFalse(dataStore.getAddBtnGuide())

        dataStore.setAddBtnGuide(true)
        assertTrue(dataStore.getAddBtnGuide())

        dataStore.setFirstDeviceGuide(false)
        assertFalse(dataStore.getFirstDeviceGuide())

        dataStore.setFirstDeviceGuide(true)
        assertTrue(dataStore.getFirstDeviceGuide())

        dataStore.setVideoCardGuide(false)
        assertFalse(dataStore.getVideoCardGuide())

        dataStore.setVideoCardGuide(true)
        assertTrue(dataStore.getVideoCardGuide())
    }

    /**
     * 测试添加设备
     */
    @Test
    fun testAddDevice() {
        val context = getInstrumentation().targetContext
        onView(withId(com.gw_reoqoo.component_family.R.id.add_btn))
            .check(matches(ViewMatchers.isDisplayed()))
        println("添加按钮存在")
        onView(withId(com.gw_reoqoo.component_family.R.id.add_btn))
            .perform(click())
        println("点击添加按钮")
        onView(withText(context.getString(com.gw_reoqoo.resource.R.string.AA0049)))
            .check(matches(ViewMatchers.isDisplayed()))
        println("添加设备按钮存在")
        onView(withText(context.getString(com.gw_reoqoo.resource.R.string.AA0049)))
            .perform(click())
        println("点击添加设备按钮")
    }

    /**
     * 测试删除设备
     */
    @Test
    fun testDeleteDevice() {
        onView(withId(com.gw_reoqoo.component_family.R.id.device_rv))
            .check(matches(ViewMatchers.isDisplayed()))
        println("设备列表出现")
        onView(withId(com.gw_reoqoo.component_family.R.id.device_rv))
            .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(0, longClick()))
        println("长按第一个设备")
        onView(withId(com.gw_reoqoo.component_family.R.id.ll_delete))
            .check(matches(ViewMatchers.isDisplayed()))
        println("删除按钮存在")
        onView(withId(com.gw_reoqoo.component_family.R.id.ll_delete))
            .perform(click())
        println("点击删除按钮,弹出确认弹窗")
        onView(withText("取消")).check(matches(ViewMatchers.isDisplayed()))
        println("取消按钮存在")
        onView(withText("取消")).perform(click())
        println("点击取消按钮")
    }

    /**
     * 测试分享设备
     */
    @Test
    fun testClickShare() {
        onView(withId(com.gw_reoqoo.component_family.R.id.device_rv))
            .check(matches(ViewMatchers.isDisplayed()))
        println("设备列表出现")
        onView(withId(com.gw_reoqoo.component_family.R.id.device_rv))
            .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(0, longClick()))
        println("长按第一个设备")
        println("弹窗显示，分享设备按钮显示")
        onView(withId(com.gw_reoqoo.component_family.R.id.ll_shared))
            .perform(click())
        println("点击设备分享")
        onView(withId(com.gw_reoqoo.component_device_share.R.id.et_account))
            .check(matches(ViewMatchers.isDisplayed()))
        println("校验是否进入设备分享界面")
        val device = UiDevice.getInstance(getInstrumentation())
        device.pressBack()
        println("按返回键")
    }

    /**
     * 长按设备
     */
    @Test
    fun testLongPressDevice() {
        onView(withId(com.gw_reoqoo.component_family.R.id.device_rv))
            .check(matches(ViewMatchers.isDisplayed()))
        println("设备列表出现")
        onView(withId(com.gw_reoqoo.component_family.R.id.device_rv))
            .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(0, longClick()))
        println("长按第一个设备")
        onView(withId(com.gw_reoqoo.component_family.R.id.ll_delete))
            .check(matches(ViewMatchers.isDisplayed()))
        println("弹窗显示，删除设备按钮显示")
    }

    /**
     * 测试点击设备
     */
    @Test
    fun testClickOnlineDevice() {
        onView(withId(com.gw_reoqoo.component_family.R.id.device_rv))
            .check(matches(ViewMatchers.isDisplayed()))
        println("设备列表出现")
        onView(withId(com.gw_reoqoo.component_family.R.id.device_rv))
            .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))
        println("点击第一个设备")
    }

    /**
     * 测试下拉刷新设备列表
     */
    @Test
    fun testPullToRefresh() {
        onView(withId(com.gw_reoqoo.component_family.R.id.pull_to_refresh_layout))
            .check(matches(ViewMatchers.isDisplayed()))
        val device = UiDevice.getInstance(getInstrumentation())
        // 获取设备屏幕的宽度和高度
        val screenWidth = device.displayWidth
        val screenHeight = device.displayHeight
        // 设置起始点和终点的坐标，这里假设下拉操作从屏幕中间位置开始，下拉到屏幕底部
        val startX = screenWidth / 2
        val startY = screenHeight / 2
        val endX = startX
        val endY = screenHeight * 3 / 4 // 下拉到屏幕底部的3/4位置
        // 设置滑动操作的持续时间，单位为毫秒
        val steps = 200 // 步数，（每步5ms）
        // 执行下拉操作
        device.swipe(startX, startY, endX, endY, steps)
        println("测试下拉刷新设备列表")
        onView(withClassName(`is`("com.gw_reoqoo.component_family.widgets.PullToRefreshHeader")))
            .check(matches(ViewMatchers.isDisplayed()))
        println("顶部的下拉刷新头部是否显示")
    }

    /**
     * 测试设备列表是否存在
     */
    @Test
    fun testDeviceListExist() {
        onView(withId(com.gw_reoqoo.component_family.R.id.device_rv))
            .check(matches(ViewMatchers.isDisplayed()))
        println("设备列表存在")
    }

    /**
     * 测试FamilyFragment是否存在
     */
    @Test
    fun testFamilyFragment() {
        onView(withId(com.gw_reoqoo.component_family.R.id.pull_to_refresh_layout))
            .check(matches(ViewMatchers.isDisplayed()))
        println("FamilyFragment显示成功")
    }

    /**
     * 测试MainActivity是否存在
     */
    @Test
    fun testMainActivity() {
        onView(withId(com.gw.reoqoo.R.id.nav_btn_menu))
            .check(matches(ViewMatchers.isDisplayed()))
        println("MainActivity显示成功")
    }
}