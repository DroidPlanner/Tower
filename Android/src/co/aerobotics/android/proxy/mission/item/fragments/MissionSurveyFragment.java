package co.aerobotics.android.proxy.mission.item.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import co.aerobotics.android.DroidPlannerApp;
import co.aerobotics.android.data.BoundaryDetail;
import co.aerobotics.android.data.SQLiteDatabaseHandler;

import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.mission.item.complex.CameraDetail;
import com.o3dr.services.android.lib.drone.mission.item.complex.SplineSurvey;
import com.o3dr.services.android.lib.drone.mission.item.complex.Survey;
import com.o3dr.services.android.lib.drone.mission.item.complex.SurveyDetail;
import com.o3dr.services.android.lib.drone.property.CameraProxy;

import org.beyene.sius.unit.length.LengthUnit;
import co.aerobotics.android.R;
import co.aerobotics.android.R.id;
import co.aerobotics.android.dialogs.AddBoundaryCheckDialog;
import co.aerobotics.android.proxy.mission.MissionProxy;
import co.aerobotics.android.proxy.mission.item.adapters.CamerasAdapter;
import co.aerobotics.android.utils.unit.providers.area.AreaUnitProvider;
import co.aerobotics.android.utils.unit.providers.length.LengthUnitProvider;
import co.aerobotics.android.view.spinnerWheel.CardWheelHorizontalView;
import co.aerobotics.android.view.spinnerWheel.adapters.LengthWheelAdapter;
import co.aerobotics.android.view.spinnerWheel.adapters.NumericWheelAdapter;
import co.aerobotics.android.view.spinners.SpinnerSelfSelect;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MissionSurveyFragment<T extends Survey> extends MissionDetailFragment implements
        CardWheelHorizontalView.OnCardWheelScrollListener, Drone.OnMissionItemsBuiltCallback, View.OnClickListener {

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
                    //boundaryDetail.setCamera(cameraInfo.toString());
                }

                onScrollingEnded(mAnglePicker, 0, 0);
                //dbHandler.runUpdateTask(boundaryDetail);
            }
        }
    };

    private CardWheelHorizontalView<Integer> mOverlapPicker;
    private CardWheelHorizontalView<Integer> mAnglePicker;
    private CardWheelHorizontalView<LengthUnit> mAltitudePicker;
    private CardWheelHorizontalView<Integer> mSidelapPicker;
    private CardWheelHorizontalView<Integer> mSpeedPicker;

    public TextView waypointType;
    public TextView distanceBetweenLinesTextView;
    public TextView areaTextView;
    public TextView distanceTextView;
    public TextView footprintTextView;
    public TextView groundResolutionTextView;
    public TextView numberOfPicturesView;
    public TextView numberOfStripsView;
    public TextView lengthView;
    public TextView flightTime;
    public TextView cameraTriggerTimeTextView;
    private CamerasAdapter cameraAdapter;
    private SpinnerSelfSelect cameraSpinner;
    private Button saveButton;
    private SQLiteDatabaseHandler dbHandler;
    private BoundaryDetail boundaryDetail;
    private MixpanelAPI mMixpanel;
    private RadioButton sunnyButton;
    private RadioButton cloudyButton;

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

        mMixpanel = MixpanelAPI.getInstance(this.getApplication(), DroidPlannerApp.getInstance().getMixpanelToken());

        final View view = getView();
        final Context context = getContext();
        dbHandler = new SQLiteDatabaseHandler(context.getApplicationContext());
        boundaryDetail = new BoundaryDetail();
        waypointType = (TextView) view.findViewById(id.WaypointType);

        CameraProxy camera = getDrone().getAttribute(AttributeType.CAMERA);
        List<CameraDetail> cameraDetails = camera == null
                ? Collections.<CameraDetail>emptyList()
                : camera.getAvailableCameraInfos();

        saveButton = (Button) getActivity().findViewById(id.save_to_aeroview_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMixpanel.track("FPA: TapSaveMissionButton");
                new AddBoundaryCheckDialog().show(getFragmentManager(), null);
            }
        });

        sunnyButton = (RadioButton) getActivity().findViewById(id.sunny);
        sunnyButton.setOnClickListener(this);

        cloudyButton = (RadioButton) getActivity().findViewById(id.cloudy);
        cloudyButton.setOnClickListener(this);

        updateSaveButton();
        updateWhiteBalance();

        cameraAdapter = new CamerasAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item, cameraDetails);

        cameraSpinner = (SpinnerSelfSelect) view.findViewById(id.cameraFileSpinner);
        cameraSpinner.setAdapter(cameraAdapter);
        cameraSpinner.setOnSpinnerItemSelectedListener(cameraSpinnerListener);

        mAnglePicker = (CardWheelHorizontalView) view.findViewById(id.anglePicker);
        mAnglePicker.setViewAdapter(new NumericWheelAdapter(context, R.layout.wheel_text_centered, 0, 359, "%dº"));

        mOverlapPicker = (CardWheelHorizontalView) view.findViewById(id.overlapPicker);
        mOverlapPicker.setViewAdapter(new NumericWheelAdapter(context, R.layout.wheel_text_centered, 65, 99, "%d %%"));

        mSidelapPicker = (CardWheelHorizontalView) view.findViewById(id.sidelapPicker);
        mSidelapPicker.setViewAdapter(new NumericWheelAdapter(context, R.layout.wheel_text_centered, 65, 99, "%d %%"));

        mSpeedPicker = (CardWheelHorizontalView) view.findViewById(id.speedPicker);
        mSpeedPicker.setViewAdapter(new NumericWheelAdapter(context, R.layout.wheel_text_centered, 4, 14, "%d m/s"));

        final LengthUnitProvider lengthUP = getLengthUnitProvider();
        mAltitudePicker = (CardWheelHorizontalView) view.findViewById(R.id.altitudePicker);
        mAltitudePicker.setViewAdapter(new LengthWheelAdapter(context, R.layout.wheel_text_centered,
                lengthUP.boxBaseValueToTarget(MIN_ALTITUDE), lengthUP.boxBaseValueToTarget(MAX_ALTITUDE)));

        areaTextView = (TextView) view.findViewById(id.areaTextView);
        distanceBetweenLinesTextView = (TextView) view.findViewById(id.distanceBetweenLinesTextView);
        //footprintTextView = (TextView) view.findViewById(id.footprintTextView);
        groundResolutionTextView = (TextView) view.findViewById(id.groundResolutionTextView);
        distanceTextView = (TextView) view.findViewById(id.distanceTextView);
        numberOfPicturesView = (TextView) view.findViewById(id.numberOfPicturesTextView);
        numberOfStripsView = (TextView) view.findViewById(id.numberOfStripsTextView);
        lengthView = (TextView) view.findViewById(id.lengthTextView);
        flightTime = (TextView) view.findViewById(id.flightTimeTextView);
        cameraTriggerTimeTextView = (TextView) view.findViewById(id.cameraTriggerTextView);

        updateViews();
        updateCamera();

        mAnglePicker.addScrollListener(this);
        mOverlapPicker.addScrollListener(this);
        mSidelapPicker.addScrollListener(this);
        mAltitudePicker.addScrollListener(this);
        mSpeedPicker.addScrollListener(this);

        if(!getMissionItems().isEmpty()) {
            //typeSpinner.setSelection(commandAdapter.getPosition(MissionItemType.SURVEY));
        }

        getBroadcastManager().registerReceiver(eventReceiver, eventFilter);
        
    }


    @Override
    public void onApiDisconnected() {
        super.onApiDisconnected();
        dbHandler.runUpdateTask(boundaryDetail);
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
            case id.speedPicker:
                final Drone drone = getDrone();
                try {
                    final List<T> surveyList = getMissionItems();
                    if (!surveyList.isEmpty()) {
                        for (final T survey : surveyList) {

                            SurveyDetail surveyDetail = survey.getSurveyDetail();
                            surveyDetail.setAltitude(mAltitudePicker.getCurrentValue().toBase().getValue());
                            surveyDetail.setSpeed(mSpeedPicker.getCurrentValue());
                            surveyDetail.setAngle(mAnglePicker.getCurrentValue());
                            surveyDetail.setOverlap(mOverlapPicker.getCurrentValue());
                            surveyDetail.setSidelap(mSidelapPicker.getCurrentValue());
                        }

                        getAppPrefs().persistSurveyPreferences(surveyList.get(0));

                        final MissionItem.ComplexItem<T>[] surveys = surveyList
                                .toArray(new MissionItem.ComplexItem[surveyList.size()]);

                        drone.buildMissionItemsAsync(surveys, this);
                        if(null != surveyList.get(0).getID()) {
                            boundaryDetail = dbHandler.getBoundaryDetail(surveyList.get(0).getID());
                            boundaryDetail.setBoundaryId(surveyList.get(0).getID());
                            boundaryDetail.setAngle(mAnglePicker.getCurrentValue());
                            boundaryDetail.setAltitude(mAltitudePicker.getCurrentValue().toBase().getValue());
                            boundaryDetail.setOverlap(mOverlapPicker.getCurrentValue());
                            boundaryDetail.setSidelap(mSidelapPicker.getCurrentValue());
                            boundaryDetail.setSpeed(mSpeedPicker.getCurrentValue());
                            boundaryDetail.setCamera(surveyList.get(0).getSurveyDetail().getCameraDetail().toString());

                        }

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
        if(!isAdded()){
            return;
        }
        boolean isCameraValid = checkCameraTriggerTime();
        boolean isFlightTimeValid = checkFlightTime();

        if (survey.isValid() && isCameraValid && isFlightTimeValid)
            mAltitudePicker.setBackgroundResource(R.drawable.bg_cell_white);
        else
            mAltitudePicker.setBackgroundColor(Color.RED);
    }

    private boolean checkFlightTime(){
        double time  = getFlightTime();
        if (time < 16f){
            flightTime.setTextColor(getResources().getColor(R.color.dark_title_bg));
            return true;
        } else
            flightTime.setTextColor(Color.RED);
            return false;
    }

    private double getFlightTime() {
        List<T> surveyList = getMissionItems();
        if (!surveyList.isEmpty()) {
            T survey = surveyList.get(0);
            SurveyDetail surveyDetail = survey.getSurveyDetail();
            double time = ((survey.getGridLength()) / surveyDetail.getSpeed()) / 60;
            double roundedTime = Math.round((time * 2) / 2.0);
            return roundedTime;
        }
        return -1;
    }

    private boolean checkCameraTriggerTime() {
        List<T> surveyList = getMissionItems();
        if (!surveyList.isEmpty()) {
            T survey = surveyList.get(0);

            double cameraDistance = survey.getSurveyDetail().getLongitudinalPictureDistance();
            double triggerSpeed = cameraDistance/mSpeedPicker.getCurrentValue();
            Log.d("time", Double.toString(triggerSpeed));
            if(triggerSpeed > 2f) {
                cameraTriggerTimeTextView.setTextColor(getResources().getColor(R.color.dark_title_bg));
                return true;
            }
            else {
                cameraTriggerTimeTextView.setTextColor(Color.RED);
                return false;
            }
        }
        return false;
    }

    private double getCameraTriggerTime(){
        List<T> surveyList = getMissionItems();
        T survey = surveyList.get(0);

        double cameraDistance = survey.getSurveyDetail().getLongitudinalPictureDistance();
        return cameraDistance / mSpeedPicker.getCurrentValue();
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

    private void updateWhiteBalance(){
        List<T> surveyList = getMissionItems();
        if (!surveyList.isEmpty()) {
            T survey = surveyList.get(0);
            if (survey.getSurveyDetail().isSunny()){
                sunnyButton.setChecked(true);
            } else {
                cloudyButton.setChecked(true);
            }
        }
    }

    private void updateSaveButton(){
        List<T> surveyList = getMissionItems();
        if (!surveyList.isEmpty()) {
            T survey = surveyList.get(0);
            if(survey.getSurveyDetail().isSaveable()){
                saveButton.setVisibility(View.VISIBLE);
            } else {
                saveButton.setVisibility(View.INVISIBLE);
            }
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
                mSpeedPicker.setCurrentValue((int) surveyDetail.getSpeed());
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

                cameraTriggerTimeTextView.setText(String.format(Locale.ENGLISH, "%s: %2.1f s", "Camera Trigger Speed", getCameraTriggerTime()));

                groundResolutionTextView.setText(String.format("%s: %s /px",
                        getString(R.string.ground_resolution),
                        areaUnitProvider.boxBaseValueToTarget(surveyDetail.getGroundResolution())));

                distanceTextView.setText(String.format("%s: %s",
                        getString(R.string.distance_between_pictures),
                        lengthUnitProvider.boxBaseValueToTarget(surveyDetail.getLongitudinalPictureDistance())));

                distanceBetweenLinesTextView.setText(String.format("%s: %s",
                        getString(R.string.distance_between_lines),
                        lengthUnitProvider.boxBaseValueToTarget(surveyDetail.getLateralPictureDistance())));

                areaTextView.setText(String.format(Locale.ENGLISH, "%s: %2.1f ha", getString(R.string.area),  (survey.getPolygonArea())));


                lengthView.setText(String.format("%s: %s", getString(R.string.mission_length),
                        lengthUnitProvider.boxBaseValueToTarget(survey.getGridLength())));

                flightTime.setText(String.format(Locale.ENGLISH, "%s: %2.1f mins",
                        getString(R.string.flight_time),
                        getFlightTime()));

                numberOfPicturesView.setText(String.format(Locale.ENGLISH, "%s: %d", getString(R.string.pictures),
                        survey.getCameraCount()));

                numberOfStripsView.setText(String.format(Locale.ENGLISH, "%s: %d", getString(R.string.number_of_strips),
                        survey.getNumberOfLines()));

                setDefault = false;
            } catch (Exception e) {
                setDefault = true;
            }
        }

        if (setDefault) {
            cameraTriggerTimeTextView.setText("Camera Trigger Speed" + ": ???");
            groundResolutionTextView.setText(getString(R.string.ground_resolution) + ": ???");
            distanceTextView.setText(getString(R.string.distance_between_pictures) + ": ???");
            distanceBetweenLinesTextView.setText(getString(R.string.distance_between_lines)
                    + ": ???");
            areaTextView.setText(getString(R.string.area) + ": ???");
            lengthView.setText(getString(R.string.mission_length) + ": ???");
            numberOfPicturesView.setText(getString(R.string.pictures) + "???");
            numberOfStripsView.setText(getString(R.string.number_of_strips) + "???");
            flightTime.setText(getString(R.string.flight_time) + "???");
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
    public void onClick(View view) {
        List<T> surveyList = getMissionItems();
        if (!surveyList.isEmpty()) {
            T survey = surveyList.get(0);
            SurveyDetail surveyDetail = survey.getSurveyDetail();
            switch (view.getId()) {
                case id.sunny:
                    surveyDetail.setSunny(true);
                    break;
                case id.cloudy:
                    surveyDetail.setSunny(false);
                    break;
                default:
                    break;
            }
        }
    }
}
