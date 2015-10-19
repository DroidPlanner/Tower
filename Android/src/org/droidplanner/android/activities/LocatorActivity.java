package org.droidplanner.android.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.MAVLink.common.msg_global_position_int;
import com.o3dr.android.client.utils.data.tlog.TLogPicker;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.data.ServiceDataContract;
import com.o3dr.services.android.lib.util.MathUtils;

import org.beyene.sius.unit.length.LengthUnit;
import org.droidplanner.android.R;
import org.droidplanner.android.fragments.LocatorListFragment;
import org.droidplanner.android.fragments.LocatorMapFragment;
import org.droidplanner.android.utils.file.IO.TLogReader;
import org.droidplanner.android.utils.file.IO.TLogReader.Event;
import org.droidplanner.android.utils.prefs.AutoPanMode;
import org.droidplanner.android.utils.unit.providers.length.LengthUnitProvider;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;

/**
 * This implements the map locator activity. The map locator activity allows the user to find
 * a lost drone using last known GPS positions from the tlogs.
 */
public class LocatorActivity extends DrawerNavigationUI implements LocatorListFragment.OnLocatorListListener,
        LocationListener {

    private static final String STATE_LAST_SELECTED_POSITION = "STATE_LAST_SELECTED_POSITION";
    private static final int TLOG_PICKER_REQUEST_CODE = 101;

    private final static List<TLogReader.Event> lastPositions = new LinkedList<>();

    private OpenTLogFileAsyncTask tlogOpener;

    /*
    View widgets.
     */
    private LocatorMapFragment locatorMapFragment;
    private LocatorListFragment locatorListFragment;
    private LinearLayout statusView;
    private TextView latView, lonView, distanceView, azimuthView, altitudeView;

    private msg_global_position_int selectedMsg;
    private LatLong lastGCSPosition;
    private float lastGCSBearingTo = Float.MAX_VALUE;
    private double lastGCSAzimuth = Double.MAX_VALUE;

    public List<Event> getLastPositions() {
        return lastPositions;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locator);

        FragmentManager fragmentManager = getSupportFragmentManager();

        locatorMapFragment = ((LocatorMapFragment) fragmentManager.findFragmentById(R.id.locator_map_fragment));
        if (locatorMapFragment == null) {
            locatorMapFragment = new LocatorMapFragment();
            fragmentManager.beginTransaction().add(R.id.locator_map_fragment, locatorMapFragment).commit();
        }

        locatorListFragment = (LocatorListFragment) fragmentManager.findFragmentById(R.id.locatorListFragment);

        statusView = (LinearLayout) findViewById(R.id.statusView);
        latView = (TextView) findViewById(R.id.latView);
        lonView = (TextView) findViewById(R.id.lonView);
        distanceView = (TextView) findViewById(R.id.distanceView);
        azimuthView = (TextView) findViewById(R.id.azimuthView);
        altitudeView = (TextView) findViewById(R.id.altitudeView);

        final FloatingActionButton zoomToFit = (FloatingActionButton) findViewById(R.id.zoom_to_fit_button);
        zoomToFit.setVisibility(View.VISIBLE);
        zoomToFit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (locatorMapFragment != null) {
                    locatorMapFragment.zoomToFit();
                }
            }
        });

        FloatingActionButton mGoToMyLocation = (FloatingActionButton) findViewById(R.id.my_location_button);
        mGoToMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locatorMapFragment.goToMyLocation();
            }
        });
        mGoToMyLocation.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                locatorMapFragment.setAutoPanMode(AutoPanMode.USER);
                return true;
            }
        });

        FloatingActionButton mGoToDroneLocation = (FloatingActionButton) findViewById(R.id.drone_location_button);
        mGoToDroneLocation.setVisibility(View.GONE);

        // clear prev state if this is a fresh start
        if (savedInstanceState == null) {
            // fresh start
            lastPositions.clear();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        locatorMapFragment.setLocationReceiver(this);
    }

    @Override
    protected int getToolbarId() {
        return R.id.actionbar_container;
    }

    @Override
    public void onPause() {
        super.onPause();
        locatorMapFragment.setLocationReceiver(null);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        final int lastSelectedPosition = lastPositions.indexOf(selectedMsg);
        outState.putInt(STATE_LAST_SELECTED_POSITION, lastSelectedPosition);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        final int lastSelectedPosition = savedInstanceState.getInt(STATE_LAST_SELECTED_POSITION, -1);
        if (lastSelectedPosition != -1 && lastSelectedPosition < lastPositions.size())
            setSelectedMsg((msg_global_position_int) lastPositions.get(lastSelectedPosition).getMavLinkMessage());
    }

    @Override
    protected int getNavigationDrawerMenuItemId() {
        return R.id.navigation_locator;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu_locator, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_open_tlog_file:
                TLogPicker.startTLogPicker(this, TLOG_PICKER_REQUEST_CODE);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent returnIntent) {
        if (requestCode != TLOG_PICKER_REQUEST_CODE || resultCode != RESULT_OK) {
            super.onActivityResult(requestCode, resultCode, returnIntent);
            return;
        }

        //Get the file's absolute path from the incoming intent
        final String tlogAbsolutePath = returnIntent.getStringExtra(ServiceDataContract.EXTRA_TLOG_ABSOLUTE_PATH);

        if (tlogOpener != null)
            tlogOpener.cancel(true);

        tlogOpener = new OpenTLogFileAsyncTask(this);
        tlogOpener.execute(tlogAbsolutePath);
    }

    /*
    Copy all messages with non-zero coords -> lastPositions
     */
    private void loadLastPositions(List<TLogReader.Event> logEvents) {
        lastPositions.clear();

        for (TLogReader.Event event : logEvents) {
            final msg_global_position_int message = (msg_global_position_int) event.getMavLinkMessage();
            if (message.lat != 0 || message.lon != 0)
                lastPositions.add(event);
        }

        setSelectedMsg(null);
        locatorListFragment.notifyDataSetChanged();

        updateInfo();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        updateMapPadding();
    }

    private void updateMapPadding() {
        int bottomPadding = 0;

        if (lastPositions.size() > 0) {
            bottomPadding = locatorListFragment.getView().getHeight();
        }

        locatorMapFragment.setMapPadding(0, 0, 0, bottomPadding);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        locatorMapFragment.saveCameraPosition();
    }

    @Override
    public void onItemClick(msg_global_position_int msg) {
        setSelectedMsg(msg);

        locatorMapFragment.zoomToFit();
        updateInfo();
    }

    public void setSelectedMsg(msg_global_position_int msg) {
        selectedMsg = msg;

        final LatLong msgCoord;
        if (msg != null)
            msgCoord = coordFromMsgGlobalPositionInt(selectedMsg);
        else
            msgCoord = new LatLong(0, 0);
        locatorMapFragment.updateLastPosition(msgCoord);
    }

    private void updateInfo() {
        if (selectedMsg != null) {
            statusView.setVisibility(View.VISIBLE);

            final LengthUnitProvider lengthUnitProvider = unitSystem.getLengthUnitProvider();

            final double altitude = selectedMsg.alt / 1000; //meters
            LengthUnit convertedAltitude = lengthUnitProvider.boxBaseValueToTarget(altitude);
            altitudeView.setText("Altitude: " + convertedAltitude.toString());

            // coords
            final LatLong msgCoord = coordFromMsgGlobalPositionInt(selectedMsg);

            // distance
            if (lastGCSPosition == null || lastGCSPosition.getLatitude() == 0 || lastGCSPosition.getLongitude() == 0) {
                // unknown
                distanceView.setText(R.string.status_waiting_for_gps, TextView.BufferType.NORMAL);
                azimuthView.setText("");
            } else {
                final double distance = MathUtils.getDistance2D(lastGCSPosition, msgCoord);
                final LengthUnit convertedDistance = lengthUnitProvider.boxBaseValueToTarget(distance);
                String distanceText = getString(R.string.editor_info_window_distance, convertedDistance.toString());
                if (lastGCSBearingTo != Float.MAX_VALUE) {
                    final String bearing = String.format(" @ %.0f°", lastGCSBearingTo);
                    distanceText += bearing;
                }
                distanceView.setText(distanceText);

                if (lastGCSAzimuth != Double.MAX_VALUE) {
                    final String azimuth = getString(R.string.editor_info_window_heading, lastGCSAzimuth);
                    azimuthView.setText(azimuth);
                }
            }

            latView.setText(getString(R.string.waypoint_latitude, msgCoord.getLatitude()));
            lonView.setText(getString(R.string.waypoint_longitude, msgCoord.getLongitude()));
        } else {
            statusView.setVisibility(View.INVISIBLE);
            latView.setText("");
            lonView.setText("");
            distanceView.setText("");
            azimuthView.setText("");
            altitudeView.setText("");
        }
    }

    private static LatLong coordFromMsgGlobalPositionInt(msg_global_position_int msg) {
        double lat = msg.lat;
        lat /= 1E7;

        double lon = msg.lon;
        lon /= 1E7;

        return new LatLong(lat, lon);
    }

    @Override
    public void onLocationChanged(Location location) {
        lastGCSPosition = new LatLong(location.getLatitude(), location.getLongitude());
        lastGCSAzimuth = location.getBearing();

        if (selectedMsg != null) {
            final LatLong msgCoord = coordFromMsgGlobalPositionInt(selectedMsg);

            final Location target = new Location(location);
            target.setLatitude(msgCoord.getLatitude());
            target.setLongitude(msgCoord.getLongitude());

            lastGCSBearingTo = Math.round(location.bearingTo(target));
            lastGCSBearingTo = (lastGCSBearingTo + 360) % 360;
        } else {
            lastGCSBearingTo = Float.MAX_VALUE;
        }

        updateInfo();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    private static class OpenTLogFileAsyncTask extends AsyncTask<String, Void, List<TLogReader.Event>> {

        private final WeakReference<LocatorActivity> activityRef;
        private final ProgressDialog progressDialog;

        public OpenTLogFileAsyncTask(LocatorActivity activity) {
            activityRef = new WeakReference<>(activity);
            progressDialog = new ProgressDialog(activity);
            progressDialog.setTitle("Loading data...");
            progressDialog.setMessage("Please wait.");
            progressDialog.setIndeterminate(true);
        }

        @Override
        protected void onPreExecute() {
            progressDialog.show();
        }

        @Override
        protected List<TLogReader.Event> doInBackground(String... params) {
            final String filename = params[0];

            TLogReader tlogReader = new TLogReader(msg_global_position_int.MAVLINK_MSG_ID_GLOBAL_POSITION_INT);
            tlogReader.openTLog(filename);

            return tlogReader.getLogEvents();
        }

        @Override
        protected void onCancelled() {
            progressDialog.dismiss();
        }

        @Override
        protected void onPostExecute(List<TLogReader.Event> events) {
            progressDialog.dismiss();
            final LocatorActivity activity = activityRef.get();
            if (activity == null)
                return;

            activity.loadLastPositions(events);
            activity.locatorMapFragment.zoomToFit();
        }
    }

}