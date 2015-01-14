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
package ro.andreibalan.media.fx;

import ro.andreibalan.media.Audio;
import android.util.Log;

public class FX extends Audio {

    public final static String TAG = FX.class.getSimpleName();

    /**
     * This is where we store the current FX's sample ID returned from the System Sound Pool.
     * We can only reference the audio by this ID.
     */
    private int mSampleID;

    /**
     * Reflects the Audio FX current state: Loaded or UnLoaded.
     * This is because all Audio FX are loaded async through the audio pool.
     */
    private boolean mIsLoaded = false;

    /**
     * When we start playback we will get a stream if from the Audio Pool and we store it here for referencing.
     */
    private int mStreamID;

    /**
     * Audio FX Playback rate. Range between 0.5f and 2.0f
     * Default playback is 1.0f
     */
    private float mRate = 1.0f;

    /**
     * This is a protected constructor and will only be instanced from the FXFactory
     * 
     * @param fxManager - FXManager Instance
     * @param sampleID - ID for the Sample Loaded using the Audio Pool.
     */
    protected FX(final FXManager fxManager, int sampleID) {
        super(fxManager);
        Log.v(TAG, "Constructor: sampleID: " + sampleID);
        mSampleID = sampleID;
    }

    /**
     * Returns the sample ID from the Audio Pool
     */
    public int getSampleID() {
        Log.v(TAG, "getSampleID: " + mSampleID);
        return mSampleID;
    }

    /**
     * This will be called by the FXManager when the Audio FX has been loaded and ready to play.
     */
    protected void setLoaded(boolean loaded) {
        Log.v(TAG, "setLoaded: " + loaded);
        mIsLoaded = loaded;
    }

    /**
     * Simply checks if the current Audio FX Instance is loaded and ready for playback.
     */
    protected boolean isLoaded() {
        Log.v(TAG, "isLoaded: " + mIsLoaded);
        return mIsLoaded;
    }

    /**
     * Changes the rate of the AudioFX playback.
     * <br/><br/>
     * Default is 1.0f - Range is between 0.5f - 2.0f.
     */
    public void setRate(final float rate) {
        Log.v(TAG, "setRate: " + rate);

        // First check if the argument rate is within the correct parameters.
        if (rate < -0.5f || rate > 2.0f)
            throw new IllegalArgumentException("FX Audio Rate Range is between 0.5f and 1.0f.");

        mRate = rate;
    }

    /**
     * Returns the Rate Value for the playback speed of the Audio FX Instance.
     */
    public float getRate() {
        Log.v(TAG, "getRate: " + mRate);
        return mRate;
    }

    @Override
    protected int getFocusType() {
        return android.media.AudioManager.AUDIOFOCUS_GAIN_TRANSIENT;
    }

    @Override
    protected void handleVolumeChange() {
        super.handleVolumeChange();

        if (this.mStreamID != 0)
            ((FXManager) getAudioManager()).getSoundPool().setVolume(this.mStreamID, getVolume().getCalculatedLeftChannel(),
                    getVolume().getCalculatedRightChannel());

    }

    @Override
    protected void handleStateChange(State state) {

    }

    @Override
    public void play() {
        if (mStreamID != 0 && isPaused()) {
            Log.v(TAG, "resume");

            // Act as Resume from here.
            ((FXManager) getAudioManager()).getSoundPool().resume(mStreamID);
        } else {
            Log.v(TAG, "play");

            // Act as normal play.
            mStreamID = ((FXManager) getAudioManager()).getSoundPool().play(mSampleID, getVolume().getCalculatedLeftChannel(),
                    getVolume().getCalculatedRightChannel(), 1, 0, mRate);
        }

        super.play();
    }

    @Override
    public void stop() {
        Log.v(TAG, "stop");
        if (this.mStreamID == 0)
            return;

        ((FXManager) getAudioManager()).getSoundPool().stop(mStreamID);
        super.stop();
    }

    @Override
    public void pause() {
        Log.v(TAG, "pause");
        if (this.mStreamID == 0)
            return;

        ((FXManager) getAudioManager()).getSoundPool().pause(mStreamID);
        super.pause();

    }

    @Override
    public void release() {
        Log.v(TAG, "release");
        ((FXManager) getAudioManager()).remove(this);
    }

}
