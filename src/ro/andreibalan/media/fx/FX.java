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

public class FX extends Audio {

    private int mSampleID;
    private boolean mIsLoaded = false;
    private int mStreamID;
    private int mLoopCount = 0;
    private float mRate = 1.0f;

    FX(final FXManager fxManager, int sampleID) {
        super(fxManager);
        mSampleID = sampleID;
    }

    public int getSampleID() {
        return mSampleID;
    }

    public void setLoaded(boolean loaded) {
        mIsLoaded = loaded;
    }

    public boolean isLoaded() {
        return mIsLoaded;
    }

    public void setRate(final float rate) {
        mRate = rate;
    }

    public float getRate() {
        return mRate;
    }

    @Override
    protected int getFocusType() {
        return android.media.AudioManager.AUDIOFOCUS_GAIN_TRANSIENT;
    }

    @Override
    protected void handleVolumeChange() {
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
            // Act as Resume from here.
            ((FXManager) getAudioManager()).getSoundPool().resume(mStreamID);
        } else {
            // Act as normal play.
            mStreamID = ((FXManager) getAudioManager()).getSoundPool().play(mSampleID, getVolume().getCalculatedLeftChannel(),
                    getVolume().getCalculatedRightChannel(), 1,
                    mLoopCount,
                    mRate);
        }

        super.play();
    }

    @Override
    public void stop() {
        if (this.mStreamID == 0)
            return;

        ((FXManager) getAudioManager()).getSoundPool().stop(mStreamID);
        super.stop();
    }

    @Override
    public void pause() {
        if (this.mStreamID == 0)
            return;

        ((FXManager) getAudioManager()).getSoundPool().pause(mStreamID);
        super.pause();

    }

    @Override
    public void release() {
        ((FXManager) getAudioManager()).remove(this);
    }

}
