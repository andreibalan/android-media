/**
 * Android Media Library, an media library for the android platform. Copyright (C) 2014 Andrei Balan
 * This file is part of Android Media Library Android Media Library is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 2 of the License, or (at your option) any later
 * version. Android Media Libraryis distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with Android Media Library. If not, see
 * <http://www.gnu.org/licenses/>. Authors: Andrei Balan
 */
package ro.andreibalan.media.volume;

import java.util.concurrent.CopyOnWriteArrayList;
import android.util.Log;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ValueAnimator;

import static com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;

public class Volume {

    public final static String TAG = Volume.class.getSimpleName();

    /**
     * Minimum Channel Volume Value.
     */
    public final static float MIN = 0.0f;

    /**
     * Maximum Channel Volume Value.
     */
    public final static float MAX = 1.0f;

    /**
     * This is the Channel Volume Lowering Threshold.
     * If the channel volumes are higher that this value they will be lowerd to it if not there will be no lowering.
     */
    private final static float TEMPORARY_OFFSET_THRESHOLD = 0.2f;

    /**
     * Short Animator Value used for Fading Volumes and Channel Balance.
     */
    public final static int FADE_DURATION_SHORT = 400;

    /**
     * Normal Animator Value used for Fading Volumes and Channel Balance.
     */
    public final static int FADE_DURATION_NORMAL = 600;

    /**
     * Long Animator Value used for Fading Volumes and Channel Balance.
     */
    public final static int FADE_DURATION_LONG = 1000;

    /**
     * Left Channel Volume Value. Default is Maximum.
     */
    private float mLeftChannel = 1.0f;

    /**
     * Right Channel Volume Value. Default is Maximum.
     */
    private float mRightChannel = 1.0f;

    /**
     * Used for offseting both of the channel volumes.
     * Acts like a mastering volume.
     */
    private float mChannelOffset = 1.0f;

    /**
     * Used for temporarily lowering the volume.
     * Saving the original channel offset before lowering it and then restoring it from here.
     */
    private Float mOriginalChannelOffset = null;

    /**
     * Left-Right Channel Balance. Default is set to middle balance.
     * 
     * Examples:
     * -1.0f: Left Channel (Right Channel Muted)
     * 0.0f: Middle (Both Channels are equal in volume)
     * +1.0f Right Channel (Left Channel Muted)
     */
    private float mBalance = 0f;

    /**
     * Used to know if the current Volume is muted.
     */
    private boolean mMuted = false;

    /**
     * Animator used to Fade Channel Volumes.
     */
    private ValueAnimator mVolumeAnimator;

    /**
     * Animator used to Fade Balance Values.
     */
    private ValueAnimator mBalanceAnimator;

    /**
     * Holder for the Listeners. Using CopyOnWriteArrayList because it is thread safe it we do not
     * need to wrap all our add, remove and notify code into a synchronized block.
     */
    private CopyOnWriteArrayList<OnVolumeChangeListener> mListeners = new CopyOnWriteArrayList<OnVolumeChangeListener>();

    /** 
     * Default Volume and Balance Listener
     * You can use this so you can receive notification on volume or balance change in values.
     * You must register/de-register the listener to the volume instance you want to watch.
     */
    public static interface OnVolumeChangeListener {

        /**
         * Called when volume get changed.
         */
        public void onVolumeChange(float leftChannel, float rightChannel);

        /**
         * Called when balance of channel gets changed.
         */
        public void onBalanceChange(float balance);
    }

    private Animator.AnimatorListener mVolumeAnimatorListener = new Animator.AnimatorListener() {

        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }

        @Override
        public void onAnimationEnd(Animator animation) {
            mVolumeAnimator = null;
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            mVolumeAnimator = null;
        }
    };

    private Animator.AnimatorListener mBalanceAnimatorListener = new Animator.AnimatorListener() {

        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }

        @Override
        public void onAnimationEnd(Animator animation) {
            mVolumeAnimator = null;
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            mVolumeAnimator = null;
        }
    };

    private AnimatorUpdateListener mVolumeUpdateListener = new AnimatorUpdateListener() {

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            setChannel((Float) animation.getAnimatedValue());
        }

    };

    private AnimatorUpdateListener mBalanceUpdateListener = new AnimatorUpdateListener() {

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            setBalance((Float) animation.getAnimatedValue());
        }

    };

    /**
     * Empty Constructor
     */
    public Volume() {
        Log.v(TAG, "Construct Empty");
    }

    /**
     * Mono Constructor
     * 
     * @param volume float value that will be passed to both channels.
     */
    public Volume(final float volume) {
        Log.v(TAG, "Construct MONO");

        this.mLeftChannel = volume;
        this.mRightChannel = volume;
    }

    /**
     * Stereo Constructor
     * 
     * @param leftChannel
     * @param rightChannel
     */
    public Volume(final float leftChannel, final float rightChannel) {
        Log.v(TAG, "Construct Stereo");

        this.mLeftChannel = leftChannel;
        this.mRightChannel = rightChannel;
    }

    /**
     * Add an instance of Volume.OnVolumeChangeListener to the current Volume instance.
     * 
     * @param listener - OnVolumeChangeListener
     */
    public void addOnVolumeChangeListener(OnVolumeChangeListener listener) {
        Log.v(TAG, "addOnVolumeChangeListener: " + listener);

        if (!mListeners.contains(listener))
            if (mListeners.add(listener))
                Log.v(TAG, "addOnVolumeChangeListener: Added");
    }

    /**
     * Removes an existing instance of Volume.OnVolumeChangeListener from the current Volume instance.
     * 
     * @param listener - OnVolumeChangeListener
     */
    public void removeOnVolumeChangeListener(OnVolumeChangeListener listener) {
        Log.v(TAG, "removeOnVolumeChangeListener: " + listener);

        if (mListeners.contains(listener))
            if (mListeners.remove(listener))
                Log.v(TAG, "removeOnVolumeChangeListener: Removed");
    }

    /**
     * Notifies all the attached listeners that a change has been made to one or both of the channels.
     */
    private void notifyVolumeChange() {
        Log.v(TAG, "notifyVolumeChange " + mListeners.size() + " Listeners");

        for (int i = 0; i < mListeners.size(); i++)
            mListeners.get(i).onVolumeChange(getCalculatedLeftChannel(), getCalculatedRightChannel());
    }

    /**
     * Notifies all the attached listeners that a change has been made to the channel balance value.
     * You will also receive onVolumeChange notification because the balance modified the current levels of both channels to compensate.
     */
    private void notifyBalanceChange() {
        Log.v(TAG, "notifyBalanceChange " + mListeners.size() + " Listeners");

        for (int i = 0; i < mListeners.size(); i++)
            mListeners.get(i).onBalanceChange(mBalance);
    }

    /**
     * Sets a new volume value for both left and right channels while fading to that value.
     * <br/><br/>
     * Throws IllegalArgumentExpcetion if value is not between 0.0 and 1.0.
     * 
     * @param volume - Desired volume
     * @param duration - Duration of the fade.
     */
    public void setChannel(final float volume, final int duration) {
        Log.v(TAG, "setChannel volume: " + volume + " duration: " + duration);

        verifyChannelInput(volume);
        fadeChannelTo(getChannel(), volume, duration);
    }

    /**
     * Sets a new volume value for both the left and right channels.
     * The volume will be directly set to the new value.
     * <br/><br/>
     * Throws IllegalArgumentExpcetion if value is not between 0.0 and 1.0.
     * <br/><br/>
     * For fading to the new value see {@link #setChannel(float, int)}
     * 
     * @param volume - Desired volume
     */
    public void setChannel(final float volume) {
        Log.v(TAG, "setChannel volume: " + volume);

        verifyChannelInput(volume);

        mLeftChannel = volume;
        mRightChannel = volume;

        verifyChannelOutput();
        notifyVolumeChange();
    }

    /**
     * Gets the channel volume in mono value.
     */
    public float getChannel() {
        Log.v(TAG, "getChannel");

        return (mLeftChannel + mRightChannel) * 0.5f;
    }

    public float getCalculatedChannel() {
        Log.v(TAG, "getCalculatedChannel");

        if (isMuted())
            return 0f;

        float volume = getChannel();
        volume *= mChannelOffset;
        return volume;
    }

    /**
     * Fades both channel volumes from and to the given values within the time duration specified.
     * This uses a ValueAnimator and the UpdateListener calls back on {@link #setChannel(float)}
     * <br/><br/>
     * Will also cancel any running fades.
     * 
     * @param startValue - Start Volume Value
     * @param endValue - End Volume Value or Desired Volume Value
     * @param duration - Duration of the change
     */
    private void fadeChannelTo(final float startValue, final float endValue, final int duration) {
        Log.v(TAG, "fadeChannelTo startValue: " + startValue + ", endValue: " + endValue + ", duration: " + duration);

        if (mVolumeAnimator != null) {
            Log.v(TAG, "fadeChannelTo: old Volume Animator found, canceling and setting to null.");
            mVolumeAnimator.cancel();
        }

        mVolumeAnimator = ValueAnimator.ofFloat(startValue, endValue);
        mVolumeAnimator.setDuration(duration);
        mVolumeAnimator.addUpdateListener(mVolumeUpdateListener);
        mVolumeAnimator.addListener(mVolumeAnimatorListener);
        mVolumeAnimator.start();
    }

    /**
     * Sets the left and right channel volume values individually (Stereo Control).
     * This will also trigger a channel balance value calculation.
     * <br/><br/>
     * Throws IllegalArgumentExpcetion if value is not between 0.0 and 1.0 for both left and right channel values.
     * <br/><br/>
     * <b>NOTE: you cannot use a fade effect when controlling both channels individually but an alternative is to set a mono volume and fade the channel balance value using {@link #setBalance(float, int)}}</b>
     * 
     * @param leftChannel - Left Channel Volume Value
     * @param rightChannel - Right Channel Volume Value
     */
    public void setChannels(final float leftChannel, final float rightChannel) {
        Log.v(TAG, "setChannels leftChannel: " + leftChannel + ", rightChannel: " + rightChannel);

        verifyChannelInput(leftChannel, rightChannel);

        this.mLeftChannel = leftChannel;
        this.mRightChannel = rightChannel;

        verifyChannelOutput();
        calculateBalance();
    }

    /**
     * Sets the left channel volume value (Stereo Control).
     * This will also trigger a channel balance value calculation.
     * <br/><br/>
     * Throws IllegalArgumentExpcetion if value is not between 0.0 and 1.0.
     * 
     * @param volume - Left Channel Volume Value
     */
    public void setLeftChannel(final float volume) {
        Log.v(TAG, "setLeftChannel volume: " + volume);

        setChannels(volume, mLeftChannel);
    }

    /**
     * Sets the right channel volume value (Stereo Control).
     * This will also trigger a channel balance value calculation.
     * <br/><br/>
     * Throws IllegalArgumentExpcetion if value is not between 0.0 and 1.0.
     * 
     * @param volume - Right Channel Volume Value
     */
    public void setRightChannel(final float volume) {
        Log.v(TAG, "setRightChannel volume: " + volume);

        setChannels(mLeftChannel, volume);
    }

    /**
     * Returns the direct left channel volume value without.
     * <br/><br/>
     * <b>NOTE: If the channel balance is not at 0.0 (Middle Value) this means that this direct volume value is not the real volume value. 
     * TO get the real volume value use {@link #getCalculatedLeftChannel()}</b>
     * 
     * @return - float value of the left channel volume without balance calculation.
     */
    public float getLeftChannel() {
        Log.v(TAG, "getLeftChannel: " + mLeftChannel);

        return mLeftChannel;
    }

    /**
     * Returns the direct right channel volume value without.
     * <br/><br/>
     * <b>NOTE: If the channel balance is not at 0.0 (Middle Value) this means that this direct volume value is not the real volume value. 
     * TO get the real volume value use {@link #getCalculatedRightChannel()}</b>
     * 
     * @return - float value of the right channel volume without balance calculation.
     */
    public float getRightChannel() {
        Log.v(TAG, "getRightChannel: " + mRightChannel);

        return mRightChannel;
    }

    /**
     * Returns the real left channel volume value based on the current channel balance settings.
     * <br/><br/>
     * If you want to get the set volume value for this channel use {@link #getLeftChannel()}
     * 
     * @return - float value of the left channel volume with balance calculation.
     */
    public float getCalculatedLeftChannel() {
        Log.v(TAG, "getCalculatedLeftChannel");

        if (isMuted())
            return 0f;

        float volume = mLeftChannel;
        volume *= mChannelOffset;
        return volume;
    }

    /**
     * Returns the real right channel volume value based on the current channel balance settings.
     * <br/><br/>
     * If you want to get the set volume value for this channel use {@link #getRightChannel()}
     * 
     * @return - float value of the right channel volume with balance calculation.
     */
    public float getCalculatedRightChannel() {
        Log.v(TAG, "getCalculatedRightChannel");

        if (isMuted())
            return 0f;

        float volume = mRightChannel;
        volume *= mChannelOffset;
        return volume;
    }

    public void setChannelOffset(final float value) {
        Log.v(TAG, "setChannelOffset: " + value);

        verifyChannelInput(value);

        // If the volume has been temporarily lowered (duck) we set the value to the saved original offset value variable.
        if (mOriginalChannelOffset != null)
            mOriginalChannelOffset = value;
        else
            mChannelOffset = value;

        verifyChannelOutput();
        notifyVolumeChange();
    }

    public float getChannelOffset() {
        Log.v(TAG, "getChannelOffset");

        return mChannelOffset;
    }

    public void lowerChannels() {
        Log.v(TAG, "lowerChannels");

        if (mOriginalChannelOffset == null && mChannelOffset > TEMPORARY_OFFSET_THRESHOLD) {
            final float currentChannelOffset = mChannelOffset;
            setChannelOffset(TEMPORARY_OFFSET_THRESHOLD);
            mOriginalChannelOffset = currentChannelOffset;
            Log.v(TAG, "lowerChannels: Channels have been lowerd.");
        }
    }

    public void raiseChannels() {
        Log.v(TAG, "raiseChannels");

        if (mOriginalChannelOffset != null) {
            final float originalChannelOffset = mOriginalChannelOffset;
            mOriginalChannelOffset = null;
            setChannelOffset(originalChannelOffset);

            Log.v(TAG, "raiseChannels: Channels have been raised.");
        }
    }

    /**
     * Sets the channel balance value using a fade effect with the duration you specified.
     * <br/><br/>
     * <b>How to use the values to balance left right channels:</b>
     * <ul>
     * <li>-1.0f means that you want to hear on the left channel therefore muting the right channel.</li>
     * <li>0.0f means that both channels are set to the equal volume. Middle Position</li>
     * <li>+1.0f means that you want to hear on the right channel therefore muting the left channel.</li>
     * </ul>
     * 
     * <br/><br/>
     * Throws IllegalArgumentExpcetion if value is not between -1.0 and +1.0.
     * <br/><br/>
     * If you want to set the channel balance directly without using a fade effect use {@link #setBalance(float)}
     * 
     * @param balance - Channel Balance Value
     * @param duration - int duration value
     */
    public void setBalance(final float balance, final int duration) {
        Log.v(TAG, "setBalance: " + balance + ", duration: " + duration);

        verifyBalanceInput(balance);
        fadeBalanceTo(mBalance, balance, duration);
    }

    /**
     * Sets the channel balance value directly.
     * <br/><br/>
     * <b>How to use the values to balance left right channels:</b>
     * <ul>
     * <li>-1.0f means that you want to hear on the left channel therefore muting the right channel.</li>
     * <li>0.0f means that both channels are set to the equal volume. Middle Position</li>
     * <li>+1.0f means that you want to hear on the right channel therefore muting the left channel.</li>
     * </ul>
     * 
     * <br/><br/>
     * Throws IllegalArgumentExpcetion if value is not between -1.0 and +1.0.
     * <br/><br/>
     * If you want to set the channel balance value using a fade efect use {@link #setBalance(float, int)}
     * 
     * @param balance - Channel Balance Value
     */
    public void setBalance(final float balance) {
        Log.v(TAG, "setBalance: " + balance);

        verifyBalanceInput(balance);
        mBalance = balance;
        notifyBalanceChange();
    }

    /**
     * Returns the current channel balance value.
     */
    public float getBalance() {
        Log.v(TAG, "getBalance: " + mBalance);

        return mBalance;
    }

    /**
     * Directly resets the balance value to middle position 0.0.
     */
    public void resetBalance() {
        Log.v(TAG, "resetBalance");

        setBalance(0f);
    }

    /**
     * When user tries to control both channel volume values individually this will be triggered so it can calculate the channel balance value.
     */
    private void calculateBalance() {
        Log.v(TAG, "calculateBalance");
        Log.e(TAG, "calculateBalance is not fully finished at the moment.");
        // TODO: create this method.
    }

    /**
     * Used to fade channel balance value from and to the given values using the time specified by the user.
     * 
     * @param startValue
     * @param endValue
     * @param duration
     */
    private void fadeBalanceTo(final float startValue, final float endValue, final int duration) {
        Log.v(TAG, "fadeBalanceTo startValue: " + startValue + ", endValue: " + endValue + ", duration: " + duration);

        if (mBalanceAnimator != null) {
            Log.v(TAG, "fadeBalanceTo old Balance Animator found. Canceling and setting to null.");
            mBalanceAnimator.cancel();
        }

        mBalanceAnimator = ValueAnimator.ofFloat(startValue, endValue);
        mBalanceAnimator.setDuration(duration);
        mBalanceAnimator.addUpdateListener(mBalanceUpdateListener);
        mBalanceAnimator.addListener(mBalanceAnimatorListener);
        mBalanceAnimator.start();
    }

    public void mute() {
        Log.v(TAG, "mute");

        if (!mMuted) {
            mMuted = true;
            notifyVolumeChange();
            Log.v(TAG, "unmute: Channels have been muted.");
        }
    }

    public void unmute() {
        Log.v(TAG, "unmute");

        if (mMuted) {
            mMuted = false;
            notifyVolumeChange();
            Log.v(TAG, "unmute: Channels have been unmuted.");
        }
    }

    public boolean isMuted() {
        Log.v(TAG, "isMuted: " + mMuted);

        return mMuted;
    }

    /**
     * Checks if the calculated output channel is equals or bigger than 0.0 and mutes or unmutes if necessary.
     * NOTE: Set's mute directly and doesn't notify of the volume change. YOu should manually take care of notifying volume change.
     */
    private void verifyChannelOutput() {
        Log.v(TAG, "verifyChannelOutput");

        // Manually calculating channel here instead of using getCalculatedChannel method because we do not want to take into consideration mute.
        float calculatedChannel = getChannel() * mChannelOffset;

        if(calculatedChannel == Volume.MIN && !isMuted()) {
            mMuted = true;
        } else if(calculatedChannel > Volume.MIN && isMuted()) {
            mMuted = false;
        }
    }

    /**
     * This is called on each Channel Volume Values set method so it can verify the inputed values.
     * Values should always be between 0.0f and 1.0f for volume control.
     * 
     * @param input - Set of input values for volume control.
     */
    private void verifyChannelInput(final float... input) {
        Log.v(TAG, "verifyChannelInput for " + input.toString());

        for (final float value : input)
            if (value < 0f || value > 1f)
                throw new IllegalArgumentException("Channel volume value should be between 0.0 and 1.0");
    }

    /**
     * This is called on each Channel Balance Value set method so it can verify the inputed values.
     * Values should always be between -1.0f and +1.0f for channel balance control.
     * 
     * @param input - Set of input values for channel balance control.
     */
    private void verifyBalanceInput(final float... input) {
        Log.v(TAG, "verifyBalanceInput for " + input.toString());

        for (final float value : input)
            if (value < -1f || value > 1f)
                throw new IllegalArgumentException("Balance value should be between -1.0 and +1.0");
    }

}
