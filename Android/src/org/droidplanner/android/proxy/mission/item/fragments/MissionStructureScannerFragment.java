package org.droidplanner.android.proxy.mission.item.fragments;

import java.util.List;

import org.droidplanner.R;
import org.droidplanner.android.api.services.DroidPlannerApi;
import org.droidplanner.android.proxy.mission.MissionProxy;
import org.droidplanner.android.widgets.spinnerWheel.CardWheelHorizontalView;
import org.droidplanner.android.widgets.spinnerWheel.adapters.NumericWheelAdapter;
import org.droidplanner.core.mission.MissionItemType;
import org.droidplanner.core.mission.survey.CylindricalSurvey;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

public class MissionCylindricalSurveyFragment extends MissionDetailFragment
		implements CardWheelHorizontalView.OnCardWheelChangedListener,
		CompoundButton.OnCheckedChangeListener {

	@Override
	protected int getResource() {
		return R.layout.fragment_editor_detail_cylindrical_mapping;
	}

    @Override
    public void onApiConnected(DroidPlannerApi api){
        super.onApiConnected(api);

        final View view = getView();
        final Context context = getActivity().getApplicationContext();

        Log.d("DEBUG", "onViewCreated");
        typeSpinner.setSelection(commandAdapter.getPosition(MissionItemType.CYLINDRICAL_SURVEY));

        CardWheelHorizontalView radiusPicker = (CardWheelHorizontalView) view.findViewById(R.id
                .radiusPicker);
        radiusPicker.setViewAdapter(new NumericWheelAdapter(context,
                R.layout.wheel_text_centered, 2, 50, "%d m"));
        radiusPicker.addChangingListener(this);

        CardWheelHorizontalView startAltitudeStepPicker = (CardWheelHorizontalView) view
                .findViewById(R.id.startAltitudePicker);
        startAltitudeStepPicker.setViewAdapter(new NumericWheelAdapter(context,
                R.layout.wheel_text_centered, MIN_ALTITUDE, MAX_ALTITUDE, "%d m"));
        startAltitudeStepPicker.addChangingListener(this);

        CardWheelHorizontalView endAltitudeStepPicker = (CardWheelHorizontalView) view
                .findViewById(R.id.heightStepPicker);
        endAltitudeStepPicker.setViewAdapter(new NumericWheelAdapter(context,
                R.layout.wheel_text_centered, MIN_ALTITUDE, MAX_ALTITUDE,				"%d m"));
        endAltitudeStepPicker.addChangingListener(this);

        CardWheelHorizontalView mNumberStepsPicker = (CardWheelHorizontalView) view.findViewById
                (R.id.stepsPicker);
        mNumberStepsPicker.setViewAdapter(new NumericWheelAdapter(context,
                R.layout.wheel_text_centered, 1, 10, "%d"));
        mNumberStepsPicker.addChangingListener(this);

        CheckBox checkBoxAdvanced = (CheckBox) view.findViewById(R.id.checkBoxSurveyCrossHatch);
        checkBoxAdvanced.setOnCheckedChangeListener(this);

// Use the first one as reference.
        final CylindricalSurvey firstItem = getMissionItems().get(0);
        radiusPicker.setCurrentValue((int) firstItem.getRadius().valueInMeters());
        startAltitudeStepPicker.setCurrentValue((int) firstItem.getStartAltitude().valueInMeters());
        endAltitudeStepPicker.setCurrentValue((int) firstItem.getEndAltitude().valueInMeters());
        mNumberStepsPicker.setCurrentValue(firstItem.getNumberOfSteps());
        checkBoxAdvanced.setChecked(firstItem.isCrossHatchEnabled());
    }

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        for(CylindricalSurvey item: getMissionItems())
            item.enableCrossHatch(isChecked);

        MissionProxy missionProxy = getMissionProxy();
        if(missionProxy != null)
            missionProxy.getMission().notifyMissionUpdate();
	}

	@Override
	public void onChanged(CardWheelHorizontalView cardWheel, int oldValue, int newValue) {
		switch (cardWheel.getId()) {
		case R.id.radiusPicker: {
            for(CylindricalSurvey item: getMissionItems())
                item.setRadius(newValue);

            MissionProxy missionProxy = getMissionProxy();
            if (missionProxy != null)
                missionProxy.getMission().notifyMissionUpdate();
            break;
        }

		case R.id.startAltitudePicker: {
            for(CylindricalSurvey item: getMissionItems())
                item.setStartAltitude(newValue);

            MissionProxy missionProxy = getMissionProxy();
            if (missionProxy != null)
                missionProxy.getMission().notifyMissionUpdate();
            break;
        }

		case R.id.heightStepPicker:
            for(CylindricalSurvey item: getMissionItems())
                item.setAltitudeStep(newValue);
			break;
		case R.id.stepsPicker:
            for(CylindricalSurvey item: getMissionItems())
                item.setNumberOfSteps(newValue);
			break;
		}
	}

    @Override
    protected List<CylindricalSurvey> getMissionItems(){
        return (List<CylindricalSurvey>) super.getMissionItems();
    }
}
