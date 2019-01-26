package com.eebbk.bfc.bfclog_demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Switch;

import com.eebbk.bfc.bfclog.BfcLogger;
import com.eebbk.bfc.bfclog.annotation.BfcLogTime;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    public static String DEFAULT_JSON_STR = "{\"element1\":\"value1\",\"obj1\":{\"a\":123},\"element2\":\"value2\"}";

    public static String save_path;


    @Bind(R.id.sw_log_switch)
    Switch mLogSwitch;

    @Bind(R.id.sw_thread_info)
    Switch mThreadInfoSwitch;

    @Bind(R.id.sw_log_save)
    Switch mLogSaveSwitch;

    @Bind(R.id.et_sdcard_save_path)
    EditText mLogSaveEditText;

    @Bind(R.id.et_method_count)
    EditText mMethodCountEditText;

    @Bind(R.id.et_default_tag)
    EditText mTagDefaultEditText;

    @Bind(R.id.et_method_offset)
    EditText mMethodOffsetEditText;

    @Bind(R.id.rgp_log_type)
    RadioGroup mTypeRadioGroup;

    @Bind(R.id.et_tag)
    EditText mTagEditText;

    @Bind(R.id.et_count)
    EditText mCountText;

    @Bind(R.id.et_message)
    EditText mMessageEditText;

    @Bind(R.id.exception_type_rg)
    RadioGroup mExceptionTypeRadioGroup;

    @Bind(R.id.et_sleep_time)
    EditText mSleepTimeEditText;

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        ButterKnife.bind(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        save_path = this.getCacheDir().getPath() + "/log";

        mLogSaveEditText.setText(save_path);
        mLogSaveSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mLogSaveEditText.setEnabled(isChecked);
            }
        });

        mTypeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.log_type_json_rbtn:
                        if (TextUtils.isEmpty(mMessageEditText.getText().toString())) {
                            mMessageEditText.setText(DEFAULT_JSON_STR);
                        }
                        break;
                    case R.id.log_type_xml_rbtn:
                        break;
                }
            }
        });
    }


    public void log() {
        String logCountStr = mCountText.getText().toString();
        long N = TextUtils.isEmpty(logCountStr) ? 0 : Long.parseLong(logCountStr);
        boolean shouldShowLog = mLogSwitch.isChecked();
        boolean shoudLogThreadInfo = mThreadInfoSwitch.isChecked();

        String methodCountStr = mMethodCountEditText.getText().toString();
        int methodCount = TextUtils.isEmpty(methodCountStr) ? 2 : Integer.parseInt(methodCountStr);

        String methodOffsetStr = mMethodOffsetEditText.getText().toString();
        int methodOffset = TextUtils.isEmpty(methodOffsetStr) ? 0 : Integer.parseInt(methodOffsetStr);

        String defaultTag = mTagDefaultEditText.getText().toString();
        String tag = mTagEditText.getText().toString();
        String message = mMessageEditText.getText().toString();

        boolean shouldSave = mLogSaveSwitch.isChecked();
        String savePath = mLogSaveEditText.getText().toString();

        Exception e = null;
        switch (mExceptionTypeRadioGroup.getCheckedRadioButtonId()) {
            case R.id.no_exception_rbtn:
                e = null;
                break;
            case R.id.null_exception_rbtn:
                e = new NullPointerException("空指针异常");
                break;
            case R.id.illegal_exception_rbtn:
                e = new IllegalArgumentException("参数错误异常");
                break;
        }

        BfcLogger.init(defaultTag)
                .showLog(shouldShowLog)
                .showThreadInfo(shoudLogThreadInfo)
                .methodCount(methodCount)
                .methodOffset(methodOffset)
                .saveLog(shouldSave, savePath);

        long startMillis = System.currentTimeMillis();

        for (int i = 0; i < N; i++) {
            switch (mTypeRadioGroup.getCheckedRadioButtonId()) {
                case R.id.log_type_v_rbtn:
                    BfcLogger.v(tag, message, e);
                    break;
                case R.id.log_type_i_rbtn:
                    BfcLogger.i(tag, message, e);
                    break;
                case R.id.log_type_d_rbtn:
                    BfcLogger.d(tag, message, e);
                    break;
                case R.id.log_type_w_rbtn:
                    BfcLogger.w(tag, message, e);
                    break;
                case R.id.log_type_e_rbtn:
                    BfcLogger.e(tag, message, e);
//                    Log.e(tag, message, e);
                    break;
                case R.id.log_type_assert_rbtn:
                    BfcLogger.wtf(tag, message, e);
//                    Log.wtf(tag, message, e);
                    break;
                case R.id.log_type_json_rbtn:
                    BfcLogger.tag(tag).json(message);
                    break;
                case R.id.log_type_xml_rbtn:
//                    BfcLogger.xml(tag, message);
                    break;
            }
        }

        // 如果只有一次log, 不打印耗时统计
        if (N < 2) return;
        long endMillis = System.currentTimeMillis();
        BfcLogger.d("BfcLogDemon", "耗时统计, 平均耗时:" + ((endMillis - startMillis) * 1.0f / N));
    }

    @OnClick({R.id.btn_start_log, R.id.btn_log_time, R.id.btn_test_log})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start_log:
                log();
                break;
            case R.id.btn_log_time:
                logTime();
                break;
            case R.id.btn_test_log:
//                Log.d(TAG, "hello");
//                BfcLogger.d(TAG, "hello");

                BfcLogger.v("Hello BfcLogger");
                BfcLogger.d("Hello BfcLogger");
                BfcLogger.i("Hello BfcLogger");
                BfcLogger.wtf("Hello BfcLogger");
//                BfcLogger.json( JSON_CONTENT );
                BfcLogger.tag("zy").i("Hello BfcLogger");
                BfcLogger.thread(true).method(3).w("Hello BfcLogger");
                BfcLogger.json("{\"title\":\"BfcLog\",\"content\":\"hello word\",\"chapter\":[\"first\",\"second\"]}");

                break;
        }
    }

    /**
     * 测试方法耗时统计,  根据睡眠的时间, 校验耗时的准确性
     */
    void logTime() {
        int sleepTime = Integer.parseInt(mSleepTimeEditText.getText().toString());
        new Thread(new TimeRunnable(sleepTime)).start();
    }

    public static class TimeRunnable implements Runnable {

        int sleepSeconds;

        public TimeRunnable(int sleepSeconds) {
            this.sleepSeconds = sleepSeconds;
        }

        @Override
        public void run() {
            add(3, "SampleInput");
        }

        @BfcLogTime
        // 测试耗时的方法
        private String add(int x, String input) {
            try {
                Thread.sleep(sleepSeconds);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return String.valueOf(x) + "_" + input;
        }
    }

}