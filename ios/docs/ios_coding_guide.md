# iOS编码指南

## 开发语言
> 使用**Swift**

[KMP](https://kotlinlang.org/docs/multiplatform-intro.html)项目导出OC的xcframework给iOS平台使用， 但是Kotlin转换到OC过程中，有些常用的语言功能特性会丢失，如Kotlin Enum会转成成OC Class，使用起来比较麻烦。

为了解决这些问题，我们在KMP项目引入[SKIE](https://skie.touchlab.co/intro)插件，它通过修改Kotlin编译器生成的xcframework来恢复对其中一些语言功能的支持，使KMP生成的Swift接口更加友好，但是仅支持Swift。

我们也通过SKIE内嵌了一些Swift扩展/方法，使KMP导出的Swift接口更加易用。

如果您是旧的OC App集成此SDK，建议通过Swift来桥接调用GWIoT接口。如果是全新开发App，那么直接使用Swift。

> KMP导出Swift产物仍在早期开发阶段，当它稳定后我们会考虑直接导出Swift框架，有兴趣可以[了解](https://kotlinlang.org/docs/whatsnew21.html#basic-support-for-swift-export)

## SDK文档以及代码补全
出现以下情况时，建议直接通过[API文档](https://reoqoo.github.io/gwiotapi/api/-g-w-io-t-api/com.gw.gwiotapi/-g-w-io-t/index.html)查找接口
1. Kotlin使用kdoc注释后，生成OC头文件会有对应的注释，但是可能存在bug，部分注释会丢失或者错乱。
2. Xcode在没有索引完全项目代码时，可能编码过程中无法提示代码补全，特别是同时打开多个项目时。

## 接口使用说明
### 
