package org.droidplanner.android.fragments.mission;

import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.activities.EditorActivity;
import org.droidplanner.android.fragments.mission.MissionItemTypes.InvalidItemException;
import org.droidplanner.core.mission.Mission;
import org.droidplanner.core.mission.MissionItem;
import org.droidplanner.core.mission.waypoints.SpatialCoordItem;
import org.droidplanner.android.widgets.spinners.SpinnerSelfSelect;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView;

public abstract class MissionDetailFragment extends DialogFragment implements
		OnItemSelectedListener {

	public interface OnWayPointTypeChangeListener {
		public void onWaypointTypeChanged(MissionItem newItem,
				MissionItem oldItem);
	}

	protected abstract int getResource();

	protected SpinnerSelfSelect typeSpinner;
	protected AdapterMissionItems commandAdapter;
	protected Mission mission;
	private OnWayPointTypeChangeListener mListener;

	protected MissionItem item;

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		final EditorActivity parentActivity = (EditorActivity) getActivity();
		if (parentActivity.getItemDetailFragment() != this) {
			dismiss();
			parentActivity.switchItemDetail(item);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(DialogFragment.STYLE_NO_TITLE, 0);
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(getResource(), null);
		setupViews(view);
		return view;
	}

	protected void setupViews(View view) {
		typeSpinner = (SpinnerSelfSelect) view
				.findViewById(R.id.spinnerWaypointType);
		commandAdapter = new AdapterMissionItems(this.getActivity(),
				android.R.layout.simple_list_item_1, MissionItemTypes.values());
		typeSpinner.setAdapter(commandAdapter);
		typeSpinner.setOnItemSelectedListener(this);
		final TextView waypointIndex = (TextView) view
				.findViewById(R.id.WaypointIndex);
		Integer temp = mission.getNumber(item);
		waypointIndex.setText(temp.toString());

		final TextView distanceView = (TextView) view
				.findViewById(R.id.DistanceValue);

		final TextView distanceLabelView = (TextView) view
				.findViewById(R.id.DistanceLabel);

		try {
			distanceLabelView.setVisibility(View.VISIBLE);
			distanceView.setText(mission.getDistanceFromLastWaypoint(
					(SpatialCoordItem) item).toString());
		} catch (NullPointerException e) {
			// Can fail if distanceView doesn't exists
		} catch (Exception e) {
			// Or if the last item doesn't have a coordinate
			// distanceView.setText("");
			// distanceLabelView.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mission = ((DroidPlannerApp) getActivity().getApplication()).drone.mission;
		mListener = (OnWayPointTypeChangeListener) activity;
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View v, int position,
			long id) {

		MissionItemTypes selected = commandAdapter.getItem(position);
		try {
			MissionItem newItem = selected.getNewItem(getItem());
			if (!newItem.getClass().equals(getItem().getClass())) {
				Log.d("CLASS", "Different waypoint Classes");
				mListener.onWaypointTypeChanged(newItem, getItem());
			}
		} catch (InvalidItemException e) {
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
	}

	public MissionItem getItem() {
		return item;
	}

	public void setItem(MissionItem item) {
		this.item = item;
	}

}