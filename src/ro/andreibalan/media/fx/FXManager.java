package ro.andreibalan.media.fx;

import java.util.ArrayList;

import ro.andreibalan.media.AudioManager;
import ro.andreibalan.media.IAudio;
import ro.andreibalan.media.music.Music;

import android.content.Context;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.util.SparseArray;


public class FXManager extends AudioManager<FX> implements OnLoadCompleteListener {

    private final static int SOUND_STATUS_OK = 0;

    public final static int STREAMS = 5;
    private SoundPool mSoundPool;
    private final SparseArray<FX> mSoundMap = new SparseArray<FX>();

    public FXManager(Context context, final int maxSimultaneousStreams) {
        super(context);

        this.mSoundPool = new SoundPool(maxSimultaneousStreams, AudioManager.STREAM_TYPE, 0);
        this.mSoundPool.setOnLoadCompleteListener(this);
    }

    @Override
    public synchronized void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
        if (status == SOUND_STATUS_OK) {
            final FX fx = mSoundMap.get(sampleId);
            if (fx != null)
                fx.setLoaded(true);
        }
    }
    
    @Override
    public ArrayList<FX> getPool() {
        ArrayList<FX> pool = new ArrayList<FX>();

        final ArrayList<IAudio> audioPool = getAudioPool();
        for (int i = 0; i < audioPool.size(); i++) {
            final IAudio audio = audioPool.get(i);

            if (audio instanceof Music)
                pool.add((FX) audio);
        }

        return pool;
    }

    SoundPool getSoundPool() {
        return mSoundPool;
    }

    @Override
    public void add(final FX fx) {
        super.add(fx);

        mSoundMap.put(fx.getSampleID(), fx);
    }

    @Override
    public boolean remove(final FX fx) {
        final boolean removed = super.remove(fx);
        if (removed)
            this.mSoundMap.remove(fx.getSampleID());

        return removed;
    }

    @Override
    public void releaseAll() {
        super.releaseAll();

        // Release our own SoundPool
        mSoundPool.release();
    }

}
