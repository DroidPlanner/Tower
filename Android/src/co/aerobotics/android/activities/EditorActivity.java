package co.aerobotics.android.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import co.aerobotics.android.DroidPlannerApp;
import co.aerobotics.android.R;
import co.aerobotics.android.activities.interfaces.OnEditorInteraction;
import co.aerobotics.android.data.DJIFlightControllerState;
import co.aerobotics.android.dialogs.AddBoundaryCheckDialog;
import co.aerobotics.android.dialogs.OkDialog;
import co.aerobotics.android.dialogs.SearchBoundariesDialog;
import co.aerobotics.android.dialogs.SupportEditInputDialog;
import co.aerobotics.android.dialogs.openfile.OpenFileDialog;
import co.aerobotics.android.fragments.EditorListFragment;
import co.aerobotics.android.fragments.EditorMapFragment;
import co.aerobotics.android.fragments.account.editor.tool.EditorToolsFragment;
import co.aerobotics.android.fragments.account.editor.tool.EditorToolsImpl;
import co.aerobotics.android.fragments.actionbar.ActionBarTelemFragment;
import co.aerobotics.android.fragments.helpers.GestureMapFragment;
import co.aerobotics.android.data.AeroviewPolygons;
import co.aerobotics.android.mission.DJIMissionImpl;
import co.aerobotics.android.mission.TimelineMissionImpl;
import co.aerobotics.android.proxy.mission.MissionProxy;
import co.aerobotics.android.proxy.mission.MissionSelection;
import co.aerobotics.android.proxy.mission.item.MissionItemProxy;
import co.aerobotics.android.proxy.mission.item.fragments.MissionDetailFragment;
import co.aerobotics.android.proxy.mission.item.fragments.MissionSurveyFragment;
import co.aerobotics.android.utils.file.DirectoryPath;
import co.aerobotics.android.utils.file.FileList;
import co.aerobotics.android.utils.file.FileStream;
import co.aerobotics.android.utils.prefs.AutoPanMode;
import co.aerobotics.android.utils.prefs.DroidPlannerPrefs;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.github.jorgecastilloprz.FABProgressCircle;
import com.github.jorgecastilloprz.listeners.FABProgressListener;
import com.google.android.gms.common.GoogleApiAvailability;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.o3dr.android.client.utils.FileUtils;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.mission.MissionItemType;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.mission.item.complex.Survey;

import org.beyene.sius.unit.length.LengthUnit;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import dji.common.error.DJIError;
import dji.keysdk.CameraKey;
import dji.keysdk.callback.GetCallback;
import dji.sdk.sdkmanager.DJISDKManager;


/**
 * This implements the map editor activity. The map editor activity allows the
 * user to create and/or modify autonomous missions for the drone.
 */
public class EditorActivity extends DrawerNavigationUI implements GestureMapFragment.OnPathFinishedListener,
        EditorToolsFragment.EditorToolListener, MissionDetailFragment.OnMissionDetailListener,
        OnEditorInteraction, MissionSelection.OnSelectionUpdateListener, OnClickListener,
        OnLongClickListener, SupportEditInputDialog.Listener, Toolbar.OnMenuItemClickListener,
        SearchBoundariesDialog.OnGoToBoundaryListener, AeroviewPolygons.OnSyncFinishedListener, FABProgressListener {

    /**
     * Used to retrieve the item detail window when the activity is destroyed,
     * and recreated.
     */

    private Handler handler = new Handler(Looper.getMainLooper());
    private Handler buttonHandler = new Handler(Looper.getMainLooper());
    private Handler listnerHandler = new Handler(Looper.getMainLooper());
    private String TAG = "editor_activity";


    private static final String ITEM_DETAIL_TAG = "Item Detail Window";

    private static final String EXTRA_OPENED_MISSION_FILENAME = "extra_opened_mission_filename";
    private static final String BUTTON_STATE = "mission_button_state";

    private static final IntentFilter eventFilter = new IntentFilter();
    private static final String MISSION_FILENAME_DIALOG_TAG = "Mission filename";
    private Snackbar bar;


    static {
        eventFilter.addAction(MissionProxy.ACTION_MISSION_PROXY_UPDATE);
        eventFilter.addAction(DroidPlannerPrefs.PREF_VEHICLE_DEFAULT_SPEED);
        eventFilter.addAction(AeroviewPolygons.ACTION_ERROR_MSG);
        eventFilter.addAction(AeroviewPolygons.SYNC_COMPLETE);
        eventFilter.addAction(DroidPlannerApp.REGISTER_DJI_SDK);
        eventFilter.addAction(DJIMissionImpl.MISSION_START);
        eventFilter.addAction(DJIMissionImpl.MiSSION_STOP);
        eventFilter.addAction(DJIMissionImpl.ERROR_CAMERA);
        eventFilter.addAction(DJIMissionImpl.ERROR_SD_CARD);
        eventFilter.addAction(DrawerNavigationUI.TOGGLE_TELEMETRY);
        eventFilter.addAction(DJIMissionImpl.ERROR_MISSION_START);
        eventFilter.addAction(DJIMissionImpl.UPLOAD_STARTING);
    }

    private final BroadcastReceiver eventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case MissionProxy.ACTION_MISSION_PROXY_UPDATE:
                    if (mAppPrefs.isZoomToFitEnable()) {
                        gestureMapFragment.getMapFragment().zoomToFit();
                    }

                case AttributeEvent.MISSION_RECEIVED:
                    final EditorMapFragment planningMapFragment = gestureMapFragment.getMapFragment();
                    if (planningMapFragment != null) {
                        planningMapFragment.zoomToFit();
                    }
                    break;

                case AeroviewPolygons.ACTION_ERROR_MSG:
                    fabProgressCircle.hide();
                    fabProgressCircle.setClickable(true);
                    final View view = findViewById(R.id.editorCoordinatorLayout);
                    Snackbar snack = Snackbar.make(view, "Server Error", Snackbar.LENGTH_LONG)
                            .setAction("Action", null);
                    View snackBarView = snack.getView();
                    snackBarView.setBackgroundColor(getResources().getColor(R.color.primary_dark_blue));
                    snack.show();
                    break;
                case AeroviewPolygons.SYNC_COMPLETE:
                    aeroviewBoundaryPromptMessage(EditorActivity.this);
                    break;
                case DJIMissionImpl.UPLOAD_STARTING:
                    final View editor_view = findViewById(R.id.editorCoordinatorLayout);
                    String waypoint = intent.getStringExtra("WAYPOINT");
                    String total = intent.getStringExtra("TOTAL_WAYPOINTS");
                    if (bar == null || !bar.isShown()) {
                        bar = Snackbar.make(editor_view, "Uploading waypoints: " + waypoint + "/" + total , Snackbar.LENGTH_INDEFINITE);
                        View barView = bar.getView();
                        barView.setBackgroundColor(getResources().getColor(R.color.primary_dark_blue));
                        bar.show();
                    } else {
                        bar.setText("Uploading waypoints: " + waypoint + "/" + total);
                    }

                    if (Objects.equals(waypoint, total) && bar != null) {
                        bar.dismiss();
                    }

                    break;
                case DJIMissionImpl.MISSION_START:
                    if (bar != null) {
                        //bar.dismiss();
                        bar = Snackbar.make(findViewById(R.id.editorCoordinatorLayout), "Starting mission", Snackbar.LENGTH_SHORT);
                        bar.getView().setBackgroundColor(getResources().getColor(R.color.primary_dark_blue));
                        bar.show();
                    }
                    mMixpanel.track("FPA: MissionStarted");
                    Answers.getInstance().logCustom(new CustomEvent("Mission Started")
                            .putCustomAttribute("user email", clientEmail));
                    setStopButton();
                    showTelemetry();
                    setTelemSwitch(false);
                    zoomToMission();
                    break;
                case DJIMissionImpl.MiSSION_STOP:
                    mMixpanel.track("FPA: MissionEnded");
                    setStartButton();
                    showSnackBar("Mission end", "");
                    break;
                case DJIMissionImpl.ERROR_CAMERA:
                    cameraWarning(EditorActivity.this);
                    break;
                case DJIMissionImpl.ERROR_SD_CARD:
                    setResultToToast("Error: SD Card not found or memory is full");
                    break;
                case DrawerNavigationUI.TOGGLE_TELEMETRY:
                    toggleTelemetry();
                    break;
                case DJIMissionImpl.ERROR_MISSION_START:
                    if (bar != null) {
                        bar.dismiss();
                    }
                    String error = intent.getStringExtra("errorMessage");
                    setResultToToast("Message start failed: " + error);
                    break;
            }
        }
    };

    /**
     * Used to provide access and interact with the
     * {@link MissionProxy} object on the Android
     * layer.
     */
    private MissionProxy missionProxy;
    private DJIMissionImpl missionControl;
    private TimelineMissionImpl timelineMission;

    /*
     * View widgets.
     */
    private android.support.v7.widget.Toolbar toolbar;
    private ActionBarTelemFragment actionBarTelem;
    private GestureMapFragment gestureMapFragment;
    private EditorToolsFragment editorToolsFragment;
    private MissionDetailFragment itemDetailFragment;
    private MissionSurveyFragment missionSurveyFragment;
    private FragmentManager fragmentManager;


    /**
     * If the mission was loaded from a file, the filename is stored here.
     */
    private File openedMissionFile;
    private FloatingActionButton drawCustomMissionButton;
    private FloatingActionButton itemDetailToggle;
    private FloatingActionButton generateMissionButton;
    private FloatingActionButton deleteMissionButton;
    private EditorListFragment editorListFragment;
    private FloatingActionButton searchBoundaries;
    private FABProgressCircle fabProgressCircle;
    private FloatingActionButton syncWithProgressBar;

    private Button missionButton;
    private Button calibrateButtton;

    private MixpanelAPI mMixpanel;
    private String clientEmail;
    private Boolean isOnTour = false;

    private static final String[] REQUIRED_PERMISSION_LIST = new String[]{
            Manifest.permission.VIBRATE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
    };
    public static final String FLAG_CONNECTION_CHANGE = "dji_sdk_connection_change";

    private List<String> missingPermission = new ArrayList<>();
    private AtomicBoolean isRegistrationInProgress = new AtomicBoolean(false);
    private static final int REQUEST_PERMISSION_CODE = 12345;
    private long numberOfImagesLeft;
    private boolean isSDCardInserted;
    private CameraKey sdCardCaptureCountKey = CameraKey.create(CameraKey.SDCARD_AVAILABLE_CAPTURE_COUNT);
    private CameraKey sdCardInsertedKey = CameraKey.create(CameraKey.SDCARD_IS_INSERTED);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        fragmentManager = getSupportFragmentManager();
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // When the compile and target version is higher than 22, please request the following permission at runtime to ensure the SDK works well.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkAndRequestPermissions();
        } else {
            DroidPlannerApp.getInstance().registerSDK();
        }
        setContentView(co.aerobotics.android.R.layout.activity_editor);
        isOnTour = false;
        SharedPreferences sharedPref = this.getSharedPreferences(this.getResources().getString(R.string.com_dji_android_PREF_FILE_KEY), Context.MODE_PRIVATE);
        clientEmail = sharedPref.getString(this.getResources().getString(R.string.username), "");

        mMixpanel = MixpanelAPI.getInstance(this, DroidPlannerApp.getInstance().getMixpanelToken());
        mMixpanel.track("FPA: OnCreateEditorActivity");

        gestureMapFragment = ((GestureMapFragment) fragmentManager.findFragmentById(co.aerobotics.android.R.id.editor_map_fragment));
        if (gestureMapFragment == null) {
            gestureMapFragment = new GestureMapFragment();
            fragmentManager.beginTransaction().add(co.aerobotics.android.R.id.editor_map_fragment, gestureMapFragment).commit();
        }

        editorToolsFragment = ((EditorToolsFragment) fragmentManager.findFragmentByTag("editorTools"));
        if (editorToolsFragment == null) {
            editorToolsFragment = new EditorToolsFragment();
            fragmentManager.beginTransaction().add(R.id.actionbar_toolbar_top, editorToolsFragment, "editorTools").commit();
            fragmentManager.beginTransaction().detach(editorToolsFragment).commit();
        }

        actionBarTelem = ((ActionBarTelemFragment) fragmentManager.findFragmentByTag("telemData"));
        if (actionBarTelem == null || !actionBarTelem.isAdded()) {
            actionBarTelem = new ActionBarTelemFragment();
            fragmentManager.beginTransaction().add(R.id.actionbar_toolbar_top, actionBarTelem, "telemData").commit();
        }


        if (missionControl == null) {
            missionControl = new DJIMissionImpl();
        }

        if (timelineMission == null) {
            timelineMission = new TimelineMissionImpl();
        }

        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.actionbar_toolbar_top);
        //toolbar.inflateMenu(R.menu.menu_mission);
        toolbar.setOnMenuItemClickListener(this);
        if (DroidPlannerApp.getInstance().hideTelemetry) {
            toolbar.setVisibility(View.GONE);
        }

        //getSupportActionBar().setDisplayShowTitleEnabled(false);
        //getSupportActionBar().hide();

        missionButton = (Button) findViewById(R.id.start_mission_button);
        missionButton.setOnClickListener(this);

        drawCustomMissionButton = (FloatingActionButton) findViewById(R.id.drawCustomMission);
        drawCustomMissionButton.setOnClickListener(this);

        generateMissionButton = (FloatingActionButton) findViewById(R.id.generateMission);
        generateMissionButton.setOnClickListener(this);

        fabProgressCircle = (FABProgressCircle) findViewById(R.id.fabProgressCircle);
        fabProgressCircle.setOnClickListener(this);
        fabProgressCircle.setClickable(true);
        fabProgressCircle.attachListener(this);

        syncWithProgressBar = (FloatingActionButton) findViewById(R.id.fab);

        final FloatingActionButton mGoToMyLocation = (FloatingActionButton) findViewById(co.aerobotics.android.R.id.my_location_button);
        mGoToMyLocation.setOnClickListener(this);
        //mGoToMyLocation.setOnLongClickListener(this);

        final FloatingActionButton mGoToDroneLocation = (FloatingActionButton) findViewById(co.aerobotics.android.R.id.drone_location_button);
        mGoToDroneLocation.setOnClickListener(this);
        //mGoToDroneLocation.setOnLongClickListener(this);

        final FloatingActionButton mSyncWithAeroView = (FloatingActionButton) findViewById(R.id.syncWithAeroViewButton);
        mSyncWithAeroView.setOnClickListener(this);

        final FloatingActionButton mSearchBoundaries = (FloatingActionButton) findViewById(R.id.search_boundaries);
        mSearchBoundaries.setOnClickListener(this);

        deleteMissionButton = (FloatingActionButton) findViewById(R.id.deleteMission);
        deleteMissionButton.setOnClickListener(this);

        if (savedInstanceState != null) {
            String openedMissionFilename = savedInstanceState.getString(EXTRA_OPENED_MISSION_FILENAME);
            if (!TextUtils.isEmpty(openedMissionFilename)) {
                openedMissionFile = new File(openedMissionFilename);
            }

            int buttonState = savedInstanceState.getInt(BUTTON_STATE);
            if (buttonState == 0) {
                setStartButton();
            } else {
                setStopButton();
            }
        } else {
            setStartButton();
        }

        gestureMapFragment.setOnPathFinishedListener(this);
        openActionDrawer();

    }

    private void checkAndRequestPermissions() {
        // Check for permissions
        for (String eachPermission : REQUIRED_PERMISSION_LIST) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), eachPermission) != PackageManager.PERMISSION_GRANTED) {
                missingPermission.add(eachPermission);
            }
        }
        // Request for missing permissions
        if (missingPermission.isEmpty()) {
            DroidPlannerApp.getInstance().registerSDK();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            showToast("Need to grant the permissions!");
            ActivityCompat.requestPermissions(this,
                    missingPermission.toArray(new String[missingPermission.size()]),
                    REQUEST_PERMISSION_CODE);
        }
    }
    /**
     * Result of runtime permission request
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Check for granted permission and remove from missing list
        if (requestCode == REQUEST_PERMISSION_CODE) {
            for (int i = grantResults.length - 1; i >= 0; i--) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    missingPermission.remove(permissions[i]);
                }
            }

            // If there is enough permission, we will start the registration
            if (missingPermission.isEmpty()) {
                DroidPlannerApp.getInstance().registerSDK();
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.READ_PHONE_STATE)) {
                        OkDialog dialog = OkDialog.newInstance(getApplicationContext(),
                                "Permissions Warning", "Access to Phone State is required for this application to function properly"
                                , new OkDialog.Listener() {
                                    @Override
                                    public void onOk() {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE},
                                                    REQUEST_PERMISSION_CODE);
                                        }
                                    }

                                    @Override
                                    public void onCancel() {
                                    }

                                    @Override
                                    public void onDismiss() {
                                    }
                                }, true);
                        dialog.show(getSupportFragmentManager(), "Permissions Dialog");
                    }
                }
            }
        }
    }

    public void checkSdCardInserted() {
        DJISDKManager.getInstance().getKeyManager().getValue(sdCardInsertedKey, new GetCallback() {
            @Override
            public void onSuccess(Object o) {
                if (o instanceof Boolean) {
                    if ((Boolean) o) {
                        checkSdCardSpaceAvailable();
                    } else {
                        showSnackBar("Error: Cannot find SD card", "");
                    }
                }
            }

            @Override
            public void onFailure(DJIError djiError) {
                showSnackBar("Error: SD card check failed", "");
            }
        });
    }
    public void checkSdCardSpaceAvailable() {
        DJISDKManager.getInstance().getKeyManager().getValue(sdCardCaptureCountKey, new GetCallback() {
            @Override
            public void onSuccess(Object value) {
                if (value instanceof Long){
                    if ((Long) value > getNumberofSurveyImages()) {
                        if (missionProxy.getItems().size() > 1) {
                            timelineMission.run(missionProxy);
                        } else {
                            missionControl.run(missionProxy);
                        }
                    } else {
                        showSnackBar("Error: SD card memory is full", "");
                    }
                }
            }

            @Override
            public void onFailure(DJIError djiError) {
                showSnackBar("Error: SD card check failed", "");
            }
        });
    }

    private long getNumberofSurveyImages() {
        List<Survey> surveyList = new ArrayList<>();
        for (MissionItemProxy item : missionProxy.getItems()) {
            surveyList.add((Survey) item.getMissionItem());
        }
        long totalImages = 0;
        for (Survey survey : surveyList) {
            totalImages = totalImages + survey.getCameraCount();
        }
        return totalImages;
    }

    private void showToast(final String toastMsg) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_LONG).show();
            }
        });
    }
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case co.aerobotics.android.R.id.menu_open_mission:
                mMixpanel.track("FPA: TapOpenMissionFile");
                openMissionFile();
                return true;

            case co.aerobotics.android.R.id.menu_save_mission:
                mMixpanel.track("FPA: TapSaveMission");
                saveMissionFile();
                return true;

            case R.id.menu_sync_boundaries:
                mMixpanel.track("FPA: TapSyncWithAeroView");
                new AeroviewPolygons(this).executeClientDataTask();
                DroidPlannerApp.getInstance().selectedPolygons.clear();
                break;
            default:
                break;
        }
        return false;
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d("EDITOR", "On New Intent");
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent == null || missionProxy == null)
            return;
        String extra = intent.getStringExtra("startTour");
        if (!TextUtils.isEmpty(extra) && extra.equals("start")) {
            startTour();
        }

        String action = intent.getAction();
        if (TextUtils.isEmpty(action))
            return;

        switch (action) {
            case Intent.ACTION_VIEW:
                Uri loadUri = intent.getData();
                if (loadUri != null) {
                    openMissionFile(loadUri);
                }
                break;
        }
    }

    @Override
    protected float getActionDrawerTopMargin() {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics());
    }

    /**
     * Account for the various ui elements and update the map padding so that it
     * remains 'visible'.
     */
    private void updateLocationButtonsMargin(boolean isOpened) {
        final View actionDrawer = getActionDrawer();
        if (actionDrawer == null)
            return;

        //itemDetailToggle.setActivated(isOpened);
    }

    @Override
    public void onApiConnected() {
        getBroadcastManager().registerReceiver(eventReceiver, eventFilter);
        super.onApiConnected();
        missionProxy = dpApp.getMissionProxy();
        if (missionProxy != null) {
            missionProxy.selection.addSelectionUpdateListener(this);
        }

        handleIntent(getIntent());
        updateMissionLength();

    }

    @Override
    public void onApiDisconnected() {
        super.onApiDisconnected();

        if (missionProxy != null)
            missionProxy.selection.removeSelectionUpdateListener(this);

        getBroadcastManager().unregisterReceiver(eventReceiver);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMixpanel.flush();
    }

    @Override
    public void onClick(View v) {
        final EditorMapFragment planningMapFragment = gestureMapFragment.getMapFragment();

        switch (v.getId()) {
            case R.id.start_mission_button:
                final int status = (Integer) v.getTag();
                if (status == 0) {
                    //confirmMissionStart(EditorActivity.this);
                    mMixpanel.track("FPA: TapStartMission");
                    if (DroidPlannerApp.isProductConnected()) {
                        confirmMissionStart(EditorActivity.this);
                    } else {
                        setResultToToast("Disconnected");
                    }
                } else {
                    mMixpanel.track("FPA: TapStopMission");
                    confirmMissionStop(EditorActivity.this);
                }
                break;

            case co.aerobotics.android.R.id.drone_location_button:
                mMixpanel.track("FPA: TapGoToDroneLocation");

                planningMapFragment.goToDroneLocation();
                break;

            case co.aerobotics.android.R.id.my_location_button:
                mMixpanel.track("FPA: TapGoToMyLocation");
                planningMapFragment.goToMyLocation();
                break;

            case R.id.drawCustomMission:
                mMixpanel.track("FPA: TapDrawCustomMission");
                if (!drawCustomMissionButton.isSelected()) {
                    drawCustomMissionButton.setSelected(true);
                    enableGestureDetection(true);
                    Toast.makeText(editorToolsFragment.getContext(), R.string.draw_the_survey_region, Toast.LENGTH_SHORT).show();
                } else {
                    drawCustomMissionButton.setSelected(false);
                    enableGestureDetection(false);
                    if (isOnTour) {
                        isOnTour = false;
                        final View view = findViewById(R.id.editorCoordinatorLayout);
                        Snackbar snack = Snackbar.make(view, "You have cancelled the tour before it was complete", Snackbar.LENGTH_LONG)
                                .setAction("Action", null);
                        View snackBarView = snack.getView();
                        snackBarView.setBackgroundColor(getResources().getColor(R.color.primary_dark_blue));
                        snack.show();
                    }
                }
                break;

            case R.id.generateMission:
                mMixpanel.track("FPA: TapGenerateMission");
                if (DroidPlannerApp.getInstance().getSelectedPolygons().size() > 1) {
                    missionProxy.createMergedConvexSurvey();
                } else {
                    missionProxy.createSurvey();
                }
                generateMissionButton.setVisibility(View.GONE);
                break;

            case R.id.syncWithAeroViewButton:
                mMixpanel.track("FPA: TapSyncWithAeroView");

                new AeroviewPolygons(this).executeClientDataTask();
                DroidPlannerApp.getInstance().selectedPolygons.clear();
                break;

            case R.id.fabProgressCircle:
                mMixpanel.track("FPA: TapSyncWithAeroView");
                fabProgressCircle.show();
                fabProgressCircle.setClickable(false);

                AeroviewPolygons aeroviewPolygons = new AeroviewPolygons(this);
                aeroviewPolygons.setOnSyncFinishedListener(this);
                aeroviewPolygons.executeClientDataTask();

                break;

            case R.id.search_boundaries:
                mMixpanel.track("FPA: TapSearchBoundaries");
                new SearchBoundariesDialog().show(getFragmentManager(), null);
                break;

            case R.id.deleteMission:
                mMixpanel.track("FPA: TapDeleteMission");
                missionProxy.removeSelection(missionProxy.selection);
                break;
            default:
                break;
        }
    }


    private void toggleActionBarView() {
        if (!editorToolsFragment.isDetached()) {
            showDataViewToolbar();
        } else {
            fragmentManager.beginTransaction().detach(actionBarTelem).commit();
            fragmentManager.beginTransaction().attach(editorToolsFragment).commit();
        }
    }

    private void toggleTelemetry() {
        setSupportActionBar(toolbar);
        if (DroidPlannerApp.getInstance().hideTelemetry) {
            mMixpanel.track("FPA: HideTelemetry");
            getSupportActionBar().hide();
            //fragmentManager.beginTransaction().attach(actionBarTelem).commit();
        } else {
            mMixpanel.track("FPA: ShowTelemetry");
            getSupportActionBar().show();
            //fragmentManager.beginTransaction().detach(actionBarTelem).commit();
        }
        setSupportActionBar(null);
    }

    private void showTelemetry() {
        setSupportActionBar(toolbar);
        if (!getSupportActionBar().isShowing()) {
            getSupportActionBar().show();
            DroidPlannerApp.getInstance().hideTelemetry = false;
        }
        setSupportActionBar(null);
    }

    private void showDataViewToolbar() {
        fragmentManager.beginTransaction().detach(editorToolsFragment).commit();
        actionBarTelem = ((ActionBarTelemFragment) fragmentManager.findFragmentByTag("telemData"));
        if (actionBarTelem == null || !actionBarTelem.isAdded()) {
            actionBarTelem = new ActionBarTelemFragment();
            fragmentManager.beginTransaction().add(R.id.actionbar_toolbar_top, actionBarTelem, "telemData").commit();

        } else {
            fragmentManager.beginTransaction().attach(actionBarTelem).commit();
        }
    }

    @Override
    public boolean onLongClick(View view) {
        final EditorMapFragment planningMapFragment = gestureMapFragment.getMapFragment();

        switch (view.getId()) {
            case co.aerobotics.android.R.id.drone_location_button:
                planningMapFragment.setAutoPanMode(AutoPanMode.DRONE);
                return true;
            case co.aerobotics.android.R.id.my_location_button:
                planningMapFragment.setAutoPanMode(AutoPanMode.USER);
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (editorToolsFragment.isAdded()) {
            editorToolsFragment.setToolAndUpdateView(getTool());
            setupTool();
        }
    }

    @Override
    protected int getToolbarId() {
        return R.id.actionbar_container;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (openedMissionFile != null) {
            outState.putString(EXTRA_OPENED_MISSION_FILENAME, openedMissionFile.getAbsolutePath());
        }
        if (missionButton != null) {
            outState.putInt(BUTTON_STATE, (Integer) missionButton.getTag());
        }
    }

    @Override
    protected int getNavigationDrawerMenuItemId() {
        return co.aerobotics.android.R.id.navigation_editor;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        //getMenuInflater().inflate(co.aerobotics.android.R.menu.menu_mission, menu);
        //toolbar.getMenu().clear();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case co.aerobotics.android.R.id.menu_open_mission:
                openMissionFile();
                return true;

            case co.aerobotics.android.R.id.menu_save_mission:
                saveMissionFile();
                return true;

            case R.id.menu_start_mission:
                confirmMissionStart(EditorActivity.this);
                return true;

            case R.id.menu_stop_mission:
                confirmMissionStop(EditorActivity.this);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setStartButton() {
        missionButton.setTag(0);
        missionButton.setText("Start");
        missionButton.setCompoundDrawablePadding(5);
        missionButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_takeoff_active_green, 0);
        missionButton.setVisibility(View.VISIBLE);
    }

    private void setStopButton() {
        missionButton.setTag(1);
        missionButton.setText("RTL");
        missionButton.setCompoundDrawablePadding(5);
        missionButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_land_active_orange, 0);
        missionButton.setVisibility(View.VISIBLE);
        if (!editorToolsFragment.isDetached()) {
            showDataViewToolbar();
        }
    }

    private void setResultToToast(final String string) {
        EditorActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(EditorActivity.this, string, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showSnackBar(String msg, String action) {
        final View view = findViewById(R.id.editorCoordinatorLayout);
        Snackbar snack = Snackbar.make(view, msg, Snackbar.LENGTH_LONG)
                .setAction(action, null);
        View snackBarView = snack.getView();
        snackBarView.setBackgroundColor(getResources().getColor(R.color.primary_dark_blue));
        snack.show();
    }

    private void zoomToMission(){
        /**
         * Move camera to show drone position and mission area
         */
        List<MissionItemProxy> items = missionProxy.getItems();
        List<LatLong> points = new ArrayList<>();
        for (MissionItemProxy itemProxy : items) {
            MissionItem item = itemProxy.getMissionItem();
            if (item instanceof Survey) {
                points.addAll(((Survey) item).getGridPoints());
            }
        }

        DJIFlightControllerState state = DJIFlightControllerState.getInstance();
        if (state.getFlightControllerState() != null) {
            points.add(new LatLong(state.getDroneLatitude(),state.getDroneLongitude()));
        }
        gestureMapFragment.getMapFragment().zoomToFit(points);
    }

    private void confirmMissionStart(Context context) {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirm Mission Start")
                .setMessage("Are you sure you want to start the mission?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (!missionProxy.getItems().isEmpty()) {
                            checkSdCardInserted();
                        } else {
                            setResultToToast("Please create a survey mission");
                        }
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(R.drawable.ic_warning_dark_blue_36dp)
                .show();
    }

    private void confirmMissionStop(Context context) {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(context);
        builder.setTitle("Abort Mission")
                .setMessage("Would you like to abort the current mission?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (DJISDKManager.getInstance().getMissionControl().isTimelineRunning()) {
                            timelineMission.stopTimelineMission();
                        } else {
                            if (missionControl != null) {
                                missionControl.stopWaypointMission();
                            }
                        }
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(R.drawable.ic_warning_dark_blue_36dp)
                .show();
    }

    private void cameraWarning(Context context) {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(context);
        builder.setTitle("Camera Trigger Limit Exceeded")
                .setMessage("The current mission parameters exceed the capabilities of the camera."
                        + "\n\n" + "Please reduce aircraft speed or increase flight altitude and restart mission.")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //do nothing
                    }
                })
                .show();
    }

    private void addBoundary() {
        new AddBoundaryCheckDialog().show(fragmentManager, null);
    }

    private void aeroviewBoundaryPromptMessage(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.AlertDialogCustom));
        builder.setTitle("AeroView Boundaries");
        builder.setMessage("You do not have any boundaries linked to your account. Please login to Aeroview online to add some.");
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //do nothing
            }
        });
        builder.setIcon(R.drawable.ic_aero_a);
        builder.show();

    }

    private void openMissionFile() {
        OpenFileDialog missionDialog = new OpenFileDialog() {
            @Override
            public void onFileSelected(String filepath) {
                File missionFile = new File(filepath);
                openedMissionFile = missionFile;
                openMissionFile(Uri.fromFile(missionFile));
            }
        };
        missionDialog.openDialog(this, DirectoryPath.getWaypointsPath(), FileList.getWaypointFileList());
    }

    private void openMissionFile(Uri missionUri) {
        if (missionProxy != null) {
            missionProxy.readMissionFromFile(missionUri);
        }
    }

    @Override
    public void onOk(String dialogTag, CharSequence input) {
        switch (dialogTag) {
            case MISSION_FILENAME_DIALOG_TAG:
                File saveFile = openedMissionFile == null
                        ? new File(DirectoryPath.getWaypointsPath(), input.toString() + FileList.WAYPOINT_FILENAME_EXT)
                        : new File(openedMissionFile.getParent(), input.toString() + FileList.WAYPOINT_FILENAME_EXT);
                missionProxy.writeMissionToFile(Uri.fromFile(saveFile));
                break;
        }
    }

    @Override
    public void onCancel(String dialogTag) {
    }

    private void saveMissionFile() {
        final String defaultFilename = openedMissionFile == null
                ? getWaypointFilename("waypoints")
                : FileUtils.getFilenameWithoutExtension(openedMissionFile);

        final SupportEditInputDialog dialog = SupportEditInputDialog.newInstance(MISSION_FILENAME_DIALOG_TAG,
                getString(co.aerobotics.android.R.string.label_enter_filename), defaultFilename, true);

        dialog.show(getSupportFragmentManager(), MISSION_FILENAME_DIALOG_TAG);
    }

    private static String getWaypointFilename(String prefix) {
        return prefix + "-" + FileStream.getTimeStamp();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        gestureMapFragment.getMapFragment().saveCameraPosition();
    }

    private void updateMissionLength() {
        if (missionProxy != null) {

            Pair<Double, Double> distanceAndTime = missionProxy.getMissionFlightTime();
            LengthUnit convertedMissionLength = unitSystem.getLengthUnitProvider()
                    .boxBaseValueToTarget(distanceAndTime.first);

            double time = distanceAndTime.second;
            String infoString = getString(co.aerobotics.android.R.string.editor_info_window_distance,
                    convertedMissionLength.toString()) +
                    ", " +
                    getString(co.aerobotics.android.R.string.editor_info_window_flight_time, time == Double.POSITIVE_INFINITY
                            ? time
                            : String.format(Locale.US, "%1$02d:%2$02d", ((int) time / 60), ((int) time % 60)));


            // Remove detail window if item is removed
            if (missionProxy.selection.getSelected().isEmpty() && itemDetailFragment != null) {
                removeItemDetail();
            }
        }
    }

    @Override
    public void onMapClick(LatLong point) {
        EditorToolsImpl toolImpl = getToolImpl();
        toolImpl.onMapClick(point);
    }

    public EditorToolsFragment.EditorTools getTool() {
        return editorToolsFragment.getTool();
    }

    public EditorToolsImpl getToolImpl() {
        return editorToolsFragment.getToolImpl();
    }

    @Override
    public void editorToolChanged(EditorToolsFragment.EditorTools tools) {
        setupTool();
    }

    @Override
    public void enableGestureDetection(boolean enable) {
        if (gestureMapFragment == null)
            return;

        if (enable)
            gestureMapFragment.enableGestureDetection();
        else
            gestureMapFragment.disableGestureDetection();
    }

    private void setupTool() {
        final EditorToolsImpl toolImpl = getToolImpl();
        toolImpl.setup();
        editorListFragment.enableDeleteMode(toolImpl.getEditorTools() == EditorToolsFragment.EditorTools.TRASH);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        updateLocationButtonsMargin(itemDetailFragment != null);
    }

    @Override
    protected void addToolbarFragment() {
        final int toolbarId = getToolbarId();
        editorListFragment = (EditorListFragment) fragmentManager.findFragmentById(toolbarId);
        if (editorListFragment == null) {
            editorListFragment = new EditorListFragment();
            fragmentManager.beginTransaction().add(toolbarId, editorListFragment).commit();
        }
    }

    private void showItemDetail(MissionDetailFragment itemDetail) {
        if (itemDetailFragment == null) {
            addItemDetail(itemDetail);
        } else {
            switchItemDetail(itemDetail);
        }

        if (!editorToolsFragment.isDetached()) {
            editorToolsFragment.setToolAndUpdateView(EditorToolsFragment.EditorTools.NONE);
        }
    }

    private void addItemDetail(MissionDetailFragment itemDetail) {
        itemDetailFragment = itemDetail;
        if (itemDetailFragment == null)
            return;

        fragmentManager.beginTransaction()
                .replace(getActionDrawerId(), itemDetailFragment, ITEM_DETAIL_TAG)
                .commit();
        getSupportFragmentManager().executePendingTransactions();
        updateLocationButtonsMargin(true);
    }

    public void switchItemDetail(MissionDetailFragment itemDetail) {
        removeItemDetail();
        addItemDetail(itemDetail);
    }

    private void removeItemDetail() {
        if (itemDetailFragment != null) {
            fragmentManager.beginTransaction().remove(itemDetailFragment).commit();
            itemDetailFragment = null;

            updateLocationButtonsMargin(false);
        }
    }

    @Override
    public void onPathFinished(List<LatLong> path) {
        final EditorMapFragment planningMapFragment = gestureMapFragment.getMapFragment();
        List<LatLong> points = planningMapFragment.projectPathIntoMap(path);
        drawCustomMissionButton.setSelected(false);

        if (points.size() > 2) {
            missionProxy.addSurveyPolygon(points, false);

            if (isOnTour) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        missionItemTapTargetDisplay();
                    }
                }, 1000);

            }
        }

        EditorToolsImpl toolImpl = getToolImpl();
        toolImpl.onPathFinished(points);

    }

    @Override
    public void onDetailDialogDismissed(List<MissionItemProxy> itemList) {
        if (missionProxy != null) missionProxy.selection.removeItemsFromSelection(itemList);
    }

    @Override
    public void onWaypointTypeChanged(MissionItemType newType, List<Pair<MissionItemProxy,
            List<MissionItemProxy>>> oldNewItemsList) {
        missionProxy.replaceAll(oldNewItemsList);
    }

    private MissionDetailFragment selectMissionDetailType(List<MissionItemProxy> proxies) {
        if (proxies == null || proxies.isEmpty())
            return null;

        MissionItemType referenceType = null;
        for (MissionItemProxy proxy : proxies) {
            final MissionItemType proxyType = proxy.getMissionItem().getType();
            if (referenceType == null) {
                referenceType = proxyType;
            } else if (referenceType != proxyType
                    || MissionDetailFragment.typeWithNoMultiEditSupport.contains(referenceType)) {
                //Return a generic mission detail.
                return new MissionDetailFragment();
            }
        }

        return MissionDetailFragment.newInstance(referenceType);
    }

    @Override
    public void onItemClick(MissionItemProxy item, boolean zoomToFit) {
        if (missionProxy == null) return;

        if (!editorToolsFragment.isDetached()) {
            EditorToolsImpl toolImpl = getToolImpl();
            toolImpl.onListItemClick(item);
        } else {
            if (missionProxy.selection.selectionContains(item)) {
                missionProxy.selection.clearSelection();
            } else {
                missionProxy.selection.setSelectionTo(item);
            }
        }

        if (zoomToFit) {
            zoomToFitSelected();
        }
    }

    @Override
    public void zoomToFitSelected() {
        final EditorMapFragment planningMapFragment = gestureMapFragment.getMapFragment();
        List<MissionItemProxy> selected = missionProxy.selection.getSelected();
        if (selected.isEmpty()) {
            planningMapFragment.zoomToFit();
        } else {
            planningMapFragment.zoomToFit(MissionProxy.getVisibleCoords(selected));
        }
    }

    @Override
    public void onListVisibilityChanged() {
    }

    @Override
    protected boolean enableMissionMenus() {
        return true;
    }

    @Override
    public void onSelectionUpdate(List<MissionItemProxy> selected) {
        EditorToolsImpl toolImpl = getToolImpl();
        toolImpl.onSelectionUpdate(selected);

        final boolean isEmpty = selected.isEmpty();

        if (isEmpty) {
            deleteMissionButton.setVisibility(View.GONE);
            removeItemDetail();
        } else {
            deleteMissionButton.setVisibility(View.VISIBLE);
            if (getTool() == EditorToolsFragment.EditorTools.SELECTOR)
                removeItemDetail();
            else {
                showItemDetail(selectMissionDetailType(selected));
            }
        }
    }

    @Override
    public void OnGoToBoundaySelected(List<LatLong> polygonPoints) {
        final EditorMapFragment planningMapFragment = gestureMapFragment.getMapFragment();
        planningMapFragment.zoomToFit(polygonPoints);
    }

    @Override
    public void onSyncFinished() {
        fabProgressCircle.beginFinalAnimation();
        //fabProgressCircle.setClickable(true);
    }

    @Override
    public void onFABProgressAnimationEnd() {
        fabProgressCircle.setClickable(true);
        final View view = findViewById(R.id.editorCoordinatorLayout);
        Snackbar snack = Snackbar.make(view, R.string.aeroview_sync_successful, Snackbar.LENGTH_LONG)
                .setAction("Action", null);
        View snackBarView = snack.getView();
        snackBarView.setBackgroundColor(getResources().getColor(R.color.primary_dark_blue));
        snack.show();
    }


    public void startTour() {
        // final Display display = getWindowManager().getDefaultDisplay();
        mMixpanel.track("FPA: TourStarted");
        final Rect rectTarget = new Rect(50, 30, 0, 0);
        rectTarget.offset(60, 60);
        isOnTour = true;
        final TapTargetSequence targetSequence = new TapTargetSequence(this).targets(
                TapTarget.forView(findViewById(R.id.my_location_button), "My location", "Tap this to center the map on your location")
                        // All options below are optional
                        .outerCircleColor(R.color.primary_dark_blue)      // Specify a color for the outer circle
                        .outerCircleAlpha(0.96f)            // Specify the alpha amount for the outer circle
                        .targetCircleColor(R.color.white)   // Specify a color for the target circle
                        .titleTextSize(24)                  // Specify the size (in sp) of the title text
                        .titleTextColor(R.color.white)      // Specify the color of the title text
                        .descriptionTextSize(16)            // Specify the size (in sp) of the description text
                        .descriptionTextColor(R.color.white)  // Specify the color of the description text
                        .textTypeface(Typeface.SANS_SERIF)  // Specify a typeface for the text
                        .dimColor(R.color.primary_dark_blue) // If set, will dim behind the view with 30% opacity of the given color
                        .drawShadow(true)                   // Whether to draw a drop shadow or not
                        .cancelable(true)                  // Whether tapping outside the outer circle dismisses the view
                        .tintTarget(true)                   // Whether to tint the target view's color
                        .transparentTarget(true)           // Specify whether the target is transparent (displays the content underneath)
                        .targetRadius(30)                  // Specify the target radius (in dp)
                        .id(1),
                TapTarget.forView(findViewById(R.id.drone_location_button), "Drone Location", "Tap this to center the map on your drone's location")
                        .outerCircleColor(R.color.primary_dark_blue)      // Specify a color for the outer circle
                        .outerCircleAlpha(0.96f)            // Specify the alpha amount for the outer circle
                        .targetCircleColor(R.color.white)   // Specify a color for the target circle
                        .titleTextSize(24)                  // Specify the size (in sp) of the title text
                        .titleTextColor(R.color.white)      // Specify the color of the title text
                        .descriptionTextSize(16)            // Specify the size (in sp) of the description text
                        .descriptionTextColor(R.color.white)  // Specify the color of the description text
                        .textTypeface(Typeface.SANS_SERIF)  // Specify a typeface for the text
                        .dimColor(R.color.primary_dark_blue)            // If set, will dim behind the view with 30% opacity of the given color
                        .drawShadow(true)                   // Whether to draw a drop shadow or not
                        .cancelable(false)                  // Whether tapping outside the outer circle dismisses the view
                        .tintTarget(true)                   // Whether to tint the target view's color
                        .transparentTarget(true)           // Specify whether the target is transparent (displays the content underneath)
                        .targetRadius(30)                  // Specify the target radius (in dp)
                        .id(2),
                TapTarget.forView(findViewById(R.id.fab), "Sync with AeroView", "Tap to sync your boundaries with AeroView")
                        .outerCircleColor(R.color.primary_dark_blue)      // Specify a color for the outer circle
                        .outerCircleAlpha(0.96f)            // Specify the alpha amount for the outer circle
                        .targetCircleColor(R.color.white)   // Specify a color for the target circle
                        .titleTextSize(24)                  // Specify the size (in sp) of the title text
                        .descriptionTextSize(16)            // Specify the size (in sp) of the description text
                        .textColor(R.color.white)            // Specify a color for both the title and description text
                        .textTypeface(Typeface.SANS_SERIF)  // Specify a typeface for the text
                        .dimColor(R.color.primary_dark_blue)            // If set, will dim behind the view with 30% opacity of the given color
                        .drawShadow(true)                   // Whether to draw a drop shadow or not
                        .cancelable(true)                  // Whether tapping outside the outer circle dismisses the view
                        .tintTarget(true)                   // Whether to tint the target view's color
                        .transparentTarget(true)           // Specify whether the target is transparent (displays the content underneath)
                        .targetRadius(30)                  // Specify the target radius (in dp)
                        .id(3),
                TapTarget.forView(findViewById(R.id.search_boundaries), "Search boundaries", "Tap to search for an existing boundary")
                        .outerCircleColor(R.color.primary_dark_blue)      // Specify a color for the outer circle
                        .outerCircleAlpha(0.96f)            // Specify the alpha amount for the outer circle
                        .targetCircleColor(R.color.white)   // Specify a color for the target circle
                        .titleTextSize(24)                  // Specify the size (in sp) of the title text
                        .descriptionTextSize(16)            // Specify the size (in sp) of the description text
                        .textColor(R.color.white)            // Specify a color for both the title and description text
                        .textTypeface(Typeface.SANS_SERIF)  // Specify a typeface for the text
                        .dimColor(R.color.primary_dark_blue)            // If set, will dim behind the view with 30% opacity of the given color
                        .drawShadow(true)                   // Whether to draw a drop shadow or not
                        .cancelable(false)                  // Whether tapping outside the outer circle dismisses the view
                        .tintTarget(true)                   // Whether to tint the target view's color
                        .transparentTarget(true)           // Specify whether the target is transparent (displays the content underneath)
                        .targetRadius(30)                  // Specify the target radius (in dp)
                        .id(4),
                TapTarget.forView(findViewById(R.id.drawCustomMission), "Draw custom survey area", "Tap to draw a new survey area on the map")
                        .outerCircleColor(R.color.primary_dark_blue)      // Specify a color for the outer circle
                        .outerCircleAlpha(0.96f)            // Specify the alpha amount for the outer circle
                        .targetCircleColor(R.color.white)   // Specify a color for the target circle
                        .titleTextSize(24)                  // Specify the size (in sp) of the title text
                        .titleTextColor(R.color.white)      // Specify the color of the title text
                        .descriptionTextSize(16)            // Specify the size (in sp) of the description text
                        .descriptionTextColor(R.color.white)  // Specify the color of the description text
                        .textTypeface(Typeface.SANS_SERIF)  // Specify a typeface for the text
                        .dimColor(R.color.primary_dark_blue)            // If set, will dim behind the view with 30% opacity of the given color
                        .drawShadow(true)                   // Whether to draw a drop shadow or not
                        .cancelable(false)                  // Whether tapping outside the outer circle dismisses the view
                        .tintTarget(true)                   // Whether to tint the target view's color
                        .transparentTarget(true)           // Specify whether the target is transparent (displays the content underneath)
                        .targetRadius(30)                  // Specify the target radius (in dp)
                        .id(5)
        ).listener(new TapTargetSequence.Listener() {
            @Override
            public void onSequenceFinish() {
                mMixpanel.track("FPA: TourFirstStageCompleted");
            }

            @Override
            public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                if (lastTarget.id() == 5) {
                    if (!drawCustomMissionButton.isSelected()) {
                        drawCustomMissionButton.setSelected(true);
                        enableGestureDetection(true);
                        final View view = findViewById(R.id.editorCoordinatorLayout);
                        Snackbar snack = Snackbar.make(view, "Draw a boundary to continue", 4000)
                                .setAction("Action", null);
                        View snackBarView = snack.getView();
                        snackBarView.setBackgroundColor(getResources().getColor(R.color.primary_dark_blue));
                        snack.show();
                    }
                }
            }

            @Override
            public void onSequenceCanceled(TapTarget lastTarget) {
                mMixpanel.track("FPA: TourCancelled");
                isOnTour = false;
                final View view = findViewById(R.id.editorCoordinatorLayout);
                Snackbar snack = Snackbar.make(view, "You have cancelled the tour before it was complete", Snackbar.LENGTH_LONG)
                        .setAction("Action", null);
                View snackBarView = snack.getView();
                snackBarView.setBackgroundColor(getResources().getColor(R.color.primary_dark_blue));
                snack.show();
            }
        });
        targetSequence.start();
    }

    /**
     * Handles the second part of app tour, called after user has drawn a boundary
     */
    private void missionItemTapTargetDisplay() {
        View view = editorListFragment.getView();
        if (view.findViewById(R.id.dragHandler) != null) {
            final TapTargetSequence targetSequence = new TapTargetSequence(this).targets(
                    TapTarget.forView(view.findViewById(R.id.dragHandler), "Edit mission", "Tap this to edit, save and delete the mission")
                            .outerCircleColor(R.color.primary_dark_blue)      // Specify a color for the outer circle
                            .outerCircleAlpha(0.96f)            // Specify the alpha amount for the outer circle
                            .targetCircleColor(R.color.white)   // Specify a color for the target circle
                            .titleTextSize(24)                  // Specify the size (in sp) of the title text
                            .titleTextColor(R.color.white)      // Specify the color of the title text
                            .descriptionTextSize(16)            // Specify the size (in sp) of the description text
                            .descriptionTextColor(R.color.white)  // Specify the color of the description text
                            .textTypeface(Typeface.SANS_SERIF)  // Specify a typeface for the text
                            .dimColor(R.color.primary_dark_blue)            // If set, will dim behind the view with 30% opacity of the given color
                            .drawShadow(true)                   // Whether to draw a drop shadow or not
                            .cancelable(true)                  // Whether tapping outside the outer circle dismisses the view
                            .tintTarget(true)                   // Whether to tint the target view's color
                            .transparentTarget(true)           // Specify whether the target is transparent (displays the content underneath)
                            .targetRadius(40)
                            .id(1),
                    TapTarget.forView(findViewById(R.id.start_mission_button), "Start mission", "After connecting your drone and planning the survey, tap this to start the mission")
                            .outerCircleColor(R.color.primary_dark_blue)      // Specify a color for the outer circle
                            .outerCircleAlpha(0.96f)            // Specify the alpha amount for the outer circle
                            .targetCircleColor(R.color.white)   // Specify a color for the target circle
                            .titleTextSize(24)                  // Specify the size (in sp) of the title text
                            .titleTextColor(R.color.white)      // Specify the color of the title text
                            .descriptionTextSize(16)            // Specify the size (in sp) of the description text
                            .descriptionTextColor(R.color.white)  // Specify the color of the description text
                            .textTypeface(Typeface.SANS_SERIF)  // Specify a typeface for the text
                            .dimColor(R.color.primary_dark_blue)            // If set, will dim behind the view with 30% opacity of the given color
                            .drawShadow(true)                   // Whether to draw a drop shadow or not
                            .cancelable(true)                  // Whether tapping outside the outer circle dismisses the view
                            .tintTarget(true)                   // Whether to tint the target view's color
                            .transparentTarget(false)           // Specify whether the target is transparent (displays the content underneath)
                            .targetRadius(40)
                            .id(2)
            ).listener(new TapTargetSequence.Listener() {
                @Override
                public void onSequenceFinish() {
                    mMixpanel.track("FPA: TourCompleted");
                    isOnTour = false;
                    final View view = findViewById(R.id.editorCoordinatorLayout);
                    Snackbar snack = Snackbar.make(view, "Thank you for completing the tour!", Snackbar.LENGTH_LONG);
                    View snackBarView = snack.getView();
                    snackBarView.setBackgroundColor(getResources().getColor(R.color.primary_dark_blue));
                    snack.show();
                }

                @Override
                public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {

                }

                @Override
                public void onSequenceCanceled(TapTarget lastTarget) {
                    mMixpanel.track("FPA: TourCancelled");
                    isOnTour = false;
                }
            });
            targetSequence.start();
        } else {
            isOnTour = false;
        }

    }


}