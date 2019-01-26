package com.eebbk.bfc.bfclog_demo;

import android.util.Log;

import com.eebbk.bfc.bfclog.BfcLog;
import com.eebbk.bfc.bfclog.BfcLogger;

/**
 * Created by Simon on 2016/9/14.
 */
public class L {
    private static BfcLog bfcLog;
    static {
        /*bfcLog = new BfcLog.Builder()
                .tag( YOUR_TAG )
                .showLog(true)
                .methodOffset(1)
                .methodCount(2)
                .showThreadInfo( true )
                .saveLog(false, SAVE_PATH)
                .build();*/
    }

    public static void e(String msg){
        Log.e("zu", msg);
        bfcLog.e(msg);
    }


    static {
       /* BfcLogger.init( YOUR_TAG )
                .showLog( true )
                .logLevel(BfcLog.VERBOSE)
                .showThreadInfo( true )
                .methodCount(2)
                .methodOffset(0)
                .saveLog( false, SAVE_PATH );*/
    }

    void test(){
//        BfcLogger.tag( TAG ).d("hello");
//        BfcLogger.method( 5 ).d("hello");
//        BfcLogger.thread( true ).d("hello");

    }
}
