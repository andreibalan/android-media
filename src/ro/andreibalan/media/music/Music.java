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
import ro.andreibalan.media.Audio;
import ro.andreibalan.media.volume.Volume;
import android.media.MediaPlayer;
import android.os.Handler;

public class Music extends Audio {

    public final static String TAG = Music.class.getSimpleName();

    public final static int CROSSFADE_DURATION = 2000;

    private boolean mIsLooping = false;
    private int mCrossfadeDuration = 0;
    private MediaPlayer mMediaPlayer;
    
    private Handler mHandler = new Handler();
    private boolean mIsMutePaused;

    Music(final MusicManager musicManager, final MediaPlayer mediaPlayer) {
        super(musicManager);
        mMediaPlayer = mediaPlayer;
    }

    @Override
    protected int getFocusType() {
        return android.media.AudioManager.AUDIOFOCUS_GAIN;
    }

    @Override
    protected void handleVolumeChange() {
        if (mMediaPlayer == null)
            return;

        // If the volume has just changed to mute and we are playing we set the music to paused state and add a flag so we know that we have paused for this reason. When the volume is un-muted we resume play.
        if(getVolume().isMuted() && isPlaying()) {
            mIsMutePaused = true;
            pause();
        } else if(!getVolume().isMuted() && isPaused() && mIsMutePaused) {
            mIsMutePaused = false;
            play();
        }

        mMediaPlayer.setVolume(getVolume().getCalculatedLeftChannel(), getVolume().getCalculatedRightChannel());
    }

    @Override
    protected void handleStateChange(State state) {

    }

    // OVERWRITTEN METHODS

    @Override
    public void play() {
        if (mMediaPlayer == null || getVolume().isMuted())
            return;

        // Music audio cannot play all at the same time.
        // We get all music instances in the pool and see if there is any playing.
        CopyOnWriteArrayList<Music> playingMusic = ((MusicManager) getAudioManager()).getPool(State.PLAYING);
        if (!playingMusic.isEmpty()) {
            for (Music musicInstance : playingMusic) {
                if (mCrossfadeDuration > 0)
                    musicInstance.enableCrossfade(mCrossfadeDuration);

                musicInstance.stop();
            }
        }
		
		// We start the play in a Handler so we can delay the playback if we neet to.
	    mHandler.postDelayed(new Runnable() {
	
			@Override
		    public void run() {
		            
		    	// Request Audio Focus and then try to play the music.
        		if (((MusicManager) getAudioManager()).requestFocus(android.media.AudioManager.STREAM_MUSIC, getFocusType()) == android.media.AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            		mMediaPlayer.start();
            		Music.super.play();

            		// If we have crossfading enabled we do this by manipulating the Volume Instance of our Object.
            		if (mCrossfadeDuration > 0) {
		                // Set the channel to 0 directly so we start from there.
		                getVolume().setChannel(Volume.MIN);
		
		                // Now raise the volume to maximum using the selected crossfade duration.
		                getVolume().setChannel(Volume.MAX, mCrossfadeDuration);
		            }
        		}
		    }
		         
		}, mCrossfadeDuration/2);
            
    }

    @Override
    public void pause() {
        if (mMediaPlayer == null)
            return;

        if (isPlaying()) {
            mMediaPlayer.pause();
            super.pause();
            ((MusicManager) getAudioManager()).abandonFocus();
        }
    }

    @Override
    public void stop() {
        if (mMediaPlayer == null)
            return;

        if (mCrossfadeDuration > 0) {
            getVolume().setChannel(Volume.MIN, mCrossfadeDuration);
        } else
            stopMediaPlayer();
    }

    // MUSIC RELATED METHODS
    public void enableCrossfade(final int duration) {
        mCrossfadeDuration = duration;
    }

    public void disableCrossfade() {
        mCrossfadeDuration = 0;
    }

    /**
     * Because when the Media Player is stopped it also releases its loaded audio source we don't have the luxury to reloaded and
     * we do not actually stop the player we just set it to a pause state and seek back to the start of the audio.
     * <br/><br/>
     * We will actually be in a STOPPED state so the client will now know of the weird things that are happening here.
     */
    private void stopMediaPlayer() {
        if (mMediaPlayer == null)
            return;

        mIsMutePaused = false;
        mMediaPlayer.pause();
        mMediaPlayer.seekTo(0);
        super.stop();
        ((MusicManager) getAudioManager()).abandonFocus();
    }

    public void setLooping(boolean isLooping) {
        if (mMediaPlayer == null)
            return;

        mIsLooping = isLooping;
        mMediaPlayer.setLooping(mIsLooping);
    }

    public boolean isLooping() {
        return mIsLooping;
    }

    @Override
    public void release() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

        ((MusicManager) getAudioManager()).remove(this);
    }

}
