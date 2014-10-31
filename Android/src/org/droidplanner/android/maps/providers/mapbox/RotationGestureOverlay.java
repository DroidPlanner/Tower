package org.droidplanner.android.maps.providers.mapbox;

import android.view.MotionEvent;

import com.mapbox.mapboxsdk.overlay.SafeDrawOverlay;
import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.views.safecanvas.ISafeCanvas;

public class RotationGestureOverlay extends SafeDrawOverlay implements
		RotationGestureDetector.RotationListener {

	private final RotationGestureDetector mRotationDetector;
	private MapView mMapView;

	public RotationGestureOverlay(MapView mapView) {
		mMapView = mapView;
		mRotationDetector = new RotationGestureDetector(this);
	}

	@Override
	protected void drawSafe(ISafeCanvas canvas, MapView mapView, boolean shadow) {
		// No drawing necessary
	}

	@Override
	public boolean onTouchEvent(MotionEvent event, MapView mapView) {
		if (this.isEnabled()) {
			return mRotationDetector.onTouch(event);
		}
		return super.onTouchEvent(event, mapView);
	}

	@Override
	public void onRotate(float deltaAngle) {
		mMapView.setMapOrientation(mMapView.getMapOrientation() + deltaAngle);
	}

}
