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

import java.util.Arrays;
import java.util.concurrent.CopyOnWriteArrayList;

import ro.andreibalan.media.volume.Volume;
import ro.andreibalan.media.volume.Volume.OnVolumeChangeListener;
import android.content.Context;
import android.util.Log;

public abstract class AudioManager<T extends Audio> {

    public final static String TAG = AudioManager.class.getSimpleName();

    /**
     * Every Audio Instance will be added to this audio pool when it's created using the Factory Methods.
     * This way the AudioManager can keep track of every initialized instance.
     */
    private final CopyOnWriteArrayList<T> mAudioPool = new CopyOnWriteArrayList<T>();

    /**
     * The system audio manager that will be retrieved in the Constructor.
     */
    private android.media.AudioManager mSystemAudioManager;

    /**
     * Context
     */
    private Context mContext;

    /**
     * This is the current assign Volume instance to be used as master volume per AudioManager Instance.
     */
    private Volume mMasterVolume;

    /**
     * The current state of this Audio manager.
     * By default it is set to STOPPED.
     */
    private ManagerState mCurrentState = ManagerState.IDLE;

    /**
     *  Interface that will be implemented by Audio to be used when notifying the master volume change to all the Audio Instances.
     */
    public static interface OnMasterVolumeChange {

        /**
         * Will notify the audio instance that the master volume has change so it can offset it's current volume instance.
         */
        public void onMasterVolumeChange(final Volume volume);

    }

    /**
     * Volume change listener that will be attached to the current volume instance.
     */
    private OnVolumeChangeListener mMasterVolumeChangeListener = new Volume.OnVolumeChangeListener() {

        @Override
        public void onVolumeChange(float leftChannel, float rightChannel) {
            // When the master volume changes we notify all the OnMasterVolumeChange instances.
            notifyMasterVolumeChange();
        }

        @Override
        public void onBalanceChange(float balance) {

        }
    };

    /**
     * Type of Audio Output Devices.
     * This will reflect what output is currently used by the phone.
     */
    public enum AudioOutputDevice {
        A2DP,
        SPEAKERPHONE,
        HEADSET,
        SPEAKER
    }

    /**
     * The state of the Audio Manager.
     */
    public enum ManagerState {
        IDLE,
        STARTED,
        STOPPED
    }

    /**
     * This is the main constructor for the AudioManager but will be called for all the child constructors.
     */
    protected AudioManager(Context context) {
        Log.v(TAG, "Constructor");

        this.mContext = context;

        // Retrieve the Audio Manager from the system context.
        mSystemAudioManager = (android.media.AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

        // Set a new Volume Object. All to maximum.
        setMasterVolume(new Volume(1.0f, 1.0f));
    }

    protected android.media.AudioManager getSystemAudioManager() {
        Log.v(TAG, "getSystemAudioManager");
        return mSystemAudioManager;
    }

    /**
     * Adds an Audio Instance to the audio pool.
     */
    public boolean add(final T audio) {
        Log.v(TAG, "add: " + audio);

        if (!mAudioPool.contains(audio))
            return mAudioPool.add(audio);

        return false;
    }

    /**
     * Removes a current Audio Instance from the audio pool only if it's still there.
     */
    public boolean remove(final T audio) {
        Log.v(TAG, "remove: " + audio);
        if (mAudioPool.contains(audio))
            return mAudioPool.remove(audio);

        return false;
    }

    /**
     * Returns the current audio pool list.
     */
    public CopyOnWriteArrayList<T> getPool() {
        Log.v(TAG, "getPool");
        return mAudioPool;
    }

    public CopyOnWriteArrayList<T> getPool(Audio.State... state) {
        Log.v(TAG, "getPool: state: " + state);

        final CopyOnWriteArrayList<T> pool = new CopyOnWriteArrayList<T>();

        for (int i = mAudioPool.size() - 1; i >= 0; i--) {
            final T audioInstance = mAudioPool.get(i);
            if (Arrays.asList(state).contains(audioInstance.getState()))
                pool.add(audioInstance);
        }

        return pool;
    }

    /**
     * Replaces the current master Volume Instance.<br/>
     * Note that it should not be necessary to replace the volume instance as it is created in the constructor and can easily be manipulated be using {@link #getMasterVolume()}. 
     */
    public void setMasterVolume(final Volume volume) {
        Log.v(TAG, "setMasterVolume: " + volume);

        if (volume == null)
            throw new IllegalArgumentException("You cannot pass a null object to setVolume.");

        mMasterVolume = volume;
        mMasterVolume.addOnVolumeChangeListener(mMasterVolumeChangeListener);

        // Manually notify our listener because the instance has changed therefore the volume will most probably not be the same.
        mMasterVolumeChangeListener.onVolumeChange(mMasterVolume.getCalculatedLeftChannel(), mMasterVolume.getCalculatedRightChannel());
    }

    /**
     * Returns the master volume instance so you can easily change it channel volume or balance values.
     */
    public Volume getMasterVolume() {
        Log.v(TAG, "getMasterVolume");

        return mMasterVolume;
    }

    /**
     * Notifies every Audio instance in the pool starting with the last one added.<br/>
     * This should normally be called when the master volume has been changed and we get notified using the Volume.OnVolumeChangeListener.
     */
    private void notifyMasterVolumeChange() {
        Log.v(TAG, "notifyMasterVolumeChange");

        for (int i = mAudioPool.size() - 1; i >= 0; i--) {
            ((OnMasterVolumeChange) mAudioPool.get(i)).onMasterVolumeChange(mMasterVolume);
        }
    }

    /**
     * Returns the current state of the Audio Manager.
     */
    public ManagerState getState() {
        Log.v(TAG, "getState: " + mCurrentState.toString());

        return mCurrentState;
    }

    /**
     * Changes the state of the Audio Manager.
     */
    public void setState(final ManagerState state) {
        Log.v(TAG, "setState: " + state);

        mCurrentState = state;
    }

    /**
     * This will return the Audio Output device.
     */
    @SuppressWarnings("deprecation")
    public AudioOutputDevice getOutputDevice() {
        Log.v(TAG, "getOutputDevice");

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

    /**
     * Releases all the Audio Instances added to the AudioPool starting with the last one added.
     */
    public void releaseAll() {
        Log.v(TAG, "releaseAll: Releasing " + mAudioPool.size() + " Audio Instances");

        for (int i = mAudioPool.size() - 1; i >= 0; i--) {
            final T audio = mAudioPool.get(i);

            audio.stop();
            audio.release();
        }

        setState(ManagerState.IDLE);
    }

    /**
     * This is called when you want to start the AudioManager.
     */
    public void start() {
        Log.v(TAG, "start");
        setState(ManagerState.STARTED);
    }

    /**
     * This is called when you want to stop the Audio Manager.
     */
    public void stop() {
        Log.v(TAG, "stop");
        setState(ManagerState.STOPPED);
    }

}
