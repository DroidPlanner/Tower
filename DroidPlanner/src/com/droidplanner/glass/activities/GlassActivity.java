package com.droidplanner.glass.activities;

import com.droidplanner.activitys.helpers.SuperActivity;
import com.droidplanner.glass.utils.GlassVoiceMenu;
import android.os.Bundle;
import com.google.android.glass.touchpad.GestureDetector;
import android.view.MotionEvent;
import android.view.InputDevice;
import android.content.Intent;
import com.google.android.glass.touchpad.Gesture;
import com.droidplanner.glass.utils.GlassUtils;

public class GlassActivity extends SuperActivity {
	
	private GlassVoiceMenu voiceMenu;
	
	/**
     * Glass gesture detector.
     * Detects glass specific swipes, and taps, and uses it for navigation.
     *
     * @since 1.2.0
     */
    protected GestureDetector mGestureDetector;
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GlassVoiceMenu.SPEECH_REQUEST && resultCode == RESULT_OK) {
            GlassVoiceMenu.onSpeechComplete(getApplicationContext(), data);
        }
    }
	
	protected GestureDetector.BaseListener getGestureDetectorBaseListener(){
		return new GestureDetector.BaseListener() {
			@Override
			public boolean onGesture(Gesture gesture) {
				return false;
			}
		};
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setUpGestureDetector();
	}
	
	@Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if (mGestureDetector != null && event.getSource() == InputDevice.SOURCE_TOUCHPAD) {
            return mGestureDetector.onMotionEvent(event);
        }
        return super.onGenericMotionEvent(event);
    }
	
	protected void invalidateVoiceMenu(){
		voiceMenu = null;
	}
	
	protected void onCreateVoiceMenu(GlassVoiceMenu voiceMenu){}
	
	protected void onPrepareVoiceMenu(GlassVoiceMenu voiceMenu){
		
	}
	
	protected void onSpeechDetected(final String speech){
		
	}
	
	protected void openVoiceMenu(){
		//Check if the voice menu instance was initialized.
		if(voiceMenu == null){
		voiceMenu = new GlassVoiceMenu(this);
		onCreateVoiceMenu(voiceMenu);
		}
		
		onPrepareVoiceMenu(voiceMenu);
	}
	
	protected void setUpGestureDetector() {
        if (GlassUtils.isGlassDevice()) {
            mGestureDetector = new GestureDetector(getApplicationContext());
            mGestureDetector.setBaseListener(getGestureDetectorBaseListener());
        }
    }
}
