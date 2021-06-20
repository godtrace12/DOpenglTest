package com.example.dj.media;

public class DPlayer {
    static {
        System.loadLibrary("native-lib");
    }

//    public static native String getStringFromNDK();


    public static native String getNativeString();

    public static native void nativeStringInit();

}
