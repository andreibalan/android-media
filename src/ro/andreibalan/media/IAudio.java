package ro.andreibalan.media;

public interface IAudio {

    /**
     * Handle Control Methods
     */
    public void play();

    public void stop();

    public void pause();

    /**
     * Query Methods
     */
    public boolean isPlaying();

    /**
     * Handle Volume Control Methods
     */
    public float getVolume();

    public float getLeftVolume();

    public float getRightVolume();

    public void setVolume(float volume);

    public void setVolume(float leftVolume, float rightVolume);

    public void onMasterVolumeChanged(final float masterVolume);

    /**
     * Release Method
     */
    public void release();
}
