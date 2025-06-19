# Android单元测试
> author：肖蕾

### 1. 前言
测试应用是应用开发过程中不可或缺的一部分。通过持续对应用运行测试，您可以在公开发布应用之前验证其正确性、功能行为和易用性。

测试还会为您提供以下优势：
 - 快速获得故障反馈。
 - 在开发周期中尽早进行故障检测。
 - 更安全的代码重构，让您可以优化代码而不必担心回归。
稳定的开发速度，帮助您最大限度地减轻技术负担。

### 2. 首先

单元测试在Android开发中，分为3种类型：
 - 纯Java类型的单元测试
 - 带Android环境的单元测试（无UI）
 - Android环境的UI单元测试

我们这次使用JUnit,Mockito,AndroidXTest来完成这三种类型的单元测试 

#### 2.1 JUnit 注解以及解释

| Annotation                                                             | 描述                                                                                                                          |
| :--------------------------------------------------------------------- | :---------------------------------------------------------------------------------------------------------------------------- |
| @Test public void method()                                             | 定义所在方法为单元测试方法                                                                                                    |
| @Test (expected = Exception.class) public void method()                | 测试方法若没有抛出Annotation中的Exception类型(子类也可以)->失败                                                               |
| @Test(timeout=100) public void method()                                | 性能测试，如果方法耗时超过100毫秒->失败                                                                                       |
| @Before public void method()                                           | 这个方法在每个测试之前执行，用于准备测试环境(如: 初始化类，读输入流等)，在一个测试类中，每个@Test方法的执行都会触发一次调用。 |
| @After public void method()                                            | 这个方法在每个测试之后执行，用于清理测试环境数据，在一个测试类中，每个@Test方法的执行都会触发一次调用。                       |
| @BeforeClass public static void method()                               | 这个方法在所有测试开始之前执行一次，用于做一些耗时的初始化工作(如: 连接数据库)，方法必须是static                              |
| @AfterClass public static void method()                                | 这个方法在所有测试结束之后执行一次，用于清理数据(如: 断开数据连接)，方法必须是static                                          |
| @Ignore或者@Ignore("太耗时") public void method()                      | 忽略当前测试方法，一般用于测试方法还没有准备好，或者太耗时之类的                                                              |
| @FixMethodOrder(MethodSorters.NAME_ASCENDING) public class TestClass{} | 使得该测试类中的所有测试方法都按照方法名的字母顺序执行，可以指定3个值，分别是DEFAULT、JVM、NAME_ASCENDING                     |

> 链接：https://www.jianshu.com/p/aa51a3e007e2

### 3. 纯Java单元测试

#### 3.1 添加依赖
纯Java的单元测试中，只需要在build.gradle内新增对Junit的依赖：

```gradle
dependencies {
    testImplementation 'junit:junit:4.13.2'
}
```

#### 3.2 编写测试代码

假设，我现在新增了一个类，用来验证邮箱的有效性，内部使用正则表达式来实现：

```kotlin
object GlobalUtil {
    fun isEmailValid(email: String): Boolean {
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
        return emailRegex.matches(email)
    }
}
```

现在我需要使用单元测试，来验证这个函数是否正常预期，那么现在应该在 ```app/src/test[unitTest]``` 里，新增一个类 ```GlobalUtilTest.kt``` ：

```kotlin
class GlobalUtilTest {
    @Test
    fun test_isEmailValid() {
        assert(GlobalUtil.isEmailValid("123@qq.com"))
    }
}
```

这时候在 ```test_isEmailValid``` 的左边会显示一个绿色的三角形按钮，点击这个按钮，点击 ```Run 'GlobalUtilTest.test_isEmailValid'``` 则会自动运行验证。

那有的时候，我们需要传入一些数据对这个数据进行校验，可是需要拿到这个数据的条件比较苛刻，譬如：传入一个Context验证获取到string的时候再进行处理，验证我们某些函数是否运行如期待，可是Context不太好拿到应该怎么办呢？总不能修改函数实现吧？

示例代码：

```kotlin

fun getContextString(context: Context): Int {
    return context.getString(R.string.number).toInt()
}
```

### 4. 带模拟数据的Java单元测试

这时候，就需要模拟一些数据来使得程序如期单元测试了，目前我们使用的数据模拟框架是 [Mockito](https://github.com/mockito/mockito-kotlin)

#### 4.1 gradle新增依赖

```gradle
dependencies {
    testImplementation 'junit:junit:4.13.2'
    // Optional -- Robolectric environment
    testImplementation "androidx.test:core:1.5.0"
    // Optional -- Mockito framework
    testImplementation "org.mockito:mockito-core:5.6.0"
    // Optional -- mockito-kotlin
    testImplementation "org.mockito.kotlin:mockito-kotlin:5.1.0"
    // Optional -- Mockk framework
    testImplementation "io.mockk:mockk:1.13.8"
}
```

#### 4.2 编写测试代码

待测试代码片段：

```kotlin
fun getContextString(context: Context): Int {
    return context.getString(R.string.number).toInt()
}
```

在 ```app/src/test[unitTest]``` 里，新增一个类 ```MockContextTest.kt``` ：

```kotlin
/**
 * @Description: -
 * @Author: XIAOLEI
 * @Date: 2023/10/8
 */
@RunWith(MockitoJUnitRunner::class)
class MockContextTest {

    @Mock
    private lateinit var mockContext: Context

    @Test
    fun test_getStringFromContext() {
        mockContext = Mockito.mock(Context::class.java, withSettings()).apply {
            KStubbing(this).apply {
                on { getString(R.string.number) } doReturn "123456"
            }
        }
        val str = GlobalUtil.getContextString(mockContext)
        assertEquals(str, 123456)
    }
}
```

这里需要注意一下，在class上需要添加```@RunWith(MockitoJUnitRunner::class)```的注解，然后使用```Mockito.mock```来模拟需要的对象，```KStubbing```来模拟对这个对象的行为，并给出响应数据。

```kotlin
KStubbing(this).apply {
    on { getString(R.string.number) } doReturn "123456"
}
```

还是和刚才一样，点击绿色按钮，执行。

> 关于 Mockito 的使用文档以及进阶使用，建议移步 [GitHub](https://github.com/mockito/mockito-kotlin) 查看更加详细的解释，这里只是做一个抛砖的作用

### 5. Android单元测试

除了上述的关于Java层面，或者是数据可以通过模拟手段来达到目的的单元测试，作为一个Android Developer 更多的是需要测试在**真实设备**上的一些数据。那这种情况应该怎么达到目的呢？

我们这边使用的是 ``` AndroidX Test ``` 来达到测试目的。

#### 5.1 设置依赖

这里列出常用的依赖项：

```gradle
dependencies {
    def espressoVersion = "3.5.1"
    // Core library
    androidTestImplementation "androidx.test:core:1.5.0"

    // AndroidJUnitRunner and JUnit Rules
    androidTestImplementation "androidx.test:runner:1.5.2"
    androidTestImplementation "androidx.test:rules:1.5.0"

    androidTestImplementation "androidx.fragment:fragment-testing:1.6.0"

    // Assertions
    androidTestImplementation "androidx.test.ext:junit:1.1.5"
    androidTestImplementation "androidx.test.ext:truth:1.5.0"

    // Espresso dependencies
    androidTestImplementation "androidx.test.espresso:espresso-core:$espressoVersion"
    androidTestImplementation "androidx.test.espresso:espresso-contrib:$espressoVersion"
    androidTestImplementation "androidx.test.espresso:espresso-intents:$espressoVersion"
    androidTestImplementation "androidx.test.espresso:espresso-accessibility:$espressoVersion"
    androidTestImplementation "androidx.test.espresso:espresso-web:$espressoVersion"
    androidTestImplementation "androidx.test.espresso:espresso-remote:$espressoVersion"
    androidTestImplementation "androidx.test.espresso.idling:idling-concurrent:$espressoVersion"

    // The following Espresso dependency can be either "implementation",
    // or "androidTestImplementation", depending on whether you want the
    // dependency to appear on your APK’"s compile classpath or the test APK
    // classpath.
    androidTestImplementation "androidx.test.espresso:espresso-idling-resource:$espressoVersion"
}
```
#### 5.2 编写测试代码

假设我们需要测试一下包名是否是正确的，或者获取当前设备产品名称

待测试代码：

```kotlin
object ContextUtil {
    fun getPackageName(context: Context): String {
        return context.packageName
    }

    fun getDeviceModel(): String? {
        return Build.MODEL
    }
}
```

我们现在需要在 ```app\src\androidTest\java\com\xiaolei\testjunit``` 新增一个单元测试类：

**NoUITest.kt**

```kotlin
package com.xiaolei.testjunit

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class NoUITest {

    private lateinit var context: Context

    @Before
    fun init() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        println("初始化资源")
    }

    @After
    fun release() {
        println("释放资源")
    }

    @Test
    fun useAppContext() {
        // Context of the app under test.
        assertEquals("com.xiaolei.testjunit", ContextUtil.getPackageName(context))
    }

    @Test
    fun testDeviceModel() {
        val deviceModel = ContextUtil.getDeviceModel()
        assertNotNull(deviceModel)
    }
}
```


这里需要注意一下，在class上使用 ```@RunWith(AndroidJUnit4::class)``` 注解，然后对每个需要做单元测试的函数上，添加 ```@Test``` 的注解，点击运行即可。

这里由于是需要设备的真实环境，所以AndroidStudio会打包2个apk安装到你的设备上，然后自动执行对应单元测试，所以这个的测试速度是比纯Java的单元测试要慢的，根据项目的大小，决定速度的慢快。

> AndroidX Test 的参考文档 [点击这里](https://developer.android.google.cn/training/testing/instrumented-tests/androidx-test-libraries/test-setup?hl=zh-cn)

### 6. Android界面单元测试

现在到了我们最复杂的环节，Android端与UI相关的测试，为了配合Android端的UI单元测试，所以需要使用 ```Espresso``` 来配合测试。

> Espresso 的详细文档教程：https://developer.android.google.cn/training/testing/espresso?hl=zh-cn

这里由于Android版本带来的限制问题，有时候实体机会导致APP不能自动启动单元测试的情况，所以建议使用模拟器来进行单元测试：

> Genymotion [https://www.genymotion.com/download/](https://www.genymotion.com/download/)
>
> Genymotion 因为是x86的CPU架构，所以需要安装一个 ARM 转 X86 的二进制转译插件：[https://github.com/m9rco/Genymotion_ARM_Translation](https://github.com/m9rco/Genymotion_ARM_Translation) 由于二进制转译插件最高支持的Android版本是Android9，所以在Genymotion中新建的Android虚拟机版本，建议为Android9

#### 6.1 依赖

如5.1

#### 6.2 常用规则

##### 6.2.1 测试Activity

> https://developer.android.google.cn/training/testing/instrumented-tests/androidx-test-libraries/rules?hl=zh-cn#activityscenariorule

##### 6.2.2 测试Service

> https://developer.android.google.cn/training/testing/other-components/services?hl=zh-cn

##### 6.2.3 测试Fragment

> https://developer.android.google.cn/guide/fragments/test?hl=zh-cn

##### 6.2.4 测试ContentProvider

> https://developer.android.google.cn/training/testing/other-components/content-providers?hl=zh-cn

##### 6.2.5 测试Intent

> https://developer.android.google.cn/training/testing/espresso/intents?hl=zh-cn

##### 6.2.6 测试列表

> https://developer.android.google.cn/training/testing/espresso/lists?hl=zh-cn

##### 6.2.7 多进程测试

> https://developer.android.google.cn/training/testing/espresso/multiprocess?hl=zh-cn

##### 6.2.8 WebView测试

> https://developer.android.google.cn/training/testing/espresso/web?hl=zh-cn

##### 6.2.9 Demo

> https://developer.android.google.cn/training/testing/espresso/additional-resources?hl=zh-cn


#### 6.3 Activity测试

> 参考资料：[https://developer.android.google.cn/training/testing/instrumented-tests/androidx-test-libraries/rules?hl=zh-cn](https://developer.android.google.cn/training/testing/instrumented-tests/androidx-test-libraries/rules?hl=zh-cn)

假设我们有一个 **MainActivity.kt** :

```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
```

**activity_main.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:gravity="center"
    tools:context=".MainActivity">

    <EditText
        android:id="@+id/tv_hello"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Hello World!" />

</LinearLayout>
```

现在需要测试你这个界面是否正常显示，而且界面上的输入框是否正常工作：

你需要在 ```app\src\androidTest\java\com\xiaolei\testjunit``` 里新建一个 **ActivityTest.kt** :

```kotlin
package com.xiaolei.testjunit

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

/**
 * @Description: -
 * @Author: XIAOLEI
 * @Date: 2023/10/8
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class ActivityTest {
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun onBefore() {
        println("Before:${Date().time}")
    }

    @After
    fun onAfter() {
        println("onAfter:${Date().time}")
    }

    @Test
    fun testMainActivity() {
        println("testMainActivity")
        // 校验控件是否显示
        Espresso.onView(ViewMatchers.withId(R.id.tv_hello))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        // 校验文本框输入内容后，文本是否符合预期
        Espresso.onView(ViewMatchers.withId(R.id.tv_hello))
            .perform(
                ViewActions.click(),
                ViewActions.replaceText("张三"),
                ViewActions.closeSoftKeyboard()
            )
            .check(ViewAssertions.matches(ViewMatchers.withText("张三")))
        // 在当前界面根据文本寻找控件，并且判断控件是否显示
        Espresso.onView(ViewMatchers.withText("张三"))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }
}
```

这里主要是校验了一下，Activity上面的控件是否正常显示，以及文本框内容修改后的内容是否符合预期，以及根据文本来查找控件且判断是否显示。

#### 6.4 Intent拦截测试

这个主要是测试在行为与行为之间，传输数据的时候，验证行为是否符合预期

> 参考资料：[https://developer.android.google.cn/training/testing/espresso/intents?hl=zh-cn](https://developer.android.google.cn/training/testing/espresso/intents?hl=zh-cn)

新建两个Activity，**PhoneActivity.kt** , **ContactActivity.kt**

开发预期是从PhoneActivity里点击按钮，进入ContactActivity，然后ContactActivity设置Result并且finish，最后在PhoneActivity显示回来的数据。

现在我们来写测试用例来测试这种场景：

**IntentTest.kt**

```kotlin
package com.xiaolei.testjunit

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.ComponentNameMatchers.hasShortClassName
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

/**
 * @Description: -
 * @Author: XIAOLEI
 * @Date: 2023/10/8
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class IntentTest {
    @get:Rule
    val intentRule = IntentsTestRule(PhoneActivity::class.java)

    @Before
    fun onBefore() {
        println("Before:${Date().time}")
    }

    @After
    fun onAfter() {
        println("onAfter:${Date().time}")
    }

    @Test
    fun validateIntentSentToPackage() {
        // Build the result to return when the activity is launched.
        val resultData = Intent()
        val phoneNumber = "123-345-6789"
        resultData.putExtra("phone_number", phoneNumber)
        val result = Instrumentation.ActivityResult(Activity.RESULT_OK, resultData)
        // Set up result stubbing when an intent sent to "contacts" is seen.
        // 定义规则，当界面上出现 类名为ContactActivity的时候，直接返回上面定义的result
        intending(hasComponent(hasShortClassName(".ContactActivity"))).respondWith(result)
        // User action that results in "contacts" activity being launched.
        // Launching activity expects phoneNumber to be returned and displayed.
        // 模拟点击界面跳转按钮
        onView(withId(R.id.bt_open_contact)).perform(click())
        // Assert that the data we set up above is shown.
        // 验证携带回来的结果是否符合预期
        onView(withId(R.id.tv_result)).check(matches(withText(phoneNumber)))
    }
}
```



### 7. 结尾


> 单元测试官方文档：[https://developer.android.google.cn/training/testing?hl=zh-cn](https://developer.android.google.cn/training/testing?hl=zh-cn)
>
> Genymotion [https://www.genymotion.com/download/](https://www.genymotion.com/download/)
>
> Genymotion ARM 转 X86 的二进制转译插件：[https://github.com/m9rco/Genymotion_ARM_Translation](https://github.com/m9rco/Genymotion_ARM_Translation)
> 
> Espresso 的详细文档教程：[https://developer.android.google.cn/training/testing/espresso?hl=zh-cn](https://developer.android.google.cn/training/testing/espresso?hl=zh-cn)
>
> Espresso类图：[https://android.github.io/android-test/downloads/espresso-cheat-sheet-2.1.0.pdf](https://android.github.io/android-test/downloads/espresso-cheat-sheet-2.1.0.pdf)
>
> 

