package org.droidplanner.android.mission.item.fragments;

import org.droidplanner.R;
import org.droidplanner.android.proxy.mission.item.fragments.MissionDetailFragment;
import org.droidplanner.android.widgets.SeekBarWithText.SeekBarWithText;
import org.droidplanner.core.mission.MissionItemType;
import org.droidplanner.core.mission.waypoints.Circle;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;


public class MissionCircleFragment extends MissionDetailFragment implements
        SeekBarWithText.OnTextSeekBarChangedListener, OnCheckedChangeListener {


    private SeekBarWithText altitudeSeekBar;
    private SeekBarWithText loiterTurnSeekBar;
    private SeekBarWithText loiterRadiusSeekBar;
    private CheckBox loiterCCW;
	private CheckBox checkBoxAdvanced;
	private SeekBarWithText altitudeStepSeekBar;
	private SeekBarWithText numberStepSeekBar;

    @Override
    protected int getResource() {
        return R.layout.fragment_editor_detail_circle;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        typeSpinner.setSelection(commandAdapter.getPosition(MissionItemType.CIRCLE));

        Circle item = (Circle) this.itemRender.getMissionItem();

        //loiterCCW = (CheckBox) view.findViewById(R.id.loiter_ccw);
        //loiterCCW.setChecked(!item.isOrbitCW());
        //loiterCCW.setOnCheckedChangeListener(this);
        
        checkBoxAdvanced = (CheckBox) view.findViewById(R.id.checkBoxAdvanced);
        checkBoxAdvanced.setOnCheckedChangeListener(this);
        

        altitudeStepSeekBar = (SeekBarWithText) view.findViewById(R.id.altitudeStep);
        altitudeStepSeekBar.setValue(item.getAltitudeStep());
        altitudeStepSeekBar.setOnChangedListener(this);

        numberStepSeekBar = (SeekBarWithText) view.findViewById(R.id.numberSteps);
        numberStepSeekBar.setOnChangedListener(this);
        numberStepSeekBar.setValue(item.getNumberOfSteps());

        altitudeSeekBar = (SeekBarWithText) view.findViewById(R.id.altitudeView);
        altitudeSeekBar.setValue(item.getCoordinate().getAltitude().valueInMeters());
        altitudeSeekBar.setOnChangedListener(this);

        loiterTurnSeekBar = (SeekBarWithText) view.findViewById(R.id.loiterTurn);
        loiterTurnSeekBar.setOnChangedListener(this);
        loiterTurnSeekBar.setValue(item.getNumeberOfTurns());

        loiterRadiusSeekBar = (SeekBarWithText) view.findViewById(R.id.loiterRadius);
        //loiterRadiusSeekBar.setAbsValue(item.getRadius());
        loiterRadiusSeekBar.setOnChangedListener(this);
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    	if (buttonView == checkBoxAdvanced) {
    		int visibility = (isChecked)?View.VISIBLE:View.GONE;
			altitudeStepSeekBar.setVisibility(visibility);
    		numberStepSeekBar.setVisibility(visibility);
		}
    }


    @Override
    public void onSeekBarChanged() {
        Circle item = (Circle) this.itemRender.getMissionItem();

        item.getCoordinate().getAltitude().set(altitudeSeekBar.getValue());
        item.setTurns((int) loiterTurnSeekBar.getValue());
        //item.setOrbitalRadius(loiterRadiusSeekBar.getValue());
        //item.setYawAngle(yawSeekBar.getValue());
        
        if (checkBoxAdvanced.isChecked()) {
        	item.setMultiCircle((int) numberStepSeekBar.getValue(),altitudeStepSeekBar.getValue());			
		}else{
			item.setSingleCircle();
		}
    }

}
