package com.cgrange.webrtcexample.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by cgrange on 12/04/17.
 *
 */

public class LiveVideoSession implements Serializable{

    @SerializedName("id")
    private int id;

    @SerializedName("token")
    private String token;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
