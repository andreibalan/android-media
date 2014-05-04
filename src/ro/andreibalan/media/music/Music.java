package ro.andreibalan.media.music;

import ro.andreibalan.media.Audio;
import ro.andreibalan.media.AudioManager;
import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.media.MediaPlayer;


public class Music extends Audio {

    public final static int CROSSFADE_DURATION = 2000;

    private boolean mIsLooping;
    private MediaPlayer mMediaPlayer;
    private boolean mIsPaused;
    private ValueAnimator mRaiseVolumeAnimator;
    private ValueAnimator mLowerVolumeAnimator;
    private boolean mIsPlaying = false;

    Music(final MusicManager musicManager, final MediaPlayer mediaPlayer) {
        super(musicManager);
        mMediaPlayer = mediaPlayer;
    }

    @Override
    protected int getFocusType() {
        return android.media.AudioManager.AUDIOFOCUS_GAIN;
    }

    protected void handleVolumeChange() {
        if (mMediaPlayer == null)
            return;

        mMediaPlayer.setVolume(getActualVolume(mVolumeLeft), getActualVolume(mVolumeRight));
    }

    @Override
    public void play() {
        play(mIsLooping, false);
    }

    public void play(boolean loop, boolean fadeIn) {
        if (mMediaPlayer == null)
            return;

        setLooping(loop);

        // Music audio cannot play all at the same time.
        // We get all music instances in the pool and see if there is any playing.
        final Music playingMusic = ((MusicManager) getAudioManager()).getPlayingMusic();
        if (playingMusic != null) {
            // If the playing music is out instance then we do nothing here. Play is useless because
            // we are already playing.
            if (playingMusic == this)
                return;

            // Stop the other music.
            playingMusic.stop(fadeIn);
        }

        // Request Audio Focus and then try to play the music.
        if (((MusicManager) getAudioManager()).requestFocus(AudioManager.STREAM_TYPE, getFocusType()) == android.media.AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {

            if (fadeIn) {
                // Handle the other Animator if it exists.
                if (mRaiseVolumeAnimator != null) {
                    mRaiseVolumeAnimator.cancel();
                    mRaiseVolumeAnimator = null;
                }

                mRaiseVolumeAnimator = ValueAnimator.ofFloat(0f, 1.0f);
                mRaiseVolumeAnimator.setDuration(CROSSFADE_DURATION);
                mRaiseVolumeAnimator.setStartDelay(Math.round(CROSSFADE_DURATION * 0.75));

                mRaiseVolumeAnimator.addListener(new AnimatorListener() {

                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mRaiseVolumeAnimator = null;

                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        mRaiseVolumeAnimator = null;
                        stopMediaPlayer();
                    }
                });

                mRaiseVolumeAnimator.addUpdateListener(new AnimatorUpdateListener() {

                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        final float value = (float) animation.getAnimatedValue();
                        Music.this.setVolume(value);
                    }
                });

                setVolume(0);
                mRaiseVolumeAnimator.start();
            }

            mMediaPlayer.start();
            mIsPlaying = true;

            // If it was in Pause state we remove this state.
            if (mIsPaused)
                mIsPaused = false;
        }
    }

    @Override
    public void stop() {
        stop(false);
    }

    public void stop(boolean fadeOut) {
        if (mMediaPlayer == null)
            return;

        if (isPlaying()) {
            if (fadeOut && mLowerVolumeAnimator == null) {
                // Stop using a fadeOut volume effect.
                mLowerVolumeAnimator = ValueAnimator.ofFloat(getVolume(), 0f);
                mLowerVolumeAnimator.setDuration(CROSSFADE_DURATION);
                mLowerVolumeAnimator.addListener(new AnimatorListener() {

                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mLowerVolumeAnimator = null;
                        stopMediaPlayer();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        mLowerVolumeAnimator = null;
                        stopMediaPlayer();
                    }
                });

                mLowerVolumeAnimator.addUpdateListener(new AnimatorUpdateListener() {

                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        final float value = (float) animation.getAnimatedValue();
                        setVolume(value);
                    }

                });

                mLowerVolumeAnimator.start();
            } else {
                // Stop directly
                stopMediaPlayer();
            }
        }
    }

    private void stopMediaPlayer() {
        // This will actually put the media player into pause and seet position at 0.
        // Stopping the mediaPlayer will also release it's configuration and you cannot start it
        // without preparing it before.
        mMediaPlayer.pause();
        mMediaPlayer.seekTo(0);
        mIsPlaying = false;
    }

    @Override
    public void pause() {
        if (mMediaPlayer == null)
            return;

        if (isPlaying()) {
            mMediaPlayer.pause();
            mIsPlaying = false;
            mIsPaused = true;
        }
    }

    public boolean isPaused() {
        return mIsPaused;
    }

    @Override
    public boolean isPlaying() {
        if (mMediaPlayer == null)
            return false;

        return mMediaPlayer.isPlaying() && mIsPlaying;
    }

    public void playLooping() {
        play(true, false);
    }

    public void setLooping(boolean isLooping) {
        mIsLooping = isLooping;
        mMediaPlayer.setLooping(mIsLooping);
    }

    public boolean isLooping() {
        return mIsLooping;
    }

    @Override
    public void release() {
        if (mRaiseVolumeAnimator != null) {
            mRaiseVolumeAnimator.cancel();
            mRaiseVolumeAnimator = null;
        }

        if (mLowerVolumeAnimator != null) {
            mLowerVolumeAnimator.cancel();
            mLowerVolumeAnimator = null;
        }

        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

        ((MusicManager) getAudioManager()).remove(this);
    }

}
