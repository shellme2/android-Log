package com.eebbk.bfc.bfclog_demo;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;

import com.github.moduth.blockcanary.BlockCanary;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

/**
 * Created by Simon on 2016/12/14.
 */

public class App extends Application {
    private static Context sContext;
    private RefWatcher mRefWatcher;

    @Override
    public void onCreate() {
        sContext = this;
        initStrickMode();
        super.onCreate();
        initLeakCanary();
        initBlockCanary();
    }


    private void initStrickMode() {
        if (BuildConfig.DEBUG) {
            //针对线程的监视策略
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()   // or .detectAll() for all detectable problems
                    .penaltyLog()
                    .build());
            //针对vm
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .penaltyDeath()
                    .build());

        }
    }

    private void initLeakCanary() {
        // leakcanary默认只监控Activity的内存泄露
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        mRefWatcher = LeakCanary.install(this);
    }

    private void initBlockCanary() {
        BlockCanary.install(this, new BlockCanaryConfig()).start();
    }

    public static RefWatcher getRefWatcher(Context context) {
        App app = (App) context.getApplicationContext();
        return app.mRefWatcher;
    }

    public static Context getAppContext() {
        return sContext;
    }
}
