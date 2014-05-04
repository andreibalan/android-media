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

public abstract class Audio implements IAudio {

    private AudioManager<? extends IAudio> mAudioManager;


    protected float mVolumeLeft = 1.0f;
    protected float mVolumeRight = 1.0f;


    protected Audio(final AudioManager<? extends IAudio> audioManager) {
        mAudioManager = audioManager;
    }

    protected AudioManager<? extends IAudio> getAudioManager() {
        return mAudioManager;
    }

    protected abstract int getFocusType();

    protected abstract void handleVolumeChange();

    protected float getActualVolume(final float volume) {
        return volume * getAudioManager().getMasterVolume();
    }

    @Override
    public float getVolume() {
        return (mVolumeLeft + mVolumeRight) * 0.5f;
    }

    @Override
    public float getLeftVolume() {
        return mVolumeLeft;
    }

    @Override
    public float getRightVolume() {
        return mVolumeRight;
    }

    @Override
    public void setVolume(float volume) {
        setVolume(volume, volume);
    }

    @Override
    public void setVolume(float leftVolume, float rightVolume) {
        mVolumeLeft = leftVolume;
        mVolumeRight = rightVolume;

        handleVolumeChange();
    }

    @Override
    public void onMasterVolumeChanged(float masterVolume) {
        handleVolumeChange();
    }

}
