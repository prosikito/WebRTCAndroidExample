package com.cgrange.webrtcexample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.util.Log;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by cgrange on 5/05/17.
 *
 */

public class AppRTCAudioManager {
    private static final String TAG = "AppRTCAudioManager";
    private final Context apprtcContext;
    private AppRTCAudioManager.OnAudioManagerStateListener onAudioStateChangeListener;
    private AppRTCAudioManager.OnWiredHeadsetStateListener wiredHeadsetStateListener;
    private boolean initialized = false;
    private AudioManager audioManager;
    private int savedAudioMode = -2;
    private boolean savedIsSpeakerPhoneOn = false;
    private boolean savedIsMicrophoneMute = false;
    private AppRTCAudioManager.AudioDevice defaultAudioDevice;
    private AppRTCAudioManager.AudioDevice selectedAudioDevice;
    private final Set<AudioDevice> audioDevices;
    private BroadcastReceiver wiredHeadsetReceiver;
    private boolean isRegisteredWiredHeadsetReceiver;
    private boolean useHeadset;

    public static AppRTCAudioManager create(Context context, AppRTCAudioManager.OnAudioManagerStateListener deviceStateChangeListener) {
        return new AppRTCAudioManager(context, deviceStateChangeListener);
    }

    public void setDefaultAudioDevice(AppRTCAudioManager.AudioDevice defaultAudioDevice) {
        this.defaultAudioDevice = defaultAudioDevice;
    }

    private AppRTCAudioManager(Context context, AppRTCAudioManager.OnAudioManagerStateListener deviceStateChangeListener) {
        this.defaultAudioDevice = AppRTCAudioManager.AudioDevice.SPEAKER_PHONE;
        this.audioDevices = new HashSet();
        this.useHeadset = true;
        this.apprtcContext = context.getApplicationContext();
        this.onAudioStateChangeListener = deviceStateChangeListener;
        this.audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
    }

    public void init() {
        if(!this.initialized) {
            this.savedAudioMode = this.audioManager.getMode();
            this.savedIsSpeakerPhoneOn = this.audioManager.isSpeakerphoneOn();
            this.savedIsMicrophoneMute = this.audioManager.isMicrophoneMute();
            this.audioManager.requestAudioFocus((AudioManager.OnAudioFocusChangeListener)null, 0, 2);
            this.audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            this.setMicrophoneMute(false);
            this.updateAudioDeviceState(this.hasWiredHeadset());
            this.registerForWiredHeadsetIntentBroadcast();
            this.initialized = true;
        }
    }

    public void close() {
        if(this.initialized) {
            this.unregisterForWiredHeadsetIntentBroadcast();
            this.setSpeakerphoneOn(this.savedIsSpeakerPhoneOn);
            this.setMicrophoneMute(this.savedIsMicrophoneMute);
            this.audioManager.setMode(this.savedAudioMode);
            this.audioManager.abandonAudioFocus((AudioManager.OnAudioFocusChangeListener)null);
            this.initialized = false;
        }
    }

    public void setManageHeadsetByDefault(boolean manage) {
        this.useHeadset = manage;
    }

    public void setOnWiredHeadsetStateListener(AppRTCAudioManager.OnWiredHeadsetStateListener wiredHeadsetStateListener) {
        this.wiredHeadsetStateListener = wiredHeadsetStateListener;
    }

    public void setOnAudioManagerStateListener(AppRTCAudioManager.OnAudioManagerStateListener onAudioManagerStateListener) {
        this.onAudioStateChangeListener = onAudioManagerStateListener;
    }

    public void setAudioDevice(AppRTCAudioManager.AudioDevice device) {
        if(!this.audioDevices.contains(device)) {
            Log.e("AppRTCAudioManager", "Device doesn\'t nave " + device);
        } else if(device != this.selectedAudioDevice) {
            switch(device) {
                case SPEAKER_PHONE:
                    this.setSpeakerphoneOn(true);
                    this.selectedAudioDevice = AppRTCAudioManager.AudioDevice.SPEAKER_PHONE;
                    break;
                case EARPIECE:
                    this.setSpeakerphoneOn(false);
                    this.selectedAudioDevice = AppRTCAudioManager.AudioDevice.EARPIECE;
                    break;
                case WIRED_HEADSET:
                    this.setSpeakerphoneOn(false);
                    this.selectedAudioDevice = AppRTCAudioManager.AudioDevice.WIRED_HEADSET;
                    break;
                default:
                    Log.e("AppRTCAudioManager", "Invalid audio device selection");
            }

            Log.e("AppRTCAudioManager", "selectedAudioDevice = " + this.selectedAudioDevice);
            this.onAudioManagerChangedState(this.selectedAudioDevice);
        }
    }

    public Set<AppRTCAudioManager.AudioDevice> getAudioDevices() {
        return Collections.unmodifiableSet(new HashSet(this.audioDevices));
    }

    public AppRTCAudioManager.AudioDevice getSelectedAudioDevice() {
        return this.selectedAudioDevice;
    }

    public AppRTCAudioManager.AudioDevice getDefaultAudioDevice() {
        return this.defaultAudioDevice;
    }

    private void registerForWiredHeadsetIntentBroadcast() {
        IntentFilter filter = new IntentFilter("android.intent.action.HEADSET_PLUG");
        this.wiredHeadsetReceiver = new BroadcastReceiver() {
            private static final int STATE_UNPLUGGED = 0;
            private static final int STATE_PLUGGED = 1;
            private static final int HAS_NO_MIC = 0;
            private static final int HAS_MIC = 1;

            public void onReceive(Context context, Intent intent) {
                int state = intent.getIntExtra("state", 0);
                int microphone = intent.getIntExtra("microphone", 0);
                Log.d("AppRTCAudioManager", "wiredHeadsetReceiver state:" + state + " has mic:" + microphone);
                String name = intent.getStringExtra("name");
                Log.d("AppRTCAudioManager", "BroadcastReceiver.onReceive: " + "a=" + intent.getAction() + ", s=" + (state == 0?"unplugged":"plugged") + ", m=" + (microphone == 1?"mic":"no mic") + ", n=" + name + ", sb=" + this.isInitialStickyBroadcast());
                boolean hasWiredHeadset = state == 1;
                AppRTCAudioManager.this.notifyWiredHeadsetListener(hasWiredHeadset, microphone == 1);
                if(AppRTCAudioManager.this.useHeadset) {
                    switch(state) {
                        case 0:
                            Log.d("AppRTCAudioManager", "Start. updateAudioDeviceState. wiredHeadsetReceiver state:" + state + " has mic:" + microphone);
                            AppRTCAudioManager.this.updateAudioDeviceState(hasWiredHeadset);
                            break;
                        case 1:
                            Log.d("AppRTCAudioManager", "Start. updateAudioDeviceState. wiredHeadsetReceiver state:" + state + " has mic:" + microphone);
                            if(AppRTCAudioManager.this.selectedAudioDevice != AppRTCAudioManager.AudioDevice.WIRED_HEADSET) {
                                AppRTCAudioManager.this.updateAudioDeviceState(hasWiredHeadset);
                            }
                            break;
                        default:
                            Log.e("AppRTCAudioManager", "Invalid state");
                    }

                }
            }
        };
        this.apprtcContext.registerReceiver(this.wiredHeadsetReceiver, filter);
        this.isRegisteredWiredHeadsetReceiver = true;
    }

    private void notifyWiredHeadsetListener(boolean plugged, boolean hasMicrophone) {
        if(this.wiredHeadsetStateListener != null) {
            this.wiredHeadsetStateListener.onWiredHeadsetStateChanged(plugged, hasMicrophone);
        }

    }

    private void unregisterForWiredHeadsetIntentBroadcast() {
        if(this.isRegisteredWiredHeadsetReceiver && this.wiredHeadsetReceiver != null) {
            this.apprtcContext.unregisterReceiver(this.wiredHeadsetReceiver);
            this.isRegisteredWiredHeadsetReceiver = false;
        }

        this.wiredHeadsetReceiver = null;
    }

    private void setSpeakerphoneOn(boolean on) {
        boolean wasOn = this.audioManager.isSpeakerphoneOn();
        if(wasOn != on) {
            this.audioManager.setSpeakerphoneOn(on);
        }
    }

    private void setMicrophoneMute(boolean on) {
        boolean wasMuted = this.audioManager.isMicrophoneMute();
        if(wasMuted != on) {
            this.audioManager.setMicrophoneMute(on);
        }
    }

    private boolean hasEarpiece() {
        return this.apprtcContext.getPackageManager().hasSystemFeature("android.hardware.telephony");
    }

    /** @deprecated */
    @Deprecated
    private boolean hasWiredHeadset() {
        return this.audioManager.isWiredHeadsetOn();
    }

    private void updateAudioDeviceState(boolean hasWiredHeadset) {
        this.audioDevices.clear();
        if(hasWiredHeadset) {
            this.audioDevices.add(AppRTCAudioManager.AudioDevice.WIRED_HEADSET);
        } else {
            this.audioDevices.add(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE);
            if(this.hasEarpiece()) {
                this.audioDevices.add(AppRTCAudioManager.AudioDevice.EARPIECE);
            }
        }

        Log.d("AppRTCAudioManager", "audioDevices: " + this.audioDevices);
        if(hasWiredHeadset) {
            this.setAudioDevice(AppRTCAudioManager.AudioDevice.WIRED_HEADSET);
        } else {
            this.setAudioDevice(this.defaultAudioDevice);
        }

    }

    private void onAudioManagerChangedState(AppRTCAudioManager.AudioDevice audioDevice) {
        Log.d("AppRTCAudioManager", "onAudioManagerChangedState: devices=" + this.audioDevices + ", selected=" + this.selectedAudioDevice);
        if(this.onAudioStateChangeListener != null) {
            this.onAudioStateChangeListener.onAudioChangedState(audioDevice);
        }

    }

    public interface OnAudioManagerStateListener {
        void onAudioChangedState(AppRTCAudioManager.AudioDevice var1);
    }

    public interface OnWiredHeadsetStateListener {
        void onWiredHeadsetStateChanged(boolean var1, boolean var2);
    }

    public static enum AudioDevice {
        SPEAKER_PHONE,
        WIRED_HEADSET,
        EARPIECE;

        private AudioDevice() {
        }
    }
}

