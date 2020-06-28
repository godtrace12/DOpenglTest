package com.example.dj.appgl.base;

import android.app.Application;
import android.content.res.Resources;

public class AppCore {
    private static AppCore sInstance;

    private Application application;

    public static AppCore getInstance() {
        if (sInstance == null) {
            sInstance = new AppCore();
        }
        return sInstance;
    }

    public void init(Application application) {
        this.application = application;
    }

    public Application getContext() {
        return application;
    }

    public Resources getResources() {
        return application.getResources();
    }

}
