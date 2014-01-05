package com.droidplanner.activitys.helpers;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.droidplanner.DroidPlannerApp.ConnectionStateListner;
import com.droidplanner.R;
import com.droidplanner.drone.Drone;
import com.droidplanner.drone.DroneInterfaces.DroneEventsType;
import com.droidplanner.drone.DroneInterfaces.OnDroneListner;
import com.droidplanner.gcs.GCSHeartbeat;


public abstract class SuperUI extends SuperActivity implements ConnectionStateListner, OnDroneListner, OnClickListener {
	/**
	 * Contains a list of help guides names, with the associated youtube video
	 * link
	 */
	public CharSequence[][] help = {
			{ "Spline", "DP v3" },
			{ "https://www.youtube.com/watch?v=v9ydP-NWoJE",
					"https://www.youtube.com/watch?v=miwWUgX6nwY" } };

	private ScreenOrientation screenOrientation = new ScreenOrientation(this);
	private InfoMenu infoMenu;
	private GCSHeartbeat gcsHeartbeat;
	
	public SuperUI() {
		super();        
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		screenOrientation.unlock();
		gcsHeartbeat = new GCSHeartbeat(drone,1);
	}

	@Override
	protected void onStart() {
		super.onStart();
		drone.events.addDroneListener(this);
		app.conectionListner = this;
		drone.MavClient.queryConnectionState();
	}

	@Override
	protected void onStop() {
		super.onStop();
		drone.events.removeDroneListener(this);
		infoMenu = null;
	}
	
	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {	
		if (infoMenu!=null) {
			infoMenu.onDroneEvent(event,drone);			
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		infoMenu = new InfoMenu(drone,this);
		infoMenu.inflateMenu(menu, getMenuInflater());	
		infoMenu.setupModeSpinner(this);
		getMenuInflater().inflate(R.menu.menu_super_activiy, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		infoMenu.forceViewsUpdate();
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		infoMenu.onOptionsItemSelected(item);
		if (item.getItemId() == R.id.menu_help) {
			showHelpDialog();
		}
		return super.onOptionsItemSelected(item);
	}

	private void showHelpDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.help);
		builder.setItems(help[0], this);
		builder.create().show();		
	}

	public void notifyDisconnected() {
		gcsHeartbeat.setActive(false);
		invalidateOptionsMenu();		
		/*
		if(armButton != null){
			armButton.setEnabled(false);
		}*/
		screenOrientation.unlock();
	}

	public void notifyConnected() {
		gcsHeartbeat.setActive(true);
		invalidateOptionsMenu();
		
		/*
		if(armButton != null){
			armButton.setEnabled(true);
		}
		*/
		screenOrientation.requestLock();
	}
	
	@Override
	public void onClick(DialogInterface dialog, int which) {
		Toast.makeText(this, help[1][which], Toast.LENGTH_LONG).show();
		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(help[1][which].toString())));		
	}
}