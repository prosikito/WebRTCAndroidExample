package com.cgrange.webrtcexample;

import android.content.Context;
import android.support.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.Volley;

import java.io.InputStream;
import java.security.KeyStore;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

/**
 * Created by cgrange on 18/08/16.
 *
 */
@SuppressWarnings("unused")
public class Application extends android.app.Application{

    private RequestQueue mRequestQueue;
    Application mInstance;
    Context mCtx;

    protected static final char[] KEYSTORE_PASSWORD = "gotrivesslcert".toCharArray();

    public Application(){
        // unused
    }

    Application(Context context) {
        mCtx = context;
        mRequestQueue = getRequestQueue();
    }



    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(this, new HurlStack(null, newSslSocketFactory(this)));
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(@NonNull Request<T> req) {
        req.setTag("WebRTCExample");
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(@NonNull Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    public static SSLSocketFactory newSslSocketFactory(Context context) {
        try {
            // Get an instance of the Bouncy Castle KeyStore format
            KeyStore trusted = KeyStore.getInstance("BKS");
            // Get the raw resource, which contains the keystore with
            // your trusted certificates (root and any intermediate certs)
            try (InputStream in = context.getResources().openRawResource(R.raw.gotrivesslcert)) {
                // Initialize the keystore with the provided trusted certificates
                // Provide the password of the keystore
                trusted.load(in, KEYSTORE_PASSWORD);
            }

            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(trusted);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);

            return sslContext.getSocketFactory();
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
