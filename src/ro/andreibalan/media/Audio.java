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
