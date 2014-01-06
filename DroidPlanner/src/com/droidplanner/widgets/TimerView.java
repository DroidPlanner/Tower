package com.droidplanner.widgets;

import android.widget.Toast;
import android.os.Handler;
import android.os.SystemClock;
import android.view.MenuItem;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.text.style.AlignmentSpan;
import android.text.Layout.Alignment;
import com.droidplanner.drone.Drone;

public class TimerView {

	private MenuItem timerValue;
	private Handler customHandler = new Handler();
	private Drone drone;

	public TimerView(MenuItem propeler, Drone _drone) {
		this.drone = _drone;
		this.timerValue = propeler;
		customHandler.postDelayed(updateTimerThread, 0);
	}

	private Runnable updateTimerThread = new Runnable() {
		public void run() {
			long timeInSeconds = drone.state.getFlightTime();
			long minutes = timeInSeconds/60;
			long seconds = timeInSeconds%60;

			SpannableString text = new SpannableString(String.format("   Flight Time\n  %02d:%02d", minutes,seconds));
			text.setSpan(new RelativeSizeSpan(.8f), 0, 14, 0);
	        text.setSpan(new AlignmentSpan.Standard(Alignment.ALIGN_NORMAL),0, text.length()-1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			timerValue.setTitle(text);
			customHandler.postDelayed(this, 1000);
		}
	};
}
