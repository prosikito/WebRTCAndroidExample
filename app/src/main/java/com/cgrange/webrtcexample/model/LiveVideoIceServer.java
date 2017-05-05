package com.cgrange.webrtcexample.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by cgrange on 12/04/17.
 *
 */

public class LiveVideoIceServer implements Serializable {

    @SerializedName("username")
    private String username;

    @SerializedName("url")
    private String url;

    @SerializedName("credential")
    private String credential;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCredential() {
        return credential;
    }

    public void setCredential(String credential) {
        this.credential = credential;
    }
}
