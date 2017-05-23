package com.cgrange.webrtcexample.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by cgrange on 23/05/17.
 */

public class Message {

    @SerializedName("render_call")
    @Expose
    private String renderCall;
    @SerializedName("call_type")
    @Expose
    private String callType;
    @SerializedName("take_session")
    @Expose
    private int takeSession;
    @SerializedName("next_call")
    @Expose
    private String nextCall;
    @SerializedName("sdp_answer")
    @Expose
    private String sdpAnswer;
    @SerializedName("session_token")
    @Expose
    private String sessionToken;
    @SerializedName("admin")
    @Expose
    private int admin;

    public String getRenderCall() {
        return renderCall;
    }

    public void setRenderCall(String renderCall) {
        this.renderCall = renderCall;
    }

    public String getCallType() {
        return callType;
    }

    public void setCallType(String callType) {
        this.callType = callType;
    }

    public int getTakeSession() {
        return takeSession;
    }

    public void setTakeSession(int takeSession) {
        this.takeSession = takeSession;
    }

    public String getNextCall() {
        return nextCall;
    }

    public void setNextCall(String nextCall) {
        this.nextCall = nextCall;
    }

    public String getSdpAnswer() {
        return sdpAnswer;
    }

    public void setSdpAnswer(String sdpAnswer) {
        this.sdpAnswer = sdpAnswer;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public int getAdmin() {
        return admin;
    }

    public void setAdmin(int admin) {
        this.admin = admin;
    }
}
