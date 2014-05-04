package ro.andreibalan.media.fx;

import java.io.File;

import android.content.Context;

public class FXFactory {

    private FXFactory() {
    }

    public static FX create(final Context context, final FXManager fxManager, final String path) {
        synchronized (fxManager) {
            return null;
        }
    };

    public static FX create(final Context context, final FXManager fxManager, final File file) {
        synchronized (fxManager) {
            return null;
        }
    }

    public static FX create(final Context context, final FXManager fxManager, final int rawResID) {
        synchronized (fxManager) {
            final int sampleID = fxManager.getSoundPool().load(context, rawResID, 1);
            final FX fx = new FX(fxManager, sampleID);

            fxManager.add(fx);
            return fx;
        }
    }

}
