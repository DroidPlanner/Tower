package org.droidplanner.android.proxy.mission.item.fragments;

import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.proxy.mission.item.MissionItemProxy;
import org.droidplanner.android.proxy.mission.MissionProxy;
import org.droidplanner.android.proxy.mission.item.adapters.AdapterMissionItems;
import org.droidplanner.core.mission.MissionItem;
import org.droidplanner.core.mission.MissionItemType;
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
		public void onWaypointTypeChanged(MissionItemProxy newItem, MissionItemProxy oldItem);
	}

	protected abstract int getResource();

	protected SpinnerSelfSelect typeSpinner;
	protected AdapterMissionItems commandAdapter;
	private OnWayPointTypeChangeListener mListener;

	protected MissionItemProxy itemRender;

    public static MissionDetailFragment newInstance(MissionItemType itemType){
        MissionDetailFragment fragment;
        switch(itemType){
            case LAND:
                fragment = new MissionLandFragment();
                break;
            case LOITER:
                fragment = new MissionLoiterFragment();
                break;
            case LOITERN:
                fragment = new MissionLoiterNFragment();
                break;
            case LOITERT:
                fragment = new MissionLoiterTFragment();
                break;
            case ROI:
                fragment = new MissionRegionOfInterestFragment();
                break;
            case RTL:
                fragment = new MissionRTLFragment();
                break;
            case SURVEY:
                fragment = new MissionSurveyFragment();
                break;
            case TAKEOFF:
                fragment = new MissionTakeoffFragment();
                break;
            case WAYPOINT:
                fragment = new MissionWaypointFragment();
                break;
            default:
                fragment = null;
                break;
        }

        return fragment;
    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(DialogFragment.STYLE_NO_TITLE, 0);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(getResource(), null);

        final MissionProxy missionProxy = ((DroidPlannerApp)getActivity().getApplication())
                .missionProxy;
        itemRender = missionProxy.selection.getSelected().get(0);

		setupViews(view);
		return view;
	}

	protected void setupViews(View view) {
        final MissionProxy missionProxy = itemRender.getMissionProxy();

        commandAdapter = new AdapterMissionItems(this.getActivity(),
                android.R.layout.simple_list_item_1, MissionItemType.values());

		typeSpinner = (SpinnerSelfSelect) view.findViewById(R.id.spinnerWaypointType);
		typeSpinner.setAdapter(commandAdapter);
		typeSpinner.setOnItemSelectedListener(this);

        final TextView waypointIndex = (TextView) view.findViewById(R.id.WaypointIndex);
        if(waypointIndex != null) {
            final int itemOrder = missionProxy.getOrder(itemRender);
            waypointIndex.setText(String.valueOf(itemOrder));
        }

		final TextView distanceView = (TextView) view.findViewById(R.id.DistanceValue);

		final TextView distanceLabelView = (TextView) view.findViewById(R.id.DistanceLabel);

		try {
			distanceLabelView.setVisibility(View.VISIBLE);
			distanceView.setText(missionProxy.getDistanceFromLastWaypoint(itemRender).toString());
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
		mListener = (OnWayPointTypeChangeListener) activity;
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View v, int position, long id) {
		MissionItemType selected = commandAdapter.getItem(position);
		try {
            final MissionItem oldItem = itemRender.getMissionItem();
            if(oldItem.getType() != selected){
				Log.d("CLASS", "Different waypoint Classes");
                MissionItem newItem = selected.getNewItem(oldItem);
				mListener.onWaypointTypeChanged(new MissionItemProxy(itemRender.getMissionProxy(), newItem),
                        itemRender);
                dismiss();
			}
		} catch (IllegalArgumentException e) {
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
	}

	public MissionItemProxy getItem() {
		return itemRender;
	}


}