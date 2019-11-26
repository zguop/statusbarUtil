package com.to.aboomy.statusbar_lib;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class StatusBarUtil {

    /**
     * 设置状态栏纯色
     *
     * @param activity 需要设置的activity
     * @param color    颜色
     */
    public static boolean setStatusBarColor(Activity activity, @ColorInt int color) {
        return setStatusBarColor(activity, color, 0);
    }

    /**
     * 修改状态栏颜色，支持5.0以上版本
     */
    public static boolean setStatusBarColor(Activity activity, @ColorInt int color, @IntRange(from = 0, to = 255) int statusBarAlpha) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            //取消设置透明状态栏,使 ContentView 内容不再覆盖状态栏
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            int calculateStatusColor = calculateStatusColor(color, statusBarAlpha);
            window.setStatusBarColor(calculateStatusColor);
            return immersiveStatusBarNeedDark(activity, calculateStatusColor);
        }
        return false;
    }

    /**
     * 修改状态栏为全透明，支持5.0以上版本 沉浸状态栏
     *
     * @param isNeedDark true 状态栏文字是亮色，false暗色，statusBar不再那么文字暗色还是亮色你自己决定
     */
    public static boolean transparencyBar(Activity activity, boolean isNeedDark) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            return immersiveStatusBarNeedDark(activity, isNeedDark);
        }
        return false;
    }

    /**
     * 为DrawerLayout 布局设置状态栏颜色,纯色
     *
     * @param activity     需要设置的activity
     * @param drawerLayout DrawerLayout
     * @param color        状态栏颜色值
     */
    public static boolean drawerLayoutForColor(Activity activity, @ColorInt int color, DrawerLayout drawerLayout) {
        return drawerLayoutForColor(activity, color, drawerLayout, 0, true);
    }

    /**
     * 为DrawerLayout 布局设置状态栏颜色,纯色
     *
     * @param activity                需要设置的activity
     * @param drawerLayout            DrawerLayout
     * @param color                   状态栏颜色值
     * @param hideStatusBarBackground true 沉浸状态栏 false 不会覆盖到statusView
     */
    public static boolean drawerLayoutForColor(Activity activity, @ColorInt int color, DrawerLayout drawerLayout, int statusBarAlpha, boolean hideStatusBarBackground) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return false;
        }
        final ViewGroup contentLayout = (ViewGroup) drawerLayout.getChildAt(0);
        View fakeStatusBarView = contentLayout.findViewById(R.id.view_status_bar);
        if (fakeStatusBarView != null) {
            if (fakeStatusBarView.getVisibility() == View.GONE) {
                fakeStatusBarView.setVisibility(View.VISIBLE);
            }
            fakeStatusBarView.setBackgroundColor(calculateStatusColor(color, statusBarAlpha));
        } else {
            StatusBarView statusBarView = new StatusBarView(activity);
            statusBarView.setId(R.id.view_status_bar);
            statusBarView.setBackgroundColor(calculateStatusColor(color, statusBarAlpha));
            contentLayout.addView(statusBarView, 0);
        }
        // 内容布局不是 LinearLayout 时,设置padding top
        if (!hideStatusBarBackground && !(contentLayout instanceof LinearLayout) && contentLayout.getChildAt(1) != null) {
            int childCount = contentLayout.getChildCount();
            for (int i = 1; i < childCount; i++) {
                final View childAt = contentLayout.getChildAt(i);
                childAt.setPadding(contentLayout.getPaddingLeft(), getStatusBarHeight(activity) + contentLayout.getPaddingTop(),
                        contentLayout.getPaddingRight(), contentLayout.getPaddingBottom());
            }
        }
        setDrawerLayoutProperty(drawerLayout, contentLayout);
        return transparencyBar(activity, isNeedDark(color));
    }

    /**
     * 设置沉浸式装填栏颜色
     */
    public static boolean immersiveStatusBarNeedDark(Activity activity) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
            return false;
        }
        int statusBarColor = activity.getWindow().getStatusBarColor();
        return immersiveStatusBarNeedDark(activity, isNeedDark(statusBarColor));
    }

    /**
     * 设置沉浸式装填栏颜色
     */
    public static boolean immersiveStatusBarNeedDark(Activity activity, @ColorInt int color) {
        return immersiveStatusBarNeedDark(activity, isNeedDark(color));
    }

    /**
     * 设置沉浸式状态栏，因为需要暗色突变，所以需要进行判断是否能设置
     *
     * @param activity 宿主activity
     * @param isDark   是否暗色图标
     * @return 能否设置
     */
    public static boolean immersiveStatusBarNeedDark(Activity activity, boolean isDark) {
        if (activity == null) {
            return false;
        }
        boolean isSuc = false;
        setStatusBarFontForMiui(activity, isDark);
        setStatusBarFontForFlyme(activity, isDark);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            isSuc = setStatusBarModeFor6_0(activity, isDark);
        }
        return isSuc;
    }

    /**
     * 修改 MIUI V6  以上状态栏颜色
     */
    private static void setStatusBarFontForMiui(@NonNull Activity activity, boolean darkIcon) {
        Class<? extends Window> clazz = activity.getWindow().getClass();
        try {
            Class<?> layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
            Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
            int darkModeFlag = field.getInt(layoutParams);
            Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
            extraFlagField.invoke(activity.getWindow(), darkIcon ? darkModeFlag : 0, darkModeFlag);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 修改魅族状态栏字体颜色 Flyme 4.0
     */
    private static void setStatusBarFontForFlyme(@NonNull Activity activity, boolean darkIcon) {
        try {
            WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
            Field darkFlag = WindowManager.LayoutParams.class.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
            Field meizuFlags = WindowManager.LayoutParams.class.getDeclaredField("meizuFlags");
            darkFlag.setAccessible(true);
            meizuFlags.setAccessible(true);
            int bit = darkFlag.getInt(null);
            int value = meizuFlags.getInt(lp);
            if (darkIcon) {
                value |= bit;
            } else {
                value &= ~bit;
            }
            meizuFlags.setInt(lp, value);
            activity.getWindow().setAttributes(lp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private static boolean setStatusBarModeFor6_0(Activity activity, boolean dark) {
        try {
            View decorView = activity.getWindow().getDecorView();
            int ui = decorView.getSystemUiVisibility();
            if (dark) {
                ui |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            } else {
                ui &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
            decorView.setSystemUiVisibility(ui);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取状态栏高度
     */
    public static int getStatusBarHeight(Context context) {
        int result;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        } else {
            result = (int) Math.ceil(25 * context.getResources().getDisplayMetrics().density);
        }
        return result;
    }

    /**
     * 根据状态栏颜色解析是否需要暗色状态栏
     *
     * @param color 状态栏颜色
     * @return 是否暗色状态栏
     */
    private static boolean isNeedDark(@ColorInt int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        return Math.sqrt(Math.pow((double) (1.0f - hsv[2]), 2.0d) + Math.pow((double) hsv[1], 2.0d)) < 0.5d;
    }

    /**
     * 计算状态栏颜色
     *
     * @param color color值
     * @param alpha alpha值
     * @return 最终的状态栏颜色
     */
    private static int calculateStatusColor(@ColorInt int color, int alpha) {
        if (alpha == 0) {
            return color;
        }
        float a = 1 - alpha / 255f;
        int red = color >> 16 & 0xff;
        int green = color >> 8 & 0xff;
        int blue = color & 0xff;
        red = (int) (red * a + 0.5);
        green = (int) (green * a + 0.5);
        blue = (int) (blue * a + 0.5);
        return 0xff << 24 | red << 16 | green << 8 | blue;
    }

    /**
     * 设置 DrawerLayout 属性
     *
     * @param drawerLayout              DrawerLayout
     * @param drawerLayoutContentLayout DrawerLayout 的内容布局
     */
    private static void setDrawerLayoutProperty(DrawerLayout drawerLayout, ViewGroup drawerLayoutContentLayout) {
        ViewGroup drawer = (ViewGroup) drawerLayout.getChildAt(1);
        drawerLayout.setFitsSystemWindows(false);
        drawerLayoutContentLayout.setFitsSystemWindows(false);
        drawerLayoutContentLayout.setClipToPadding(true);
        drawer.setFitsSystemWindows(false);
    }
}

