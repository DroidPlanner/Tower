package com.droidplanner.activitys;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.droidplanner.R;
import com.droidplanner.activitys.helpers.SuperUI;
import com.droidplanner.drone.DroneInterfaces.OnStateListner;
import com.droidplanner.drone.variables.mission.MissionItem;
import com.droidplanner.drone.variables.mission.waypoints.SpatialCoordItem;
import com.droidplanner.fragments.MissionControlFragment.OnMissionControlInteraction;
import com.droidplanner.fragments.RCFragment;
import com.droidplanner.fragments.helpers.OnMapInteractionListener;
import com.droidplanner.glass.activities.GlassActivity;
import com.droidplanner.glass.utils.GlassUtils;
import com.droidplanner.polygon.PolygonPoint;
import com.droidplanner.widgets.adapterViews.NavigationHubAdapter;
import com.google.android.gms.maps.model.LatLng;

public class FlightActivity extends SuperUI implements
		OnMapInteractionListener, OnMissionControlInteraction, OnStateListner {

    /**
     * Activity logo.
     * Used by the navigation drawer.
     * @since 1.2.0
     */
    public static final int LOGO_RESOURCE = R.drawable.ic_action_plane;

    /**
     * Activity title.
     * Used to update the action bar when the navigation drawer opens/closes.
     * @since 1.2.0
     */
    public static final int LABEL_RESOURCE = R.string.screen_flight_data;

	private FragmentManager fragmentManager;
	private RCFragment rcFragment;
	private View failsafeTextView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!GlassUtils.isGlassDevice()) {
            setContentView(R.layout.activity_flight);
            fragmentManager = getFragmentManager();

            failsafeTextView = findViewById(R.id.failsafeTextView);
            drone.state.addFlightStateListner(this);

            //Setup the navigation drawer
            setupNavDrawer();
        }
        else {
            //Start the glass activity
            startActivity(new Intent(this, GlassActivity.class));
            finish();
        }
    }

	@Override
	protected void onDestroy() {
		super.onDestroy();
		drone.state.removeFlightStateListner(this);
	}

	@Override
	public void onAddPoint(LatLng point) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMoveHome(LatLng coord) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMoveWaypoint(SpatialCoordItem waypoint, LatLng latLng) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMovePolygonPoint(PolygonPoint source, LatLng newCoord) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMapClick(LatLng point) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onMarkerClick(MissionItem wp) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onJoystickSelected() {
		toggleRCFragment();
	}

    @Override
    protected NavigationHubAdapter.HubItem getNavigationHubItem(){
        return NavigationHubAdapter.HubItem.FLIGHT_DATA;
    }

	private void toggleRCFragment() {
		if (rcFragment == null) {
			rcFragment = new RCFragment();
			fragmentManager.beginTransaction()
					.add(R.id.containerRC, rcFragment).commit();
		} else {
			fragmentManager.beginTransaction().remove(rcFragment).commit();
			rcFragment = null;
		}
	}

	@Override
	public void onMovingWaypoint(SpatialCoordItem source, LatLng latLng) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFlightStateChanged() {

	}

	@Override
	public void onArmChanged() {

	}

	@Override
	public void onFailsafeChanged() {
		if (drone.state.isFailsafe()) {
			failsafeTextView.setVisibility(View.VISIBLE);
		} else {
			failsafeTextView.setVisibility(View.GONE);
		}
	}

    @Override
    protected int getLabelResource(){
        return LABEL_RESOURCE;
    }

}
