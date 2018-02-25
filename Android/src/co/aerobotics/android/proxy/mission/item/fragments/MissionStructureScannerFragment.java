package co.aerobotics.android.proxy.mission.item.fragments;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;

import co.aerobotics.android.proxy.mission.item.adapters.CamerasAdapter;
import co.aerobotics.android.utils.Utils;
import co.aerobotics.android.view.spinners.SpinnerSelfSelect;
import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.mission.MissionItemType;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.mission.item.complex.CameraDetail;
import com.o3dr.services.android.lib.drone.mission.item.complex.StructureScanner;
import com.o3dr.services.android.lib.drone.mission.item.complex.SurveyDetail;
import com.o3dr.services.android.lib.drone.property.CameraProxy;

import org.beyene.sius.unit.length.LengthUnit;
import co.aerobotics.android.R;
import co.aerobotics.android.R.id;
import co.aerobotics.android.proxy.mission.MissionProxy;
import co.aerobotics.android.utils.unit.providers.length.LengthUnitProvider;
import co.aerobotics.android.view.spinnerWheel.CardWheelHorizontalView;
import co.aerobotics.android.view.spinnerWheel.adapters.LengthWheelAdapter;
import co.aerobotics.android.view.spinnerWheel.adapters.NumericWheelAdapter;

import java.util.Collections;
import java.util.List;

public class MissionStructureScannerFragment extends MissionDetailFragment implements
        CardWheelHorizontalView.OnCardWheelScrollListener, CompoundButton.OnCheckedChangeListener, Drone.OnMissionItemsBuiltCallback {

    private final SpinnerSelfSelect.OnSpinnerItemSelectedListener cameraSpinnerListener = new SpinnerSelfSelect.OnSpinnerItemSelectedListener() {
        @Override
        public void onSpinnerItemSelected(Spinner spinner, int position) {
            if (spinner.getId() == id.cameraFileSpinner) {

                if(cameraAdapter.isEmpty())
                    return;

                CameraDetail cameraInfo = cameraAdapter.getItem(position);
                for (StructureScanner scan : getMissionItems()) {
                    SurveyDetail surveyDetail = scan.getSurveyDetail();
                    surveyDetail.setCameraDetail(cameraInfo);
                }

                submitForBuilding();
            }
        }
    };

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

        CameraProxy camera = getDrone().getAttribute(AttributeType.CAMERA);
        List<CameraDetail> cameraDetails = camera == null
                ? Collections.<CameraDetail>emptyList()
                : camera.getAvailableCameraInfos();
        cameraAdapter = new CamerasAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item, cameraDetails);
        SpinnerSelfSelect cameraSpinner = (SpinnerSelfSelect) view.findViewById(id.cameraFileSpinner);
        cameraSpinner.setAdapter(cameraAdapter);
        cameraSpinner.setOnSpinnerItemSelectedListener(cameraSpinnerListener);

        final LengthUnitProvider lengthUP = getLengthUnitProvider();

        CardWheelHorizontalView<LengthUnit> radiusPicker = (CardWheelHorizontalView) view
                .findViewById(R.id.radiusPicker);
        radiusPicker.setViewAdapter(new LengthWheelAdapter(context, R.layout.wheel_text_centered,
                lengthUP.boxBaseValueToTarget(Utils.MIN_DISTANCE), lengthUP.boxBaseValueToTarget(Utils.MAX_RADIUS)));
        radiusPicker.addScrollListener(this);

        CardWheelHorizontalView<LengthUnit> startAltitudeStepPicker = (CardWheelHorizontalView) view
                .findViewById(R.id.startAltitudePicker);
        startAltitudeStepPicker.setViewAdapter(new LengthWheelAdapter(context, R.layout.wheel_text_centered,
                lengthUP.boxBaseValueToTarget(MIN_ALTITUDE), lengthUP.boxBaseValueToTarget(MAX_ALTITUDE)));
        startAltitudeStepPicker.addScrollListener(this);

        CardWheelHorizontalView<LengthUnit> endAltitudeStepPicker = (CardWheelHorizontalView) view
                .findViewById(R.id.heightStepPicker);
        endAltitudeStepPicker.setViewAdapter(new LengthWheelAdapter(context, R.layout.wheel_text_centered,
                lengthUP.boxBaseValueToTarget(MIN_ALTITUDE), lengthUP.boxBaseValueToTarget(MAX_ALTITUDE)));
        endAltitudeStepPicker.addScrollListener(this);

        CardWheelHorizontalView<Integer> numberStepsPicker = (CardWheelHorizontalView<Integer>) view
                .findViewById(R.id.stepsPicker);
        numberStepsPicker.setViewAdapter(new NumericWheelAdapter(context, R.layout.wheel_text_centered, 1, 100, "%d"));
        numberStepsPicker.addScrollListener(this);

        CheckBox checkBoxAdvanced = (CheckBox) view.findViewById(R.id.checkBoxSurveyCrossHatch);
        checkBoxAdvanced.setOnCheckedChangeListener(this);

        // Use the first one as reference.
        final StructureScanner firstItem = getMissionItems().get(0);

        final int cameraSelection = cameraAdapter.getPosition(firstItem.getSurveyDetail().getCameraDetail());
        cameraSpinner.setSelection(Math.max(cameraSelection, 0));

        radiusPicker.setCurrentValue(lengthUP.boxBaseValueToTarget(firstItem.getRadius()));
        startAltitudeStepPicker.setCurrentValue(lengthUP.boxBaseValueToTarget(firstItem.getCoordinate().getAltitude()));
        endAltitudeStepPicker.setCurrentValue(lengthUP.boxBaseValueToTarget(firstItem.getHeightStep()));
        numberStepsPicker.setCurrentValue(firstItem.getStepsCount());
        checkBoxAdvanced.setChecked(firstItem.isCrossHatch());
    }

    private void submitForBuilding() {
        final List<StructureScanner> scannerList = getMissionItems();
        if (scannerList.isEmpty()) return;

        getDrone().buildMissionItemsAsync(scannerList.toArray(new MissionItem.ComplexItem[scannerList.size()]), this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        for (StructureScanner item : getMissionItems()) {
            item.setCrossHatch(isChecked);
        }

        submitForBuilding();
    }

    @Override
    public void onScrollingStarted(CardWheelHorizontalView cardWheel, Object startValue) {
    }

    @Override
    public void onScrollingUpdate(CardWheelHorizontalView cardWheel, Object oldValue, Object newValue) {
    }

    @Override
    public void onScrollingEnded(CardWheelHorizontalView cardWheel, Object startValue, Object endValue) {
        switch (cardWheel.getId()) {
            case R.id.radiusPicker: {
                final double radius = ((LengthUnit) endValue).toBase().getValue();
                for (StructureScanner item : getMissionItems()) {
                    item.setRadius(radius);
                }
                break;
            }

            case R.id.startAltitudePicker: {
                final double altitude =  ((LengthUnit) endValue).toBase().getValue();
                for (StructureScanner item : getMissionItems()) {
                    item.getCoordinate().setAltitude(altitude);
                }
                break;
            }

            case R.id.heightStepPicker: {
                final double heightStep = ((LengthUnit) endValue).toBase().getValue();
                for (StructureScanner item : getMissionItems()) {
                    item.setHeightStep(heightStep);
                }
                break;
            }

            case R.id.stepsPicker:
                final int stepsCount = (Integer) endValue;
                for (StructureScanner item : getMissionItems()) {
                    item.setStepsCount(stepsCount);
                }
                break;
        }

        submitForBuilding();
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

