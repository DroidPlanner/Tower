package org.droidplanner.android.view.spinners;

import org.droidplanner.android.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Spinner;

/**
 * Spinner like widget that has the ability to disable updates to its view based
 * on a settable flag. This is used in instances of the application that
 * requires confirmation from the drone before updating the GCS ui.
 */
public class SpinnerSelfSelect extends Spinner {

	public interface OnSpinnerItemSelectedListener {
		void onSpinnerItemSelected(Spinner spinner, int position);
	}

	private OnSpinnerItemSelectedListener listener;

	/**
	 * View update flag. If set to true, the widget updates its view on item
	 * selection. Otherwise, the view can only be updated programmatically.
	 */
	private boolean isSelectable;

	public SpinnerSelfSelect(Context context) {
		this(context, null);
	}

	public SpinnerSelfSelect(Context context, AttributeSet attrs) {
		super(context, attrs);

		final TypedArray attributes = context.obtainStyledAttributes(attrs,
				R.styleable.SpinnerSelfSelect, 0, 0);

		try {
			isSelectable = attributes.getBoolean(R.styleable.SpinnerSelfSelect_isSelectable, true);
		} finally {
			attributes.recycle();
		}
	}

	@Override
	public void setSelection(int position) {
		Log.d("SPIN", "selected - " + position);

		if (isSelectable) {
			forcedSetSelection(position);
		}

		if (listener != null) {
			listener.onSpinnerItemSelected(this, position);
		}
	}

	public void forcedSetSelection(int position) {
		super.setSelection(position);
	}

	public void setOnSpinnerItemSelectedListener(OnSpinnerItemSelectedListener listener) {
		this.listener = listener;
	}

	/*
	 * Widget accessor properties
	 */
	public boolean isSelectable() {
		return isSelectable;
	}

	public void setSelectable(boolean isSelectable) {
		this.isSelectable = isSelectable;
		invalidate();
	}
}
