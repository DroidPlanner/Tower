package org.droidplanner.android.proxy.mission.item.fragments;

import java.util.ArrayList;
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
import org.droidplanner.core.mission.commands.MissionCMD;
import org.droidplanner.core.mission.survey.Survey2D;
import org.droidplanner.core.mission.waypoints.StructureScanner;
import org.droidplanner.core.util.Pair;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TextView;

public class MissionDetailFragment extends DialogFragment implements SpinnerSelfSelect.OnSpinnerItemSelectedListener {

	private static final String TAG = MissionDetailFragment.class.getSimpleName();

	protected static final int MIN_ALTITUDE = -200; // meter
	protected static final int MAX_ALTITUDE = +200; // meters

    public interface OnMissionDetailListener {
		/**
		 * Only fired when the mission detail is shown as a dialog. Notifies the
		 * listener that the mission detail dialog has been dismissed.
		 * 
		 * @param itemList
		 *            list of mission items proxies whose details the dialog is showing.
		 */
		public void onDetailDialogDismissed(List<MissionItemProxy> itemList);

		/**
		 * Notifies the listener that the mission item proxy was changed.
		 *
         * @param oldNewItemsList a list of pairs containing the previous,
         *                         and the new mission item proxy.
		 */
		public void onWaypointTypeChanged(List<Pair<MissionItemProxy,
                MissionItemProxy>> oldNewItemsList);
	}

	protected int getResource(){
        return R.layout.fragment_editor_detail_generic;
    }

	protected SpinnerSelfSelect typeSpinner;
	protected AdapterMissionItems commandAdapter;
	private OnMissionDetailListener mListener;

    private MissionProxy mMissionProxy;
    private List<MissionItem> mSelectedItems;
    private List<MissionItemProxy> mSelectedProxies;

	public static MissionDetailFragment newInstance(MissionItemType itemType) {
		MissionDetailFragment fragment;
		switch (itemType) {
		case LAND:
			fragment = new MissionLandFragment();
			break;
		case CIRCLE:
			fragment = new MissionCircleFragment();
			break;
		case CHANGE_SPEED:
			fragment = new MissionChangeSpeedFragment();
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
		case SURVEY3D:
			fragment = new MissionSurvey3DFragment();
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
		case CYLINDRICAL_SURVEY:
			fragment = new MissionStructureScannerFragment();
			break;
		case CAMERA_TRIGGER:
			fragment = new MissionCameraTriggerFragment();
			break;
		case EPM_GRIPPER:
			fragment = new MissionEpmGrabberFragment();
			break;
		case SET_SERVO:
			fragment = new SetServoFragment();
			break;
		case CONDITION_YAW:
			fragment = new MissionConditionYawFragment();
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
		mMissionProxy = ((DroidPlannerApp) getActivity().getApplication()).getMissionProxy();
		mSelectedProxies = new ArrayList<MissionItemProxy>(mMissionProxy.selection.getSelected());
		if (mSelectedProxies.isEmpty()) {
			return null;
		}

        mSelectedItems = new ArrayList<MissionItem>(mSelectedProxies.size());
        for(MissionItemProxy mip : mSelectedProxies){
            mSelectedItems.add(mip.getMissionItem());
        }

		return inflater.inflate(getResource(), container, false);
	}

    protected MissionProxy getMissionProxy(){
        return mMissionProxy;
    }

    protected List<? extends MissionItem> getMissionItems(){
        return mSelectedItems;
    }

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		List<MissionItemType> list = new LinkedList<MissionItemType>(Arrays.asList(MissionItemType.values()));

        if(mSelectedProxies.size() == 1) {
            final MissionItemProxy itemProxy = mSelectedProxies.get(0);
            final MissionItem currentItem = itemProxy.getMissionItem();

            if ((currentItem instanceof Survey2D)) {
                list.clear();
                list.add(MissionItemType.SURVEY);
            } else {
                list.remove(MissionItemType.SURVEY);
            }
            
            if ((currentItem instanceof StructureScanner)) {
                list.clear();
                list.add(MissionItemType.CYLINDRICAL_SURVEY);
            }

            if (mMissionProxy.getItems().indexOf(itemProxy) != 0) {
                list.remove(MissionItemType.TAKEOFF);
            }

            if (mMissionProxy.getItems().indexOf(itemProxy) != (mMissionProxy.getItems().size() - 1)) {
                list.remove(MissionItemType.LAND);
                list.remove(MissionItemType.RTL);
            }

            if (currentItem instanceof MissionCMD) {
                list.remove(MissionItemType.LAND);
                list.remove(MissionItemType.SPLINE_WAYPOINT);
                list.remove(MissionItemType.CIRCLE);
                list.remove(MissionItemType.ROI);
                list.remove(MissionItemType.WAYPOINT);
                list.remove(MissionItemType.CYLINDRICAL_SURVEY);
            }

            final TextView waypointIndex = (TextView) view.findViewById(R.id.WaypointIndex);
            if (waypointIndex != null) {
                final int itemOrder = mMissionProxy.getOrder(itemProxy);
                waypointIndex.setText(String.valueOf(itemOrder));
            }

            final TextView distanceView = (TextView) view.findViewById(R.id.DistanceValue);
            if (distanceView != null) {
                try {
                    distanceView.setText(mMissionProxy.getDistanceFromLastWaypoint(itemProxy)
                            .toString());
                } catch (IllegalArgumentException e) {
                    Log.w(TAG, e.getMessage(), e);
                }
            }

            final TextView distanceLabelView = (TextView) view.findViewById(R.id.DistanceLabel);
            if (distanceLabelView != null) {
                distanceLabelView.setVisibility(View.VISIBLE);
            }
        }
        else if(mSelectedProxies.size() > 1){
            //Remove the mission item types that don't apply to multiple items.
            list.remove(MissionItemType.TAKEOFF);
            list.remove(MissionItemType.LAND);
            list.remove(MissionItemType.RTL);
            list.remove(MissionItemType.SURVEY);
            list.remove(MissionItemType.CYLINDRICAL_SURVEY);
        }
        else{
            //Invalid state. We should not have been able to get here.
            throw new IllegalStateException("Mission Detail Fragment cannot be shown when no " +
                    "mission items is selected.");
        }
		
		commandAdapter = new AdapterMissionItems(getActivity(),
				android.R.layout.simple_list_item_1, list.toArray(new MissionItemType[list.size()]));

		typeSpinner = (SpinnerSelfSelect) view.findViewById(R.id.spinnerWaypointType);
		typeSpinner.setAdapter(commandAdapter);
        typeSpinner.setOnSpinnerItemSelectedListener(this);
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
			mListener.onDetailDialogDismissed(mSelectedProxies);
		}
	}

    @Override
    public void onSpinnerItemSelected(Spinner spinner, int position) {
        final MissionItemType selectedType = commandAdapter.getItem(position);

        try {
            if (mSelectedProxies == null || mSelectedProxies.isEmpty())
                return;

            final List<Pair<MissionItemProxy, MissionItemProxy>> updatesList = new ArrayList<Pair<MissionItemProxy, MissionItemProxy>>(
                    mSelectedProxies.size());

            for (MissionItemProxy missionItemProxy : mSelectedProxies) {
                final MissionItem oldItem = missionItemProxy.getMissionItem();
                if (oldItem.getType() != selectedType) {
                    updatesList.add(Pair.create(missionItemProxy, new MissionItemProxy(
                            mMissionProxy, selectedType.getNewItem(oldItem))));
                }
            }

            if(!updatesList.isEmpty()) {
                mListener.onWaypointTypeChanged(updatesList);
                dismiss();
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }
}