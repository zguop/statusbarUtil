# statusbarUtil

Gradle 
```groovy
dependencies{
    implementation 'com.to.aboomy:statusbar_lib:1.1.4'  //最新版本
    implementation 'com.to.aboomy:statusbar_lib:1.1.5' //androidx 版本
}
```

## 使用
```groovy

   private boolean isImmersiveStatusBar;

  @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ChangeModeController.get().setTheme(this);
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            isImmersiveStatusBar = StatusBarUtil.setImmersiveStatusBarBackgroundColor(this, ThemeUtils.getThemeAttrColor(this, R.attr.colorPrimary));
        }
    }
    
    /**
     * 设置状态栏透明，需要在onCreate之后调用
     */
    public void transparencyBar() {
        if (isImmersiveStatusBar && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            StatusBarUtil.transparencyBar(this, ThemeUtils.getThemeAttrColor(this, R.attr.colorPrimary));
        }
    }
```

具体使用可以我的demo项目 BaseActivity：
https://github.com/zguop/Towards

-
xiexie ni de guāng gù ！ 喜欢的朋友轻轻右上角赏个star.








