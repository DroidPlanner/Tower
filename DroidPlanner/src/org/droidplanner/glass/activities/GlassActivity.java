package org.droidplanner.glass.activities;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.widget.Toast;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import org.droidplanner.activities.helpers.SuperActivity;
import org.droidplanner.drone.DroneInterfaces;
import org.droidplanner.glass.utils.GlassUtils;
import org.droidplanner.glass.utils.voice_menu.VoiceMenu;

import java.util.List;

/**
 * Parent to most activities running on glass. This holds common glass specific functionalities.
 */
public abstract class GlassActivity extends SuperActivity implements DroneInterfaces
        .OnDroneListener{

    /**
     * This is used to instantiate, and activate the voice menu.
     */
    private VoiceMenu voiceMenu;

    /**
     * This is used to track which menu, or sub menus last launched the recognizer intent.
     */
    private VoiceMenu recognizerIntentOriginMenu;

    /**
     * Stored the text returned by the recognizer intent in onActivityResult. If not null,
     * this text is used to trigger the voice menu in onResume.
     */
    private String recognizedSpeech;

    /**
     * Glass gesture detector.
     * Detects glass specific swipes, and taps, and uses it for navigation.
     *
     * @since 1.2.0
     */
    protected GestureDetector mGestureDetector;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VoiceMenu.SPEECH_REQUEST && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            recognizedSpeech = null;
            if (!results.isEmpty()) {
                recognizedSpeech = results.get(0);
            }

            if (recognizedSpeech == null) {
                Toast.makeText(getApplicationContext(), "Unable to recognize speech!",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    protected GestureDetector.BaseListener getGestureDetectorBaseListener() {
        return new GestureDetector.BaseListener() {
            @Override
            public boolean onGesture(Gesture gesture) {
                return false;
            }
        };
    }

    public void setRecognizerIntentOriginMenu(VoiceMenu menu){
        this.recognizerIntentOriginMenu = menu;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpGestureDetector();
    }

    @Override
    protected void onStart() {
        super.onStart();
        drone.events.addDroneListener(this);
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if (mGestureDetector != null && event.getSource() == InputDevice.SOURCE_TOUCHPAD) {
            return mGestureDetector.onMotionEvent(event);
        }
        return super.onGenericMotionEvent(event);
    }

    @Override
    public void onResume(){
        super.onResume();

        if(recognizedSpeech != null && recognizerIntentOriginMenu != null){
            recognizerIntentOriginMenu.dispatchVoiceMenuItemRecognized(recognizedSpeech);
        }
        recognizedSpeech = null;
    }

    @Override
    public void onPause(){
        super.onPause();

        recognizedSpeech = null;
    }

    protected void invalidateVoiceMenu() {
        voiceMenu = null;
    }

    protected void openVoiceMenu() {
        //Check if the voice menu instance was initialized.
        if (voiceMenu == null) {
            voiceMenu = new VoiceMenu(this);
            voiceMenu.setPromptHeader("Say, ");
            onCreateOptionsMenu(voiceMenu);
        }

        onPrepareOptionsMenu(voiceMenu);
        voiceMenu.openVoiceMenu();
    }

    protected void setUpGestureDetector() {
        if (GlassUtils.isGlassDevice()) {
            mGestureDetector = new GestureDetector(getApplicationContext());
            mGestureDetector.setBaseListener(getGestureDetectorBaseListener());
        }
    }
}
