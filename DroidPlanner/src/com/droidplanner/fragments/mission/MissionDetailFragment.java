package com.droidplanner.fragments.mission;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import com.droidplanner.DroidPlannerApp;
import com.droidplanner.R;
import com.droidplanner.drone.variables.mission.Mission;
import com.droidplanner.drone.variables.mission.MissionItem;
import com.droidplanner.fragments.mission.MissionItemTypes.InvalidItemException;
import com.droidplanner.widgets.spinners.SpinnerSelfSelect;

public abstract class MissionDetailFragment extends DialogFragment implements
		OnItemSelectedListener {
	
	public interface OnWayPointTypeChangeListener{
		public void onWaypointTypeChanged(MissionItem newItem, MissionItem oldItem);
	}
	
	protected abstract int getResource();
	
	protected SpinnerSelfSelect typeSpinner;
	protected AdapterMissionItens commandAdapter;
	protected Mission mission;
	private OnWayPointTypeChangeListener mListner;
	
	protected MissionItem item;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE,0);
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
		commandAdapter = new AdapterMissionItens(this.getActivity(),
				android.R.layout.simple_list_item_1, MissionItemTypes.values());
		typeSpinner.setAdapter(commandAdapter);
		typeSpinner.setOnItemSelectedListener(this);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mission = ((DroidPlannerApp) getActivity().getApplication()).drone.mission;
		mListner = (OnWayPointTypeChangeListener) activity;
	}

	
	@Override
	public void onItemSelected(AdapterView<?> arg0, View v, int position,
			long id) {

		MissionItemTypes selected = commandAdapter.getItem(position);
		try {
			MissionItem newItem = selected.getNewItem(getItem());
			if (!newItem.getClass().equals(getItem().getClass())) {
				Log.d("CLASS", "Diferent waypoint Classes");
				mListner.onWaypointTypeChanged(newItem, getItem());
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