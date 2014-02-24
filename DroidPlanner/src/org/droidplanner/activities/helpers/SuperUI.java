package org.droidplanner.activities.helpers;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import org.droidplanner.R;
import org.droidplanner.activities.interfaces.HelpProvider;
import org.droidplanner.drone.Drone;
import org.droidplanner.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.gcs.GCSHeartbeat;
import org.droidplanner.utils.Utils;
import org.droidplanner.widgets.actionProviders.InfoBarActionProvider;

public abstract class SuperUI extends SuperActivity implements OnDroneListener {
	private ScreenOrientation screenOrientation = new ScreenOrientation(this);
	private InfoBarActionProvider infoBar;
	private GCSHeartbeat gcsHeartbeat;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		screenOrientation.unlock();
        Utils.updateUILanguage(getApplicationContext());
		gcsHeartbeat = new GCSHeartbeat(drone,1);
	}

	@Override
	protected void onStart() {
		super.onStart();
		drone.events.addDroneListener(this);
		drone.MavClient.queryConnectionState();
		drone.events.notifyDroneEvent(DroneEventsType.MISSION_UPDATE);
	}

	@Override
	protected void onStop() {
		super.onStop();
		drone.events.removeDroneListener(this);

        if (infoBar != null) {
            infoBar.setDrone(null);
            infoBar = null;
        }
    }

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		if (infoBar != null) {
			infoBar.onDroneEvent(event, drone);
		}

		switch (event) {
		case CONNECTED:
			gcsHeartbeat.setActive(true);
			invalidateOptionsMenu();
			screenOrientation.requestLock();
			break;
		case DISCONNECTED:
			gcsHeartbeat.setActive(false);
			invalidateOptionsMenu();
			screenOrientation.unlock();
			break;
		default:
			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        //Reset the previous info bar
        if(infoBar != null){
            infoBar.setDrone(null);
            infoBar = null;
        }

		getMenuInflater().inflate(R.menu.menu_super_activiy, menu);

        final MenuItem toggleConnectionItem = menu.findItem(R.id.menu_connect);
        final MenuItem infoBarItem = menu.findItem(R.id.menu_info_bar);
        if(infoBarItem != null)
            infoBar = (InfoBarActionProvider) infoBarItem.getActionProvider();

        //Configure the info bar action provider if we're connected
        if(drone.MavClient.isConnected()){
            menu.setGroupEnabled(R.id.menu_group_connected, true);
            menu.setGroupVisible(R.id.menu_group_connected, true);

            toggleConnectionItem.setTitle(R.string.menu_disconnect);
            toggleConnectionItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

            if(infoBar != null){
                infoBar.setDrone(drone);
            }
        }
        else{
            menu.setGroupEnabled(R.id.menu_group_connected, false);
            menu.setGroupVisible(R.id.menu_group_connected, false);

            toggleConnectionItem.setTitle(R.string.menu_connect);
            toggleConnectionItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS|MenuItem
                    .SHOW_AS_ACTION_WITH_TEXT);

            if(infoBar != null){
                infoBar.setDrone(null);
            }
        }
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
            case R.id.menu_send_mission:
                drone.mission.sendMissionToAPM();
                return true;

            case R.id.menu_load_mission:
                drone.waypointMananger.getWaypoints();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
	}
}