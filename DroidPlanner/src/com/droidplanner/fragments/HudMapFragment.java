package com.droidplanner.fragments;

import java.util.List;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.gesture.GestureOverlayView;
import android.graphics.Point;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.droidplanner.R;
import com.droidplanner.fragments.PathGesture.OnPathFinishedListner;

public class HudMapFragment extends Fragment implements OnPathFinishedListner {
	public enum State {
		HUD, HUD_AND_MAP, MAP
	}

	public State state = State.HUD_AND_MAP;
	private HudFragment hud;
	private PlanningMapFragment map;
	private View centerDivider;
	private PathGesture pathGesture;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		RelativeLayout view = (RelativeLayout) inflater.inflate(
				R.layout.fragment_hud_map, container, false);

		centerDivider = view.findViewById(R.id.strutCenter);
		GestureOverlayView overlay = (GestureOverlayView) view
				.findViewById(R.id.overlay1);
		pathGesture = new PathGesture(overlay);
		pathGesture.setOnPathFinishedListner(this);

		update();

		return view;
	}

	public void nextLayout() {
		switch (state) {
		case HUD:
			update(State.HUD_AND_MAP);
			break;
		case HUD_AND_MAP:
			update(State.MAP);
			break;
		case MAP:
			update(State.HUD);
			break;
		default:
			break;
		}

	}

	public void update(State newState) {
		state = newState;
		update();
	}

	private void update() {
		FragmentTransaction transaction = getFragmentManager()
				.beginTransaction();
		RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(0,
				0);
		if (hud != null) {
			transaction.detach(hud);
		}
		if (map != null) {
			transaction.detach(map);
		}
		switch (state) {
		case HUD:
			layout.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			addHud(transaction);
			break;
		case MAP:
			layout.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			addMap(transaction);
			break;
		case HUD_AND_MAP:
			layout.addRule(RelativeLayout.CENTER_HORIZONTAL);
			addHud(transaction);
			addMap(transaction);
		}
		transaction.commit();
		centerDivider.setLayoutParams(layout);
	}

	private void addMap(FragmentTransaction transaction) {
		if (map != null) {
			transaction.attach(map);
		} else {
			map = new PlanningMapFragment();
			transaction.add(R.id.containerMap, map);
		}
	}

	private void addHud(FragmentTransaction transaction) {
		if (hud != null) {
			transaction.attach(hud);
		} else {
			hud = new HudFragment();
			transaction.add(R.id.containerHud, hud);
		}
	}

	@Override
	public void onPathFinished(List<Point> path) {
		pathGesture.enableGestureDetection();		
	}

}
