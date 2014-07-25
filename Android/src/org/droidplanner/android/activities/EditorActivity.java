package org.droidplanner.android.activities;

import java.util.List;

import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.activities.helpers.SuperUI;
import org.droidplanner.android.activities.interfaces.OnEditorInteraction;
import org.droidplanner.android.dialogs.YesNoDialog;
import org.droidplanner.android.dialogs.openfile.OpenFileDialog;
import org.droidplanner.android.dialogs.openfile.OpenMissionDialog;
import org.droidplanner.android.fragments.EditorListFragment;
import org.droidplanner.android.fragments.EditorMapFragment;
import org.droidplanner.android.fragments.EditorToolsFragment;
import org.droidplanner.android.fragments.EditorToolsFragment.EditorTools;
import org.droidplanner.android.fragments.EditorToolsFragment.OnEditorToolSelected;
import org.droidplanner.android.fragments.helpers.GestureMapFragment;
import org.droidplanner.android.fragments.helpers.GestureMapFragment.OnPathFinishedListener;
import org.droidplanner.android.proxy.mission.MissionProxy;
import org.droidplanner.android.proxy.mission.MissionSelection;
import org.droidplanner.android.proxy.mission.item.MissionItemProxy;
import org.droidplanner.android.proxy.mission.item.fragments.MissionDetailFragment;
import org.droidplanner.android.utils.file.IO.MissionReader;
import org.droidplanner.android.utils.file.IO.MissionWriter;
import org.droidplanner.android.utils.prefs.AutoPanMode;
import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.coordinates.Coord3D;
import org.droidplanner.core.helpers.geoTools.GeoTools;
import org.droidplanner.core.helpers.units.Length;
import org.droidplanner.core.mission.Mission;
import org.droidplanner.core.mission.MissionItem;
import org.droidplanner.core.mission.survey.Survey;
import org.droidplanner.core.mission.survey.grid.Grid;
import org.droidplanner.core.mission.waypoints.Circle;
import org.droidplanner.core.mission.waypoints.SpatialCoordItem;
import org.droidplanner.core.parameters.Parameter;

import android.os.Bundle;
import android.preference.Preference;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This implements the map editor activity. The map editor activity allows the
 * user to create and/or modify autonomous missions for the drone.
 */
public class EditorActivity extends SuperUI implements OnPathFinishedListener,
		OnEditorToolSelected, MissionDetailFragment.OnMissionDetailListener,
		OnEditorInteraction, Callback,
		MissionSelection.OnSelectionUpdateListener {

	/**
	 * Used to retrieve the item detail window when the activity is destroyed,
	 * and recreated.
	 */
	private static final String ITEM_DETAIL_TAG = "Item Detail Window";

	/**
	 * Used to provide access and interact with the
	 * {@link org.droidplanner.core.mission.Mission} object on the Android
	 * layer.
	 */
	private MissionProxy missionProxy;

	/*
	 * View widgets.
	 */
	private EditorMapFragment planningMapFragment;
	private GestureMapFragment gestureMapFragment;
	private EditorToolsFragment editorToolsFragment;
	private MissionDetailFragment itemDetailFragment;
	private FragmentManager fragmentManager;
	private EditorListFragment missionListFragment;

	private View mSplineToggleContainer;
	private boolean mIsSplineEnabled;

	private View mLocationButtonsContainer;
	private TextView editorInfoView;

	/**
	 * This view hosts the mission item detail fragment. On phone, or device
	 * with limited screen estate, it's removed from the layout, and the item
	 * detail ends up displayed as a dialog.
	 */
	private View mContainerItemDetail;

	private ActionMode contextualActionBar;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_editor);

		fragmentManager = getSupportFragmentManager();

		planningMapFragment = ((EditorMapFragment) fragmentManager
				.findFragmentById(R.id.mapFragment));
		gestureMapFragment = ((GestureMapFragment) fragmentManager
				.findFragmentById(R.id.gestureMapFragment));
		editorToolsFragment = (EditorToolsFragment) fragmentManager
				.findFragmentById(R.id.editorToolsFragment);
		missionListFragment = (EditorListFragment) fragmentManager
				.findFragmentById(R.id.missionFragment1);
		editorInfoView = (TextView) findViewById(R.id.editorInfoWindow);

		mSplineToggleContainer = findViewById(R.id.editorSplineToggleContainer);
		mSplineToggleContainer.setVisibility(View.VISIBLE);

		mLocationButtonsContainer = findViewById(R.id.location_button_container);
		ImageButton mGoToMyLocation = (ImageButton) findViewById(R.id.my_location_button);
		mGoToMyLocation.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				planningMapFragment.goToMyLocation();
			}
		});
		mGoToMyLocation.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				planningMapFragment.setAutoPanMode(AutoPanMode.USER);
				return true;
			}
		});

		ImageButton mGoToDroneLocation = (ImageButton) findViewById(R.id.drone_location_button);
		mGoToDroneLocation.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				planningMapFragment.goToDroneLocation();
			}
		});
		mGoToDroneLocation
				.setOnLongClickListener(new View.OnLongClickListener() {
					@Override
					public boolean onLongClick(View v) {
						planningMapFragment.setAutoPanMode(AutoPanMode.DRONE);
						return true;
					}
				});

		final RadioButton normalToggle = (RadioButton) findViewById(R.id.normalWpToggle);
		normalToggle.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mIsSplineEnabled = !normalToggle.isChecked();
			}
		});

		final RadioButton splineToggle = (RadioButton) findViewById(R.id.splineWpToggle);
		splineToggle.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mIsSplineEnabled = splineToggle.isChecked();
			}
		});

		// Retrieve the item detail fragment using its tag
		itemDetailFragment = (MissionDetailFragment) fragmentManager
				.findFragmentByTag(ITEM_DETAIL_TAG);

		/*
		 * On phone, this view will be null causing the item detail to be shown
		 * as a dialog.
		 */
		mContainerItemDetail = findViewById(R.id.containerItemDetail);

		missionProxy = ((DroidPlannerApp) getApplication()).missionProxy;
		gestureMapFragment.setOnPathFinishedListener(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		setupTool(getTool());
	}

	@Override
	public void onStart() {
		super.onStart();
		missionProxy.selection.addSelectionUpdateListener(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		missionProxy.selection.removeSelectionUpdateListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		getMenuInflater().inflate(R.menu.menu_mission, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_zoom_fit_mission:
			planningMapFragment.zoomToFit();
			return true;

		case R.id.menu_open_mission:
			openMissionFile();
			return true;

		case R.id.menu_save_mission:
			saveMissionFile();
			return true;

		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}

	private void openMissionFile() {
		OpenFileDialog missionDialog = new OpenMissionDialog(drone) {
			@Override
			public void waypointFileLoaded(MissionReader reader) {
				drone.mission.onMissionLoaded(reader.getMsgMissionItems());
				planningMapFragment.zoomToFit();
			}
		};
		missionDialog.openDialog(this);
	}

	private void saveMissionFile() {

		if (MissionWriter.write(drone.mission.getMsgMissionItems())) {
			Toast.makeText(this, "File saved", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, "Error saving file", Toast.LENGTH_SHORT)
					.show();
		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		updateMapPadding();
	}

	/**
	 * Account for the various ui elements and update the map padding so that it
	 * remains 'visible'.
	 */
	private void updateMapPadding() {
		int topPadding = mLocationButtonsContainer.getBottom()
				+ mLocationButtonsContainer.getPaddingBottom();
		int leftPadding = mLocationButtonsContainer.getLeft()
				- mLocationButtonsContainer.getPaddingLeft();
		planningMapFragment.setMapPadding(leftPadding, topPadding, 0, 0);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		planningMapFragment.saveCameraPosition();
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		super.onDroneEvent(event, drone);

		switch (event) {
		case MISSION_UPDATE:
			// Remove detail window if item is removed
			if (itemDetailFragment != null) {
				if (!missionProxy.contains(itemDetailFragment.getItem())) {
					removeItemDetail();
				}
			}
			recalculateMissionLength();
			break;
		case HOME:
			recalculateMissionLength();
		default:
			break;
		}
	}

	private void recalculateMissionLength() {
		// get mission items
		String distance = getString(R.string.distance);
		Length dist = new Length(0.0);
		List<MissionItem> waypoints = drone.mission.getItems();
		if (waypoints.size() < 2) {
			editorInfoView.setText(distance + ": " + dist);
			return;
		}
		for (MissionItem waypoint : waypoints) {
			Mission mission = waypoint.getMission();
			MissionItem previousWaypoint = mission
					.getPreviousItem((MissionItem) waypoint);
			Coord2D previousWaypointCoordinate = null;
			if (previousWaypoint instanceof Survey) {
				if (((Survey) previousWaypoint).getGrid() != null) {
					List<Coord2D> gridPoints = ((Survey) previousWaypoint)
							.getGrid().gridPoints;
					previousWaypointCoordinate = gridPoints.get(gridPoints
							.size() - 1);
				}
			} else if ((SpatialCoordItem) previousWaypoint != null) {
				previousWaypointCoordinate = ((SpatialCoordItem) previousWaypoint)
						.getCoordinate();
			}

			switch (waypoint.getType()) {
			case SPLINE_WAYPOINT:
			case WAYPOINT:
				Length altDelta = new Length(0.0),
				distDelta = new Length(0.0);
				try {
					altDelta = drone.mission
							.getAltitudeDiffFromPreviousItem((SpatialCoordItem) waypoint);
					if (previousWaypoint != null) {
						distDelta = GeoTools.getDistance(
								previousWaypointCoordinate,
								((SpatialCoordItem) waypoint).getCoordinate());
					}
				} catch (IllegalArgumentException e) {// if this is the first
														// waypoint after a
														// takeoff, this
														// happens. Use drone
														// "home" only if
														// available, else just
														// ignore from
														// calculations.
					if (planningMapFragment.drone.home.isValid()) {
						Coord2D home = planningMapFragment.drone.home
								.getCoord();
						Coord3D waypointCoordinate = ((SpatialCoordItem) waypoint)
								.getCoordinate();
						altDelta = new Length(waypointCoordinate.getAltitude()
								.valueInMeters());
						distDelta = GeoTools.getDistance(home,
								waypointCoordinate);
					}
				}
				dist.add(pythagoreamTheorem(altDelta, distDelta));
				break;
			case TAKEOFF:
				dist.add(drone.mission.getDefaultAlt());
				break;
			case LAND:
				dist.add(drone.mission.getLastAltitude());
				break;
			case CIRCLE:
				// Add the circumferences (2*PI*r), but subtract twice the
				// radius, b/c the drone never actually travels to/from the
				// center of the circle. It stops at the edge of the circle and
				// begins strafing. Also add all altitude steps. And remember to
				// multiply each circumference by the number of turns it does
				Circle circle = (Circle) waypoint;
				Length altDelta2 = new Length(0.0),
				distDelta2 = new Length(0.0);
				try {
					altDelta2 = drone.mission
							.getAltitudeDiffFromPreviousItem((SpatialCoordItem) waypoint);
					if (previousWaypoint != null) {
						distDelta2 = GeoTools.getDistance(
								previousWaypointCoordinate,
								((SpatialCoordItem) waypoint).getCoordinate());
					}
					distDelta2.addMeters(-1 * circle.getRadius());
				} catch (IllegalArgumentException e) {// if this is the first
														// waypoint after a
														// takeoff, this
														// happens. Use drone
														// "home" only if
														// available, else just
														// ignore from
														// calculations.
					if (planningMapFragment.drone.home.isValid()) {
						Coord2D home = planningMapFragment.drone.home
								.getCoord();
						Coord3D waypointCoordinate = ((SpatialCoordItem) waypoint)
								.getCoordinate();
						altDelta2 = new Length(waypointCoordinate.getAltitude()
								.valueInMeters());
						distDelta2 = GeoTools.getDistance(home,
								waypointCoordinate);
					}
				}
				dist.add(pythagoreamTheorem(altDelta2, distDelta2));
				dist.addMeters(-1 * circle.getRadius());
				for (int step = 0; step < circle.getNumberOfSteps(); step++) {
					double circumference = circle.getRadius() * 2 * Math.PI;
					dist.addMeters(circumference * circle.getNumberOfTurns());
					dist.addMeters(circle.getAltitudeStep());
				}
				break;
			case RTL:
				// first, change altitude to rTLALT
				double rTLAlt = 15.0;// default RTL value in case we haven't
										// loaded this param yet
				Parameter prefAlt = drone.parameters.getParameter("RTL_ATL");
				if (prefAlt != null) {
					rTLAlt = prefAlt.value / 10.0;// it's in centimeters
				}
				double lastAltitude = waypoint.getMission().getLastAltitude()
						.valueInMeters();
				Length altDelta3 = new Length(Math.abs(lastAltitude - rTLAlt));
				dist.add(altDelta3);
				// then, travel back to home
				if (planningMapFragment.drone.home.isValid()) {
					Coord2D home = planningMapFragment.drone.home.getCoord();
					if (previousWaypoint != null) {
						dist.add(GeoTools.getDistance(home,
								previousWaypointCoordinate));
					}
				}
				// now, land from the rTLALT
				dist.addMeters(rTLAlt);
				break;
			case SURVEY:
				Survey survey = (Survey) waypoint;
				Grid surveyGrid = survey.getGrid();
				if (previousWaypoint != null) {
					if (surveyGrid != null && surveyGrid.gridPoints != null
							&& surveyGrid.gridPoints.size() > 0) {
						Coord2D startOfSurvey = surveyGrid.gridPoints.get(0);
						dist.add(GeoTools.getDistance(
								previousWaypointCoordinate, startOfSurvey));
					}

				}
				if (surveyGrid != null) {
					List<Coord2D> surveyGridWaypoints = survey.getGrid()
							.getGridPoints();
					for (int i = 0; i < surveyGridWaypoints.size() - 1; i++) {
						dist.add(GeoTools.getDistance(
								surveyGridWaypoints.get(i),
								surveyGridWaypoints.get(i + 1)));
					}
				}
				break;
			default:
				break;

			}
		}

		// TODO calculate flight time. Or don't. Seems pretty hard.
		// String flightTime = getString(R.string.flight_time);

		editorInfoView.setText(distance + ": " + dist);
	}

	private Length pythagoreamTheorem(Length altDelta, Length distDelta) {
		return new Length(Math.sqrt(Math.pow(altDelta.valueInMeters(), 2)
				+ Math.pow(distDelta.valueInMeters(), 2)));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			planningMapFragment.saveCameraPosition();
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onMapClick(Coord2D point) {
		// If an mission item is selected, unselect it.
		missionProxy.selection.clearSelection();

		switch (getTool()) {
		case MARKER:
			if (mIsSplineEnabled) {
				missionProxy.addSplineWaypoint(point);
			} else {
				missionProxy.addWaypoint(point);
			}
			break;
		case DRAW:
			break;
		case POLY:
			break;
		case TRASH:
			break;
		case NONE:
			break;
		}
	}

	public EditorTools getTool() {
		return editorToolsFragment.getTool();
	}

	@Override
	public void editorToolChanged(EditorTools tools) {
		missionProxy.selection.clearSelection();
		setupTool(tools);
	}

	private void setupTool(EditorTools tool) {
		switch (tool) {
		case DRAW:
			enableSplineToggle(true);
			gestureMapFragment.enableGestureDetection();
			break;

		case POLY:
			enableSplineToggle(false);
			Toast.makeText(this, R.string.draw_the_survey_region,
					Toast.LENGTH_SHORT).show();
			gestureMapFragment.enableGestureDetection();
			break;

		case MARKER:
			// Enable the spline selection toggle
			enableSplineToggle(true);
			gestureMapFragment.disableGestureDetection();
			break;

		case TRASH:
		case NONE:
			enableSplineToggle(false);
			gestureMapFragment.disableGestureDetection();
			break;
		}
	}

	@Override
	public void editorToolLongClicked(EditorTools tools) {
		switch (tools) {
		case TRASH: {
			// Clear the mission?
			doClearMissionConfirmation();
			break;
		}

		default: {
			break;
		}
		}
	}

	private void enableSplineToggle(boolean isEnabled) {
		if (mSplineToggleContainer != null) {
			mSplineToggleContainer.setVisibility(isEnabled ? View.VISIBLE
					: View.INVISIBLE);
		}
	}

	private void showItemDetail(MissionItemProxy item) {
		if (itemDetailFragment == null) {
			addItemDetail(item);
		} else {
			switchItemDetail(item);
		}
	}

	private void addItemDetail(MissionItemProxy item) {
		itemDetailFragment = item.getDetailFragment();
		if (itemDetailFragment == null)
			return;

		if (mContainerItemDetail == null) {
			itemDetailFragment.show(fragmentManager, ITEM_DETAIL_TAG);
		} else {
			fragmentManager
					.beginTransaction()
					.replace(R.id.containerItemDetail, itemDetailFragment,
							ITEM_DETAIL_TAG).commit();
		}
	}

	public void switchItemDetail(MissionItemProxy item) {
		removeItemDetail();
		addItemDetail(item);
	}

	private void removeItemDetail() {
		if (itemDetailFragment != null) {
			if (mContainerItemDetail == null) {
				itemDetailFragment.dismiss();
			} else {
				fragmentManager.beginTransaction().remove(itemDetailFragment)
						.commit();
			}
			itemDetailFragment = null;
		}
	}

	@Override
	public void onPathFinished(List<Coord2D> path) {
		List<Coord2D> points = planningMapFragment.projectPathIntoMap(path);
		switch (getTool()) {
		case DRAW:
			if (mIsSplineEnabled) {
				missionProxy.addSplineWaypoints(points);
			} else {
				missionProxy.addWaypoints(points);
			}
			break;

		case POLY:
			if (path.size() > 2) {
				missionProxy.addSurveyPolygon(points);
			} else {
				editorToolsFragment.setTool(EditorTools.POLY);
				return;
			}
			break;

		default:
			break;
		}
		editorToolsFragment.setTool(EditorTools.NONE);
	}

	@Override
	public void onDetailDialogDismissed(MissionItemProxy item) {
		missionProxy.selection.removeItemFromSelection(item);
	}

	@Override
	public void onWaypointTypeChanged(MissionItemProxy newItem,
			MissionItemProxy oldItem) {
		missionProxy.replace(oldItem, newItem);
	}

	private static final int MENU_DELETE = 1;
	private static final int MENU_REVERSE = 2;

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		switch (item.getItemId()) {
		case MENU_DELETE:
			missionProxy.removeSelection(missionProxy.selection);
			mode.finish();
			return true;

		case MENU_REVERSE:
			missionProxy.reverse();
			return true;

		default:
			return false;
		}
	}

	@Override
	public boolean onCreateActionMode(ActionMode arg0, Menu menu) {
		menu.add(0, MENU_DELETE, 0, "Delete");
		menu.add(0, MENU_REVERSE, 0, "Reverse");
		editorToolsFragment.getView().setVisibility(View.INVISIBLE);
		return true;
	}

	@Override
	public void onDestroyActionMode(ActionMode arg0) {
		missionListFragment.updateChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
		missionProxy.selection.clearSelection();
		contextualActionBar = null;
		editorToolsFragment.getView().setVisibility(View.VISIBLE);
	}

	@Override
	public boolean onPrepareActionMode(ActionMode arg0, Menu arg1) {
		return false;
	}

	@Override
	public boolean onItemLongClick(MissionItemProxy item) {
		if (contextualActionBar != null) {
			if (missionProxy.selection.selectionContains(item)) {
				missionProxy.selection.clearSelection();
			} else {
				missionProxy.selection.setSelectionTo(missionProxy.getItems());
			}
		} else {
			editorToolsFragment.setTool(EditorTools.NONE);
			missionListFragment
					.updateChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
			contextualActionBar = startActionMode(this);
			missionProxy.selection.setSelectionTo(item);
		}
		return true;
	}

	@Override
	public void onItemClick(MissionItemProxy item) {
		switch (getTool()) {
		default:
			if (contextualActionBar != null) {
				if (missionProxy.selection.selectionContains(item)) {
					missionProxy.selection.removeItemFromSelection(item);
				} else {
					missionProxy.selection.addToSelection(item);
				}
			} else {
				if (missionProxy.selection.selectionContains(item)) {
					missionProxy.selection.clearSelection();
				} else {
					editorToolsFragment.setTool(EditorTools.NONE);
					missionProxy.selection.setSelectionTo(item);
				}
			}
			break;

		case TRASH:
			missionProxy.removeItem(item);
			missionProxy.selection.clearSelection();
			if (missionProxy.getItems().size() <= 0) {
				editorToolsFragment.setTool(EditorTools.NONE);
			}
			break;
		}
	}

	@Override
	public void onListVisibilityChanged() {
		updateMapPadding();
	}

	@Override
	public void onSelectionUpdate(List<MissionItemProxy> selected) {
		final int selectedCount = selected.size();

		missionListFragment.setArrowsVisibility(selectedCount > 0);

		if (selectedCount != 1) {
			removeItemDetail();
		} else {
			if (contextualActionBar != null)
				removeItemDetail();
			else {
				showItemDetail(selected.get(0));
			}
		}

		planningMapFragment.update();
	}

	private void doClearMissionConfirmation() {
		YesNoDialog ynd = YesNoDialog.newInstance(
				getString(R.string.dlg_clear_mission_title),
				getString(R.string.dlg_clear_mission_confirm),
				new YesNoDialog.Listener() {
					@Override
					public void onYes() {
						missionProxy.clear();
						missionProxy.addTakeoff();
					}

					@Override
					public void onNo() {
					}
				});

		ynd.show(getSupportFragmentManager(), "clearMission");
	}
}
