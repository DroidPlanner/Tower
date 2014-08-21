package org.droidplanner.android.maps.providers.mapbox;

import android.view.MotionEvent;

public class RotationGestureDetector {

	public interface RotationListener {
		public void onRotate(float deltaAngle);
	}

	protected float mRotation;
	private RotationListener mListener;

	public RotationGestureDetector(RotationListener listener) {
		mListener = listener;
	}

	private float rotation(MotionEvent event) {
		double delta_x = (event.getX(0) - event.getX(1));
		double delta_y = (event.getY(0) - event.getY(1));
		double radians = Math.atan2(delta_y, delta_x);
		return (float) Math.toDegrees(radians);
	}

	public boolean onTouch(MotionEvent e) {
		if (e.getPointerCount() != 2)
			return false;

		if (e.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN) {
			mRotation = rotation(e);
		}

		float rotation = rotation(e);
		float delta = rotation - mRotation;
		mRotation += delta;
		mListener.onRotate(delta);
        return true;
	}

}
