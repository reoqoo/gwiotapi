[API Referfence](https://reoqoo.github.io/gwiotapi/api/-g-w-io-t-api/com.gw.gwiotapi/-g-w-io-t/index.html)

## Introduction
This is a demo of the `GWIoTKit` usage.  
`GWIoTKit` is combined from lots of frameworks, our original intention was just let users focus on the `GWIoTApi` Framework, but it is obvious that this expectation has not been achieved yet. We will continue to update in the future.

## Installation
#### CocoaPods
```ruby
source 'https://github.com/CocoaPods/Specs.git'
platform :ios, '15.0'
use_frameworks!

target 'MyApp' do
  # pod 'GWIoTKit', :subspecs => ['Core', 'RQIAPKit'] # with In App Purchase function
  # pod 'GWIoTKit' # without In App Purchase function
end
```

## Launch the demo
1. After registered from our dev webside, you would get the `AppID` `AppToken` `AppName`.
2. Edit the `AppInfo.swift` file in `GWIoTAPIDemo`, replace `AppID` `AppToken` `AppName`.

## Compatibility
#### It should be noted that the sdk requires a specific version of xcode to be compiled normally. The following is the compatibility table

| SDK Version | Xcode Version |
| --- | --- |
| 1.0.1 | 16.2 |
| 1.0.2 | 16.4 |
| 1.0.3 | 16.4 |
