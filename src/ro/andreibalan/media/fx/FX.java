package ro.andreibalan.media.fx;

import ro.andreibalan.media.Audio;

public class FX extends Audio {

    private int mSampleID;
    private boolean mIsLoaded = false;
    private int mStreamID;
    private int mLoopCount = 0;
    private float mRate = 1.0f;
    private boolean mIsPaused;

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
    public void release() {
        ((FXManager) getAudioManager()).remove(this);
    }

    @Override
    public void play() {
        if (mStreamID != 0 && mIsPaused) {
            // Act as Resume from here.
            ((FXManager) getAudioManager()).getSoundPool().resume(mStreamID);
            mIsPaused = false;
        } else {
            // Act as normal play.
            mStreamID = ((FXManager) getAudioManager()).getSoundPool().play(mSampleID, getActualVolume(mVolumeLeft), getActualVolume(mVolumeRight), 1,
                    mLoopCount,
                    mRate);
        }
    }

    @Override
    public void stop() {
        if (this.mStreamID != 0) {
            ((FXManager) getAudioManager()).getSoundPool().stop(mStreamID);
        }
    }

    @Override
    public void pause() {
        if (this.mStreamID != 0) {
            ((FXManager) getAudioManager()).getSoundPool().pause(mStreamID);
            mIsPaused = true;
        }
    }

    @Override
    public boolean isPlaying() {
        return false;
    }

    @Override
    protected void handleVolumeChange() {
        if (this.mStreamID != 0)
            ((FXManager) getAudioManager()).getSoundPool().setVolume(this.mStreamID, getActualVolume(mVolumeLeft), getActualVolume(mVolumeRight));

    }

}
