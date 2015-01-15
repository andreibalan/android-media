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
package ro.andreibalan.media.music;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.util.Log;

public class MusicFactory {

    public final static String TAG = MusicFactory.class.getSimpleName();

    private MusicFactory() {
    }

    /**
     * Internal method used to create and add a Music Instance.
     * Mainly used to prevent code duplication all around the factory.
     * 
     * @param musicManager
     * @param mediaPlayer
     */
    private static Music create(final MusicManager musicManager, final MediaPlayer mediaPlayer) {
        Log.v(TAG, "create and add Music Instance");

        Music music = new Music(musicManager, mediaPlayer);
        musicManager.add(music);
        return music;
    }

    /**
     * Creates a Music Instance from asset files.<br/>
     * <b>NOTE: This will only load the AssetFileDescriptor from the given path and call on {@link #create(Context, MusicManager, AssetFileDescriptor)}}</b>
     * 
     * @param context - Application Context
     * @param musicManager - Loaded Music Manager
     * @param assetPath - Path of the Asset to load.
     * @return - Music Instance with the loaded asset.
     * 
     * @throws IOException
     */
    public static Music create(final Context context, final MusicManager musicManager, final String assetPath) throws IOException {
        synchronized (musicManager) {
            Log.v(TAG, "create assetPath: " + assetPath);

            final AssetFileDescriptor assetFileDescritor = context.getAssets().openFd(assetPath);
            return create(context, musicManager, assetFileDescritor);
        }
    };

    /**
     * Creates a Music Instance from the loaded AssetFileDescriptor.
     * 
     * @param context - Application Context
     * @param musicManager - Loaded Music Manager
     * @param assetFileDescritor - AssetFileDescriptor to load.
     * @return - Music Instance with the loaded asset.
     * 
     * @throws IOException
     */
    public static Music create(final Context context, final MusicManager musicManager, final AssetFileDescriptor assetFileDescritor) throws IOException {
        synchronized (musicManager) {
            Log.v(TAG, "create assetFileDescritor: " + assetFileDescritor.describeContents());

            final MediaPlayer mediaPlayer = new MediaPlayer();

            mediaPlayer.setDataSource(assetFileDescritor.getFileDescriptor(), assetFileDescritor.getStartOffset(), assetFileDescritor.getLength());
            mediaPlayer.prepare();

            return create(musicManager, mediaPlayer);
        }
    };

    /**
     * Creates a Music Instance from a file on the disk.
     * 
     * @param context - Application Context
     * @param musicManager - Loaded Music Manager
     * @param file - File on disk.
     * @return - Music Instance with the loaded audio file.
     * 
     * @throws IOException
     */
    @SuppressWarnings("resource")
    public static Music create(final Context context, final MusicManager musicManager, final File file) throws IOException {
        synchronized (musicManager) {
            Log.v(TAG, "create file: " + file.getAbsolutePath());

            final MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(new FileInputStream(file).getFD());
            mediaPlayer.prepare();

            return create(musicManager, mediaPlayer);
        }
    }

    /**
     * Creates a Music Instance from a resource.
     * 
     * @param context - Application Context
     * @param musicManager - Loaded Music Manager
     * @param rawResID - Internal Resource ID.
     * @return - Music Instance with the loaded resource.
     * 
     * @throws IOException
     */
    public static Music create(final Context context, final MusicManager musicManager, final int rawResID) {
        synchronized (musicManager) {
            Log.v(TAG, "create rawResID: " + rawResID);

            final MediaPlayer mediaPlayer = MediaPlayer.create(context, rawResID);
            return create(musicManager, mediaPlayer);
        }
    }

}
