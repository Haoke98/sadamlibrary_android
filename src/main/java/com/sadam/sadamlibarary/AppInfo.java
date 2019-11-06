package com.sadam.sadamlibarary;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.annotation.RequiresApi;

/**
 * 在这个class里放一些通用方法，先当于一个工具库，在以后的app开发中能循环利用
 */
public class AppInfo {
    private PackageManager packageManager;
    private PackageInfo packageInfo;

    /**
     * @param context 相当于一个 Activity.this
     */
    public AppInfo(Context context) {
        packageManager = context.getPackageManager();
        try {
            packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }


    /**
     * @return 用代码获取app自身的版本信息 VersionCode  是程序员区别版本的唯一标志
     */
    @RequiresApi(api = Build.VERSION_CODES.P)
    public long getVersionCode() {
        return packageInfo.getLongVersionCode();
    }


    /**
     * @return App的Version Name  类似于 1.3.2   是给用户看的
     */
    public String getVersionName() {
        return packageInfo.versionName;
    }


}
