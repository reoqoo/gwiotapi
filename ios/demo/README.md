[API Referfence](https://reoqoo.github.io/gwiotapi/api/-g-w-io-t-api/com.gw.gwiotapi/-g-w-io-t/index.html)

## Installation
#### CocoaPods
```ruby
source 'https://github.com/CocoaPods/Specs.git'
platform :ios, '15.0'
use_frameworks!

target 'MyApp' do
  # pod 'GWIotKit', :subspecs => ['Core', 'RQIAPKit'] # with In App Purchase function
  # pod 'GWIotKit' # without In App Purchase function
end
```

## Launch the project quickly
1. After registered from our dev webside, you would get the `AppID` `AppToken` `AppName`.
2. Edit the `AppInfo.swift` file in `GWIoTAPIDemo`, replace `AppID` `AppToken` `AppName`.

## Compatibility
#### It should be noted that the sdk requires a specific version of xcode to be compiled normally. The following is the compatibility table

| SDK Version | Xcode Version |
| --- | --- |
| 1.0.1 | 16.2 |
| 1.0.2 | 16.2 |