package org.droidplanner.android.proxy.mission.item.fragments;

import java.util.List;

import org.droidplanner.R;
import org.droidplanner.R.id;
import org.droidplanner.android.proxy.mission.item.adapters.CamerasAdapter;
import org.droidplanner.android.api.services.DroidPlannerApi;
import org.droidplanner.android.proxy.mission.MissionProxy;
import org.droidplanner.android.widgets.spinnerWheel.CardWheelHorizontalView;
import org.droidplanner.android.widgets.spinnerWheel.adapters.NumericWheelAdapter;
import org.droidplanner.android.widgets.spinners.SpinnerSelfSelect;
import org.droidplanner.core.helpers.units.Altitude;
import org.droidplanner.core.mission.MissionItemType;
import org.droidplanner.core.mission.survey.CameraInfo;
import org.droidplanner.core.mission.waypoints.StructureScanner;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;

public class MissionStructureScannerFragment extends MissionDetailFragment
		implements CardWheelHorizontalView.OnCardWheelChangedListener,
		CompoundButton.OnCheckedChangeListener {

    private CamerasAdapter cameraAdapter;
    
	@Override
	protected int getResource() {
		return R.layout.fragment_editor_detail_structure_scanner;
	}

    @Override
    public void onApiConnected(DroidPlannerApi api){
        super.onApiConnected(api);

        final View view = getView();
        final Context context = getActivity().getApplicationContext();

        Log.d("DEBUG", "onViewCreated");
        typeSpinner.setSelection(commandAdapter.getPosition(MissionItemType.CYLINDRICAL_SURVEY));
		
        cameraAdapter = new CamerasAdapter(getActivity(),
                android.R.layout.simple_spinner_dropdown_item);
		SpinnerSelfSelect cameraSpinner = (SpinnerSelfSelect) view.findViewById(id
                .cameraFileSpinner);
        cameraSpinner.setAdapter(cameraAdapter);
        cameraSpinner.setOnSpinnerItemSelectedListener(this);

        CardWheelHorizontalView radiusPicker = (CardWheelHorizontalView) view.findViewById(R.id
                .radiusPicker);
        radiusPicker.setViewAdapter(new NumericWheelAdapter(context,
                R.layout.wheel_text_centered, 2, 100, "%d m"));
        radiusPicker.addChangingListener(this);

        CardWheelHorizontalView startAltitudeStepPicker = (CardWheelHorizontalView) view
                .findViewById(R.id.startAltitudePicker);
        startAltitudeStepPicker.setViewAdapter(new NumericWheelAdapter(context,
                R.layout.wheel_text_centered, MIN_ALTITUDE, MAX_ALTITUDE, "%d m"));
        startAltitudeStepPicker.addChangingListener(this);

        CardWheelHorizontalView endAltitudeStepPicker = (CardWheelHorizontalView) view
                .findViewById(R.id.heightStepPicker);
        endAltitudeStepPicker.setViewAdapter(new NumericWheelAdapter(context,
                R.layout.wheel_text_centered, 1, MAX_ALTITUDE,				"%d m"));
        endAltitudeStepPicker.addChangingListener(this);

        CardWheelHorizontalView mNumberStepsPicker = (CardWheelHorizontalView) view.findViewById
                (R.id.stepsPicker);
        mNumberStepsPicker.setViewAdapter(new NumericWheelAdapter(context,
                R.layout.wheel_text_centered, 1, 10, "%d"));
        mNumberStepsPicker.addChangingListener(this);

        CheckBox checkBoxAdvanced = (CheckBox) view.findViewById(R.id.checkBoxSurveyCrossHatch);
        checkBoxAdvanced.setOnCheckedChangeListener(this);

// Use the first one as reference.
        final StructureScanner firstItem = getMissionItems().get(0);
        cameraAdapter.setTitle(firstItem.getCamera());
        radiusPicker.setCurrentValue((int) firstItem.getRadius().valueInMeters());
        startAltitudeStepPicker.setCurrentValue((int) firstItem.getCoordinate().getAltitude().valueInMeters());
        endAltitudeStepPicker.setCurrentValue((int) firstItem.getEndAltitude().valueInMeters());
        mNumberStepsPicker.setCurrentValue(firstItem.getNumberOfSteps());
        checkBoxAdvanced.setChecked(firstItem.isCrossHatchEnabled());
    }

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        for(StructureScanner item: getMissionItems())
            item.enableCrossHatch(isChecked);

        MissionProxy missionProxy = getMissionProxy();
        if(missionProxy != null)
            missionProxy.getMission().notifyMissionUpdate();
	}

	@Override
	public void onChanged(CardWheelHorizontalView cardWheel, int oldValue, int newValue) {
		switch (cardWheel.getId()) {
		case R.id.radiusPicker: {
            for(StructureScanner item: getMissionItems())
                item.setRadius(newValue);
            break;
        }

		case R.id.startAltitudePicker: {
            for(StructureScanner item: getMissionItems())
                item.setAltitude(new Altitude(newValue));
            break;
        }

		case R.id.heightStepPicker:
            for(StructureScanner item: getMissionItems())
                item.setAltitudeStep(newValue);
			break;
		case R.id.stepsPicker:
            for(StructureScanner item: getMissionItems())
                item.setNumberOfSteps(newValue);
			break;
		}

        MissionProxy missionProxy = getMissionProxy();
        if (missionProxy != null)
            missionProxy.getMission().notifyMissionUpdate();
	}

    @Override
    public void onSpinnerItemSelected(Spinner spinner, int position) {
        if (spinner.getId() == id.cameraFileSpinner) {
            CameraInfo cameraInfo = cameraAdapter.getCamera(position);
            cameraAdapter.setTitle(cameraInfo.name);
            for (StructureScanner scan : getMissionItems()) {
                scan.setCamera(cameraInfo);
            }
            getMissionProxy().getMission().notifyMissionUpdate();
        }
    }
    
    @Override
    protected List<StructureScanner> getMissionItems(){
        return (List<StructureScanner>) super.getMissionItems();
    }
}
