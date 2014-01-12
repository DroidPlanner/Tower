package org.droidplanner.widgets.spinners;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Spinner;

public class SpinnerSelfSelect extends Spinner {

	public interface OnSpinnerItemSelectedListener {
		void onSpinnerItemSelected(Spinner spinner, int position, String text);
	}

	OnSpinnerItemSelectedListener listener;

	protected boolean selectable = true;

	public SpinnerSelfSelect(Context context) {
		super(context);
	}

	public SpinnerSelfSelect(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void setSelection(int position) {
		Log.d("SPIN", "selected - " + position);
		
		if (selectable) {
			forcedSetSelection(position);
		}
		
		if (listener != null) {
			listener.onSpinnerItemSelected(this, position,
					getItemAtPosition(position).toString());
		}
	}

	public void forcedSetSelection(int position) {
		super.setSelection(position);
	}

	public void setOnSpinnerItemSelectedListener(
			OnSpinnerItemSelectedListener listener) {
		this.listener = listener;
	}

}
