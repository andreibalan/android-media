package ro.andreibalan.media;

import java.util.ArrayList;
import java.util.Iterator;

import android.content.Context;

public abstract class AudioManager<T extends IAudio> {

    public final static int STREAM_TYPE = android.media.AudioManager.STREAM_MUSIC;

    private final static float AUDIO_DUCK_VOLUME = 0.2f;

    protected final ArrayList<T> mAudioPool = new ArrayList<T>();
    protected float mMasterVolume = 1.0f;
    private Float mOriginalVolume = null;
    private Context mContext;
    protected android.media.AudioManager mSystemAudioManager;

    public enum AudioOutputDevice {
        A2DP,
        SPEAKERPHONE,
        HEADSET,
        SPEAKER
    }

    protected AudioManager(Context context) {
        this.mContext = context;

        mSystemAudioManager = (android.media.AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
    }

    public abstract ArrayList<T> getPool();

    private void notifyMasterVolumeChange() {
        final ArrayList<T> audioPool = mAudioPool;

        for (int i = audioPool.size() - 1; i >= 0; i--) {
            final T audio = audioPool.get(i);
            audio.onMasterVolumeChanged(mMasterVolume);
        }
    }

    protected ArrayList<IAudio> getAudioPool() {
        final ArrayList<IAudio> audioPool = (ArrayList<IAudio>) this.mAudioPool;
        return audioPool;
    }

    public AudioOutputDevice getOutputDevice() {
        // Query for Bluetooth A2DP
        if (mSystemAudioManager.isBluetoothA2dpOn())
            return AudioOutputDevice.A2DP;

        // Query for SpeakerPhone
        if (mSystemAudioManager.isSpeakerphoneOn())
            return AudioOutputDevice.SPEAKERPHONE;

        // Query for Wired Headset
        if (mSystemAudioManager.isWiredHeadsetOn())
            return AudioOutputDevice.HEADSET;

        // Return Default Device Output (Device Speaker)
        return AudioOutputDevice.SPEAKER;
    }

    protected boolean isDucked() {
        return (mOriginalVolume != null);
    }

    protected void duckVolume() {
        if (mMasterVolume > AUDIO_DUCK_VOLUME && !isDucked()) {
            final float currentVolume = mMasterVolume;
            mOriginalVolume = currentVolume;
            setMasterVolume(AUDIO_DUCK_VOLUME);
        }
    }

    protected void raiseVolume() {
        if (isDucked()) {
            setMasterVolume((float) mOriginalVolume);
            mOriginalVolume = null;
        }
    }

    /**
     * Abstract Methods
     */
    public float getMasterVolume() {
        return mMasterVolume;
    }

    public void setMasterVolume(final float masterVolume) {
        mMasterVolume = masterVolume;
        notifyMasterVolumeChange();
    }

    public void add(final T audio) {
        mAudioPool.add(audio);
    }

    public boolean remove(final T audio) {
        if (mAudioPool.contains(audio))
            return mAudioPool.remove(audio);

        return false;
    }

    public void releaseAll() {
        final ArrayList<T> audioPool = mAudioPool;

        for (int i = audioPool.size() - 1; i >= 0; i--) {
            final T audio = audioPool.get(i);

            audio.stop();
            audio.release();
        }
    }

}
