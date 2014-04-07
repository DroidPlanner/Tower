package com.droidplanner.activitys;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View.OnLongClickListener; // Allows custom GUI buttons
import android.view.View.OnClickListener;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.MAVLink.Messages.ApmModes;
import com.droidplanner.DroidPlannerApp.OnWaypointUpdateListner;
import com.droidplanner.MAVLink.MavLinkArm;
import com.droidplanner.R;
import com.droidplanner.activitys.helpers.SuperFlightActivity;
import com.droidplanner.drone.DroneInterfaces.ModeChangedListener;
import com.droidplanner.fragments.FlightMapFragment;
import com.droidplanner.fragments.helpers.GuidePointListener;
import com.droidplanner.helpers.RcOutput;
import com.droidplanner.helpers.geoTools.GeoTools;
import com.droidplanner.helpers.units.Length;
import com.google.android.gms.maps.model.LatLng;

public class FlightDataActivity extends SuperFlightActivity implements
		OnWaypointUpdateListner, ModeChangedListener, GuidePointListener, OnLongClickListener {
	private TextView distanceView;

	@Override
	public int getNavigationItem() {
		return 1;
	}
	
	Button launchButton, armDisarmButton, landButton,stabilizeButton ,RTLButton;
	RcOutput rcOutput;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_flightdata);

		mapFragment = ((FlightMapFragment) getFragmentManager()
				.findFragmentById(R.id.flightMapFragment));

		distanceView = (TextView) findViewById(R.id.textViewDistance);
		mapFragment.setGuidePointListener(this);
		mapFragment.updateFragment();

		drone.mission.missionListner = this;
		drone.setDroneTypeChangedListner(this);
		drone.setModeChangedListener(this);
		
		
		rcOutput = new RcOutput(drone, this);
		launchButton = (Button)findViewById(R.id.Launch);
		armDisarmButton = (Button)findViewById(R.id.ArmDisarm);
		landButton = (Button)findViewById(R.id.Land);
		stabilizeButton = (Button)findViewById(R.id.Stabilize);
		RTLButton = (Button)findViewById(R.id.RTL);
		
		if(launchButton!=null){
			launchButton.setOnLongClickListener(this); // All need to be long press??
			armDisarmButton.setOnLongClickListener(this);
			landButton.setOnLongClickListener(this);
			stabilizeButton.setOnLongClickListener(this);
			RTLButton.setOnLongClickListener(this);
		}
		
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_flightdata, menu);
		getMenuInflater().inflate(R.menu.menu_map_type, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_zoom:
			mapFragment.zoomToLastKnowPosition();
			return true;
		case R.id.menu_clearFlightPath:
			mapFragment.clearFlightPath();
			return true;
		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}

	@Override
	public void onAltitudeChanged(double newAltitude,boolean applyToAll) {
		super.onAltitudeChanged(newAltitude,applyToAll);
		mapFragment.updateFragment();
	}

	@Override
	public void onModeChanged() {
		mapFragment.onModeChanged();
		checkDistanceVisible();
	}

	@Override
	public void OnGuidePointMoved() {
		updateDistanceView();
	}

	private void updateDistanceView() {
		final Location myLoc = mapFragment.mMap.getMyLocation();
		if(myLoc != null) {
			Length distance = new Length(GeoTools.getDistance(drone.guidedPoint.getCoord(),
			                                                  new LatLng(myLoc.getLatitude(), myLoc.getLongitude())));
			distanceView.setText(getString(R.string.length) + ": " + distance);
		}
		else {
			new Handler().postDelayed(new Runnable()
			{
				@Override
				public void run()
				{
					updateDistanceView();
				}
			}, 2000);
		}
		checkDistanceVisible();
	}

	private void checkDistanceVisible() {
		distanceView.setVisibility(drone.guidedPoint.isCoordValid() && distanceView.getText().length() > 0 ? View.VISIBLE : View.INVISIBLE);
	}

	/**
	 * 
	 * @param view
	 * @return True if we have handled the long click, false otherwise
	 */
	@Override
	public boolean onLongClick(View view) {
	
		if(view == launchButton){

			// Check if we have a MAVLink connection to the drone
			if (drone.MavClient.isConnected()) {

				// Check if the drone is armed
				if (drone.state.isArmed())
				{
					
					// Announce that we are launching using text to speech 
					drone.tts.speak("Launching");
					
					// Change the drone state to auto mode
					drone.state.changeFlightMode(ApmModes.ROTOR_AUTO);
					
					// Sent RC input to override safety in APM
					super.sendRC();
					
				}
				else
				{
					// Announce the failure that the drone is not armed
					drone.tts.speak("Drone is not armed");
				}
			}
			else
			{
				// Announce the failure that the telemetry link is not connected
				drone.tts.speak("No telemetry link");
			}
			
			// We have handled the long press so return true
			return true;
		}
		
		if(view == landButton)
		{
			// Change the drone state to land mode
			drone.state.changeFlightMode(ApmModes.ROTOR_LAND);
			
			// We have handled the long press so return true
			return true;
		}
		
		if(view==armDisarmButton){
			
			// Check if we have a MAVLink connection to the drone
			if (drone.MavClient.isConnected())
			{
				
				if (drone.waypointsSynced == true)
				{
					
					// Check if the drone is disarmed
					// Not a sanity check, this is program logic
					if (!drone.state.isArmed())
					{
						
						// Announce that we are arming the vehicle using text to speech
						drone.tts.speak("Arming the vehicle, please standby");
					}
					else
					{
						
						// Announce that we are disarming the vehicle using text to speech
						drone.tts.speak("Disarming the vehicle, please standby");						
					}
					
					// Send the ARM message over MAVLink, this is always executed as we toggle the state with the same button
					MavLinkArm.sendArmMessage(drone, !drone.state.isArmed()); // Since this is inside we can not use this to disarm if only observing 
					
				}
				else
				{
					drone.tts.speak("Waypoints not saved to quad");
				}

			}
			else
			{
				
				// No MAVLink connection to quad, give error
				drone.tts.speak("No telemetry link");
			}
			
			// We have handled the long press so return true
			return true;
		}
		if(view==stabilizeButton){
			
			// Change the drone state to stabilize mode
			drone.state.changeFlightMode(ApmModes.ROTOR_STABILIZE);
			
			// We have handled the long press so return true
			return true;
		}
		if(view==RTLButton){
			
			// Change the drone state to RTL mode
			drone.state.changeFlightMode(ApmModes.ROTOR_RTL);
			
			// We have handled the long press so return true
			return true;
		}
		
		//We have not handled the long press so let someone above us handle it by returning false
		return false;
	}
	
	@Override
	public void notifyArmed()
	{
		// Call the superclass method
		super.notifyArmed();
		
		if (armDisarmButton != null)
		{
			// Armed
			armDisarmButton.setText("Disarm");
			armDisarmButton.setBackgroundColor(0xff0000ff); // Should be standard green
		}
	}
	
	@Override
	public void notifyDisarmed()
	{
		// Call the superclass method
		super.notifyDisarmed();
		
		if (armDisarmButton != null)
		{
			// Disarmed
			armDisarmButton.setText("Arm");
			armDisarmButton.setBackgroundColor(0xffffa500); // Should be orange
		}
		
	}

}
