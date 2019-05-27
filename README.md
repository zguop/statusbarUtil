# statusbarUtil
状态栏工具类，只支持android 5.0以上版本，不兼容5.0以下

Gradle 
```groovy
dependencies{
    implementation 'com.to.aboomy:statusbar_lib:1.1.8'  //最新版本
}
```

## 使用
```groovy

   private boolean isImmersiveStatusBar;

  @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ChangeModeController.get().setTheme(this);
        super.onCreate(savedInstanceState);
        
        //返回值为是否成功设置了状态栏 文字颜色 6.0 以上可以根据状态栏颜色调整文字颜色

        //设置状态栏颜色
        isImmersiveStatusBar  = StatusBarUtil.setStatusBarColor();
        //透明状态栏
        isImmersiveStatusBar = StatusBarUtil.transparencyBar()
        //有drawerLayout为状态栏设置颜色
        isImmersiveStatusBar =  StatusBarUtil.drawerLayoutForColor()
    
    }
    

```

具体使用可以我的demo项目 BaseActivity：
https://github.com/zguop/Towards

-
xiexie ni de guāng gù ！ 喜欢的朋友轻轻右上角赏个star.








