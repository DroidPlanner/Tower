package com.diydrones.droidplanner.helpers;

import java.util.Locale;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;

public class TTS {
	TextToSpeech tts;

	public TTS(Context context) {
		tts = new TextToSpeech(context, new OnInitListener() {

			@Override
			public void onInit(int status) {
				tts.setLanguage(Locale.US);
			}
		});
	}

	public void speak(String string) {
		tts.speak(string, TextToSpeech.QUEUE_FLUSH, null);
	}
}
