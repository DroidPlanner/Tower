package com.droidplanner.activitys;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.droidplanner.DroidPlannerApp.OnWaypointUpdateListner;
import com.droidplanner.R;
import com.droidplanner.activitys.helpers.SuperActivity;
import com.droidplanner.circle.Circle;
import com.droidplanner.circle.CirclePoint;
import com.droidplanner.dialogs.AltitudeDialog.OnAltitudeChangedListner;
import com.droidplanner.dialogs.OpenFileDialog;
import com.droidplanner.dialogs.OpenMissionDialog;
import com.droidplanner.dialogs.PolygonDialog;
import com.droidplanner.drone.variables.waypoint;
import com.droidplanner.file.IO.MissionReader;
import com.droidplanner.file.IO.MissionWriter;
import com.droidplanner.fragments.MissionFragment;
import com.droidplanner.fragments.PlanningMapFragment;
import com.droidplanner.fragments.PlanningMapFragment.OnMapInteractionListener;
import com.droidplanner.fragments.PlanningMapFragment.modes;
import com.droidplanner.polygon.Polygon;
import com.droidplanner.polygon.PolygonPoint;
import com.google.android.gms.maps.model.LatLng;

public class PlanningActivity extends SuperActivity implements
		OnMapInteractionListener, OnWaypointUpdateListner,
		OnAltitudeChangedListner {

	PlanningMapFragment planningMapFragment;
	private MissionFragment missionFragment;

	public Polygon polygon = new Polygon();;
	public Circle circle = new Circle();

	@Override
	public int getNavigationItem() {
		return 0;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.planning);

		planningMapFragment = ((PlanningMapFragment) getFragmentManager()
				.findFragmentById(R.id.planningMapFragment));
		missionFragment = (MissionFragment) getFragmentManager()
				.findFragmentById(R.id.missionFragment);

		missionFragment.setMission(drone.mission);

		drone.mission.missionListner = this;

		checkIntent();

		update();
	}

	private void checkIntent() {
		Intent intent = getIntent();
		String action = intent.getAction();
		String type = intent.getType();
		if (Intent.ACTION_VIEW.equals(action) && type != null) {
			Toast.makeText(this, intent.getData().getPath(), Toast.LENGTH_LONG)
					.show();
			openMission(intent.getData().getPath());
			update();
			planningMapFragment
					.zoomToExtents(drone.mission.getAllCoordinates());
		}
	}

	private void openMission(String path) {
		MissionReader missionReader = new MissionReader();
		if (missionReader.openMission(path)) {
			drone.mission.setHome(missionReader.getHome());
			drone.mission.setWaypoints(missionReader.getWaypoints());
		}

	}

	private boolean writeMission() {
		MissionWriter missionWriter = new MissionWriter(
				drone.mission.getHome(), drone.mission.getWaypoints());
		return missionWriter.saveWaypoints();
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		switch (planningMapFragment.mode) {
		default:
		case MISSION:
			getMenuInflater().inflate(R.menu.menu_planning, menu);
			break;
		case POLYGON:
			getMenuInflater().inflate(R.menu.menu_planning_polygon, menu);
			break;
		case CIRCLE:
			getMenuInflater().inflate(R.menu.menu_planning_circle, menu);
		}

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_zoom:
			planningMapFragment
					.zoomToExtents(drone.mission.getAllCoordinates());
			return true;
		case R.id.menu_save_file:
			menuSaveFile();
			return true;
		case R.id.menu_open_file:
			openMissionFile();
			return true;
		case R.id.menu_send_to_apm:
			List<waypoint> data = new ArrayList<waypoint>();
			data.add(drone.mission.getHome());
			data.addAll(drone.mission.getWaypoints());
			drone.waypointMananger.writeWaypoints(data);
			return true;
		case R.id.menu_clear_wp:
			clearWaypointsAndUpdate();
			return true;
		case R.id.menu_polygon:
			setMode(modes.POLYGON);
			return true;
		case R.id.menu_circle:
			setMode(modes.CIRCLE);
			return true;
		case R.id.menu_generate_polygon:
			openPolygonGenerateDialog();
			return true;
		case R.id.menu_clear_polygon:
			polygon.clearPolygon();
			update();
			return true;
		case R.id.menu_exit:
			setMode(modes.MISSION);
			update();
			return true;
		case R.id.menu_generate_circle:
			circle.generateCircle(drone.mission);
			update();
			return true;
		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}

	private void openMissionFile() {
		OpenFileDialog missionDialog = new OpenMissionDialog(drone) {
			@Override
			public void waypointFileLoaded(MissionReader reader) {
				drone.mission.setHome(reader.getHome());
				drone.mission.setWaypoints(reader.getWaypoints());
				planningMapFragment.zoomToExtents(drone.mission
						.getAllCoordinates());
				update();
			}
		};
		missionDialog.openDialog(this);
	}

	public void openPolygonGenerateDialog() {
		double defaultHatchAngle = (planningMapFragment.getMapRotation() + 90) % 180;
		PolygonDialog polygonDialog = new PolygonDialog() {
			@Override
			public void onPolygonGenerated(List<waypoint> list) {
				drone.mission.addWaypoints(list);
				update();
			}
		};
		polygonDialog.generatePolygon(defaultHatchAngle, 50.0, polygon,
				drone.mission.getLastWaypoint().getCoord(),
				drone.mission.getDefaultAlt(), this);
	}

	private void clearWaypointsAndUpdate() {
		drone.mission.clearWaypoints();
		update();
	}

	private void setMode(modes mode) {
		planningMapFragment.setMode(mode);
		invalidateOptionsMenu();
	}

	private void menuSaveFile() {
		if (writeMission()) {
			Toast.makeText(this, R.string.file_saved, Toast.LENGTH_SHORT)
					.show();
		} else {
			Toast.makeText(this, R.string.error_when_saving, Toast.LENGTH_SHORT)
					.show();
		}
	}

	private void update() {
		planningMapFragment.update(drone, polygon);
		missionFragment.update();
	}

	@Override
	public void onAddPoint(LatLng point) {
		switch (planningMapFragment.mode) {
		default:
		case MISSION:
			drone.mission.addWaypoint(point);
			break;
		case POLYGON:
			polygon.addWaypoint(point);
			break;
		case CIRCLE:
			circle.moveCircle(planningMapFragment, point);
			return;
		}
		update();
	}

	@Override
	public void onMoveHome(LatLng coord) {
		drone.mission.setHome(coord);
		update();
	}

	@Override
	public void onMoveWaypoint(waypoint source, LatLng latLng) {
		source.setCoord(latLng);
		update();
	}

	@Override
	public void onMovePolygonPoint(PolygonPoint source, LatLng newCoord) {
		source.coord = newCoord;
		update();
	}

	@Override
	public void onWaypointsUpdate() {
		update();
		planningMapFragment.zoomToExtents(drone.mission.getAllCoordinates());
	}

	@Override
	public void onAltitudeChanged(double newAltitude) {
		super.onAltitudeChanged(newAltitude);
		update();
	}

	@Override
	public void onMoveCirclePoint(CirclePoint point, LatLng newCoord) {
		circle.moveCircle(planningMapFragment, newCoord);
	}

}
