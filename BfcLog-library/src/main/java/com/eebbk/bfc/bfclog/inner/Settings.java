package com.eebbk.bfc.bfclog.inner;

import android.text.TextUtils;

import com.eebbk.bfc.bfclog.BfcLog;
import com.eebbk.bfc.bfclog.BfcLogger;

/**
 * 每个{@link com.eebbk.bfc.bfclog.BfcLog} 的配置信息
 * 通过@{@link com.eebbk.bfc.bfclog.BfcLog.Builder}设置每个实例的配置信息
 * 对外部透明
 * <p>
 * Created by Simon on 2016/9/5.
 */
public class Settings {
    private static final String DEFAULT_TAG = "BfcLog";
    private boolean shouldShowLog = true;
    private int logLevel = BfcLog.VERBOSE;
    private String tag = DEFAULT_TAG;
    // 默认不显示线程信息
    private boolean showThreadInfo = false;
    private int methodCount = 2;
    private int methodOffset = 0;

    private boolean shouldSave = false;
    private String logSavePath;

    /***
     * 是否要打印线程信息, 默认false
     *
     * @param showThreadInfo true显示 false不显示
     */
    public Settings showThreadInfo(boolean showThreadInfo) {
        this.showThreadInfo = showThreadInfo;
        return this;
    }

    /**
     * Log打印的开关 默认true 打开
     *
     * @param showLog true打开  false关闭
     */
    public Settings showLog(boolean showLog) {
        this.shouldShowLog = showLog;
        return this;
    }

    /***
     * 打印方法调用的层数  默认是2, 如果设置为0, 则不打印方法调用关系
     *
     * @param methodCount 方法调用层数
     */
    public Settings methodCount(int methodCount) {
        this.methodCount = methodCount;
        return this;
    }

    /***
     * 打印方法调用时的偏移量, 若直接使用的是 {@link BfcLogger}则不需要设置, 若程序中,自己封装了 并且不想显示封装一层的调用关系, 一般需要设置为1
     *
     * @param methodOffset 方法调用层级的偏移量, 默认0
     */
    public Settings methodOffset(int methodOffset) {
        if (methodOffset < 0) {
            this.methodOffset = 0;
        } else {
            this.methodOffset = methodOffset;
        }
        return this;
    }

    /**
     * @param tag 基本的tag, 默认为 bfcLog; android系统中要求 tag.length()<23, 因此,此tag长度和log打印中tag的长度, 拼接起来长度不能超过 23
     */
    public Settings tag(String tag) {
        if (TextUtils.isEmpty(tag) || tag.trim().length() == 0) {
            return this;
        }
        this.tag = tag;
        return this;
    }


    boolean isShouldSave() {
        return shouldSave;
    }

    String getLogSavePath(){
        return logSavePath;
    }

    /**
     * 是否保存log到文件中, 默认不保存;
     * 在正式外发版本中, 请务必关闭此功能
     *
     * @param shouldSave      是否保存log到文件; true保存, false不保存
     * @param logSaveLocation log保存的位置
     */
    public Settings saveLog(boolean shouldSave, String logSaveLocation) {
        this.shouldSave = shouldSave;
        if (shouldSave) {
            this.logSavePath = logSaveLocation;
            try {
                Log2SDCardUtil.ensureSaveLocationLegal( logSaveLocation );
            } catch (Exception e) {
                this.shouldSave = false;
                LogInner.e("BfcLog.Settings", e, "初始化传入时,log保存路径,无法创建用于保存log的文件; 将自动关闭log保存功能");
            }
        }
        return this;
    }

    int getLogLevel() {
        return logLevel;
    }

    /**
     * 设置要打印的log的等级, 如果等级小于该等级时, 该log将不会打印, 默认为{@link BfcLog#VERBOSE}, 即显示全部log
     *
     * @param logLevel Assert {@link BfcLog#ASSERT}  只显示严重的log
     *                 Error {@link BfcLog#ERROR}  只显示Error以上的log
     *                 Warn {@link BfcLog#WARN}  只显示Warn以上的log
     *                 Info {@link BfcLog#INFO}  只显示Info以上的log
     *                 Debug {@link BfcLog#DEBUG}  只显示Debug以上的log
     *                 Verbose {@link BfcLog#VERBOSE}  只显示Verbose以上的log, 即显示全部log
     */
    public Settings logLevel(@BfcLog.Level int logLevel) {
        this.logLevel = logLevel;
        return this;
    }

    boolean isShouldShowLog() {
        return shouldShowLog;
    }

    boolean isShowThreadInfo() {
        return showThreadInfo;
    }

    int getMethodOffset() {
        return methodOffset;
    }

    int getMethodCount() {
        return methodCount;
    }

    String getTag() {
        return tag;
    }

}
