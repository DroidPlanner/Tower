package com.droidplanner.widgets;

import android.os.Handler;
import android.os.SystemClock;
import android.view.MenuItem;

public class TimerView {

	private MenuItem timerValue;

	private Handler customHandler = new Handler();

	private long startTime;
	private long offsetTime;


	public TimerView(MenuItem propeler) {
		this.timerValue = propeler;
		resetTimer();
	}

	public void resetTimer() {
		startTime = SystemClock.elapsedRealtime();
		offsetTime = 0;
		timerValue.setTitle(" 00:00");
	}

	public void start() {
		customHandler.postDelayed(updateTimerThread, 0);
		startTime = SystemClock.elapsedRealtime() - offsetTime;
	}

	public void stop() {
		customHandler.removeCallbacks(updateTimerThread);
		offsetTime = SystemClock.elapsedRealtime() - startTime; // elapsed time so far
	}

	private Runnable updateTimerThread = new Runnable() {

		public void run() {

			long timeInSeconds = (SystemClock.elapsedRealtime() - startTime)/1000;

			long minutes = timeInSeconds/60;
			long seconds = timeInSeconds%60;
			timerValue.setTitle(String.format(" %02d:%02d", minutes,seconds));

			customHandler.postDelayed(this, 1000);
		}
	};
}
