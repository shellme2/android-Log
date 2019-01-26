package com.eebbk.bfc.bfclog;

import android.support.annotation.IntDef;
import android.util.Log;

import com.eebbk.bfc.bfclog.inner.BfcLogImpl;
import com.eebbk.bfc.bfclog.inner.Settings;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Simon on 2016/9/5.
 */
public interface BfcLog {
    int VERBOSE = Log.VERBOSE;
    int DEBUG = Log.DEBUG;
    int INFO = Log.INFO;
    int WARN = Log.WARN;
    int ERROR = Log.ERROR;
    int ASSERT = Log.ASSERT;

    @IntDef({VERBOSE, DEBUG, INFO, WARN, ERROR, ASSERT})
    @Retention(RetentionPolicy.CLASS)
    @interface Level {
    }

    /**
     * 修改下一条打印的log, 是否显示线程信息
     *
     * @param showThread true 显示线程信息; false 不显示
     */
    BfcLog thread(boolean showThread);

    /**
     * 修改下一条打印的log, 显示的堆栈层数
     */
    BfcLog method(int methodCount);

    /**
     * 修改下一条打印的log, 显示的tag
     */
    BfcLog tag(String tag);


    /**
     * 使用默认的tag 打印消息,
     *
     * @param message 消息内容
     */
    void v(String message);

    void d(String message);

    void i(String message);

    void w(String message);

    void w(Throwable e);

    void w(Throwable e, String message);

    void e(String message);

    void e(Throwable e);

    void e(Throwable e, String message);

    void wtf(String message);

    void wtf(Throwable e);

    void wtf(Throwable e, String message);

    void json(String json);


    class Builder {
        final Settings settings;

        public Builder() {
            settings = new Settings();
        }

        /**
         * @param tag 基本的tag, 默认为 bfcLog; android系统中要求 tag.length()<23, 因此,此tag长度和log打印中tag的长度, 拼接起来长度不能超过 23
         */
        public Builder tag(String tag) {
            settings.tag(tag);
            return this;
        }

        /**
         * Log打印的开关 默认true 打开
         *
         * @param shouldLog true打开  false关闭
         */
        public Builder showLog(boolean shouldLog) {
            settings.showLog(shouldLog);
            return this;
        }


        /***
         * 打印方法调用的层数  默认是2, 如果设置为0, 则不打印方法调用关系
         *
         * @param methodCount 方法调用层数
         */
        public Builder methodCount(int methodCount) {
            settings.methodCount(methodCount);
            return this;
        }

        /***
         * 打印方法调用时的偏移量, 若直接使用的是 {@link BfcLogger}则不需要设置, 若程序中,自己封装了 并且不想显示封装一层的调用关系, 一般需要设置为1
         *
         * @param methodOffset 方法调用层级的偏移量, 默认0
         */
        public Builder methodOffset(int methodOffset) {
            settings.methodOffset(methodOffset);
            return this;
        }

        /***
         * 是否要打印线程信息, 默认false
         *
         * @param shouldShowThreadInfo true显示 false不显示
         */
        public Builder showThreadInfo(boolean shouldShowThreadInfo) {
            settings.showThreadInfo(shouldShowThreadInfo);
            return this;
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
        public Builder logLevel(@Level int logLevel) {
            settings.logLevel(logLevel);
            return this;
        }


        /**
         * 是否保存log到文件中, 默认不保存;
         * 在正式外发版本中, 请务必关闭此功能
         *
         * @param shouldSave      是否保存log到文件; true保存, false不保存
         * @param logSaveLocation log保存的位置
         */
        public Builder saveLog(boolean shouldSave, String logSaveLocation) {
            settings.saveLog(shouldSave, logSaveLocation);
            return this;
        }

        public BfcLog build() {
            return new BfcLogImpl(settings);
        }
    }
}
