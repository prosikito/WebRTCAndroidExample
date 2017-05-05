package com.cgrange.webrtcexample.cloud;

import android.app.Activity;

import com.android.volley.Response;
import com.cgrange.webrtcexample.model.LiveVideoAvailableResponse;

/**
 *
 */
public class LiveVideoAvailableGetConnection extends AbstractConnection<LiveVideoAvailableResponse> {
    public LiveVideoAvailableGetConnection(Activity context,
                                           boolean showProgress,
                                           Response.Listener<LiveVideoAvailableResponse> listener,
                                           Response.ErrorListener errorListener) {

        super(LiveVideoAvailableResponse.class,
                context,
                showProgress,
                Method.GET,
                ConnectionConstants.LIVE_VIDEO,
                listener,
                errorListener);
    }
}