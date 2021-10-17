package com.example.dj.appgl;

import android.app.Application;

import com.example.dj.appgl.base.AppCore;
import com.tencent.bugly.crashreport.CrashReport;

public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AppCore.getInstance().init(this);
        CrashReport.initCrashReport(getApplicationContext(),"e4c59fc9cf",true);
    }

}
