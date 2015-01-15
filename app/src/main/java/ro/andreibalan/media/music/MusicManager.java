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

import java.util.concurrent.CopyOnWriteArrayList;

import ro.andreibalan.media.Audio.State;
import ro.andreibalan.media.AudioManager;
import android.content.Context;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.util.Log;

public class MusicManager extends AudioManager<Music> {

    public final static String TAG = MusicManager.class.getSimpleName();

    /**
     * Audio Focus Change Listener that is registered every time the Music Instance requests a focus of playing back audio.
     * <br/><br/>
     * Will handle audio focus gain and loss, transient and duck so we can manage the volumes or playback from here.
     */
    private final OnAudioFocusChangeListener mAudioFocusChangeListener = new OnAudioFocusChangeListener() {

        @Override
        public void onAudioFocusChange(int focusChange) {

            switch (focusChange) {
                case android.media.AudioManager.AUDIOFOCUS_GAIN:
                    getMasterVolume().raiseChannels();

                    changeMusicState(State.PAUSED, State.PLAYING);
                    break;

                case android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    changeMusicState(State.PLAYING, State.PAUSED);
                    break;

                case android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    getMasterVolume().lowerChannels();
                    break;

                case android.media.AudioManager.AUDIOFOCUS_LOSS:
                    changeMusicState(new State[]{
                            State.PAUSED, State.PLAYING
                    }, State.STOPPED);
                    break;
            }
        }
    };

    /**
     * Default Constructor for Music Manager.
     */
    public MusicManager(Context context) {
        super(context);
        Log.v(TAG, "Construct");
    }

    private void changeMusicState(final State from, final State to) {
        changeMusicState(new State[]{
                from
        }, to);
    }

    private void changeMusicState(final State[] from, final State to) {
        Log.v(TAG, "changeState from: " + from.toString() + ", to: " + to.toString());

        CopyOnWriteArrayList<Music> music = getPool(from);
        for (Music musicInstance : music) {
            musicInstance.disableCrossfade();
            switch (to) {
                case STOPPED:
                    musicInstance.stop();
                    break;

                case PLAYING:
                    musicInstance.play();
                    break;

                case PAUSED:
                    musicInstance.pause();
                    break;
            }
        }
    }

    /**
     * Requests focus for starting music playback from Music Instance.<br/>
     * It returns the status code of the request.
     */
    protected int requestFocus(int streamType, int audioFocusType) {
        Log.v(TAG, "requestFocus: streamType: " + streamType + ", audioFocusType: " + audioFocusType);

        if (getSystemAudioManager() != null)
            // Request audio focus for playback.
            return getSystemAudioManager().requestAudioFocus(mAudioFocusChangeListener, streamType, audioFocusType);

        return android.media.AudioManager.AUDIOFOCUS_REQUEST_FAILED;
    }

    /**
     * Explicitly abandons the requested audio focus.
     */
    protected int abandonFocus() {
        Log.v(TAG, "abandonFocus");

        return getSystemAudioManager().abandonAudioFocus(mAudioFocusChangeListener);
    }

    @Override
    public void start() {
        // If this audio manager has previously been stopped we resume all Music Playback.
        if (getState() == ManagerState.STOPPED)
            changeMusicState(State.PAUSED, State.PLAYING);

        super.start();
    }

    @Override
    public void stop() {
        // We pause all music playback for the moment until this manager will be started again.
        changeMusicState(State.PLAYING, State.PAUSED);

        super.stop();
    }

}
