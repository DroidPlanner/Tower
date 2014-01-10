package org.droidplanner.fragments.mission;

import org.droidplanner.R;
import org.droidplanner.R.id;
import org.droidplanner.fragments.mission.survey.CamerasAdapter;
import org.droidplanner.widgets.SeekBarWithText.SeekBarWithText;
import org.droidplanner.widgets.SeekBarWithText.SeekBarWithText.OnTextSeekBarChangedListner;
import org.droidplanner.widgets.spinners.SpinnerSelfSelect;
import org.droidplanner.widgets.spinners.SpinnerSelfSelect.OnSpinnerItemSelectedListener;

import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

public class MissionSurveyFragment extends MissionDetailFragment implements
		OnClickListener, OnTextSeekBarChangedListner,
		OnSpinnerItemSelectedListener {

	public SeekBarWithText overlapView;
	public SeekBarWithText angleView;
	public SeekBarWithText altitudeView;
	public SeekBarWithText sidelapView;
	public TextView distanceBetweenLinesTextView;
	public TextView areaTextView;
	public TextView distanceTextView;
	public TextView footprintTextView;
	public TextView groundResolutionTextView;
	public SpinnerSelfSelect cameraSpinner;
	public CheckBox innerWPsCheckbox;
	public TextView numberOfPicturesView;
	public TextView numberOfStripsView;
	public TextView lengthView;
	public CheckBox footprintCheckBox;
	private CamerasAdapter cameraAdapter;

	@Override
	protected int getResource() {
		return R.layout.fragment_editor_detail_survey;
	}

	@Override
	protected void setupViews(View view) {
		super.setupViews(view);
		typeSpinner.setSelection(commandAdapter
				.getPosition(MissionItemTypes.SURVEY));
		setupLocalViews(view);
	}

	public void setupLocalViews(View view) {
		cameraSpinner = (SpinnerSelfSelect) view
				.findViewById(id.cameraFileSpinner);
		footprintCheckBox = (CheckBox) view.findViewById(id.CheckBoxFootprints);

		angleView = (SeekBarWithText) view.findViewById(id.angleView);
		overlapView = (SeekBarWithText) view.findViewById(id.overlapView);
		sidelapView = (SeekBarWithText) view.findViewById(id.sidelapView);
		altitudeView = (SeekBarWithText) view.findViewById(id.altitudeView);

		innerWPsCheckbox = (CheckBox) view.findViewById(id.checkBoxInnerWPs);

		areaTextView = (TextView) view.findViewById(id.areaTextView);
		distanceBetweenLinesTextView = (TextView) view
				.findViewById(id.distanceBetweenLinesTextView);
		footprintTextView = (TextView) view.findViewById(id.footprintTextView);
		groundResolutionTextView = (TextView) view
				.findViewById(id.groundResolutionTextView);
		distanceTextView = (TextView) view.findViewById(id.distanceTextView);
		numberOfPicturesView = (TextView) view
				.findViewById(id.numberOfPicturesTextView);
		numberOfStripsView = (TextView) view
				.findViewById(id.numberOfStripsTextView);
		lengthView = (TextView) view.findViewById(id.lengthTextView);

		cameraAdapter = new CamerasAdapter(getActivity(),
				android.R.layout.simple_spinner_dropdown_item);
		cameraSpinner.setAdapter(cameraAdapter);

		footprintCheckBox.setOnClickListener(this);
		angleView.setOnChangedListner(this);
		altitudeView.setOnChangedListner(this);
		overlapView.setOnChangedListner(this);
		sidelapView.setOnChangedListner(this);
		innerWPsCheckbox.setOnClickListener(this);
		cameraSpinner.setOnSpinnerItemSelectedListener(this);

	}

	@Override
	public void onSpinnerItemSelected(Spinner spinner, int position, String text) {
		Log.d("SL", "selected " + text);
		cameraAdapter.getCamera(position);
	}

	@Override
	public void onSeekBarChanged() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

	}
}
