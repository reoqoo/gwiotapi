# iOS编码指南

## 开发语言

使用**Swift**

[KMP](https://kotlinlang.org/docs/multiplatform-intro.html)项目导出OC的xcframework给iOS平台使用，
但是Kotlin转换到OC过程中，有些常用的语言功能特性会丢失，如Kotlin Enum会转成成OC Class，使用起来比较麻烦。

为了解决这些问题，我们在KMP项目引入[SKIE](https://skie.touchlab.co/intro)
插件，它通过修改Kotlin编译器生成的xcframework来恢复对其中一些语言功能的支持，使KMP生成的Swift接口更加友好，但是仅支持Swift。

我们也通过SKIE内嵌了一些Swift扩展/方法，使KMP导出的Swift接口更加易用。

如果您是旧的OC App集成此SDK，建议通过Swift来桥接调用GWIoT接口。如果是全新开发App，那么直接使用Swift。

> KMP导出Swift产物仍在早期开发阶段，当它稳定后我们会考虑直接导出Swift框架，有兴趣可以[了解](https://kotlinlang.org/docs/whatsnew21.html#basic-support-for-swift-export)

## SDK文档以及代码补全

出现以下情况时，建议直接通过[API文档](https://reoqoo.github.io/gwiotapi/api/-g-w-io-t-api/com.gw.gwiotapi/-g-w-io-t/index.html)
查找接口

1. Kotlin使用kdoc注释后，生成OC头文件会有对应的注释，但是可能存在bug，部分注释会丢失或者错乱。
2. Xcode在没有索引完全项目代码时，可能编码过程中无法提示代码补全，特别是同时打开多个项目时。

## 接口使用说明

### GWIoT

在接口文档里面，我们看到有如下`GWIoT`定义，这是应用层接口集合，App开发所需的所有接口均通过`GWIoT`
来调用。

```kotlin
// kotlin
object GWIoT : IBindComponent, IInitializeComponent, IPluginConfigComponent,
    IListenerWatchComponent, IPluginComponent, IPropComponent, IAlbumComponent, IUserInfoComponent,
    IHttpServiceComponent, IDevMangerComponent, IDevShareComponent, IAccountRegisterComponent,
    IAccountManagerComponent
```

###  

- `object`类型，简单理解为kotlin默认实现单例的类，iOS直接通过`GWIoT.shared`
   使用其单例。其他object同理，但是App层目前仅需要用到GWIoT。

- `IBindComponent`, `IInitializeComponent`...，是GWIoT实现的接口(Interface)，即iOS的Protocol。

- 对于GWIoT实现的每个协议，通常有一个对应实现这个协议的属性，命名为`xxxComp`，如`IBindComponent` ->
   `bindComp`，`IInitializeComponent` -> `initializeComp`，以此类推。 这是由于GWIoT实现的接口比较多，导致使用时自动提示的方法属性比较多，不方便开发者使用，增加这些Comp对象来对其接口进行分组。
   以下调用方式是等价的。

```swift
  GWIoT.shared.accountComp.login(account: account, password: password) { result, error in
    // handle result
  }
  
  GWIoT.shared.login(account: account, password: password) { result, error in
    // handle result
  }
  
  // or use Concurrency
  let result = try await GWIoT.shared.accountComp.login(account: account, password: password)
  
  let result = try await GWIoT.shared.login(account: account, password: password)
```

### 方法调用以及结果处理
前面一节调用`login()`方法的例子，在Kotlin里面定义如下：
```kotlin
// kotlin
suspend fun login(account: AccountType, password: String): GWResult<User>
```
`supsend`是Kotlin的协程关键字，类似于Swift使用`async`关键字来声明一个异步函数。导出的swift头文件如下，Kotlin的`suspend`关键字会被转换成OC的`completionHandler`回调，并且OC转换Swift头文件时生成了其对应的`async`方法。
```swift
// kotlin -> OC -> swift
func login(account: any AccountType, password: String, completionHandler: @escaping (GWResult<User>?, (any Error)?) -> Void)

// swift completionHandler -> async
func login(account: any AccountType, password: String) async throws -> GWResult<User>
```

这两个方法在Swift里面都可以正常使用，但是在处理结果时，由于在iOS平台无法捕获Kotlin的exception，我们会在内部处理catch异常，并且将其转换成`GWResult.Failure`返回，
所以`completionHandler`中的`error`通常都是空的，不需要处理，使用GWIoT的async throws方法时也不需要`try catch`。另外，我们提供了`gw_iot`前缀的方法来将GWResult转换成Swift的Result方便使用。
完整例子如下：
```swift
GWIoT.shared.login(account: AccountTypeEmail(email: "test@example.com"), password: "Testpwd123!") { result, error in
   switch(gwiot_handleCb(result, error)) {
   case .success(let user):
       print("user \(user) login.")
   case .failure(let err):
       print("login failed. \(err.message)")
   }
}

// or use Concurrency
let result = try await GWIoT.shared.login(account: AccountTypeEmail(email: "test@example.com"), password: "Testpwd123!")
switch(gwiot_swiftResult(of: result)) {
case .success(let user):
    print("user \(user) login.")
case .failure(let err):
    print("login failed. \(err.message)")
}
```

### 值枚举
kotlin中没有值枚举，类似的有`sealed class`，但是导出到OC时，每个值会被转换成OC的Class，SKIE有`onEnum(of:)`方法将其转成枚举的形式方便使用。
以上提到的GWResult就是其中一个例子，但是对于GWResult的处理建议直接用上述提到的`gwiot_swiftResult(:)`。
```swift
// kotlin
sealed class GWResult<T> {
    class Success<T>(val data: T?) : GWResult<T>() 
    class Failure<T>(val err: GWError?) : GWResult<T>()
}

// swift usage
let result = try await GWIoT.shared.login()...
switch(onEnum(of: result)) {
    case .success(let s): // s is GWResult.Success<User>
    case .failure(let f): // f is GWResult.Failure
}

// 由于onEnum(of:)方法只是解析出类型，处理result一般只关注成功的data或者failure的error，所以我们额外增加gwiot_swiftResult方法直接转成Swift的Result。
switch(gwiot_swiftResult(of: result)) {
    case.success(let user): 
    case.failure(let error): 
}
```

### 其他
这里仅列出部分常用的iOS编码情况，有必要时我们会持续补充，其他请参考demo，有任何问题或者意见欢迎随时联系我们。