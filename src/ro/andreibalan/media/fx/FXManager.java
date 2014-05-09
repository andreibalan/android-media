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

import java.util.ArrayList;

import ro.andreibalan.media.AudioManager;
import ro.andreibalan.media.IAudio;
import ro.andreibalan.media.music.Music;

import android.content.Context;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.util.SparseArray;


public class FXManager extends AudioManager<FX> implements OnLoadCompleteListener {

    private final static int SOUND_STATUS_OK = 0;

    public final static int STREAMS = 5;
    private SoundPool mSoundPool;
    private final SparseArray<FX> mSoundMap = new SparseArray<FX>();

    public FXManager(Context context, final int maxSimultaneousStreams) {
        super(context);

        this.mSoundPool = new SoundPool(maxSimultaneousStreams, AudioManager.STREAM_TYPE, 0);
        this.mSoundPool.setOnLoadCompleteListener(this);
    }

    @Override
    public synchronized void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
        if (status == SOUND_STATUS_OK) {
            final FX fx = mSoundMap.get(sampleId);
            if (fx != null)
                fx.setLoaded(true);
        }
    }

    SoundPool getSoundPool() {
        return mSoundPool;
    }

    @Override
    public void add(final FX fx) {
        super.add(fx);

        mSoundMap.put(fx.getSampleID(), fx);
    }

    @Override
    public boolean remove(final FX fx) {
        final boolean removed = super.remove(fx);
        if (removed)
            this.mSoundMap.remove(fx.getSampleID());

        return removed;
    }

    @Override
    public void releaseAll() {
        super.releaseAll();

        // Release our own SoundPool
        mSoundPool.release();
    }

}
