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
