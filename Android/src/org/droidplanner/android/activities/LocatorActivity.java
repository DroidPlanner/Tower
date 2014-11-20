package org.droidplanner.android.activities;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.MAVLink.common.msg_global_position_int;
import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.util.MathUtils;

import org.droidplanner.android.R;
import org.droidplanner.android.dialogs.openfile.OpenFileDialog;
import org.droidplanner.android.dialogs.openfile.OpenTLogDialog;
import org.droidplanner.android.fragments.LocatorListFragment;
import org.droidplanner.android.fragments.LocatorMapFragment;
import org.droidplanner.android.utils.file.IO.TLogReader;
import org.droidplanner.android.utils.prefs.AutoPanMode;

import java.util.LinkedList;
import java.util.List;

/**
 * This implements the map locator activity. The map locator activity allows the user to find
 * a lost drone using last known GPS positions from the tlogs.
 */
public class LocatorActivity extends DrawerNavigationUI implements LocatorListFragment
        .OnLocatorListListener, LocationListener {

    private static final String STATE_LAST_SELECTED_POSITION = "STATE_LAST_SELECTED_POSITION";

    private final static List<msg_global_position_int> lastPositions = new
            LinkedList<msg_global_position_int>();

    /*
    View widgets.
     */
    private LocatorMapFragment locatorMapFragment;
    private LocatorListFragment locatorListFragment;
    private LinearLayout statusView;
    private TextView latView, lonView, distanceView, azimuthView;

    private msg_global_position_int selectedMsg;
    private LatLong lastGCSPosition;
    private float lastGCSBearingTo = Float.MAX_VALUE;
    private double lastGCSAzimuth = Double.MAX_VALUE;


    public List<msg_global_position_int> getLastPositions() {
        return lastPositions;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locator);

        FragmentManager fragmentManager = getSupportFragmentManager();

        locatorMapFragment = ((LocatorMapFragment) fragmentManager
                .findFragmentById(R.id.mapFragment));
        locatorListFragment = (LocatorListFragment) fragmentManager
                .findFragmentById(R.id.locatorListFragment);

        statusView = (LinearLayout) findViewById(R.id.statusView);
        latView = (TextView) findViewById(R.id.latView);
        lonView = (TextView) findViewById(R.id.lonView);
        distanceView = (TextView) findViewById(R.id.distanceView);
        azimuthView = (TextView) findViewById(R.id.azimuthView);

        final ImageButton resetMapBearing = (ImageButton) findViewById(R.id.map_orientation_button);
        resetMapBearing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(locatorMapFragment != null) {
                    locatorMapFragment.updateMapBearing(0);
                }
            }
        });

        final ImageButton zoomToFit = (ImageButton) findViewById(R.id.zoom_to_fit_button);
        zoomToFit.setVisibility(View.VISIBLE);
        zoomToFit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(locatorMapFragment != null){
                    locatorMapFragment.zoomToFit();
                }
            }
        });

        ImageButton mGoToMyLocation = (ImageButton) findViewById(R.id.my_location_button);
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

        ImageButton mGoToDroneLocation = (ImageButton) findViewById(R.id.drone_location_button);
        mGoToDroneLocation.setVisibility(View.GONE);

        // clear prev state if this is a fresh start
        if(savedInstanceState == null) {
            // fresh start
            lastPositions.clear();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        locatorMapFragment.setLocationReceiver(this);
    }

    @Override
    public void onPause(){
        super.onPause();
        locatorMapFragment.setLocationReceiver(null);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        final int lastSelectedPosition = lastPositions.indexOf(selectedMsg);
        outState.putInt(STATE_LAST_SELECTED_POSITION, lastSelectedPosition);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        final int lastSelectedPosition = savedInstanceState.getInt(STATE_LAST_SELECTED_POSITION, -1);
        if(lastSelectedPosition != -1 && lastSelectedPosition < lastPositions.size())
            setSelectedMsg(lastPositions.get(lastSelectedPosition));
    }

    @Override
    protected int getNavigationDrawerEntryId() {
        return R.id.navigation_locator;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu_locator, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_open_tlog_file:
                openLogFile();
                return true;

            default:
                return super.onMenuItemSelected(featureId, item);
        }
    }

    private void openLogFile() {
        OpenFileDialog tlogDialog = new OpenTLogDialog() {
            @Override
            public void tlogFileLoaded(TLogReader reader) {
                loadLastPositions(reader.getLogEvents());
                locatorMapFragment.zoomToFit();
            }
        };
        tlogDialog.openDialog(this);
    }

    /*
    Copy all messages with non-zero coords -> lastPositions and reverse the list (most recent first)
     */
    private void loadLastPositions(List<TLogReader.Event> logEvents) {
        lastPositions.clear();

        for (TLogReader.Event event : logEvents) {
            final msg_global_position_int message = (msg_global_position_int) event.getMavLinkMessage();
            if(message.lat != 0 || message.lon != 0)
                lastPositions.add(0, message);
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

        if(lastPositions.size() > 0) {
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
        if(msg != null)
            msgCoord = coordFromMsgGlobalPositionInt(selectedMsg);
        else
            msgCoord = new LatLong(0, 0);
        locatorMapFragment.updateLastPosition(msgCoord);
    }

    private void updateInfo() {
        if(selectedMsg != null) {
            statusView.setVisibility(View.VISIBLE);

            // coords
            final LatLong msgCoord = coordFromMsgGlobalPositionInt(selectedMsg);

            // distance
            if(lastGCSPosition == null || lastGCSPosition.getLatitude() == 0 || lastGCSPosition
                    .getLongitude() == 0) {
                // unknown
                distanceView.setText(R.string.status_waiting_for_gps, TextView.BufferType.NORMAL);
                azimuthView.setText("");
            } else {
                String distance = String.format("Distance: %.01fm",
                        MathUtils.getDistance(lastGCSPosition, msgCoord));
                if(lastGCSBearingTo != Float.MAX_VALUE) {
                    final String bearing = String.format(" @ %.0f°", lastGCSBearingTo);
                    distance += bearing;
                }
                distanceView.setText(distance);

                if(lastGCSAzimuth != Double.MAX_VALUE) {
                    final String azimuth = String.format("Heading: %.0f°", lastGCSAzimuth);
                    azimuthView.setText(azimuth);
                }
            }

            latView.setText(String.format("Latitude: %f°", msgCoord.getLatitude()));
            lonView.setText(String.format("Longitude: %f°", msgCoord.getLongitude()));
        } else {
            statusView.setVisibility(View.INVISIBLE);
            latView.setText("");
            lonView.setText("");
            distanceView.setText("");
            azimuthView.setText("");
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

        if(selectedMsg != null) {
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
    public void onStatusChanged(String provider, int status, Bundle extras) {   }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

}