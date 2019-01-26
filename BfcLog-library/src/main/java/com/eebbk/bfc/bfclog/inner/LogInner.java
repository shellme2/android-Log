package com.eebbk.bfc.bfclog.inner;

import android.util.Log;

import com.eebbk.bfc.bfclog.SDKVersion;

/**
 * BfcCommon内部使用的log
 *
 * Created by Simon on 2016/10/6.
 */

public class LogInner {

    public static void w(String tag, Throwable e, String msg){
        Log.w(tag, msg, e);
    }

    public static void e(String tag, Throwable e, String msg){
        Log.e(tag, msg, e);
    }

    public static void e(String tag,  String msg){
        Log.e(tag, msg);
    }

    /***
     * 打印版本信息  方便追踪app使用的版本
     */
    public static void printVersionInfo(){
        Log.i("日志库版本信息"," " + SDKVersion.getLibraryName() + " init, version: " +
                SDKVersion.getVersionName() + "  code: " + SDKVersion.getSDKInt() +
                " build: " + SDKVersion.getBuildName());
    }
}
