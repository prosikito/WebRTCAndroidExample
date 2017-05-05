package com.cgrange.webrtcexample.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.cgrange.webrtcexample.loggers.Logger;

/**
 * Created by cgrange on 5/05/17.
 *
 */

public class Common {

    private Common(){
        // unused
    }

    // Check internet connection
    public static boolean checkConnection(@NonNull Context context){
        return isNetworkAvailable(context);
    }

    private static boolean isNetworkAvailable(@NonNull Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static void dismissProgressDialog(@Nullable Activity activity, @Nullable Dialog pgDialog){
        try {
            if (activity == null)
                return;
            if (activity.isFinishing()) { // or call isFinishing() if min sdk version < 17
                return;
            }
            if (activity.isDestroyed()) {
                return;
            }
            if (pgDialog != null && pgDialog.isShowing()) {
                pgDialog.dismiss();
            }
        }
        catch (Exception e){
            Logger.log(e);
        }
    }
}
