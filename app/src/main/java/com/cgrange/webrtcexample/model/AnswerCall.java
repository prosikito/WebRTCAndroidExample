package com.cgrange.webrtcexample.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by cgrange on 23/05/17.
 *
 */

public class AnswerCall {

    @SerializedName("identifier")
    @Expose
    private String identifier;
    @SerializedName("message")
    @Expose
    private Message message;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }
}
