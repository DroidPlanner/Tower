package com.droidplanner.helpers;

import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;

public class TTS implements OnInitListener {
	TextToSpeech tts;
	private SharedPreferences prefs;

	public TTS(Context context) {
		tts = new TextToSpeech(context, this);
		this.prefs = PreferenceManager.getDefaultSharedPreferences(context);
	}

	@Override
	public void onInit(int status) {
		tts.setLanguage(Locale.US);
	}

	public void speak(String string) {
		if (tts != null) {
			if (shouldEnableTTS()) {
				tts.speak(string, TextToSpeech.QUEUE_FLUSH, null);
			}
		}
	}

	private boolean shouldEnableTTS() {
		return prefs.getBoolean("pref_enable_tts", false);
	}
}
