package com.droidplanner.widgets;

import android.os.Handler;
import android.os.SystemClock;
import android.view.MenuItem;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.text.style.AlignmentSpan;
import android.text.Layout.Alignment;

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
		SpannableString text = new SpannableString("   Flight Time\n  00:00");
		text.setSpan(new RelativeSizeSpan(.8f), 0, 14, 0);
		text.setSpan(new AlignmentSpan.Standard(Alignment.ALIGN_NORMAL),0, text.length()-1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		timerValue.setTitle(text);

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


			SpannableString text = new SpannableString(String.format("   Flight Time\n  %02d:%02d", minutes,seconds));
			text.setSpan(new RelativeSizeSpan(.8f), 0, 14, 0);
	        text.setSpan(new AlignmentSpan.Standard(Alignment.ALIGN_NORMAL),0, text.length()-1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			timerValue.setTitle(text);

			//timerValue.setTitle(String.format(" %02d:%02d", minutes,seconds));
			customHandler.postDelayed(this, 1000);
		}
	};
}
