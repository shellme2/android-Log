package com.eebbk.bfc.bfclog.aop.weaving;

import com.eebbk.bfc.bfclog.BfcLog;

/**
 * 算了 不放在inner包里面, 就用包访问权限去控制了, 防止外面引用了
 *
 * Created by Simon on 2016/9/26.
 */
public final class BfcInnerLog {
    static BfcLog log;

    static {
        log = new BfcLog.Builder()
                .tag("time-consuming")
                .showThreadInfo(false)
//                .methodOffset(1)
                .build();
    }


    static void d(String message){
        log.d( message );
    }

}
