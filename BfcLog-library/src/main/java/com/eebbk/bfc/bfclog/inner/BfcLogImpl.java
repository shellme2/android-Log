package com.eebbk.bfc.bfclog.inner;

import android.text.TextUtils;
import android.util.Log;

import com.eebbk.bfc.bfclog.BfcLog;
import com.eebbk.bfc.bfclog.BfcLogger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 打印log的具体实现类
 * 对外部透明
 * Created by Simon on 2016/9/5.
 */
public class BfcLogImpl implements BfcLog {

    private final static SimpleDateFormat DATA_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    private static final String MIDDLE_CORNER = " ";
    private static final String SINGLE_DIVIDER = "────────────────────────────────────────────";
    private static final String MIDDLE_BORDER = MIDDLE_CORNER + SINGLE_DIVIDER + SINGLE_DIVIDER;

    /**
     * 最小的方法调用栈层级, 最小的好像应该是5, 保险起见 用4
     */
    private static final int MIN_STACK_OFFSET = 4;
    /**
     * 加入aop后(用于统计方法调用时间), 额外增加的方法调用栈的层数  这是个经验值, 可能会变
     */
    private static final int ASPECTJ_AOP_STACK_OFFSET = 4;

    private static final String EMPTY_MESSAGE = "Empty/NULL log message";

    /**
     * 系统底层 允许的log一次最大的字符数
     */
    private static final int CHUNK_SIZE = 4000;


    private final ThreadLocal<String> localTag = new ThreadLocal<>();
    private final ThreadLocal<Integer> localMethodCount = new ThreadLocal<>();
    private final ThreadLocal<Boolean> localShowThread = new ThreadLocal<>();

    private Settings mSettings = new Settings();

    public BfcLogImpl() {}

    public BfcLogImpl(Settings settings) {
        this.mSettings = settings;
    }

    public Settings getSettings() {
        return mSettings;
    }


    @Override
    public BfcLog tag(String tag) {
        localTag.set(tag);
        return this;
    }

    @Override
    public BfcLog thread(boolean showThread) {
        localShowThread.set(showThread);
        return this;
    }

    @Override
    public BfcLog method(int methodCount) {
        localMethodCount.set(methodCount);
        return this;
    }

    /**
     * 获取要打印的方法数, 如果使用 {@link #method(int)}覆盖过, 就使用覆盖过的值
     */
    private int getMethodCount(Settings settings) {
        Integer count = localMethodCount.get();
        int result = settings.getMethodCount();
        if (count != null) {
            localMethodCount.remove();
            result = count;
        }

        if (result < 0) {
            result = 0;
        }

        return result;
    }

    private boolean isShowThread(Settings settings) {
        Boolean localShow = localShowThread.get();
        if (localShow != null) {
            localShowThread.remove();
            return localShow;
        }

        return settings.isShowThreadInfo();
    }

    private String generateTag(Settings settings, String tag) {
        String overTag = localTag.get();
        if (overTag != null) {
            localTag.remove();
            tag = overTag;
        }

        if (TextUtils.isEmpty(tag) || tag.trim().length() == 0) {
            return settings.getTag();
        } else {
//            return settings.getTag() + "_" + tag;
            return tag;
        }
    }


    public void log(@Level int logType, Settings settings, String tag, Throwable e, String message) {
        final Settings finalSetting = settings != null ? settings : mSettings;

        if (!finalSetting.isShouldShowLog() || logType < finalSetting.getLogLevel()) {
            return;
        }

        final String finalTag = generateTag(finalSetting, tag);
        final String finalMessage = generateMessage(finalSetting, e, message);

        logChunk(logType, finalTag, finalMessage);

        if (finalSetting.isShouldSave()) {
            final String savePath = finalSetting.getLogSavePath();
            final String log = "Time: " + DATA_FORMAT.format(new Date()) + "\nTag: " + finalTag + "\n" + finalMessage + "\r\n";
            Log2SDCardUtil.getInstance().saveLog(  new Log2SDCardUtil.LogSaveInfo(log, savePath));
        }
    }

    private String generateMessage(Settings settings, Throwable e, String msg) {
        final StringBuilder msgBuilder = new StringBuilder();

        setupThreadInfo(settings, msgBuilder);
        setupStackInfo(settings, msgBuilder);
        setupMessage(e, msg, msgBuilder);

        final String ret = msgBuilder.toString();

        return TextUtils.isEmpty(ret) ? EMPTY_MESSAGE : ret;
    }

    private void setupThreadInfo(Settings settings, StringBuilder builder) {
        if (isShowThread(settings)) {
            builder.append(" Thread: ").append(Thread.currentThread().getName()).append("\n");
            builder.append(MIDDLE_BORDER).append("\n");
        }
    }

    private void setupStackInfo(Settings settings, StringBuilder builder) {
        final int methodCount = getMethodCount(settings);

        if (methodCount <= 0) {
            return;
        }

        final StackTraceElement[] trace = Thread.currentThread().getStackTrace();

        final int maxStackLength = trace.length;
        // 要打印堆栈的起始位置
        int stackStartIndex = -1;
        boolean hasAopInsertCode = false;

        for (int i = MIN_STACK_OFFSET; i < trace.length; i++) {
            StackTraceElement e = trace[i];
            String name = e.getClassName();
            if (BfcLogImpl.class.getName().equals(name)
                    || BfcLogger.class.getName().equals(name)
                    // 对aop插入代码的调用栈进行跳过
                    || "com.eebbk.bfc.bfclog.aop.weaving.BfcLogAspect".equals(name)) {

                continue;
            }

            stackStartIndex = --i;
            break;
        }

        // 起始位置 还要加上 用户想要堆栈的偏移量
        stackStartIndex = stackStartIndex + settings.getMethodOffset();

        if (stackStartIndex >= maxStackLength) {
            // 要显示的堆栈偏移量 比整个堆栈的长度还多, 就无法显示堆栈了, 直接返回
            return;
        }

        // 加入统计时间的注解后, aop会在前后加入代码, 方法内部log的调用栈会增加aop的栈, 根据字符串跳过, 可能会对性能造成影响
        // TODO: 2016/9/13 need find a better way to ignore aop stack
        if (trace[stackStartIndex + 1].getMethodName().contains("_aroundBody")) {
            hasAopInsertCode = true;
            stackStartIndex = stackStartIndex + ASPECTJ_AOP_STACK_OFFSET;
        }

        // 计算出最终要打印的堆栈的行数
        final int finalMethodCount;
        if (methodCount + stackStartIndex >= maxStackLength) {
            finalMethodCount = maxStackLength - stackStartIndex - 1;
        } else {
            finalMethodCount = methodCount;
        }


        String level = "";
        for (int i = finalMethodCount; i > 0; i--) {
            int stackIndex = i + stackStartIndex;

            // 对于aop插入代码的调用栈 最后一次确定最终的调用方法
            if (hasAopInsertCode && i == 1) {
                stackIndex = i + stackStartIndex - ASPECTJ_AOP_STACK_OFFSET;
            }

            builder.append(" ")
                    .append(level)
                    .append(getSimpleClassName(trace[stackIndex].getClassName()))
                    .append(".")
                    .append(trace[stackIndex].getMethodName())
                    .append(" ")
                    .append(" (")
                    .append(trace[stackIndex].getFileName())
                    .append(":")
                    .append(trace[stackIndex].getLineNumber())
                    .append(")");
            builder.append("\n");
            level += " ";
        }

        builder.append(MIDDLE_BORDER).append("\n");
    }

    private static void setupMessage(Throwable e, String message, StringBuilder builder) {
        if (!TextUtils.isEmpty(message)) {
            builder.append(message);
            builder.append("\n");
        }
        if (e != null) {
            builder.append( getStackTraceString(e) );
        }
    }


    /** 获取错误信息的堆栈 */
    private static String getStackTraceString(Throwable tr) {
        if (tr == null) {
            return "";
        }

        Throwable t = tr;
        while (t != null) {
            t = t.getCause();
        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        tr.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }

    private static String getSimpleClassName(String name) {
        int lastIndex = name.lastIndexOf(".");
        return name.substring(lastIndex + 1);
    }


    private static void logChunk(int logType, String tag, String chunk) {
        int length = chunk.length();
        if (length < CHUNK_SIZE) {
            logChunkInSize(logType, tag, chunk);
        } else {
            for (int i = 0; i < length; i += CHUNK_SIZE) {
                int lastIndex = Math.min(length - 1, i + CHUNK_SIZE);
                logChunkInSize(logType, tag, chunk.substring(i, lastIndex));
            }
        }


    }

    private static void logChunkInSize(int logType, String tag, String chunk) {
        switch (logType) {
            case VERBOSE:
                Log.v(tag, chunk);
                break;
            case INFO:
                Log.i(tag, chunk);
                break;
            case WARN:
                Log.w(tag, chunk);
                break;
            case ERROR:
                Log.e(tag, chunk);
                break;
            case ASSERT:
                Log.wtf(tag, chunk);
                break;
            // 默认和debug相同
            case DEBUG:
            default:
                Log.d(tag, chunk);
        }
    }


    @Override
    public void v(String msg) {
        log(VERBOSE, mSettings, null, null, msg);
    }

    @Override
    public void d(String message) {
        log(DEBUG, mSettings, null, null, message);
    }


    @Override
    public void i(String message) {
        log(INFO, mSettings, null, null, message);
    }

    @Override
    public void w(String message) {
        this.w(null, message);
    }

    @Override
    public void w(Throwable e) {
        this.w(e, null);
    }

    @Override
    public void w(Throwable e, String message) {
        log(WARN, mSettings, null, e, message);
    }


    @Override
    public void e(String message) {
        this.e(null, message);
    }

    @Override
    public void e(Throwable e) {
        this.e(e, null);
    }

    @Override
    public void e(Throwable e, String message) {
        log(ERROR, mSettings, null, e, message);
    }


    @Override
    public void wtf(String message) {
        this.wtf(null, message);
    }

    @Override
    public void wtf(Throwable e) {
        this.wtf(e, null);
    }

    @Override
    public void wtf(Throwable e, String message) {
        log(ASSERT, mSettings, null, e, message);
    }

    /**
     * json字符串格式化输出的间隔
     */
    private static final int JSON_INDENT = 4;


    public void json(String json) {
        if (TextUtils.isEmpty(json)) {
            d(EMPTY_MESSAGE);
            return;
        }

        try {
            json = json.trim();
            if (json.startsWith("{")) {
                JSONObject jsonObject = new JSONObject(json);
                String message = jsonObject.toString(JSON_INDENT);
                d(message);
                return;
            }
            if (json.startsWith("[")) {
                JSONArray jsonArray = new JSONArray(json);
                String message = jsonArray.toString(JSON_INDENT);
                d(message);
                return;
            }
            e("Invalid Json");
        } catch (JSONException e) {
            e("Invalid Json");
        }
    }

    /*@Override
    public void xml(String xml) {
        xml(null, xml);
    }

    @Override
    public void xml(String tag, String xml) {
        if (TextUtils.isEmpty(xml)) {
            d(tag, "Empty/Null xml content");
            return;
        }
        try {
            Source xmlInput = new StreamSource(new StringReader(xml));
            StreamResult xmlOutput = new StreamResult(new StringWriter());
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(xmlInput, xmlOutput);
            d(tag, xmlOutput.getWriter().toString().replaceFirst(">", ">\n"));
        } catch (TransformerException e) {
            e(tag, null, "Invalid xml");
        }
    }*/
}
