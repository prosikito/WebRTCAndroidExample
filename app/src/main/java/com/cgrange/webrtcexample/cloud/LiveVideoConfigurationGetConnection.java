package com.cgrange.webrtcexample.cloud;

import android.app.Activity;

import com.android.volley.Response;
import com.cgrange.webrtcexample.model.LiveVideoConfigurationResponse;

import java.util.Map;

/**
 *
 */
public class LiveVideoConfigurationGetConnection extends AbstractConnection<LiveVideoConfigurationResponse> {
    public LiveVideoConfigurationGetConnection(Activity context,
                                               boolean showProgress,
                                               Map<String, String> params,
                                               Response.Listener<LiveVideoConfigurationResponse> listener,
                                               Response.ErrorListener errorListener) {

        super(LiveVideoConfigurationResponse.class,
                context,
                showProgress,
                Method.POST,
                ConnectionConstants.LIVE_VIDEO,
                listener,
                errorListener);

        mPostParams = params;
    }
}