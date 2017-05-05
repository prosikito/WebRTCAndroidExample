package com.cgrange.webrtcexample.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by cgrange on 6/10/16.
 *
 */
public class LiveVideoAvailableResponse {

    @SerializedName("success")
    private boolean success;

    public boolean isSuccess() {
        return success;
    }
}
