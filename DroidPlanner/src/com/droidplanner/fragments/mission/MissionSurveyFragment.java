package com.droidplanner.fragments.mission;

import android.view.View;

import com.droidplanner.R;

public class MissionSurveyFragment extends MissionDetailFragment {
	
	@Override
	protected int getResource() {
		return R.layout.fragment_detail_survey;
	}

	@Override
	protected void setupViews(View view) {
		super.setupViews(view);
		typeSpinner.setSelection(commandAdapter
				.getPosition(MissionItemTypes.SURVEY));
	}
}
