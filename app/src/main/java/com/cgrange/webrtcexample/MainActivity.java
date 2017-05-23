package com.cgrange.webrtcexample;

import android.Manifest;
import android.content.pm.PackageManager;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.cgrange.webrtcexample.cloud.LiveVideoConfigurationGetConnection;
import com.cgrange.webrtcexample.loggers.Logger;
import com.cgrange.webrtcexample.model.AnswerCall;
import com.cgrange.webrtcexample.model.LiveVideoConfigurationResponse;
import com.cgrange.webrtcexample.model.LiveVideoIceServer;
import com.google.gson.Gson;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.CameraEnumerationAndroid;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RendererCommon;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.VideoCapturerAndroid;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRendererGui;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;


/*
//
// Created by cgrange on 04/05/2017
//
*/

public class MainActivity extends AppCompatActivity {

    private static final String SIGNALING_URI       = "wss://portal-dev.gotrive.com:443/live_video/cable";
//    private static final String SIGNALING_ECHO_URI  = "wss://echo.websocket.org";
//    private static final String SIGNALING_URI       = "http://webrtc.theboton.io:7000";

    private static final String VIDEO_TRACK_ID      = "video1";
    private static final String AUDIO_TRACK_ID      = "audio1";
    private static final String LOCAL_STREAM_ID     = "stream1";

    protected static final char[] KEYSTORE_PASSWORD = "gotrivesslcert".toCharArray();

    private Button connectButton;
    private Button switchAudioButton;

    private PeerConnectionFactory peerConnectionFactory;
    private PeerConnection peerConnection;
    private MediaStream localMediaStream;
    private VideoRenderer otherPeerRenderer;
//    private Socket socket;
    private boolean createOfferBool = false;
    private VideoSource localVideoSource;

    private static final int MY_PERMISSIONS_REQUEST = 1005;
    private AppRTCAudioManager audioManager;
    private LiveVideoConfigurationResponse liveVideoConfigurationResponse;
    private ArrayList<PeerConnection.IceServer> iceServers = new ArrayList<>();
    private OkHttpClient client;
    private WebSocket ws;
    private boolean connecting = false;
//    private KeyStore trusted;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            checkCameraPermission();
        }
        else {
            initActivity();
        }
    }

    private void initActivity(){
        setContentView(R.layout.activity_main);

        connectButton = (Button) findViewById(R.id.connect);
        switchAudioButton = (Button) findViewById(R.id.switchAudio);

        initAudioManager();

        PeerConnectionFactory.initializeAndroidGlobals(
                this,  // Context
                true,  // Audio Enabled
                true,  // Video Enabled
                true  // Hardware Acceleration Enabled
                );

        peerConnectionFactory = new PeerConnectionFactory();

        VideoCapturerAndroid videoCapturerAndroid = VideoCapturerAndroid.create(CameraEnumerationAndroid.getNameOfFrontFacingDevice(), null);

        localVideoSource = peerConnectionFactory.createVideoSource(videoCapturerAndroid, new MediaConstraints());
        VideoTrack localVideoTrack = peerConnectionFactory.createVideoTrack(VIDEO_TRACK_ID, localVideoSource);
        localVideoTrack.setEnabled(true);

        AudioSource audioSource = peerConnectionFactory.createAudioSource(new MediaConstraints());
        AudioTrack localAudioTrack = peerConnectionFactory.createAudioTrack(AUDIO_TRACK_ID, audioSource);
        localAudioTrack.setEnabled(true);

        localMediaStream = peerConnectionFactory.createLocalMediaStream(LOCAL_STREAM_ID);
        localMediaStream.addTrack(localVideoTrack);
        localMediaStream.addTrack(localAudioTrack);

        GLSurfaceView videoView = (GLSurfaceView) findViewById(R.id.glview_call);

        VideoRendererGui.setView(videoView, null);
        try {
            otherPeerRenderer = VideoRendererGui.createGui(0, 0, 100, 100, RendererCommon.ScalingType.SCALE_ASPECT_FILL, true);
            VideoRenderer renderer = VideoRendererGui.createGui(50, 50, 50, 50, RendererCommon.ScalingType.SCALE_ASPECT_FILL, true);
            localVideoTrack.addRenderer(renderer);
        } catch (Exception e) {
            log(e);
        }

        getLiveVideoConfiguration();
    }

    private void initAudioManager(){
        audioManager = AppRTCAudioManager.create(this, audioDevice -> {
            // unused
        });
        audioManager.setDefaultAudioDevice(AppRTCAudioManager.AudioDevice.EARPIECE);
        audioManager.setOnWiredHeadsetStateListener((plugged, hasMicrophone) -> {
            //unused
        });
        audioManager.init();

        audioManager.setAudioDevice(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE);
    }

    public void onSwitchAudio(View v) {
        if (audioManager != null) {
            if (audioManager.getSelectedAudioDevice() == AppRTCAudioManager.AudioDevice.WIRED_HEADSET
                    || audioManager.getSelectedAudioDevice() == AppRTCAudioManager.AudioDevice.EARPIECE) {
                audioManager.setAudioDevice(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE);
            } else {
                audioManager.setAudioDevice(AppRTCAudioManager.AudioDevice.EARPIECE);
            }
        }
    }

    private void checkCameraPermission(){
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Needs camera permission", Toast.LENGTH_SHORT).show());
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)){
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Needs record audio permission", Toast.LENGTH_SHORT).show());
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO},
                        MY_PERMISSIONS_REQUEST);
            }
        }
        else {
            initActivity();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST
                && (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)) {

                // TODO: SHOW PROBLEM WITH PERMISSIONS!

        } else {
            initActivity();
        }
    }

    public void onConnect() {
        connecting = true;
        peerConnection = peerConnectionFactory.createPeerConnection(
                iceServers,
                new MediaConstraints(),
                peerConnectionObserver);

        peerConnection.addStream(localMediaStream);
        peerConnection.createOffer(sdpObserver, new MediaConstraints());


//        try {
//            socket = IO.socket(liveVideoConfigurationResponse.getSocketUrl());
////            socket = IO.socket(SIGNALING_URI);
//
//            socket.on(CREATEOFFER, args -> {
//                createOfferBool = true;
//                peerConnection.createOffer(sdpObserver, new MediaConstraints());
//                log("CREATE OFFER");
//            }).on(OFFER, args -> {
//                try {
//                    JSONObject obj = (JSONObject) args[0];
//                    SessionDescription sdp = new SessionDescription(SessionDescription.Type.OFFER,
//                            obj.getString(SDP));
//                    peerConnection.setRemoteDescription(sdpObserver, sdp);
//                    peerConnection.createAnswer(sdpObserver, new MediaConstraints());
//                    log("OFFER");
//                } catch (JSONException e) {
//                    log(e);
//                }
//            }).on(ANSWER, args -> {
//                try {
//                    JSONObject obj = (JSONObject) args[0];
//                    SessionDescription sdp = new SessionDescription(SessionDescription.Type.ANSWER,
//                            obj.getString(SDP));
//                    peerConnection.setRemoteDescription(sdpObserver, sdp);
//                    log("ANSWER");
//                } catch (JSONException e) {
//                    log(e);
//                }
//            }).on(CANDIDATE, args -> {
//                try {
//                    JSONObject obj = (JSONObject) args[0];
//                    peerConnection.addIceCandidate(new IceCandidate(obj.getString(SDP_MID),
//                            obj.getInt(SDP_M_LINE_INDEX),
//                            obj.getString(SDP)));
//                    log("CANDIDATE");
//
//                } catch (JSONException e) {
//                    log(e);
//                }
//            });
//            socket.connect();
//            runOnUiThread(() -> log("CONNECT"));
//
//        } catch (URISyntaxException e) {
//            log(e);
//        }
    }

    SdpObserver sdpObserver = new SdpObserver() {
        @Override
        public void onCreateSuccess(SessionDescription sessionDescription) {
            peerConnection.setLocalDescription(sdpObserver, sessionDescription);
            String userId = "2";
            String modelId = "2";

            String sdp = sessionDescription.description.replace("\r", "\\\\r").replace("\n", "\\\\n").replace("OFFER", "offer");
            sendMessage("{\"command\":\"message\", \"identifier\":\"{\\\"channel\\\":\\\"LiveVideo::SessionChannel\\\"}\", \"data\":\"{\\\"user\\\":\\\"" + userId + "\\\",\\\"model\\\":\\\"" + modelId + "\\\",\\\"description\\\":{\\\"type\\\":\\\"offer\\\",\\\"sdp\\\":\\\"" + sdp + "\\\"},\\\"action\\\":\\\"join\\\"}\"}");
        }

        @Override
        public void onSetSuccess() {
            log("ON SDP SET SUCCESS");
        }

        @Override
        public void onCreateFailure(String s) {
            log("ON SDP CREATE FAILURE " + s);
        }

        @Override
        public void onSetFailure(String s) {
            log("ON SDP SET FAILURE " + s);
        }
    };

    PeerConnection.Observer peerConnectionObserver = new PeerConnection.Observer() {
        @Override
        public void onSignalingChange(PeerConnection.SignalingState signalingState) {
            log("onSignalingChange:" + signalingState.toString());
        }

        @Override
        public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
            log("onIceConnectionChange:" + iceConnectionState.toString());
        }

        @Override
        public void onIceConnectionReceivingChange(boolean b) {
            log("ON ICE CONNECTION RECEIVING CHANGE " + b);
        }

        @Override
        public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
            log("ON ICE GATHERING CHANGE");
        }

        @Override
        public void onIceCandidate(IceCandidate iceCandidate) {

            String candidate = "{\\\"candidate\\\":\\\"" + iceCandidate.sdp + "\\\", \\\"sdpMid\\\":\\\""
                    + iceCandidate.sdpMid + "\\\", \\\"sdpMLineIndex\\\":\\\"" + iceCandidate.sdpMLineIndex + "\\\"}";

            sendMessage("{\"command\":\"message\", \"identifier\":\"{\\\"channel\\\":\\\"LiveVideo::SessionChannel\\\"}\", \"data\":\"{\\\"candidate\\\":" + candidate + ",\\\"session\\\":\\\"" + liveVideoConfigurationResponse.getSession().getId() + "\\\",\\\"action\\\":\\\"candidates\\\"}\"}");
        }

        @Override
        public void onAddStream(MediaStream mediaStream) {
            mediaStream.videoTracks.getFirst().addRenderer(otherPeerRenderer);
            log("ADD REMOTE STREAM");
        }

        @Override
        public void onRemoveStream(MediaStream mediaStream) {
            mediaStream.videoTracks.getFirst().removeRenderer(otherPeerRenderer);
            log("REMOVE STREAM");
        }

        @Override
        public void onDataChannel(DataChannel dataChannel) {
            log("ON DATA CHANNEL");
        }

        @Override
        public void onRenegotiationNeeded() {
            log("ON RENEGOTIATION NEEDED");
        }
    };

    private void log(Exception e){
        if (e != null && e.getMessage() != null)
            Log.e("WEBRTC ERROR", e.getMessage());
    }

    private void log(String message){
        if (message != null)
            Log.e("WEBRTC", message);
    }

    private void hangup(){

        try {
            if (peerConnection != null && localMediaStream != null)
                peerConnection.removeStream(localMediaStream);
        }
        catch (Exception e){
            log(e);
        }

        try {
            if (localVideoSource != null) {
                localVideoSource.stop();
            }

        }
        catch (Exception e){
            log(e);
        }

        try {
            if (peerConnectionFactory != null) {
                peerConnectionFactory.stopAecDump();
            }
        }
        catch (Exception e){
            log(e);
        }

        try {
            if (peerConnection != null) {
                peerConnection.dispose();
                peerConnection = null;
            }
        }
        catch (Exception e){
            log(e);
        }


        try {
            if (peerConnectionFactory != null) {
                peerConnectionFactory.dispose();
                peerConnectionFactory = null;
            }
        }
        catch (Exception e){
            log(e);
        }

        try {
            PeerConnectionFactory.stopInternalTracingCapture();
            PeerConnectionFactory.shutdownInternalTracer();
        }
        catch (Exception e){
            log(e);
        }

        if (audioManager != null) {
            audioManager.close();
        }
    }

    @Override
    protected void onDestroy() {
        hangup();
        super.onDestroy();
    }

    void getLiveVideoConfiguration() {
        Map<String, String> liveVideoParams = new ArrayMap<>();
        liveVideoParams.put("model_master_id", String.valueOf(2));

        new LiveVideoConfigurationGetConnection(
                this,
                true,
                liveVideoParams,
                this::onLiveVideoConfigurationResponse,
                this::onLiveVideoConfigurationErrorResponse)
                .request();
    }

    private void onLiveVideoConfigurationErrorResponse(VolleyError volleyError) {
        Logger.log(volleyError);
        Toast.makeText(this, "Error en la conexión " + volleyError.getMessage(), Toast.LENGTH_SHORT).show();
        connectButton.setEnabled(false);
        switchAudioButton.setEnabled(false);
    }

    private void onLiveVideoConfigurationResponse(LiveVideoConfigurationResponse liveVideoConfigurationResponse) {
        this.liveVideoConfigurationResponse = liveVideoConfigurationResponse;
        connectButton.setEnabled(true);
        switchAudioButton.setEnabled(true);

        iceServers = new ArrayList<>();
        try {
            for (LiveVideoIceServer iceServer : liveVideoConfigurationResponse.getIceServers().getIceServers()){
                String username = iceServer.getUsername() == null ? "" : iceServer.getUsername();
                String password = iceServer.getCredential() == null ? "" : iceServer.getCredential();
                PeerConnection.IceServer peerConnectionIceServer = new PeerConnection.IceServer(iceServer.getUrl(), username, password);
                iceServers.add(peerConnectionIceServer);

                initSockets();
            }
        }
        catch (Exception e){
            Logger.log(e);
        }
    }


    private void initSockets() throws KeyStoreException, NoSuchAlgorithmException {
        client = new OkHttpClient.Builder()
                .retryOnConnectionFailure(false)
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .writeTimeout(0, TimeUnit.MILLISECONDS)
                .build();


        Request request = new Request.Builder().url(liveVideoConfigurationResponse.getSocketUrl()).build();
//        Request request = new Request.Builder().url(SIGNALING_URI).build();


        EchoWebSocketListener listener = new EchoWebSocketListener();
        ws = client.newWebSocket(request, listener);

        sendMessage("{\"command\":\"subscribe\",\"identifier\":\"{\\\"channel\\\":\\\"LiveVideo::SessionChannel\\\"}\"}");

        // Trigger shutdown of the dispatcher's executor so this process can exit cleanly.
        client.dispatcher().executorService().shutdown();
    }

    private void sendMessage(@NonNull String message){
        Logger.log("Sending: " + message);
        ws.send(message);
    }





    class EchoWebSocketListener extends WebSocketListener {
        private static final int NORMAL_CLOSURE_STATUS = 1000;

        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            Logger.log("Socket opened successfully!");
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            if (!text.contains("ping") && !text.contains("welcome"))
                Logger.log("Receiving : " + text);

            if (text.contains("confirm_subscription") && !connecting){
                onConnect();
            }

            if (text.contains("take_session")){
                Gson gson = new Gson();
                AnswerCall answerCall = gson.fromJson(text, AnswerCall.class);

                if (answerCall.getMessage().getSessionToken().equals(liveVideoConfigurationResponse.getSession().getToken())){
//                    SessionDescription sdp = new SessionDescription(SessionDescription.Type.ANSWER, obj.getString(SDP));
//                    peerConnection.setRemoteDescription(sdpObserver, sdp);
                    Logger.log("RECEIVED THE SAME SESSION TOKEN!!! " + answerCall.getMessage().getSessionToken());

                }
            }
        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            Logger.log("Receiving bytes : " + bytes.hex());
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            webSocket.close(NORMAL_CLOSURE_STATUS, null);
            Logger.log("Closing : " + code + " / " + reason);
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            Logger.log("Error : " + t.getMessage());
        }
    }
}