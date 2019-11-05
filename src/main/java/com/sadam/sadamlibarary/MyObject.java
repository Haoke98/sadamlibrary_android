package com.sadam.sadamlibarary;

import android.util.Log;

public class MyObject extends Object {
    public static void logE(Object object, String warning) {
        Log.e(object.getClass().getSimpleName(), warning);
    }

}
