package com.eebbk.bfc.bfclog;


/**
 * Created by Simon on 2016/11/5.
 */

public class SDKVersion {

    /**
     * 获取库名称
     * @return 库名称
     */
    public static String getLibraryName(){
        return BuildConfig.LIBRARY_NAME;
    }

    /**
     * 构建时的版本值，如：1, 2, 3, ...
     */
    public static int getSDKInt() {
        return BuildConfig.VERSION_CODE;
    }

    /**
     * 版本名称，如：1.0.0, 2.1.2-alpha, ...
     */
    public static String getVersionName() {
        return BuildConfig.VERSION_NAME;
    }

    /**
     * 构建版本以及时间，主要从git获取,由GIT_TAG + "_" + GIT_SHA + "_" + BUILD_TIME组成
     */
    public static String getBuildName() {
        return getBuildTag() +
                "_" +
                getBuildHead() +
                "_" +
                getBuildTime();
    }

    /**
     * 构建时间
     */
    public static String getBuildTime() {
        return BuildConfig.BUILD_TIME;
    }

    /**
     * 构建时的git 标签
     */
    public static String getBuildTag() {
        return BuildConfig.GIT_TAG;
    }

    /**
     * 构建时的git HEAD值
     */
    public static String getBuildHead() {
        return BuildConfig.GIT_HEAD;
    }

}
