package com.droidplanner.activitys;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.droidplanner.DroidPlannerApp.OnWaypointUpdateListner;
import com.droidplanner.R;
import com.droidplanner.R.string;
import com.droidplanner.activitys.helpers.SuperActivity;
import com.droidplanner.dialogs.AltitudeDialog.OnAltitudeChangedListner;
import com.droidplanner.dialogs.GridDialog;
import com.droidplanner.dialogs.OpenFileDialog;
import com.droidplanner.dialogs.OpenMissionDialog;
import com.droidplanner.dialogs.SurveyDialog;
import com.droidplanner.drone.variables.waypoint;
import com.droidplanner.file.IO.MissionReader;
import com.droidplanner.file.IO.MissionWriter;
import com.droidplanner.fragments.MissionFragment;
import com.droidplanner.fragments.PlanningMapFragment;
import com.droidplanner.fragments.PlanningMapFragment.OnMapInteractionListener;
import com.droidplanner.fragments.helpers.GestureMapFragment;
import com.droidplanner.fragments.helpers.GestureMapFragment.OnPathFinishedListner;
import com.droidplanner.fragments.helpers.MapProjection;
import com.droidplanner.polygon.Polygon;
import com.droidplanner.polygon.PolygonPoint;
import com.google.android.gms.maps.model.LatLng;

public class PlanningActivity extends SuperActivity implements
		OnMapInteractionListener, OnWaypointUpdateListner,
		OnAltitudeChangedListner, OnPathFinishedListner {
	public enum modes {
		MISSION, POLYGON;
	}

	public Polygon polygon;
	private PlanningMapFragment planningMapFragment;
	private MissionFragment missionFragment;
	private GestureMapFragment gestureMapFragment;
	public modes mode = modes.MISSION;

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
		gestureMapFragment = ((GestureMapFragment) getFragmentManager()
				.findFragmentById(R.id.gestureMapFragment));
		missionFragment = (MissionFragment) getFragmentManager()
				.findFragmentById(R.id.missionFragment);

		polygon = new Polygon();

		gestureMapFragment.setOnPathFinishedListner(this);
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

	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		switch (mode) {
		default:
		case MISSION:
			getMenuInflater().inflate(R.menu.menu_planning, menu);
			break;
		case POLYGON:
			getMenuInflater().inflate(R.menu.menu_planning_polygon, menu);
			break;
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
			drone.mission.sendMissionToAPM();
			return true;
		case R.id.menu_clear_wp:
			clearWaypointsAndUpdate();
			return true;
		case R.id.menu_polygon:
			setMode(modes.POLYGON);
			return true;
		case R.id.menu_generate_polygon:
			openPolygonGenerateDialog();
			return true;
		case R.id.menu_survey:
			openSurveyDialog();
			return true;
		case R.id.menu_clear_polygon:
			polygon.clearPolygon();
			update();
			return true;
		case R.id.mode_exit:
			setMode(modes.MISSION);
			update();
			return true;
		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}

	private void processReceivedPoints(List<LatLng> points) {
		switch (mode) {
		case MISSION:
			drone.mission.addWaypointsWithDefaultAltitude(points);			
			break;
		case POLYGON:
			polygon.addPoints(points);
			break;
		default:
			break;
		}
		update();
	}

	public void openPolygonGenerateDialog() {
		double defaultHatchAngle = (planningMapFragment.getMapRotation() + 90) % 180;
		GridDialog polygonDialog = new GridDialog() {
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
	public void setMode(modes mode) {
		this.mode = mode;
		switch (mode) {
		default:
		case MISSION:
			Toast.makeText(this, string.exiting_polygon_mode,
					Toast.LENGTH_SHORT).show();
			break;
		case POLYGON:
			Toast.makeText(this, string.entering_polygon_mode,
					Toast.LENGTH_SHORT).show();
			break;
		}
		invalidateOptionsMenu();
	}

	private void openSurveyDialog() {
		double defaultHatchAngle = ((planningMapFragment.getMapRotation() + 90) % 180);
		SurveyDialog dialog = new SurveyDialog() {
			@Override
			public void onPolygonGenerated(List<waypoint> list) {
				drone.mission.addWaypoints(list);
				update();
			}
		};
		dialog.generateSurveyDialog(polygon, defaultHatchAngle,
				drone.mission.getLastWaypoint().getCoord(), drone.mission.getDefaultAlt(),
				this);
	}

	private void clearWaypointsAndUpdate() {
		drone.mission.clearWaypoints();
		update();
	}

	private void update() {
		planningMapFragment.update(drone, polygon);
		missionFragment.update();
	}

	@Override
	public void onMapClick(LatLng point) {
		Toast.makeText(this, "Draw your path", Toast.LENGTH_SHORT).show();
		gestureMapFragment.enableGestureDetection();
	}

	@Override
	public void onAddPoint(LatLng point) {
		List<LatLng> points = new ArrayList<LatLng>();
		points.add(point);
		processReceivedPoints(points);
	}

	@Override
	public void onAltitudeChanged(double newAltitude) {
		super.onAltitudeChanged(newAltitude);
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
	public void onPathFinished(List<Point> path) {
		List<LatLng> points = MapProjection.projectPathIntoMap(path,
				planningMapFragment.mMap);
		processReceivedPoints(points);
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

	private void menuSaveFile() {
		if (writeMission()) {
			Toast.makeText(this, R.string.file_saved, Toast.LENGTH_SHORT)
					.show();
		} else {
			Toast.makeText(this, R.string.error_when_saving, Toast.LENGTH_SHORT)
					.show();
		}
	}

}
