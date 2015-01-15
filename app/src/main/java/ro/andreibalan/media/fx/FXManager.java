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

import ro.andreibalan.media.AudioManager;
import android.content.Context;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.util.Log;
import android.util.SparseArray;

public class FXManager extends AudioManager<FX> implements OnLoadCompleteListener {

    public final static String TAG = FXManager.class.getSimpleName();

    /**
     * This is the SoundPool where we will load all the audio FX Instances.
     * We create this SoundPool in the constructor.
     */
    private SoundPool mSoundPool;

    /**
     * Sparse Array of FX Instances.
     */
    private final SparseArray<FX> mSoundMap = new SparseArray<FX>();

    /**
     * Default constructor for the FXManager.<br/><br/>
     * 
     * @param context - Application Context
     * @param maxSimultaneousStreams - Number of simultaneous playback streams or audio instances.
     */
    public FXManager(Context context, final int maxSimultaneousStreams) {
        super(context);
        Log.v(TAG, "Constructor: maxSimultaneousStreams: " + maxSimultaneousStreams);

        this.mSoundPool = new SoundPool(maxSimultaneousStreams, android.media.AudioManager.STREAM_MUSIC, 0);
        this.mSoundPool.setOnLoadCompleteListener(this);
    }

    @Override
    public synchronized void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
        if (status == 0) {
            final FX fx = mSoundMap.get(sampleId);
            if (fx != null)
                fx.setLoaded(true);
        }
    }

    /**
     * Returns the loaded SoundPool
     */
    protected SoundPool getSoundPool() {
        Log.v(TAG, "getSoundPool");
        return mSoundPool;
    }

    @Override
    public boolean add(final FX fx) {
        Log.v(TAG, "add: " + fx);

        // Before adding this to the audio pool we also add it to our own SparseArray
        final boolean added = super.add(fx);
        mSoundMap.put(fx.getSampleID(), fx);

        return added;
    }

    @Override
    public boolean remove(final FX fx) {
        Log.v(TAG, "remove: " + fx);

        final boolean removed = super.remove(fx);
        if (removed)
            this.mSoundMap.remove(fx.getSampleID());

        return removed;
    }

    @Override
    public void releaseAll() {
        Log.v(TAG, "releaseAll");

        super.releaseAll();

        // Release our own SoundPool
        mSoundPool.release();
    }

}
