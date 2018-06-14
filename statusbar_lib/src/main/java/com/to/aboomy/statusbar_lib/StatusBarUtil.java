package com.to.aboomy.statusbar_lib;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static android.os.Build.MANUFACTURER;

public class StatusBarUtil {

    private static final String MANUFACTURER_XIAOMI = "Xiaomi";
    private static final String MANUFACTURER_MEIZU  = "Meizu";

    /**
     * 修改状态栏为全透明，支持5.0以上版本
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void transparencyBar(Activity activity, @ColorInt int color) {
        Window window = activity.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);
        setImmersiveStatusBarBackgroundColor(activity, isNeedDark(color));
    }

    /**
     * 设置沉浸式装填栏颜色
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static boolean setImmersiveStatusBarBackgroundColor(Activity activity, @ColorInt int color) {
        boolean isSuc = setImmersiveStatusBarBackgroundColor(activity, isNeedDark(color));
        if (isSuc) {
            setStatusBarColor(activity, color);
        }
        return isSuc;
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
     * 修改状态栏颜色，支持5.0以上版本
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void setStatusBarColor(Activity activity, @ColorInt int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            //取消设置透明状态栏,使 ContentView 内容不再覆盖状态栏
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(color);
        }
    }

    /**
     * 设置沉浸式状态栏，因为需要暗色突变，所以需要进行判断是否能设置
     *
     * @param activity 宿主activity
     * @param isDark   是否暗色图标
     * @return 能否设置
     */
    public static boolean setImmersiveStatusBarBackgroundColor(Activity activity, boolean isDark) {
        if (activity == null) {
            return false;
        }
        boolean isSuc = false;
        if (Build.VERSION.SDK_INT >= 23) {
            isSuc = setStatusBarModeFor6_0(activity.getWindow(), isDark);
            if (MANUFACTURER_XIAOMI.equals(MANUFACTURER)) {
                if (setStatusBarFontForMiui(activity.getWindow(), isDark)) {
                    isSuc = true;
                }
            } else if (MANUFACTURER_MEIZU.equals(MANUFACTURER)) {
                if (setStatusBarFontForFlyme(activity.getWindow(), isDark)) {
                    isSuc = true;
                }
            }
        } else if (MANUFACTURER_XIAOMI.equals(MANUFACTURER)) {
            if (setStatusBarFontForMiui(activity.getWindow(), isDark)) {
                isSuc = true;
            }
        } else if (MANUFACTURER_MEIZU.equals(MANUFACTURER)) {
            if (setStatusBarFontForFlyme(activity.getWindow(), isDark)) {
                isSuc = true;
            }
        }
        return isSuc;
    }

    private static boolean setStatusBarFontForMiui(Window window, boolean dark) {
        if (window == null) {
            return false;
        }
        Class clazz = window.getClass();
        try {
            Class layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
            int darkModeFlag = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE").getInt(layoutParams);
            Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
            if (dark) {
                extraFlagField.invoke(window, darkModeFlag, darkModeFlag);
            } else {
                extraFlagField.invoke(window, 0, darkModeFlag);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean setStatusBarFontForFlyme(Window window, boolean dark) {
        if (window == null) {
            return false;
        }
        try {
            WindowManager.LayoutParams lp = window.getAttributes();
            Field darkFlag = WindowManager.LayoutParams.class.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
            Field meizuFlags = WindowManager.LayoutParams.class.getDeclaredField("meizuFlags");
            darkFlag.setAccessible(true);
            meizuFlags.setAccessible(true);
            int bit = darkFlag.getInt(null);
            int value = meizuFlags.getInt(lp);
            if (dark) {
                value |= bit;
            } else {
                value &= ~bit;
            }
            meizuFlags.setInt(lp, value);
            window.setAttributes(lp);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean setStatusBarFontForZuk(Window window, boolean dark) {
        if (window == null) {
            return false;
        }
        Class clazz = window.getClass();
        try {
            int color;
            Method setIcon = clazz.getMethod("setStatusBarIconColor", int.class);
            if (dark) {
                color = Color.BLACK;
            } else {
                color = Color.WHITE;
            }
            setIcon.invoke(window, color);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private static boolean setStatusBarModeFor6_0(Window window, boolean dark) {
        if (window == null) {
            return false;
        }
        try {
            View decorView = window.getDecorView();
            int ui = decorView.getSystemUiVisibility();
            if (dark) {
                ui |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            } else {
                ui &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
            decorView.setSystemUiVisibility(ui);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
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
}

