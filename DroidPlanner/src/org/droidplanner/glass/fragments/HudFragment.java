package org.droidplanner.glass.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.droidplanner.R;
import com.droidplanner.activitys.helpers.SuperActivity;
import org.droidplanner.glass.utils.hud.HUD;

public class HudFragment extends Fragment {

	private HUD hudWidget;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_hud, container, false);
		hudWidget = (HUD) view.findViewById(R.id.hudWidget);
		return view;
	}

    @Override
    public void onStart(){
        super.onStart();
        ((SuperActivity) getActivity()).app.drone.events.addDroneListener(hudWidget);
    }

    @Override
    public void onStop(){
        super.onStop();
        ((SuperActivity) getActivity()).app.drone.events.removeDroneListener(hudWidget);
    }

}
