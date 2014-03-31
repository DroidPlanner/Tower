package org.droidplanner.android.mission.item.fragments;

import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.activities.EditorActivity;
import org.droidplanner.android.mission.item.MissionItemRender;
import org.droidplanner.android.mission.item.MissionRender;
import org.droidplanner.android.mission.item.adapters.AdapterMissionItems;
import org.droidplanner.core.mission.MissionItem;
import org.droidplanner.core.mission.MissionItemTypes;
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

    /**
     * Mission item extra key used to pass the mission item via the fragment's argument bundle.
     */
    public static final String EXTRA_MISSION_ITEM_RENDER = MissionDetailFragment.class.getPackage() +
            ".EXTRA_MISSION_ITEM_RENDER";

	public interface OnWayPointTypeChangeListener {
		public void onWaypointTypeChanged(MissionItemRender newItem, MissionItemRender oldItem);
	}

	protected abstract int getResource();

	protected SpinnerSelfSelect typeSpinner;
	protected AdapterMissionItems commandAdapter;
	protected MissionRender missionRender;
	private OnWayPointTypeChangeListener mListener;

	protected MissionItemRender itemRender;

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		final EditorActivity parentActivity = (EditorActivity) getActivity();
		if (parentActivity.getItemDetailFragment() != this) {
			dismiss();
			parentActivity.switchItemDetail(itemRender);
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

        Bundle args = getArguments();
        if(args == null)
            throw new IllegalStateException("Missing mission item render argument.");

        itemRender = (MissionItemRender)args.getSerializable(EXTRA_MISSION_ITEM_RENDER);

		setupViews(view);
		return view;
	}

	protected void setupViews(View view) {
        commandAdapter = new AdapterMissionItems(this.getActivity(),
                android.R.layout.simple_list_item_1, MissionItemTypes.values());

		typeSpinner = (SpinnerSelfSelect) view.findViewById(R.id.spinnerWaypointType);
		typeSpinner.setAdapter(commandAdapter);
		typeSpinner.setOnItemSelectedListener(this);

        final TextView waypointIndex = (TextView) view.findViewById(R.id.WaypointIndex);
		Integer temp = missionRender.getOrder(itemRender);
		waypointIndex.setText(temp.toString());

		final TextView distanceView = (TextView) view.findViewById(R.id.DistanceValue);

		final TextView distanceLabelView = (TextView) view.findViewById(R.id.DistanceLabel);

		try {
			distanceLabelView.setVisibility(View.VISIBLE);
			distanceView.setText(missionRender.getDistanceFromLastWaypoint(itemRender).toString());
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
		missionRender = ((DroidPlannerApp) getActivity().getApplication()).missionRender;
		mListener = (OnWayPointTypeChangeListener) activity;
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View v, int position, long id) {

		MissionItemTypes selected = commandAdapter.getItem(position);
		try {
			MissionItem newItem = selected.getNewItem(missionRender.getMission());
			if (!newItem.getClass().equals(getItem().getClass())) {
				Log.d("CLASS", "Different waypoint Classes");
				mListener.onWaypointTypeChanged(new MissionItemRender(missionRender, newItem),
                        getItem());
			}
		} catch (IllegalArgumentException e) {
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
	}

	public MissionItemRender getItem() {
		return itemRender;
	}


}