package com.droidplanner.helpers;

import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;

public class TTS implements OnInitListener {
	TextToSpeech tts;

	public TTS(Context context) {
		if(shouldEnableTTS(context)) {
			tts = new TextToSpeech(context, this);
		}
	}

	@Override
	public void onInit(int status) {
		tts.setLanguage(Locale.US);
	}

	public void speak(String string) {
		if (tts != null)
			tts.speak(string, TextToSpeech.QUEUE_FLUSH, null);
	}

	private boolean shouldEnableTTS(Context context) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		return prefs.getBoolean("pref_enable_tts", false);
	}
}
