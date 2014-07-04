package org.droidplanner.android.activities;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.MAVLink.Messages.ardupilotmega.msg_global_position_int;
import com.google.common.collect.Lists;

import org.droidplanner.R;
import org.droidplanner.android.activities.helpers.SuperUI;
import org.droidplanner.android.activities.interfaces.OnLocatorListListener;
import org.droidplanner.android.dialogs.openfile.OpenFileDialog;
import org.droidplanner.android.dialogs.openfile.OpenTLogDialog;
import org.droidplanner.android.fragments.LocatorListFragment;
import org.droidplanner.android.fragments.LocatorMapFragment;
import org.droidplanner.android.utils.file.IO.TLogReader;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.geoTools.GeoTools;

import java.util.ArrayList;
import java.util.List;

/**
 * This implements the map locator activity. The map locator activity allows the user to find
 * a lost drone using last known GPS positions from the tlogs.
 */
public class LocatorActivity extends SuperUI implements OnLocatorListListener, LocationListener, SensorEventListener {

    private final List<msg_global_position_int> lastPositions = new ArrayList<msg_global_position_int>();

    /*
    View widgets.
     */
    private LocationManager locationManager;
    private SensorManager sensorManager;
    private FragmentManager fragmentManager;

    private LocatorMapFragment locatorMapFragment;
	private LocatorListFragment locatorListFragment;
	private TextView infoView;

    private msg_global_position_int selectedMsg;
    private Coord2D lastGCSPosition;
    private float lastGCSBearingTo = Float.MAX_VALUE;
    private double lastGCSAzimuth = Double.MAX_VALUE;

    private float[] valuesAccelerometer;
    private float[] valuesMagneticField;

    private float[] matrixR;
    private float[] matrixI;
    private float[] matrixValues;


    public List<msg_global_position_int> getLastPositions() {
        return lastPositions;
    }

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_locator);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        fragmentManager = getSupportFragmentManager();

		locatorMapFragment = ((LocatorMapFragment) fragmentManager
				.findFragmentById(R.id.mapFragment));
		locatorListFragment = (LocatorListFragment) fragmentManager
				.findFragmentById(R.id.locatorListFragment);

		infoView = (TextView) findViewById(R.id.locatorInfoWindow);

        valuesAccelerometer = new float[3];
        valuesMagneticField = new float[3];

        matrixR = new float[9];
        matrixI = new float[9];
        matrixValues = new float[3];
	}

    @Override
    public void onResume(){
        super.onResume();

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this, null);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();

        locationManager.removeUpdates(this);
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onStart(){
        super.onStart();
    }

    @Override
    public void onStop(){
        super.onStop();
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
            case R.id.menu_zoom_fit_locator:
                locatorMapFragment.zoomToFit();
                return true;

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
        final ArrayList<msg_global_position_int> positions = new ArrayList<msg_global_position_int>();
        for (TLogReader.Event event : logEvents) {
            final msg_global_position_int message = (msg_global_position_int) event.getMavLinkMessage();
            if(message.lat != 0 || message.lon != 0)
                positions.add(message);
        }

        setSelectedMsg(null);
        lastPositions.clear();
        lastPositions.addAll(Lists.reverse(positions));
        locatorListFragment.notifyDataSetChanged();

        updateInfo();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		updateMapPadding();
	}

	private void updateMapPadding() {
		final int topPadding = infoView.getBottom();
        final int bottomPadding = locatorListFragment.getView().getHeight();

		locatorMapFragment.setMapPadding(0, topPadding, 0, bottomPadding);
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

        final Coord2D msgCoord;
        if(msg != null)
            msgCoord = coordFromMsgGlobalPositionInt(selectedMsg);
        else
            msgCoord = new Coord2D(0, 0);
        locatorMapFragment.updateLastPosition(msgCoord);
    }

    private void updateInfo() {
        if(selectedMsg != null) {
            // coords
            final Coord2D msgCoord = coordFromMsgGlobalPositionInt(selectedMsg);

            // distance
            String distance;
            if(lastGCSPosition == null || lastGCSPosition.isEmpty()) {
                // unknown
                distance = "";
            } else {
                distance = String.format("Distance: %.01fm", GeoTools.getDistance(lastGCSPosition, msgCoord).valueInMeters());

                if(lastGCSBearingTo != Float.MAX_VALUE) {
                    final String bearing = String.format(" @ %.0fdeg", lastGCSBearingTo);
                    distance += bearing;
                }
                if(lastGCSAzimuth != Double.MAX_VALUE) {
                    final String azimuth = String.format("  Az: %.0fdeg", lastGCSAzimuth);
                    distance += azimuth;
                }
                distance += "    ";
            }

            infoView.setText(String.format("%sLat: %f    Lon: %f", distance, msgCoord.getLat(), msgCoord.getLng()));
        } else {
            infoView.setText("");
        }
    }

    private static Coord2D coordFromMsgGlobalPositionInt(msg_global_position_int msg) {
        double lat = msg.lat;
        lat /= 1E7;

        double lon = msg.lon;
        lon /= 1E7;

        return new Coord2D(lat, lon);
    }

    @Override
    public void onLocationChanged(Location location) {
        lastGCSPosition = new Coord2D(location.getLatitude(), location.getLongitude());

        if(selectedMsg != null) {
            final Coord2D msgCoord = coordFromMsgGlobalPositionInt(selectedMsg);

            final Location target = new Location(location);
            target.setLatitude(msgCoord.getLat());
            target.setLongitude(msgCoord.getLng());

            lastGCSBearingTo = Math.round(location.bearingTo(target));
            lastGCSBearingTo = (lastGCSBearingTo + 360) % 360;
        } else {
            lastGCSBearingTo = Float.MAX_VALUE;
        }

        updateInfo();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // NOP
    }

    @Override
    public void onProviderEnabled(String provider) {
        // NOP
    }

    @Override
    public void onProviderDisabled(String provider) {
        // NOP
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        switch(event.sensor.getType()){
            case Sensor.TYPE_ACCELEROMETER:
                for(int i =0; i < 3; i++){
                    valuesAccelerometer[i] = event.values[i];
                }
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                for(int i =0; i < 3; i++){
                    valuesMagneticField[i] = event.values[i];
                }
                break;
        }

        boolean success = SensorManager.getRotationMatrix(
                matrixR,
                matrixI,
                valuesAccelerometer,
                valuesMagneticField);
        if(success) {
            SensorManager.getOrientation(matrixR, matrixValues);
            lastGCSAzimuth = Math.round(Math.toDegrees(matrixValues[0]));
            lastGCSAzimuth = (lastGCSAzimuth + 360) % 360;
        } else {
            lastGCSAzimuth = Double.MAX_VALUE;
        }

        updateInfo();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
