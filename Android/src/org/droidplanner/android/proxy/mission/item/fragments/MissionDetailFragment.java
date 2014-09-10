package org.droidplanner.android.proxy.mission.item.fragments;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.proxy.mission.MissionProxy;
import org.droidplanner.android.proxy.mission.item.MissionItemProxy;
import org.droidplanner.android.proxy.mission.item.adapters.AdapterMissionItems;
import org.droidplanner.android.widgets.spinners.SpinnerSelfSelect;
import org.droidplanner.core.mission.MissionItem;
import org.droidplanner.core.mission.MissionItemType;

import android.app.Activity;
import android.content.DialogInterface;
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

    private static final String TAG = MissionDetailFragment.class.getSimpleName();

	public interface OnMissionDetailListener {
		/**
		 * Only fired when the mission detail is shown as a dialog. Notifies the
		 * listener that the mission detail dialog has been dismissed.
		 * 
		 * @param item
		 *            mission item proxy whose details the dialog is showing.
		 */
		public void onDetailDialogDismissed(MissionItemProxy item);

		/**
		 * Notifies the listener that the mission item proxy was changed.
		 * 
		 * @param newItem
		 *            previous mission item proxy
		 * @param oldItem
		 *            new mission item proxy
		 */
		public void onWaypointTypeChanged(MissionItemProxy newItem, MissionItemProxy oldItem);
	}

	protected abstract int getResource();

	protected SpinnerSelfSelect typeSpinner;
	protected AdapterMissionItems commandAdapter;
	private OnMissionDetailListener mListener;
	private MissionProxy mMissionProxy;

	protected MissionItemProxy itemRender;

	public static MissionDetailFragment newInstance(MissionItemType itemType) {
		MissionDetailFragment fragment;
		switch (itemType) {
		case LAND:
			fragment = new MissionLandFragment();
			break;
		case LOITER:
			fragment = new MissionLoiterFragment();
			break;
		case CIRCLE:
			fragment = new MissionCircleFragment();
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

		case SPLINE_WAYPOINT:
			fragment = new MissionSplineWaypointFragment();
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
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final MissionProxy missionProxy = ((DroidPlannerApp) getActivity().getApplication()).missionProxy;
        final List<MissionItemProxy> selections = missionProxy.selection.getSelected();
        if(selections.isEmpty()){
            return null;
        }

		itemRender = selections.get(0);
        return inflater.inflate(getResource(), container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mMissionProxy = itemRender.getMissionProxy();

		List<MissionItemType> list = new LinkedList<MissionItemType>(Arrays.asList(MissionItemType
				.values()));
		if (mMissionProxy.getItems().indexOf(itemRender) != 0) {
			list.remove(MissionItemType.TAKEOFF);
		}

		if (mMissionProxy.getItems().indexOf(itemRender) != (mMissionProxy.getItems().size() - 1)) {
			list.remove(MissionItemType.LAND);
			list.remove(MissionItemType.RTL);
		}
		commandAdapter = new AdapterMissionItems(this.getActivity(),
				android.R.layout.simple_list_item_1, list.toArray(new MissionItemType[0]));

		typeSpinner = (SpinnerSelfSelect) view.findViewById(R.id.spinnerWaypointType);
		typeSpinner.setAdapter(commandAdapter);
		typeSpinner.setOnItemSelectedListener(this);

		final TextView waypointIndex = (TextView) view.findViewById(R.id.WaypointIndex);
		if (waypointIndex != null) {
			final int itemOrder = mMissionProxy.getOrder(itemRender);
			waypointIndex.setText(String.valueOf(itemOrder));
		}

		final TextView distanceView = (TextView) view.findViewById(R.id.DistanceValue);
        if(distanceView != null){
            try {
                distanceView.setText(mMissionProxy.getDistanceFromLastWaypoint(itemRender).toString());
            }catch(IllegalArgumentException e){
               Log.e(TAG, e.getMessage(), e);
            }
        }

		final TextView distanceLabelView = (TextView) view.findViewById(R.id.DistanceLabel);
        if(distanceLabelView != null){
            distanceLabelView.setVisibility(View.VISIBLE);
        }
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (!(activity instanceof OnMissionDetailListener)) {
			throw new IllegalStateException("Parent activity must be an instance of "
					+ OnMissionDetailListener.class.getName());
		}

		mListener = (OnMissionDetailListener) activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
		if (mListener != null) {
			mListener.onDetailDialogDismissed(itemRender);
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View v, int position, long id) {
		MissionItemType selected = commandAdapter.getItem(position);
		try {
			final MissionItem oldItem = itemRender.getMissionItem();
			if (oldItem.getType() != selected) {
				Log.d("CLASS", "Different waypoint Classes");
				MissionItem newItem = selected.getNewItem(oldItem);
				mListener.onWaypointTypeChanged(new MissionItemProxy(itemRender.getMissionProxy(),
						newItem), itemRender);
				dismiss();
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
	}

	public MissionItemProxy getItem() {
		return itemRender;
	}

}