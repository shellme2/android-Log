package com.eebbk.bfc.bfclog.inner;

import com.eebbk.bfc.bfclog.BfcLogger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 用于把log文件 记录到sd卡中
 * <p>
 * Created by Simon on 2016/9/12.
 */
class Log2SDCardUtil {
    /**
     * 需要保存的log的信息
     */
    static class LogSaveInfo {
        LogSaveInfo(String log, String savePath) {
            this.log = log;
            this.savePath = savePath;
        }

        String savePath;
        String log;
    }

    /**
     * 确保保存log的文件存在, 或者能创建成功
     */
    static void ensureSaveLocationLegal(String saveLocation) {
        File targetFile = new File(saveLocation);
        if (targetFile.isDirectory()) {
            throw new IllegalArgumentException("传入的log保存路径是文件夹,请传具体的文件");
        }

        if (!FileUtils.createFileOrExists(targetFile)) {
            throw new IllegalArgumentException("传入的log保存文件路径不存在,并且自动创建失败; 请确认, 或者关闭BfcLog库的log保存");
        }
    }


    private final static String TAG = "BfcLog_Log2SDCardUtil";

    private boolean isThreadStarted = false;
    private BlockingQueue<LogSaveInfo> logStringQueue;

    private static Log2SDCardUtil sInstance;

    static Log2SDCardUtil getInstance() {
        if (sInstance == null) {
            synchronized (Log2SDCardUtil.class) {
                if (sInstance == null) {
                    sInstance = new Log2SDCardUtil();
                }
            }
        }

        return sInstance;
    }

    private Log2SDCardUtil() {
        logStringQueue = new LinkedBlockingQueue<>();
    }

    void saveLog(LogSaveInfo saveInfo) {
        if (!isThreadStarted) {
            new Thread(new WriteToStorageRunnable(), "保存log的线程").start();
        }
        logStringQueue.add(saveInfo);
    }


    private Map<String, Writer> fileWriteMap = new HashMap<>();

    private Writer openFile(String filePath) {
        if (fileWriteMap.get(filePath) != null) {
            return fileWriteMap.get(filePath);
        }

        File targetFile = new File(filePath);
        if (!targetFile.exists()) {
            try {
                targetFile.createNewFile();
            } catch (IOException e) {
                BfcLogger.e(TAG, "创建log保存的文件失败", e);
            }
        }

        Writer os = null;
        try {
            os = new FileWriter(targetFile, true);
        } catch (IOException e) {
            BfcLogger.e(TAG, "创建log保存的输出流失败", e);
        }

        fileWriteMap.put(filePath, os);
        return os;
    }

    private class WriteToStorageRunnable implements Runnable {
        boolean needExit = false;

        @Override
        public void run() {
            isThreadStarted = true;

            while (true) {
                try {
                    final LogSaveInfo saveInfo = logStringQueue.take();
                    final Writer osw = openFile(saveInfo.savePath);

                    if (osw != null) {
                        osw.write(saveInfo.log);
                        osw.flush();
                    } else {
                        BfcLogger.e(TAG, "保存log文件的数据输出流未创建成功, 请检查或者关闭BfcLog库的log保存");
                    }
                } catch (InterruptedException e) {
                    BfcLogger.e(TAG, "保存log到文件,线程读取数据队列出错", e);
                } catch (IOException e) {
                    BfcLogger.e(TAG, "保存log到文件,写入出错", e);
                }

                if (needExit) {
                    isThreadStarted = false;
                    break;
                }
            }
        }
    }
}
