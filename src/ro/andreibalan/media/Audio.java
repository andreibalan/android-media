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

import ro.andreibalan.media.volume.Volume;
import ro.andreibalan.media.volume.Volume.OnVolumeChangeListener;

public abstract class Audio implements AudioManager.OnMasterVolumeChange {

    public final static String TAG = Audio.class.getSimpleName();

    /**
     * An instance of Audio Manager child class that extends our own Audio class.
     */
    private AudioManager<? extends Audio> mAudioManager;

    /**
     * This is the current state of the Audio Instance. 
     * Default this will be set to STOPPED.
     */
    private State mState = State.STOPPED;

    /**
     * The Volume instance that the user can use to manipulate channel volume and balance value.
     * This cannot ever be null and a new Volume instance will be set in the constructor.
     */
    private Volume mVolume;

    /** 
     * This represents all the states of an Audio Instance.
     */
    public enum State {

        /**
         * This is set when the playing has started.
         */
        PLAYING,

        /**
         * This is set when the Audio Instance is not playing or in pause mode.
         */
        STOPPED,

        /**
         * This is set when the Audio Instance is set to pause mode.
         */
        PAUSED
    }

    /**
     * An instance of the {@link OnVolumeChangeListener} that will be attached to the current Volume Instance.
     * If the Volume instance is changed that this listener will be attached to the new changed instance.
     * 
     * For the moment it only needs to listen to onVolumeChange evets so it can call handleVolumeChange method to call 
     * the child classes so they can manage the volume change.
     */
    private OnVolumeChangeListener mVolumeChangeListener = new OnVolumeChangeListener() {

        @Override
        public void onVolumeChange(float leftChannel, float rightChannel) {
            // Call handleVolumeChange on child classes to let them do the change they need.
            handleVolumeChange();
        }

        @Override
        public void onBalanceChange(float balance) {
        }
    };

    /**
     * This is the main constructor. Must always be called from child constructors so it can set the AudioManager Instance.
     * @param audioManager
     */
    protected Audio(final AudioManager<? extends Audio> audioManager) {
        mAudioManager = audioManager;

        // Set a new Volume Object. All to maximum but we take into consideration the master volume.
        Volume volume = new Volume(1.0f, 1.0f);
        volume.setChannelOffset(audioManager.getMasterVolume().getCalculatedChannel());
        setVolume(volume);
    }

    /**
     * Returns the AudioManager instance of this class internally to child classes.
     */
    protected AudioManager<? extends Audio> getAudioManager() {
        return mAudioManager;
    }

    /**
     * This should be called from the child class only when they are sure that the audio has started playing.<br/>
     * It changes the current state to PLAYING.
     */
    public void play() {
        setState(mState = State.PLAYING);
    }

    /**
     * This should be called from the child class only when tey are sure that the audio has stopped playing.<br/>
     * It changes the current state to STOPPED.
     */
    public void stop() {
        setState(State.STOPPED);
    }

    /**
     * This should be called from the child class only when they are sure that the audio has been set to pause state.
     * It changes the current state to PAUSED.
     */
    public void pause() {
        setState(State.PAUSED);
    }

    /**
     * Every child class should return the focus type they need using this method.
     * @return
     */
    protected abstract int getFocusType();

    /**
     * Implemented by child classes this is used to be notified when the volume values have changed so you can 
     * control your media player.
     */
    protected void handleVolumeChange() {
        // If the volume has just changed to mute and we are not in stopped state we immediately stop. Also releasing audio focus.
        if(getVolume().isMuted() && !isStopped())
            stop();
    }

    /**
     * Implemented by child classes this is used to be notified when the current state of the Audio Instance has changed.
     */
    protected abstract void handleStateChange(State state);

    /**
     * Implemented by child classes this will be publicly called when the application needs to release the current instances.
     */
    public abstract void release();

    /**
     * Internally returns the current state of the Audio instance.
     */
    protected State getState() {
        return mState;
    }

    /**
     * Internally sets the set of the Audio Instance.
     * <br/>
     * This will also trigger handleStateChange();
     */
    protected void setState(final State state) {
        mState = state;
        handleStateChange(state);
    }

    /**
     * Return true if the current Audio Instance is playing and can be heard.
     * @return
     */
    public boolean isPlaying() {
        return (mState == State.PLAYING);
    }

    /**
     * Return true if the current Audio Instance is in Paused mode.
     */
    public boolean isPaused() {
        return (mState == State.PAUSED);
    }

    /**
     * Returns true if the current Audio Instance is Stopped and cannot be heard.
     */
    public boolean isStopped() {
        return (mState == State.STOPPED);
    }

    /**
     * Returns the Volume Instance so that the user can use it's public methods to control the volume directly.
     * @see Volume
     */
    public Volume getVolume() {
        return mVolume;
    }

    /**
     * Changes the current volume instance with another one and attaches the listener to it.<br/>
     * After the new listener has been set a manual call to {@link #handleVolumeChange()} will be called so we can let the child classes know of a volume change.
     * <br/><br/>
     * <b>NOTE: It should not be necessary to change the Volume object because the new volume object will do the same thing. 
     * <br/>
     * Instead just use {@link #getVolume()} to get the current Volume Instance and modify it.}</b>
     * 
     * @param volume - Volume Instance
     */
    public void setVolume(final Volume volume) {
        if (volume == null)
            throw new IllegalArgumentException("You cannot pass a null object to setVolume.");

        mVolume = volume;
        mVolume.addOnVolumeChangeListener(mVolumeChangeListener);

        // Manually notify our listener because the instance has changed therefore the volume will most probably not be the same.
        mVolumeChangeListener.onVolumeChange(mVolume.getCalculatedLeftChannel(), mVolume.getCalculatedRightChannel());
    }

    @Override
    public void onMasterVolumeChange(final Volume volume) {
        if (mVolume != null)
            mVolume.setChannelOffset(volume.getCalculatedChannel());
    }

}
