package org.droidplanner.android.utils.sound;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.support.annotation.IntDef;
import android.util.SparseIntArray;

import org.droidplanner.android.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import timber.log.Timber;

/**
 * Created by Fredia Huya-Kouadio on 3/30/15.
 */
public class SoundManager {

    private static final String TAG = SoundManager.class.getSimpleName();

    @IntDef({NO_SOUND, ALERT_MESSAGE, ALERT_NEUTRAL, ARM, DISARM, RTH, UPDATE_SUCCESS})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SoundType {}

    public static final int NO_SOUND = -1;
    public static final int ALERT_MESSAGE = R.raw.alert_message;
    public static final int ALERT_NEUTRAL = R.raw.alert_neutral;
    public static final int ARM = R.raw.arm;
    public static final int DISARM = R.raw.disarm;
    public static final int RTH = R.raw.return_to_home;
    public static final int UPDATE_SUCCESS = R.raw.update_success;

    private final SparseIntArray loadedSoundsIds = new SparseIntArray(16);
    private final SoundPool soundPool;
    private final Context context;

    public SoundManager(Context context){
        this.context = context;
        this.soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
    }

    public void start(){
        //Load the audio
        loadSound(context, ALERT_MESSAGE);
        loadSound(context, ALERT_NEUTRAL);
        loadSound(context, ARM);
        loadSound(context, DISARM);
        loadSound(context, RTH);
        loadSound(context, UPDATE_SUCCESS);
    }

    public void stop(){
        //Unload the audio
        final int soundIdsCount = loadedSoundsIds.size();
        for(int i = 0; i < soundIdsCount; i++){
            soundPool.unload(loadedSoundsIds.valueAt(i));
        }
    }

    private void loadSound(Context context, @SoundType int soundType){
        loadedSoundsIds.put(soundType, soundPool.load(context, soundType, 1));
    }

    public boolean play(@SoundType int soundType){
        if(soundType == NO_SOUND)
            return true;

        final int soundId = loadedSoundsIds.get(soundType, -1);
        if(soundId == -1){
            Timber.e("Unable to retrieve sound id for resource " + soundType);
            return false;
        }

        return soundPool.play(soundId, 1, 1, 1, 0, 1) != 0;
    }
}
