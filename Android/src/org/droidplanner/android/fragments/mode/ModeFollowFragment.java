package org.droidplanner.android.fragments.mode;

import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.gcs.FollowMe;
import org.droidplanner.core.helpers.units.Length;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class ModeFollowFragment extends ModeGuidedFragment implements
		OnClickListener {
	private Button radiusPlus1;
	private Button radiusMinus1;
	private TextView radiusTextView;
	private FollowMe followMe;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		DroidPlannerApp app = (DroidPlannerApp) getActivity().getApplication();
		followMe = app.followMe;
		drone = app.getDrone();
		View view = inflater.inflate(R.layout.fragment_mode_follow, container,
				false);

		setupViews(view);
		setupListener();
		updateLabel();
		return view;
	}

	protected void setupViews(View parentView) {
		radiusPlus1 = (Button) parentView
				.findViewById(R.id.button_radius_plus_1);
		radiusMinus1 = (Button) parentView
				.findViewById(R.id.button_radius_minus_1);
		radiusTextView = (TextView) parentView.findViewById(R.id.follow_radius);
		super.setupViews(parentView);
	}

	protected void setupListener() {
		radiusPlus1.setOnClickListener(this);
		radiusMinus1.setOnClickListener(this);
		super.setupListener();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_radius_minus_1:
			followMe.changeRadius(-1.0);
			break;
		case R.id.button_radius_plus_1:
			followMe.changeRadius(+1.0);
			break;
		default:
			super.onClick(v);
			break;
		}
		updateLabel();
	}

	protected void updateLabel() {
		super.updateLabel();
		Length radius = followMe.getRadius();
		if(radiusTextView!= null){
			this.radiusTextView.setText("Radius: (" + radius + ")");
		}
	}

}
