package com.droidplanner.helpers;

import android.content.Context;
import android.widget.Spinner;

public class SpinnerSelfSelect extends Spinner {
	public interface OnItemSelectedEvenIfUnchangedListner {
		void onItemSelectedEvenIfUnchanged(int position, String text);
	}

	OnItemSelectedEvenIfUnchangedListner listener;

	public SpinnerSelfSelect(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setSelection(int position) {
		super.setSelection(position);
		if (listener != null)
			listener.onItemSelectedEvenIfUnchanged(position,
					getItemAtPosition(position).toString());
	}

	public void setOnItemSelectedEvenIfUnchangedListener(
			OnItemSelectedEvenIfUnchangedListner listener) {
		this.listener = listener;
	}

}
