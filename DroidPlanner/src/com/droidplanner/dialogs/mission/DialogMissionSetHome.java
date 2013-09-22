package com.droidplanner.dialogs.mission;

import java.util.Locale;

import android.content.DialogInterface;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;

import com.droidplanner.R;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText;
import com.droidplanner.widgets.SeekBarWithText.SeekBarWithText.OnTextSeekBarChangedListner;

public class DialogMissionSetHome extends DialogMission implements
		OnTextSeekBarChangedListner, OnCheckedChangeListener  {

	private SeekBarWithText altitudeSeekBar;
	private CheckBox useCurrentCheckBox;
	private EditText lonEditText;
	private EditText latEditText;
	private Spinner coordSrcSpinner;
	
	@Override
	protected int getResource() {
		return R.layout.dialog_mission_set_home;
	}
	
	protected View buildView() {
		super.buildView();
		altitudeSeekBar = (SeekBarWithText) view
				.findViewById(R.id.waypointSpeed);
		altitudeSeekBar.setValue(wp.getHeight());
		altitudeSeekBar.setOnChangedListner(this);
		
		coordSrcSpinner = (Spinner) view
				.findViewById(R.id.spinnerCoordSrc);		
		lonEditText = (EditText) view
				.findViewById(R.id.editTextLon);		
		latEditText = (EditText) view
				.findViewById(R.id.editTextLat);
		useCurrentCheckBox = (CheckBox) view
				.findViewById(R.id.checkBoxHomeCurrent);
		useCurrentCheckBox.setOnCheckedChangeListener(this);
		
		if(wp.missionItem.param1>0)
			useCurrentCheckBox.setChecked(true);
		else
			useCurrentCheckBox.setChecked(false);
		
		useCurrentCheckBox.requestFocus();
		
		coordSrcSpinner.setEnabled(useCurrentCheckBox.isChecked());
		lonEditText.setText(String.format(Locale.ENGLISH, "%1.8f ", wp.missionItem.y));
		latEditText.setText(String.format(Locale.ENGLISH, "%1.8f ", wp.missionItem.x));
		
		lonEditText.setEnabled(false);
		latEditText.setEnabled(false);
		

		return view;
	}

	@Override
	public void onSeekBarChanged() {
		wp.setHeight(altitudeSeekBar.getValue());
	}
 
	@Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		wp.missionItem.param1 = useCurrentCheckBox.isChecked()?1:0;
		coordSrcSpinner.setEnabled(useCurrentCheckBox.isChecked());
		setLonLatValue();
    }
	
	@Override
	public void onClick(DialogInterface arg0, int which) {
		
		setLonLatValue();
		super.onClick(arg0, which);
	}

	private void setLonLatValue(){
		if(useCurrentCheckBox.isChecked()){
			//Todo : set wp.missionItem.x & y get from drone location
		}
		else {
			wp.missionItem.x = Float.valueOf(latEditText.getText().toString()); 
			wp.missionItem.y = Float.valueOf(lonEditText.getText().toString()); 
		}
	}

}
