package com.cgrange.webrtcexample.cloud;

import com.cgrange.webrtcexample.BuildConfig;

/**
 * Created by cgrange on 5/05/17.
 *
 */

public class ConnectionConstants {

    public static final String API_KEY_AUTH                 = "Authorization";
    public static final String API_KEY_ACCEPT               = "Accept";
    public static final String LOCALE_HEADER                = "Accept-Language";
    public static final String ENDPOINT_TYPE_HEADER         = "Endpoint-Type";
    public static final String ENDPOINT_TOKEN_HEADER        = "Endpoint-Token";
    public static final String DEVICE_UID_HEADER            = "Device-Uid";

    public static final String USER_TOKEN                   = "access_token=";
    public static final String OAUTH_TOKEN                  = "oauth_token=";
    public static final String API_KEY_VALUE                = "Token api_key=ea0a50951204d15ca8c1aa73e";

    public static final String API_VERSION                  = "application/vnd.trive.+json; version=";

    // CONSTANT VALUES
    public static final String ENDPOINT_TYPE                = "Android";

    private static final String DOMAIN              = getUrl();
    private static final String BASE_URL            = DOMAIN + "/api/";
    public static final String API_VERSION_NUMBER   = "1";

    public static final String LIVE_VIDEO           = BASE_URL + "live-video/sessions";

    public static String getUrl(){
        if (BuildConfig.DEBUG)
            return "http://c6cc2fd1.ngrok.io";
//            return "https://portal-dev.gotrive.com";
        else
            return "https://gotrive.com";
    }

}
