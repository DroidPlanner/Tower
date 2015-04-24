package org.droidplanner.android.fragments.helpers;

import java.util.ArrayList;
import java.util.List;

import org.droidplanner.android.R;
import org.droidplanner.android.fragments.EditorMapFragment;

import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGestureListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.util.MathUtils;

public class GestureMapFragment extends Fragment implements OnGestureListener {
	private static final int TOLERANCE = 15;
	private static final int STROKE_WIDTH = 3;

	private double toleranceInPixels;

	public interface OnPathFinishedListener {

		void onPathFinished(List<LatLong> path);
	}

	private GestureOverlayView overlay;
	private OnPathFinishedListener listener;
    private EditorMapFragment mapFragment;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_gesture_map, container, false);
	}

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        final FragmentManager fm = getChildFragmentManager();
        mapFragment = ((EditorMapFragment) fm.findFragmentById(R.id.gesture_map_fragment));
        if(mapFragment == null){
            mapFragment = new EditorMapFragment();
            fm.beginTransaction().add(R.id.gesture_map_fragment, mapFragment).commit();
        }

        overlay = (GestureOverlayView) view.findViewById(R.id.overlay1);
        overlay.addOnGestureListener(this);
        overlay.setEnabled(false);

        overlay.setGestureStrokeWidth(scaleDpToPixels(STROKE_WIDTH));
        toleranceInPixels = scaleDpToPixels(TOLERANCE);
    }

	private int scaleDpToPixels(double value) {
		final float scale = getResources().getDisplayMetrics().density;
		return (int) Math.round(value * scale);
	}

    public EditorMapFragment getMapFragment(){
        return mapFragment;
    }

	public void enableGestureDetection() {
		overlay.setEnabled(true);
	}

	public void disableGestureDetection() {
		overlay.setEnabled(false);
	}

	public void setOnPathFinishedListener(OnPathFinishedListener listener) {
		this.listener = listener;
	}

	@Override
	public void onGestureEnded(GestureOverlayView arg0, MotionEvent arg1) {
		overlay.setEnabled(false);
		List<LatLong> path = decodeGesture();
		if (path.size() > 1) {
			path = MathUtils.simplify(path, toleranceInPixels);
		}
		listener.onPathFinished(path);
	}

	private List<LatLong> decodeGesture() {
		List<LatLong> path = new ArrayList<LatLong>();
		extractPathFromGesture(path);
		return path;
	}

	private void extractPathFromGesture(List<LatLong> path) {
		float[] points = overlay.getGesture().getStrokes().get(0).points;
		for (int i = 0; i < points.length; i += 2) {
			path.add(new LatLong((int) points[i], (int) points[i + 1]));
		}
	}

	@Override
	public void onGesture(GestureOverlayView arg0, MotionEvent arg1) {
	}

	@Override
	public void onGestureCancelled(GestureOverlayView arg0, MotionEvent arg1) {
	}

	@Override
	public void onGestureStarted(GestureOverlayView arg0, MotionEvent arg1) {
	}

}
