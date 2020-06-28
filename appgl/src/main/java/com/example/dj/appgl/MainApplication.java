package com.example.dj.appgl;

import android.app.Application;

import com.example.dj.appgl.base.AppCore;

public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AppCore.getInstance().init(this);
    }
}
