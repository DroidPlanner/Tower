package org.droidplanner.android.proxy.mission.item.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.mission.MissionItemType;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.mission.item.complex.CameraDetail;
import com.o3dr.services.android.lib.drone.mission.item.complex.SplineSurvey;
import com.o3dr.services.android.lib.drone.mission.item.complex.Survey;
import com.o3dr.services.android.lib.drone.mission.item.complex.SurveyDetail;
import com.o3dr.services.android.lib.drone.property.CameraProxy;

import org.beyene.sius.unit.length.LengthUnit;
import org.droidplanner.android.R;
import org.droidplanner.android.R.id;
import org.droidplanner.android.proxy.mission.MissionProxy;
import org.droidplanner.android.proxy.mission.item.adapters.CamerasAdapter;
import org.droidplanner.android.utils.unit.providers.area.AreaUnitProvider;
import org.droidplanner.android.utils.unit.providers.length.LengthUnitProvider;
import org.droidplanner.android.view.spinnerWheel.CardWheelHorizontalView;
import org.droidplanner.android.view.spinnerWheel.adapters.LengthWheelAdapter;
import org.droidplanner.android.view.spinnerWheel.adapters.NumericWheelAdapter;
import org.droidplanner.android.view.spinners.SpinnerSelfSelect;

import java.util.Collections;
import java.util.List;

public class MissionSurveyFragment<T extends Survey> extends MissionDetailFragment implements
        CardWheelHorizontalView.OnCardWheelScrollListener, Drone.OnMissionItemsBuiltCallback, CompoundButton.OnCheckedChangeListener {

    private static final String TAG = MissionSurveyFragment.class.getSimpleName();

    private static final IntentFilter eventFilter = new IntentFilter(MissionProxy.ACTION_MISSION_PROXY_UPDATE);

    private final BroadcastReceiver eventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (MissionProxy.ACTION_MISSION_PROXY_UPDATE.equals(action)) {
                updateViews();
            }
        }
    };

    private final SpinnerSelfSelect.OnSpinnerItemSelectedListener cameraSpinnerListener = new SpinnerSelfSelect.OnSpinnerItemSelectedListener() {
        @Override
        public void onSpinnerItemSelected(Spinner spinner, int position) {
            if (spinner.getId() == id.cameraFileSpinner) {
                if(cameraAdapter.isEmpty())
                    return;

                CameraDetail cameraInfo = cameraAdapter.getItem(position);
                for (T survey : getMissionItems()) {
                    survey.getSurveyDetail().setCameraDetail(cameraInfo);
                }

                onScrollingEnded(mAnglePicker, 0, 0);
            }
        }
    };

    private CardWheelHorizontalView<Integer> mOverlapPicker;
    private CardWheelHorizontalView<Integer> mAnglePicker;
    private CardWheelHorizontalView<LengthUnit> mAltitudePicker;
    private CardWheelHorizontalView<Integer> mSidelapPicker;

    public TextView waypointType;
    public TextView distanceBetweenLinesTextView;
    public TextView areaTextView;
    public TextView distanceTextView;
    public TextView footprintTextView;
    public TextView groundResolutionTextView;
    public TextView numberOfPicturesView;
    public TextView numberOfStripsView;
    public TextView lengthView;
    private CamerasAdapter cameraAdapter;
    private SpinnerSelfSelect cameraSpinner;

    @Override
    protected int getResource() {
        return R.layout.fragment_editor_detail_survey;
    }

    @Override
    protected List<T> getMissionItems() {
        return (List<T>) super.getMissionItems();
    }

    @Override
    public void onApiConnected() {
        super.onApiConnected();

        final View view = getView();
        final Context context = getContext();

        waypointType = (TextView) view.findViewById(id.WaypointType);

        CameraProxy camera = getDrone().getAttribute(AttributeType.CAMERA);
        List<CameraDetail> cameraDetails = camera == null
                ? Collections.<CameraDetail>emptyList()
                : camera.getAvailableCameraInfos();
        cameraAdapter = new CamerasAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item, cameraDetails);

        cameraSpinner = (SpinnerSelfSelect) view.findViewById(id.cameraFileSpinner);
        cameraSpinner.setAdapter(cameraAdapter);
        cameraSpinner.setOnSpinnerItemSelectedListener(cameraSpinnerListener);

        mAnglePicker = (CardWheelHorizontalView) view.findViewById(id.anglePicker);
        mAnglePicker.setViewAdapter(new NumericWheelAdapter(context, R.layout.wheel_text_centered, 0, 180, "%dº"));

        mOverlapPicker = (CardWheelHorizontalView) view.findViewById(id.overlapPicker);
        mOverlapPicker.setViewAdapter(new NumericWheelAdapter(context, R.layout.wheel_text_centered, 0, 99, "%d %%"));

        mSidelapPicker = (CardWheelHorizontalView) view.findViewById(R.id.sidelapPicker);
        mSidelapPicker.setViewAdapter(new NumericWheelAdapter(context, R.layout.wheel_text_centered, 0, 99, "%d %%"));

        final LengthUnitProvider lengthUP = getLengthUnitProvider();
        mAltitudePicker = (CardWheelHorizontalView) view.findViewById(R.id.altitudePicker);
        mAltitudePicker.setViewAdapter(new LengthWheelAdapter(context, R.layout.wheel_text_centered,
                lengthUP.boxBaseValueToTarget(MIN_ALTITUDE), lengthUP.boxBaseValueToTarget(MAX_ALTITUDE)));

        areaTextView = (TextView) view.findViewById(id.areaTextView);
        distanceBetweenLinesTextView = (TextView) view.findViewById(id.distanceBetweenLinesTextView);
        footprintTextView = (TextView) view.findViewById(id.footprintTextView);
        groundResolutionTextView = (TextView) view.findViewById(id.groundResolutionTextView);
        distanceTextView = (TextView) view.findViewById(id.distanceTextView);
        numberOfPicturesView = (TextView) view.findViewById(id.numberOfPicturesTextView);
        numberOfStripsView = (TextView) view.findViewById(id.numberOfStripsTextView);
        lengthView = (TextView) view.findViewById(id.lengthTextView);

        updateViews();
        updateCamera();

        mAnglePicker.addScrollListener(this);
        mOverlapPicker.addScrollListener(this);
        mSidelapPicker.addScrollListener(this);
        mAltitudePicker.addScrollListener(this);

        CheckBox startCameraBeforeFirstWaypointCheck = (CheckBox) view.findViewById(id.check_start_camera_before_first_waypoint);
        CheckBox lockCopterYawCheck = (CheckBox) view.findViewById(id.check_lock_copter_yaw);

        if(!getMissionItems().isEmpty()) {
            final T referenceItem = getMissionItems().get(0);
            if (referenceItem instanceof SplineSurvey)
                typeSpinner.setSelection(commandAdapter.getPosition(MissionItemType.SPLINE_SURVEY));
            else
                typeSpinner.setSelection(commandAdapter.getPosition(MissionItemType.SURVEY));

            startCameraBeforeFirstWaypointCheck.setChecked(referenceItem.isStartCameraBeforeFirstWaypoint());

            SurveyDetail surveyDetail = referenceItem.getSurveyDetail();
            if(surveyDetail != null) {
                lockCopterYawCheck.setChecked(surveyDetail.getLockOrientation());
            }
        }

        startCameraBeforeFirstWaypointCheck.setOnCheckedChangeListener(this);
        lockCopterYawCheck.setOnCheckedChangeListener(this);

        getBroadcastManager().registerReceiver(eventReceiver, eventFilter);
    }

    @Override
    public void onApiDisconnected() {
        super.onApiDisconnected();
        getBroadcastManager().unregisterReceiver(eventReceiver);
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
            case R.id.anglePicker:
            case R.id.altitudePicker:
            case R.id.overlapPicker:
            case R.id.sidelapPicker:
                final Drone drone = getDrone();
                try {
                    final List<T> surveyList = getMissionItems();
                    if (!surveyList.isEmpty()) {
                        for (final T survey : surveyList) {
                            SurveyDetail surveyDetail = survey.getSurveyDetail();
                            surveyDetail.setAltitude(mAltitudePicker.getCurrentValue().toBase().getValue());
                            surveyDetail.setAngle(mAnglePicker.getCurrentValue());
                            surveyDetail.setOverlap(mOverlapPicker.getCurrentValue());
                            surveyDetail.setSidelap(mSidelapPicker.getCurrentValue());
                        }

                        getAppPrefs().persistSurveyPreferences(surveyList.get(0));

                        final MissionItem.ComplexItem<T>[] surveys = surveyList
                                .toArray(new MissionItem.ComplexItem[surveyList.size()]);

                        drone.buildMissionItemsAsync(surveys, this);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error while building the survey.", e);
                }
                break;
        }
    }

    private void checkIfValid(T survey) {
        if (mAltitudePicker == null)
            return;

        if (survey.isValid())
            mAltitudePicker.setBackgroundResource(R.drawable.bg_cell_white);
        else
            mAltitudePicker.setBackgroundColor(Color.RED);
    }

    private void updateViews() {
        if (getActivity() == null)
            return;

        updateTextViews();
        updateSeekBars();
    }

    private void updateCamera() {
        List<T> surveyList = getMissionItems();
        if (!surveyList.isEmpty()) {
            T survey = surveyList.get(0);
            final int cameraSelection = cameraAdapter.getPosition(survey.getSurveyDetail().getCameraDetail());
            cameraSpinner.setSelection(Math.max(cameraSelection, 0));
        }
    }

    private void updateSeekBars() {
        List<T> surveyList = getMissionItems();
        if (!surveyList.isEmpty()) {
            T survey = surveyList.get(0);
            SurveyDetail surveyDetail = survey.getSurveyDetail();
            if (surveyDetail != null) {
                mAnglePicker.setCurrentValue((int) surveyDetail.getAngle());
                mOverlapPicker.setCurrentValue((int) surveyDetail.getOverlap());
                mSidelapPicker.setCurrentValue((int) surveyDetail.getSidelap());
                mAltitudePicker.setCurrentValue(getLengthUnitProvider().boxBaseValueToTarget(surveyDetail.getAltitude()));
            }

            checkIfValid(survey);
        }
    }

    private void updateTextViews() {
        boolean setDefault = true;
        List<T> surveyList = getMissionItems();
        if (!surveyList.isEmpty()) {
            T survey = surveyList.get(0);
            SurveyDetail surveyDetail = survey.getSurveyDetail();
            if(survey instanceof SplineSurvey){
                waypointType.setText(getResources().getText(R.string.waypointType_Spline_Survey));
            }

            try {
                final LengthUnitProvider lengthUnitProvider = getLengthUnitProvider();
                final AreaUnitProvider areaUnitProvider = getAreaUnitProvider();

                footprintTextView.setText(String.format("%s: %s x %s",
                        getString(R.string.footprint),
                        lengthUnitProvider.boxBaseValueToTarget(surveyDetail.getLateralFootPrint()),
                        lengthUnitProvider.boxBaseValueToTarget(surveyDetail.getLongitudinalFootPrint())));

                groundResolutionTextView.setText(String.format("%s: %s /px",
                        getString(R.string.ground_resolution),
                        areaUnitProvider.boxBaseValueToTarget(surveyDetail.getGroundResolution())));

                distanceTextView.setText(String.format("%s: %s",
                        getString(R.string.distance_between_pictures),
                        lengthUnitProvider.boxBaseValueToTarget(surveyDetail.getLongitudinalPictureDistance())));

                distanceBetweenLinesTextView.setText(String.format("%s: %s",
                        getString(R.string.distance_between_lines),
                        lengthUnitProvider.boxBaseValueToTarget(surveyDetail.getLateralPictureDistance())));

                areaTextView.setText(String.format("%s: %s", getString(R.string.area),
                        areaUnitProvider.boxBaseValueToTarget(survey.getPolygonArea())));


                lengthView.setText(String.format("%s: %s", getString(R.string.mission_length),
                        lengthUnitProvider.boxBaseValueToTarget(survey.getGridLength())));

                numberOfPicturesView.setText(String.format("%s: %d", getString(R.string.pictures),
                        survey.getCameraCount()));

                numberOfStripsView.setText(String.format("%s: %d", getString(R.string.number_of_strips),
                        survey.getNumberOfLines()));

                setDefault = false;
            } catch (Exception e) {
                setDefault = true;
            }
        }

        if (setDefault) {
            footprintTextView.setText(getString(R.string.footprint) + ": ???");
            groundResolutionTextView.setText(getString(R.string.ground_resolution) + ": ???");
            distanceTextView.setText(getString(R.string.distance_between_pictures) + ": ???");
            distanceBetweenLinesTextView.setText(getString(R.string.distance_between_lines)
                    + ": ???");
            areaTextView.setText(getString(R.string.area) + ": ???");
            lengthView.setText(getString(R.string.mission_length) + ": ???");
            numberOfPicturesView.setText(getString(R.string.pictures) + "???");
            numberOfStripsView.setText(getString(R.string.number_of_strips) + "???");
        }
    }

    @Override
    public void onMissionItemsBuilt(MissionItem.ComplexItem[] complexItems) {
        for (MissionItem.ComplexItem<T> item : complexItems) {
            checkIfValid((T) item);
        }

        getMissionProxy().notifyMissionUpdate();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        final List<T> surveyList = getMissionItems();
        if(!surveyList.isEmpty()) {
            int viewId = buttonView.getId();
            for(T survey : surveyList) {
                switch(viewId) {
                    case R.id.check_start_camera_before_first_waypoint:
                    survey.setStartCameraBeforeFirstWaypoint(isChecked);
                        break;

                    case id.check_lock_copter_yaw:
                        SurveyDetail surveyDetail = survey.getSurveyDetail();
                        if(surveyDetail != null) {
                            surveyDetail.setLockOrientation(isChecked);
                        }
                        break;
                }
            }

            getAppPrefs().persistSurveyPreferences(surveyList.get(0));
        }
    }
}
