package com.cgrange.webrtcexample.model;

import com.cgrange.webrtcexample.cloud.ConnectionConstants;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by cgrange on 6/10/16.
 *
 */
public class LiveVideoConfigurationResponse implements Serializable {

    @SerializedName("online_admin")
    private boolean onlineAdmin;

    @SerializedName("model_master_id")
    private boolean modelMasterId;

    @SerializedName("current_user")
    private boolean currentUser;


    @SerializedName("session")
    private LiveVideoSession session;

    @SerializedName("peerConnectionConfig")
    private IceServerArray iceServers;

    @SerializedName("signal")
    private String signal;

    @SerializedName("wss_url")
    private String socketUrl;



    public boolean isOnlineAdmin() {
        return onlineAdmin;
    }

    public void setOnlineAdmin(boolean onlineAdmin) {
        this.onlineAdmin = onlineAdmin;
    }

    public boolean isModelMasterId() {
        return modelMasterId;
    }

    public void setModelMasterId(boolean modelMasterId) {
        this.modelMasterId = modelMasterId;
    }

    public boolean isCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(boolean currentUser) {
        this.currentUser = currentUser;
    }

    public LiveVideoSession getSession() {
        return session;
    }

    public void setSession(LiveVideoSession session) {
        this.session = session;
    }

    public IceServerArray getIceServers() {
        return iceServers;
    }

    public void setIceServers(IceServerArray iceServers) {
        this.iceServers = iceServers;
    }

    public String getSignal() {
        return signal;
    }

    public void setSignal(String signal) {
        this.signal = signal;
    }

    public String getSocketUrl() {
        return "ws://2c2375c4.ngrok.io/live_video/cable";
//        return socketUrl;
    }

    public void setSocketUrl(String socketUrl) {
        this.socketUrl = socketUrl;
    }
}
