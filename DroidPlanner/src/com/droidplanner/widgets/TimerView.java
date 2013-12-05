package com.droidplanner.widgets;

import android.os.Handler;
import android.os.SystemClock;
import android.view.MenuItem;

public class TimerView {

	private MenuItem timerValue;

	private Handler customHandler = new Handler();

	private long startTime = 0L;

	public TimerView(MenuItem propeler) {
		this.timerValue = propeler;
		timerValue.setTitle("00:00");
	}

	public void reStart() {
		startTime = SystemClock.elapsedRealtime();
		customHandler.postDelayed(updateTimerThread, 0);
	}

	public void stop() {
		customHandler.removeCallbacks(updateTimerThread);
	}

	private Runnable updateTimerThread = new Runnable() {

		public void run() {

			long timeInSeconds = (SystemClock.elapsedRealtime() - startTime)/1000;

			long minutes = timeInSeconds/60;
			long seconds = timeInSeconds%60;
			timerValue.setTitle(String.format("%02d:%02d", minutes,seconds));

			customHandler.postDelayed(this, 1000);
		}

	};
}
