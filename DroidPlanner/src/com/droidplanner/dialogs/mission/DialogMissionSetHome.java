package com.droidplanner.dialogs.mission;

import java.util.Locale;

import android.content.DialogInterface;
import android.view.View;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;
import com.droidplanner.R;
import com.droidplanner.gps.GPS;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText.OnTextSeekBarChangedListner;

public class DialogMissionSetHome extends DialogMission implements
		OnTextSeekBarChangedListner, OnCheckedChangeListener,
		OnItemSelectedListener {

	private SeekBarWithText altitudeSeekBar;
	private CheckBox useCurrentCheckBox;
	private EditText lonEditText;
	private EditText latEditText;
	private Spinner coordSrcSpinner;

	private ArrayAdapter<CharSequence> coord_adapter;

	private GPS gps;

	@Override
	protected int getResource() {
		return R.layout.dialog_mission_set_home;
	}

	protected View buildView() {
		super.buildView();
		findViewItems();
		setupViewItems();
		setupViewListeners();
		setupViewEnables();
		
		return view;
	}

	private void setupViewEnables() {
		coordSrcSpinner.setEnabled(!useCurrentCheckBox.isChecked());
		lonEditText.setEnabled(!useCurrentCheckBox.isChecked() && coordSrcSpinner.getSelectedItemPosition() == 2);
		latEditText.setEnabled(!useCurrentCheckBox.isChecked() && coordSrcSpinner.getSelectedItemPosition() == 2);
	}

	private void findViewItems() {
		// TODO Auto-generated method stub
		altitudeSeekBar = (SeekBarWithText) view.findViewById(R.id.waypointSpeed);
		coordSrcSpinner = (Spinner) view.findViewById(R.id.spinnerCoordSrc);
		lonEditText = (EditText) view.findViewById(R.id.editTextLon);
		latEditText = (EditText) view.findViewById(R.id.editTextLat);
		useCurrentCheckBox = (CheckBox) view.findViewById(R.id.checkBoxHomeCurrent);
	}

	private void setupViewItems() {
		altitudeSeekBar.setValue(wp.getHeight());

		coord_adapter = ArrayAdapter.createFromResource(context,
				R.array.CoordSource, android.R.layout.simple_spinner_item);
		coord_adapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		coordSrcSpinner.setSelection(wp.homeType);


		if (wp.missionItem.param1 > 0)
			useCurrentCheckBox.setChecked(true);
		else
			useCurrentCheckBox.setChecked(false);

		useCurrentCheckBox.requestFocus();

		coordSrcSpinner.setEnabled(useCurrentCheckBox.isChecked());
		lonEditText.setText(String.format(Locale.ENGLISH, "%1.12f ",
				wp.missionItem.y));
		latEditText.setText(String.format(Locale.ENGLISH, "%1.12f ",
				wp.missionItem.x));

		lonEditText.setEnabled(false);
		latEditText.setEnabled(false);
	}

	private void setupViewListeners() {
		altitudeSeekBar.setOnChangedListner(this);
		useCurrentCheckBox.setOnCheckedChangeListener(this);
		coordSrcSpinner.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				setupHomePosition();
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
	}


	private void setupHomePosition() {
		
		setupViewEnables();
		
		switch (coordSrcSpinner.getSelectedItemPosition()) {
		case 0:
			wp.homeType = 0;
			lonEditText.setText(String.format(Locale.ENGLISH, "%1.12f ",
					wp.missionItem.y));
			latEditText.setText(String.format(Locale.ENGLISH, "%1.12f ",
					wp.missionItem.x));
			break;
		case 1:
			wp.homeType = 1;
			gps = new GPS(this.context);

			// check if GPS enabled
			if (gps.canGetLocation()) {

				latEditText.setText(String.format(Locale.ENGLISH, "%1.12f ",gps.getLatitude()));
				lonEditText.setText(String.format(Locale.ENGLISH, "%1.12f ",gps.getLongitude()));
			} else {
				// can't get location
				// GPS or Network is not enabled
				// Ask user to enable GPS/network in settings
				gps.showSettingsAlert();
			}
			break;
		case 2:
			wp.homeType = 2;
			break;
		default:
		}

	}

	private void setLonLatValue() {
		wp.missionItem.param1 = useCurrentCheckBox.isChecked() ? 1 : 0;
		if (!useCurrentCheckBox.isChecked()) {
			wp.missionItem.x = Float.valueOf(latEditText.getText().toString());
			wp.missionItem.y = Float.valueOf(lonEditText.getText().toString());
		}
	}
	@Override
	public void onSeekBarChanged() {
		wp.setHeight(altitudeSeekBar.getValue());
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		setupViewEnables();
		setLonLatValue();
	}

	@Override
	public void onClick(DialogInterface arg0, int which) {
		setLonLatValue();
		super.onClick(arg0, which);
	}

}
