//
//  RTContainerNavigationController+CommonStyleConfiguration.m
//  RQSDKDemo
//
//  Created by xiaojuntao on 3/8/2023.
//

#import "RTContainerNavigationController+CommonStyleConfiguration.h"
#import <objc/runtime.h>
#import <BaseKit/BaseKit-Swift.h>

/*
 RTRootNavigationController 层级结构
 - RTRootNavigationController
    - RTContainerController
        - RTContainerNavigationController
            - ViewController (被 Push 的 ViewController, 其 navigationItem 配置会呈现在 RTContainerNavigationController 上)
 */

/// 由于采用了 RTRootNavigationController
/// RTContainerNavigationController 是被展示的 NavigationBar 的提供者.
/// 所以需要对 RTContainerNavigationController 的 viewDidLoad 进行方法交换, 以便对 NavigationBar 做全局统一的样式配置
@implementation RTContainerNavigationController (CommonStyleConfiguration)

+ (void)load {
    [self exchangeInstanceImplementation:@selector(viewDidLoad) swizzled:@selector(sw_viewDidLoad)];
}

// MARK: Helper
+ (void)exchangeInstanceImplementation:(SEL)origin swizzled:(SEL)swizzled {
    Method originMethod = class_getInstanceMethod(self, origin);
    Method swizzledMethod = class_getInstanceMethod(self, swizzled);
    method_exchangeImplementations(originMethod, swizzledMethod);
}

// MARK: Swizzled Implementaciton
- (void)sw_viewDidLoad {
    // 先调 super 的方法
    [super viewDidLoad];

    // 背景颜色
    self.navigationBar.barTintColor = [UIColor colorNamed:@"background_F2F3F6_thinGray"];
    // title / navigationItem 颜色
    self.navigationBar.tintColor = [UIColor blackColor];

    // 背景颜色和细节
    UINavigationBarAppearance *appearance = [[UINavigationBarAppearance alloc] init];
    // 去掉navigationBar底部黑色线
    appearance.shadowImage = [UIImage new];
    appearance.shadowColor = [UIColor clearColor];
    appearance.backgroundColor = [UIColor colorNamed:@"background_F2F3F6_thinGray"];
    self.navigationBar.scrollEdgeAppearance = appearance;
    self.navigationBar.standardAppearance = appearance;

    // 最后再执行原生 viewDidLoad 方法
    [self sw_viewDidLoad];
}

@end
