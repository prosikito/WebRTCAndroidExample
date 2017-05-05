package com.cgrange.webrtcexample.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by cgrange on 3/05/17.
 */

public class IceServerArray implements Serializable {
    @SerializedName("iceServers")
    private ArrayList<LiveVideoIceServer> iceServers;

    public ArrayList<LiveVideoIceServer> getIceServers() {
        return iceServers;
    }

    public void setIceServers(ArrayList<LiveVideoIceServer> iceServers) {
        this.iceServers = iceServers;
    }
}
