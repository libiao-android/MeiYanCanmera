package com.meitu.meiyancamera;

import android.app.Application;
import android.util.Log;

import com.meitu.core.JNIConfig;

/**
 * Created by libiao on 2017/6/5.
 */
public class BeautyApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        JNIConfig.instance().ndkInit(this, getExternalCacheDir().toString());
    }
}
