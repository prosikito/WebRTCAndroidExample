package com.cgrange.webrtcexample.loggers;

import android.support.annotation.Nullable;
import android.util.Log;

import com.cgrange.webrtcexample.BuildConfig;

import java.util.Arrays;

/**
 * Created by cgrange on 23/08/16.
 *
 */
public class Logger {

    private static final String TAG = "TAG_Trive";

    private Logger(){
        // not used
    }

    public static void log(@Nullable String message){
        if (BuildConfig.DEBUG && message != null) {
            Log.d(TAG, message);
        }
    }

    public static void log(@Nullable Exception exception){
        if (BuildConfig.DEBUG && exception != null && exception.getMessage() != null) {
            Log.e(TAG, exception.getMessage());
            if (exception.getStackTrace() != null){
                Log.e(TAG, Arrays.toString(exception.getStackTrace()));
            }
        }
    }

    public static void log(int exception){
        if (BuildConfig.DEBUG) {
            Log.e(TAG, String.valueOf(exception));
        }
    }
}
