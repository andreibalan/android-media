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

import android.content.Context;
import android.media.MediaPlayer;

public class MusicFactory {

    private MusicFactory() {
    }

    public static Music create(final Context context, final MusicManager musicManager, final String path) {
        synchronized (musicManager) {
            return null;
        }
    };

    public static Music create(final Context context, final MusicManager musicManager, final File file) {
        synchronized (musicManager) {
            return null;
        }
    }

    public static Music create(final Context context, final MusicManager musicManager, final int rawResID) {
        synchronized (musicManager) {
            final MediaPlayer mediaPlayer = MediaPlayer.create(context, rawResID);
            Music music = new Music(musicManager, mediaPlayer);

            musicManager.add(music);
            return music;
        }
    }

}
