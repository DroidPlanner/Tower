package org.droidplanner.android.proxy.mission.item.fragments;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;

import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.drone.mission.MissionItemType;
import com.o3dr.services.android.lib.drone.mission.item.complex.CameraDetail;
import com.o3dr.services.android.lib.drone.mission.item.complex.StructureScanner;
import com.o3dr.services.android.lib.drone.mission.item.complex.SurveyDetail;
import com.o3dr.services.android.lib.drone.property.CameraProxy;

import org.droidplanner.android.R;
import org.droidplanner.android.R.id;
import org.droidplanner.android.proxy.mission.MissionProxy;
import org.droidplanner.android.proxy.mission.item.adapters.CamerasAdapter;
import org.droidplanner.android.widgets.spinnerWheel.CardWheelHorizontalView;
import org.droidplanner.android.widgets.spinnerWheel.adapters.NumericWheelAdapter;
import org.droidplanner.android.widgets.spinners.SpinnerSelfSelect;

import java.util.Collections;
import java.util.List;

public class MissionStructureScannerFragment extends MissionDetailFragment implements
		CardWheelHorizontalView.OnCardWheelChangedListener, CompoundButton.OnCheckedChangeListener {

	private CamerasAdapter cameraAdapter;

	@Override
	protected int getResource() {
		return R.layout.fragment_editor_detail_structure_scanner;
	}

	@Override
	public void onApiConnected() {
		super.onApiConnected();

		final View view = getView();
		final Context context = getActivity().getApplicationContext();

		typeSpinner.setSelection(commandAdapter.getPosition(MissionItemType.STRUCTURE_SCANNER));

        CameraProxy camera = getDrone().getCamera();
        List<CameraDetail> cameraDetails = camera == null
                ? Collections.<CameraDetail>emptyList()
                :  camera.getAvailableCameraInfos();
		cameraAdapter = new CamerasAdapter(getActivity(),
                android.R.layout.simple_spinner_dropdown_item, cameraDetails);
		SpinnerSelfSelect cameraSpinner = (SpinnerSelfSelect) view.findViewById(id.cameraFileSpinner);
		cameraSpinner.setAdapter(cameraAdapter);
		cameraSpinner.setOnSpinnerItemSelectedListener(this);

		CardWheelHorizontalView radiusPicker = (CardWheelHorizontalView) view
				.findViewById(R.id.radiusPicker);
		radiusPicker.setViewAdapter(new NumericWheelAdapter(context, R.layout.wheel_text_centered,
				2, 100, "%d m"));
		radiusPicker.addChangingListener(this);

		CardWheelHorizontalView startAltitudeStepPicker = (CardWheelHorizontalView) view
				.findViewById(R.id.startAltitudePicker);
		startAltitudeStepPicker.setViewAdapter(new NumericWheelAdapter(context,
				R.layout.wheel_text_centered, MIN_ALTITUDE, MAX_ALTITUDE, "%d m"));
		startAltitudeStepPicker.addChangingListener(this);

		CardWheelHorizontalView endAltitudeStepPicker = (CardWheelHorizontalView) view
				.findViewById(R.id.heightStepPicker);
		endAltitudeStepPicker.setViewAdapter(new NumericWheelAdapter(context,
				R.layout.wheel_text_centered, 1, MAX_ALTITUDE, "%d m"));
		endAltitudeStepPicker.addChangingListener(this);

		CardWheelHorizontalView mNumberStepsPicker = (CardWheelHorizontalView) view
				.findViewById(R.id.stepsPicker);
		mNumberStepsPicker.setViewAdapter(new NumericWheelAdapter(context,
				R.layout.wheel_text_centered, 1, 10, "%d"));
		mNumberStepsPicker.addChangingListener(this);

		CheckBox checkBoxAdvanced = (CheckBox) view.findViewById(R.id.checkBoxSurveyCrossHatch);
		checkBoxAdvanced.setOnCheckedChangeListener(this);

		// Use the first one as reference.
		final StructureScanner firstItem = getMissionItems().get(0);
		radiusPicker.setCurrentValue((int) firstItem.getRadius());
		startAltitudeStepPicker.setCurrentValue((int) firstItem.getCoordinate().getAltitude());
		endAltitudeStepPicker.setCurrentValue((int) firstItem.getHeightStep());
		mNumberStepsPicker.setCurrentValue(firstItem.getStepsCount());
		checkBoxAdvanced.setChecked(firstItem.isCrossHatch());
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Drone drone = getDrone();
		for (StructureScanner item : getMissionItems()) {
            item.setCrossHatch(isChecked);
            drone.buildStructureScanner(item);
        }

		MissionProxy missionProxy = getMissionProxy();
		if (missionProxy != null)
			missionProxy.notifyMissionUpdate();
	}

	@Override
	public void onChanged(CardWheelHorizontalView cardWheel, int oldValue, int newValue) {
        Drone drone = getDrone();

		switch (cardWheel.getId()) {
		case R.id.radiusPicker: {
			for (StructureScanner item : getMissionItems()) {
                item.setRadius(newValue);
                drone.buildStructureScanner(item);
            }
			break;
		}

		case R.id.startAltitudePicker: {
			for (StructureScanner item : getMissionItems()) {
                item.getCoordinate().setAltitude(newValue);
                drone.buildStructureScanner(item);
            }
			break;
		}

		case R.id.heightStepPicker:
			for (StructureScanner item : getMissionItems()) {
                item.setHeightStep(newValue);
                drone.buildStructureScanner(item);
            }
			break;

		case R.id.stepsPicker:
			for (StructureScanner item : getMissionItems()) {
                item.setStepsCount(newValue);
                drone.buildStructureScanner(item);
            }
			break;
		}

		MissionProxy missionProxy = getMissionProxy();
		if (missionProxy != null)
			missionProxy.notifyMissionUpdate();
	}

	@Override
	public void onSpinnerItemSelected(Spinner spinner, int position) {
		if (spinner.getId() == id.cameraFileSpinner) {
            Drone drone = getDrone();

			CameraDetail cameraInfo = cameraAdapter.getItem(position);
			for (StructureScanner scan : getMissionItems()) {
                SurveyDetail surveyDetail = scan.getSurveyDetail();
                surveyDetail.setCameraDetail(cameraInfo);
                drone.buildStructureScanner(scan);
			}

			getMissionProxy().notifyMissionUpdate();
		}
	}

	@Override
	protected List<StructureScanner> getMissionItems() {
		return (List<StructureScanner>) super.getMissionItems();
	}
}

