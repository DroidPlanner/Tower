package com.droidplanner.fragments;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.droidplanner.R;
import com.google.android.gms.maps.MapFragment;

public class HudMapFragment extends Fragment {
	public enum State {
		HUD, HUD_AND_MAP, MAP
	}

	public State state = State.HUD_AND_MAP;
	private HudFragment hud;
	private MapFragment map;
	private View centerDivider;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		RelativeLayout view = (RelativeLayout) inflater.inflate(
				R.layout.fragment_hud_map, container, false);
		centerDivider = view.findViewById(R.id.strutCenter);
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
			map = new MapFragment();
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

}
