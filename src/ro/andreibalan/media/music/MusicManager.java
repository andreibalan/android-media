package ro.andreibalan.media.music;

import java.util.ArrayList;

import ro.andreibalan.media.AudioManager;
import ro.andreibalan.media.IAudio;

import android.content.Context;
import android.media.AudioManager.OnAudioFocusChangeListener;


public class MusicManager extends AudioManager<Music> {

    private final OnAudioFocusChangeListener mAudioFocusChangeListener = new OnAudioFocusChangeListener() {

        @Override
        public void onAudioFocusChange(int focusChange) {
            final Music playingMusic;
            final Music pausedMusic;

            switch (focusChange) {
                case android.media.AudioManager.AUDIOFOCUS_GAIN:
                    // Raise volume from duck.
                    raiseVolume();

                    pausedMusic = getPausedMusic();
                    if (pausedMusic != null)
                        pausedMusic.play();

                    break;

                case android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    playingMusic = getPlayingMusic();
                    if (playingMusic != null)
                        playingMusic.pause();
                    break;

                case android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    duckVolume();
                    break;

                case android.media.AudioManager.AUDIOFOCUS_LOSS:
                    playingMusic = getPlayingMusic();
                    if (playingMusic != null)
                        playingMusic.stop();
                    break;
            }
        }
    };

    public MusicManager(Context context) {
        super(context);
    }

    public int requestFocus(int streamType, int audioFocusType) {
        if (mSystemAudioManager != null)
            // Request audio focus for playback.
            return mSystemAudioManager.requestAudioFocus(mAudioFocusChangeListener, streamType, audioFocusType);

        return android.media.AudioManager.AUDIOFOCUS_REQUEST_FAILED;
    }

    public ArrayList<Music> getPool() {
        ArrayList<Music> pool = new ArrayList<Music>();

        final ArrayList<IAudio> audioPool = getAudioPool();
        for (int i = 0; i < audioPool.size(); i++) {
            final IAudio audio = audioPool.get(i);

            if (audio instanceof Music)
                pool.add((Music) audio);
        }

        return pool;
    }

    public Music getPlayingMusic() {
        final ArrayList<Music> musicPool = getPool();
        if (musicPool.isEmpty())
            return null;

        for (int i = musicPool.size() - 1; i >= 0; i--) {
            final Music music = musicPool.get(i);
            if (music.isPlaying())
                return music;
        }

        return null;
    }

    public Music getPausedMusic() {
        final ArrayList<Music> musicPool = getPool();
        if (musicPool.isEmpty())
            return null;

        for (int i = musicPool.size() - 1; i >= 0; i--) {
            final Music music = musicPool.get(i);
            if (music.isPaused())
                return music;
        }

        return null;
    }

    public boolean isMusicPaused() {
        return (getPausedMusic() != null);
    }

    public boolean isMusicPlaying() {
        return (getPlayingMusic() != null);
    }

}
