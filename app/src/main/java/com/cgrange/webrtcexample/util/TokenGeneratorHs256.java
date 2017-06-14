package com.cgrange.webrtcexample.util;

import org.jose4j.base64url.internal.apache.commons.codec.binary.Base64;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.keys.HmacKey;
import org.jose4j.lang.JoseException;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class TokenGeneratorHs256 implements ICallStatsTokenGenerator {

    private HmacKey key;
    private String keyId = null;
    private String appId;
    private String userId;

    /**
     * Initialize HS256 JWT generator
     * @param appSecret Application secret. The contents will be cleared after constructor is done.
     * @param appId Application ID
     * @param userId User ID
     */
    public TokenGeneratorHs256(char[] appSecret, final int appId, final String userId) {
        this.userId = userId;
        this.appId = (new Integer(appId)).toString();

        byte[] keyIdBuilder = new byte[appSecret.length];
        boolean split = false;
        byte[] keyBytes = null;
        int keyIdx = 0;

        // Split key id and app secret
        for (int i=0; i<appSecret.length; i++) {
            if (!split && appSecret[i] != ':') {
                keyIdBuilder[i] = (byte)appSecret[i];
            } else if (appSecret[i] == ':') {
                split = true;
                keyBytes = new byte[appSecret.length-i-1];
            } else {
                keyBytes[keyIdx++] = (byte)appSecret[i];
            }
        }
        if (split) {
            try {
                keyId = bytesToHex(Base64.decodeBase64(new String(keyIdBuilder, "UTF-8")));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            clearByteArray(keyIdBuilder);

        } else {
            // There wasn't key ID in the app secret, so keep keyid as null
            keyBytes = keyIdBuilder;
        }
        key = new HmacKey(keyBytes);
        clearByteArray(keyBytes);
        clearCharArray(appSecret);
    }
    /**
     * Clear character array
     * @param secret secret key
     */
    public static void clearCharArray(char[] secret) {
        Arrays.fill(secret, '\0');
    }

    /**
     * Clear byte array
     * @param secret secret key
     */
    public static void clearByteArray(byte[] secret) {
        Arrays.fill(secret, (byte)0);
    }

    public String generateToken(boolean forcenew) {
        JwtClaims claims = new JwtClaims();
        claims.setClaim("appID", appId);
        claims.setClaim("userID", userId);
        if (keyId != null) {
            claims.setClaim("keyID", keyId);
        }
        claims.setExpirationTimeMinutesInTheFuture(10);
        claims.setNotBeforeMinutesInThePast(10);

        JsonWebSignature jws = new JsonWebSignature();
        jws.setPayload(claims.toJson());
        jws.setKey(key);
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA256);
        // For backwards compatibility with old app secrets
        jws.setDoKeyValidation(false);
        try {
            String jwt = jws.getCompactSerialization();
            return jwt;
        } catch (JoseException e) {
            e.printStackTrace();
        }
        return null;
    }

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
