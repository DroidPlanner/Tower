package com.droidplanner.widgets.spinners;

import android.content.Context;
import android.widget.Spinner;

public class SpinnerSelfSelect extends Spinner {
	public interface OnSpinnerItemSelectedListener {
		void onSpinnerItemSelected(Spinner spinner, int position, String text);
	}

	OnSpinnerItemSelectedListener listener;

	public SpinnerSelfSelect(Context context) {
		super(context);
	}

	@Override
	public void setSelection(int position) {
		super.setSelection(position);
		if (listener != null)
			listener.onSpinnerItemSelected(this,position,
					getItemAtPosition(position).toString());
	}

	public void setOnSpinnerItemSelectedListener(
			OnSpinnerItemSelectedListener listener) {
		this.listener = listener;
	}

}
