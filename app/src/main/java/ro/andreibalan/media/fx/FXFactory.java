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

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.util.Log;

public class FXFactory {

    public final static String TAG = FXFactory.class.getSimpleName();

    private FXFactory() {
    }

    /**
     * Internal method used to create and add a FX Instance.
     * Mainly used to prevent code duplication all around the factory.
     * 
     * @param musicManager
     * @param sampleID
     */
    private static FX create(final FXManager fxManager, final int sampleID) {
        Log.v(TAG, "create and add FX Instance");
        final FX fx = new FX(fxManager, sampleID);

        fxManager.add(fx);
        return fx;
    }

    /**
     * Creates a FX Instance from asset files.<br/>
     * <b>NOTE: This will only load the AssetFileDescriptor from the given path and call on {@link #create(Context, FXManager, AssetFileDescriptor)}}</b>
     * 
     * @param context - Application Context
     * @param fxManager - Loaded FX Manager
     * @param assetPath - Path of the Asset to load.
     * @return - FX Instance with the loaded asset.
     * 
     * @throws IOException
     */
    public static FX create(final Context context, final FXManager fxManager, final String assetPath) throws IOException {
        synchronized (fxManager) {
            Log.v(TAG, "create assetPath: " + assetPath);

            final AssetFileDescriptor assetFileDescritor = context.getAssets().openFd(assetPath);
            return create(context, fxManager, assetFileDescritor);
        }
    };

    /**
     * Creates a FX Instance from AssetFileDescriptor
     * 
     * @param context - Application Context
     * @param fxManager - Loaded FX Manager
     * @param assetFileDescriptor - AssetFileDescriptor from loaded asset
     * @return - FX Instance with the loaded asset.
     * 
     * @throws IOException
     */
    public static FX create(final Context context, final FXManager fxManager, final AssetFileDescriptor assetFileDescriptor) throws IOException {
        synchronized (fxManager) {
            Log.v(TAG, "create assetFileDescriptor: " + assetFileDescriptor.describeContents());

            final int sampleID = fxManager.getSoundPool().load(assetFileDescriptor, 1);
            return create(fxManager, sampleID);
        }
    };

    /**
     * Creates a FX Instance from File on the disk.
     * 
     * @param context - Application Context
     * @param fxManager - Loaded FX Manager
     * @param file - File
     * @return - FX Instance with the loaded file.
     * 
     * @throws IOException
     */
    public static FX create(final Context context, final FXManager fxManager, final File file) throws IOException {
        synchronized (fxManager) {
            Log.v(TAG, "create file: " + file.getAbsolutePath());

            final int sampleID = fxManager.getSoundPool().load(file.getAbsolutePath(), 1);
            return create(fxManager, sampleID);
        }
    }

    /**
     * Creates a FX Instance from a resource.
     * 
     * @param context - Application Context
     * @param fxManager - Loaded FX Manager
     * @param rawResID - internal resource id 
     * @return - FX Instance with the loaded file.
     * 
     * @throws IOException
     */
    public static FX create(final Context context, final FXManager fxManager, final int rawResID) {
        synchronized (fxManager) {
            Log.v(TAG, "create rawResID: " + rawResID);

            final int sampleID = fxManager.getSoundPool().load(context, rawResID, 1);
            return create(fxManager, sampleID);
        }
    }

}
