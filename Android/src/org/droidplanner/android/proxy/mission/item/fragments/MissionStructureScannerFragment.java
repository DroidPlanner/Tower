package org.droidplanner.android.proxy.mission.item.fragments;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;

import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.drone.mission.Mission;
import com.o3dr.services.android.lib.drone.mission.MissionItemType;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
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
        CardWheelHorizontalView.OnCardWheelScrollListener, CompoundButton.OnCheckedChangeListener, Drone.OnMissionItemsBuiltCallback {

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
		radiusPicker.addScrollListener(this);

		CardWheelHorizontalView startAltitudeStepPicker = (CardWheelHorizontalView) view
				.findViewById(R.id.startAltitudePicker);
		startAltitudeStepPicker.setViewAdapter(new NumericWheelAdapter(context,
				R.layout.wheel_text_centered, MIN_ALTITUDE, MAX_ALTITUDE, "%d m"));
		startAltitudeStepPicker.addScrollListener(this);

		CardWheelHorizontalView endAltitudeStepPicker = (CardWheelHorizontalView) view
				.findViewById(R.id.heightStepPicker);
		endAltitudeStepPicker.setViewAdapter(new NumericWheelAdapter(context,
				R.layout.wheel_text_centered, 1, MAX_ALTITUDE, "%d m"));
		endAltitudeStepPicker.addScrollListener(this);

		CardWheelHorizontalView mNumberStepsPicker = (CardWheelHorizontalView) view
				.findViewById(R.id.stepsPicker);
		mNumberStepsPicker.setViewAdapter(new NumericWheelAdapter(context,
				R.layout.wheel_text_centered, 1, 10, "%d"));
		mNumberStepsPicker.addScrollListener(this);

		CheckBox checkBoxAdvanced = (CheckBox) view.findViewById(R.id.checkBoxSurveyCrossHatch);
		checkBoxAdvanced.setOnCheckedChangeListener(this);

		// Use the first one as reference.
		final StructureScanner firstItem = getMissionItems().get(0);

        final int cameraSelection = cameraAdapter.getPosition(firstItem.getSurveyDetail().getCameraDetail());
        cameraSpinner.setSelection(Math.max(cameraSelection, 0));

		radiusPicker.setCurrentValue((int) firstItem.getRadius());
		startAltitudeStepPicker.setCurrentValue((int) firstItem.getCoordinate().getAltitude());
		endAltitudeStepPicker.setCurrentValue((int) firstItem.getHeightStep());
		mNumberStepsPicker.setCurrentValue(firstItem.getStepsCount());
		checkBoxAdvanced.setChecked(firstItem.isCrossHatch());
	}

    private void submitForBuilding(){
        final List<StructureScanner> scannerList = getMissionItems();
        if(scannerList.isEmpty()) return;

        getDrone().buildMissionItemsAsync(this, scannerList.toArray(new MissionItem.ComplexItem[scannerList.size()]));
    }

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		for (StructureScanner item : getMissionItems()) {
            item.setCrossHatch(isChecked);
        }

        submitForBuilding();
	}

    @Override
    public void onScrollingStarted(CardWheelHorizontalView cardWheel, int startValue) {}

    @Override
    public void onScrollingUpdate(CardWheelHorizontalView cardWheel, int oldValue, int newValue) {}

    @Override
	public void onScrollingEnded(CardWheelHorizontalView cardWheel, int startValue, int endValue) {
		switch (cardWheel.getId()) {
		case R.id.radiusPicker: {
			for (StructureScanner item : getMissionItems()) {
                item.setRadius(endValue);
            }
			break;
		}

		case R.id.startAltitudePicker: {
			for (StructureScanner item : getMissionItems()) {
                item.getCoordinate().setAltitude(endValue);
            }
			break;
		}

		case R.id.heightStepPicker:
			for (StructureScanner item : getMissionItems()) {
                item.setHeightStep(endValue);
            }
			break;

		case R.id.stepsPicker:
			for (StructureScanner item : getMissionItems()) {
                item.setStepsCount(endValue);
            }
			break;
		}

        submitForBuilding();
	}

	@Override
	public void onSpinnerItemSelected(Spinner spinner, int position) {
		if (spinner.getId() == id.cameraFileSpinner) {

			CameraDetail cameraInfo = cameraAdapter.getItem(position);
			for (StructureScanner scan : getMissionItems()) {
                SurveyDetail surveyDetail = scan.getSurveyDetail();
                surveyDetail.setCameraDetail(cameraInfo);
			}

            submitForBuilding();
		}
	}

	@Override
	protected List<StructureScanner> getMissionItems() {
		return (List<StructureScanner>) super.getMissionItems();
	}

    @Override
    public void onMissionItemsBuilt(MissionItem.ComplexItem[] complexItems) {
        MissionProxy missionProxy = getMissionProxy();
        if (missionProxy != null)
            missionProxy.notifyMissionUpdate();
    }
}

