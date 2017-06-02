package com.cgrange.webrtcexample.cloud;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;
import android.view.Window;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.cgrange.webrtcexample.Application;
import com.cgrange.webrtcexample.R;
import com.cgrange.webrtcexample.loggers.Logger;
import com.cgrange.webrtcexample.util.Common;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by cgrange on 6/09/16.
 *
 */
public abstract class AbstractConnection<T> extends Request<T> {

    private Dialog pgDialog;
    private final Gson gson = new Gson();
    private Class<T> clazz;
    private Activity context;
    private final Response.Listener<T> listener;
    private final Response.ErrorListener errorListener;

    protected Map<String, String> mPostParams;
    protected List<String> headerExtras;

    protected String oauthToken;

    public AbstractConnection(Class<T> clazz, @NonNull Activity context, boolean showProgress, int method, String url, Response.Listener<T> listener, Response.ErrorListener errorListener) {
        super(method, url, errorListener);

        this.clazz    = clazz;
        this.listener = listener;
        this.errorListener = errorListener;
        this.context  = context;

        if (Common.checkConnection(context) && showProgress) {
            pgDialog = new Dialog(context);
            pgDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

            if (pgDialog.getWindow() != null) {
                pgDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                pgDialog.getWindow().setContentView(R.layout.loading_view);
            }

            pgDialog.setCancelable(false);
            if (!pgDialog.isShowing())
                pgDialog.show();
        }
        this.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        this.setTag(context.getClass().getName());
    }


    public AbstractConnection(Class<T> clazz, Activity context, int method, String url, Response.Listener<T> listener, Response.ErrorListener errorListener) {
        super(method, url, errorListener);

        this.clazz    = clazz;
        this.listener = listener;
        this.errorListener = errorListener;
        this.context  = context;

        this.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    @Override
    protected void deliverResponse(T response) {
        listener.onResponse(response);
        Common.dismissProgressDialog(context, pgDialog);
    }

    @Override
    public void deliverError(VolleyError error) {
        errorListener.onErrorResponse(error);
        Common.dismissProgressDialog(context, pgDialog);
    }

    @NonNull
    @Override
    protected Response<T> parseNetworkResponse(@NonNull NetworkResponse response) {
        try {
            String json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            return Response.success(gson.fromJson(json, clazz), HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException | JsonSyntaxException e) {
            return Response.error(new ParseError(e));
        }
    }

    @NonNull
    @Override
    //This will make the volley error message to contain your server's error message
    protected VolleyError parseNetworkError(@NonNull VolleyError volleyError) {
        Common.dismissProgressDialog(context, pgDialog);
        return volleyError;
    }

    @NonNull
    @Override
    public String getBodyContentType() {
        return "application/json";
    }

    @NonNull
    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = new ArrayMap<>();

        headers.put("Content-Type", "application/x-www-form-urlencoded");

        // new headers
        headers.put(ConnectionConstants.ENDPOINT_TYPE_HEADER, ConnectionConstants.ENDPOINT_TYPE);
        headers.put(ConnectionConstants.LOCALE_HEADER, Locale.getDefault().getLanguage());
        headers.put(ConnectionConstants.DEVICE_UID_HEADER, Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID));
        headers.put(ConnectionConstants.ENDPOINT_TOKEN_HEADER, "");


        StringBuilder sb = new StringBuilder(ConnectionConstants.API_KEY_VALUE);

        try {
            String userToken = "RPxu1-GrwAJoZUWfPcY4ww";
            if (!userToken.isEmpty())
                sb.append(", ").append(ConnectionConstants.USER_TOKEN).append(userToken);
        }
        catch (Exception e){
            Logger.log(e);
        }

        if (oauthToken != null){
            sb.append(", ").append(ConnectionConstants.OAUTH_TOKEN).append(oauthToken);
        }

        if (headerExtras != null) {
            for (String s : headerExtras) {
                sb.append(", ").append(s);
            }
        }
        headers.put(ConnectionConstants.API_KEY_AUTH, sb.toString());

        String accept = ConnectionConstants.API_VERSION + ConnectionConstants.API_VERSION_NUMBER;
        headers.put(ConnectionConstants.API_KEY_ACCEPT, accept);

        Logger.log("Headers: " + headers.toString());

        return headers;
    }

    public void request(){
        ((Application) context.getApplication()).getRequestQueue().add(this);
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        if (mPostParams != null) {
            Logger.log(mPostParams.toString());
            return mPostParams;
        }
        return super.getParams();
    }

    public void setHeaderExtras(List<String> headerExtras) {
        this.headerExtras = headerExtras;
    }
}
