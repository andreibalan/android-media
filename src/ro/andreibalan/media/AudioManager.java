/**
 * Android Media Library, an media library for the android platform.
 * 
 * Copyright (C) 2014 Andrei Balan
 * 
 * This file is part of Android Media Library
 * 
 * Android Media Library is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 * 
 * Android Media Libraryis distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * 
 * See the GNU General Public License for more details. You should have received a copy of the GNU
 * General Public License along with Android Media Library. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Authors: Andrei Balan
 */
package ro.andreibalan.media;

import java.util.ArrayList;

import android.content.Context;

public abstract class AudioManager<T extends Audio> {

    public final static int STREAM_TYPE = android.media.AudioManager.STREAM_MUSIC;

    private final static float AUDIO_DUCK_VOLUME = 0.2f;

    protected final ArrayList<T> mAudioPool = new ArrayList<T>();
    protected float mMasterVolume = 1.0f;
    private Float mOriginalVolume = null;
    private Context mContext;
    protected android.media.AudioManager mSystemAudioManager;

    public enum AudioOutputDevice {
        A2DP,
        SPEAKERPHONE,
        HEADSET,
        SPEAKER
    }

    protected AudioManager(Context context) {
        this.mContext = context;

        mSystemAudioManager = (android.media.AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
    }

    public ArrayList<T> getPool() {
        final ArrayList<T> audioPool = mAudioPool;
        return audioPool;
    }

    private void notifyMasterVolumeChange() {
        final ArrayList<T> audioPool = mAudioPool;

        for (int i = audioPool.size() - 1; i >= 0; i--) {
            final T audio = audioPool.get(i);
            audio.onMasterVolumeChanged(mMasterVolume);
        }
    }

    @SuppressWarnings("deprecation")
    public AudioOutputDevice getOutputDevice() {
        // Query for Bluetooth A2DP
        if (mSystemAudioManager.isBluetoothA2dpOn())
            return AudioOutputDevice.A2DP;

        // Query for SpeakerPhone
        if (mSystemAudioManager.isSpeakerphoneOn())
            return AudioOutputDevice.SPEAKERPHONE;

        // Query for Wired Headset
        if (mSystemAudioManager.isWiredHeadsetOn())
            return AudioOutputDevice.HEADSET;

        // Return Default Device Output (Device Speaker)
        return AudioOutputDevice.SPEAKER;
    }

    protected boolean isDucked() {
        return (mOriginalVolume != null);
    }

    protected void duckVolume() {
        if (mMasterVolume > AUDIO_DUCK_VOLUME && !isDucked()) {
            final float currentVolume = mMasterVolume;
            mOriginalVolume = currentVolume;
            setMasterVolume(AUDIO_DUCK_VOLUME);
        }
    }

    protected void raiseVolume() {
        if (isDucked()) {
            setMasterVolume((float) mOriginalVolume);
            mOriginalVolume = null;
        }
    }

    /**
     * Abstract Methods
     */
    public float getMasterVolume() {
        return mMasterVolume;
    }

    public void setMasterVolume(final float masterVolume) {
        mMasterVolume = masterVolume;
        notifyMasterVolumeChange();
    }

    public void add(final T audio) {
        mAudioPool.add(audio);
    }

    public boolean remove(final T audio) {
        if (mAudioPool.contains(audio))
            return mAudioPool.remove(audio);

        return false;
    }

    public void releaseAll() {
        final ArrayList<T> audioPool = mAudioPool;

        for (int i = audioPool.size() - 1; i >= 0; i--) {
            final T audio = audioPool.get(i);

            audio.stop();
            audio.release();
        }
    }

}
