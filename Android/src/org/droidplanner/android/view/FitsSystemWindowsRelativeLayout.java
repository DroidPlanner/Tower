package org.droidplanner.android.view;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Overrides parent's fitSystemWindows to allow the call to be propagated.
 */
public class FitsSystemWindowsRelativeLayout extends RelativeLayout {
	public FitsSystemWindowsRelativeLayout(Context context) {
		super(context);
	}

	public FitsSystemWindowsRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public FitsSystemWindowsRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected boolean fitSystemWindows(Rect insets) {
		final Rect insetsCopy = new Rect(insets);
		super.fitSystemWindows(insetsCopy);
		return false;
	}
}
