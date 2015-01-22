package org.droidplanner.android.proxy.mission.item.fragments;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.o3dr.services.android.lib.drone.mission.MissionItemType;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.mission.item.complex.StructureScanner;
import com.o3dr.services.android.lib.drone.mission.item.complex.Survey;

import org.droidplanner.android.R;
import org.droidplanner.android.fragments.helpers.ApiListenerDialogFragment;
import org.droidplanner.android.proxy.mission.MissionProxy;
import org.droidplanner.android.proxy.mission.item.MissionItemProxy;
import org.droidplanner.android.proxy.mission.item.adapters.AdapterMissionItems;
import org.droidplanner.android.widgets.spinners.SpinnerSelfSelect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class MissionDetailFragment extends ApiListenerDialogFragment implements SpinnerSelfSelect
        .OnSpinnerItemSelectedListener {

    private static final String TAG = MissionDetailFragment.class.getSimpleName();

    protected static final int MIN_ALTITUDE = -200; // meter
    protected static final int MAX_ALTITUDE = +200; // meters

    public static final List<MissionItemType> typeWithNoMultiEditSupport = new
            ArrayList<MissionItemType>();

    static {
        typeWithNoMultiEditSupport.add(MissionItemType.LAND);
        typeWithNoMultiEditSupport.add(MissionItemType.TAKEOFF);
        typeWithNoMultiEditSupport.add(MissionItemType.SURVEY);
        typeWithNoMultiEditSupport.add(MissionItemType.RETURN_TO_LAUNCH);
        typeWithNoMultiEditSupport.add(MissionItemType.STRUCTURE_SCANNER);
    }

    public interface OnMissionDetailListener {
        /**
         * Only fired when the mission detail is shown as a dialog. Notifies the
         * listener that the mission detail dialog has been dismissed.
         *
         * @param itemList list of mission items proxies whose details the dialog is showing.
         */
        public void onDetailDialogDismissed(List<MissionItemProxy> itemList);

        /**
         * Notifies the listener that the mission item proxy was changed.
         *
         * @param newType         the new selected mission item type
         * @param oldNewItemsList a list of pairs containing the previous,
         *                        and the new mission item proxy.
         */
        public void onWaypointTypeChanged(MissionItemType newType, List<Pair<MissionItemProxy,
                MissionItemProxy>> oldNewItemsList);
    }

    protected int getResource() {
        return R.layout.fragment_editor_detail_generic;
    }

    protected SpinnerSelfSelect typeSpinner;
    protected AdapterMissionItems commandAdapter;
    private OnMissionDetailListener mListener;

    private MissionProxy mMissionProxy;
    private final List<MissionItem> mSelectedItems = new ArrayList<MissionItem>();
    private final List<MissionItemProxy> mSelectedProxies = new ArrayList<MissionItemProxy>();

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
            case REGION_OF_INTEREST:
                fragment = new MissionRegionOfInterestFragment();
                break;
            case RETURN_TO_LAUNCH:
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
            case STRUCTURE_SCANNER:
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
            case YAW_CONDITION:
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
    public void onApiConnected() {
        mMissionProxy = getMissionProxy();

        mSelectedProxies.clear();
        mSelectedProxies.addAll(mMissionProxy.selection.getSelected());

        mSelectedItems.clear();
        for (MissionItemProxy mip : mSelectedProxies) {
            mSelectedItems.add(mip.getMissionItem());
        }

        final View view = getView();
        if (view == null) return;

        List<MissionItemType> list = new LinkedList<MissionItemType>(Arrays.asList(MissionItemType.values()));

        if (mSelectedProxies.size() == 1) {
            final MissionItemProxy itemProxy = mSelectedProxies.get(0);
            final MissionItem currentItem = itemProxy.getMissionItem();

            if (currentItem instanceof Survey) {
                list.clear();
                list.add(MissionItemType.SURVEY);
            } else {
                list.remove(MissionItemType.SURVEY);
            }

            if ((currentItem instanceof StructureScanner)) {
                list.clear();
                list.add(MissionItemType.STRUCTURE_SCANNER);
            }

            if (mMissionProxy.getItems().indexOf(itemProxy) != 0) {
                list.remove(MissionItemType.TAKEOFF);
            }

            if (mMissionProxy.getItems().indexOf(itemProxy) != (mMissionProxy.getItems().size() - 1)) {
                list.remove(MissionItemType.LAND);
                list.remove(MissionItemType.RETURN_TO_LAUNCH);
            }

            if (currentItem instanceof MissionItem.Command) {
                list.remove(MissionItemType.LAND);
                list.remove(MissionItemType.SPLINE_WAYPOINT);
                list.remove(MissionItemType.CIRCLE);
                list.remove(MissionItemType.REGION_OF_INTEREST);
                list.remove(MissionItemType.WAYPOINT);
                list.remove(MissionItemType.STRUCTURE_SCANNER);
            }

            final TextView waypointIndex = (TextView) view.findViewById(R.id.WaypointIndex);
            if (waypointIndex != null) {
                final int itemOrder = mMissionProxy.getOrder(itemProxy);
                waypointIndex.setText(String.valueOf(itemOrder));
            }

            final TextView distanceView = (TextView) view.findViewById(R.id.DistanceValue);
            if (distanceView != null) {
                distanceView.setText(getLengthUnitProvider().boxBaseValueToTarget(mMissionProxy
                        .getDistanceFromLastWaypoint(itemProxy)).toString());
            }

            final TextView distanceLabelView = (TextView) view.findViewById(R.id.DistanceLabel);
            if (distanceLabelView != null) {
                distanceLabelView.setVisibility(View.VISIBLE);
            }
        } else if (mSelectedProxies.size() > 1) {
            //Remove the mission item types that don't apply to multiple items.
            list.removeAll(typeWithNoMultiEditSupport);
        } else {
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
    public void onApiDisconnected() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(getResource(), container, false);
    }

    protected List<? extends MissionItem> getMissionItems() {
        return mSelectedItems;
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
            if (mSelectedProxies.isEmpty())
                return;

            final List<Pair<MissionItemProxy, MissionItemProxy>> updatesList = new ArrayList<Pair<MissionItemProxy, MissionItemProxy>>(
                    mSelectedProxies.size());

            for (MissionItemProxy missionItemProxy : mSelectedProxies) {
                final MissionItem oldItem = missionItemProxy.getMissionItem();
                if (oldItem.getType() != selectedType) {
                    final MissionItem newItem = selectedType.getNewItem();

                    if (oldItem instanceof MissionItem.SpatialItem && newItem instanceof
                            MissionItem.SpatialItem) {
                        ((MissionItem.SpatialItem) newItem).setCoordinate(((MissionItem
                                .SpatialItem) oldItem).getCoordinate());
                    }

                    updatesList.add(Pair.create(missionItemProxy, new MissionItemProxy(
                            mMissionProxy, newItem)));
                }
            }

            if (!updatesList.isEmpty()) {
                mListener.onWaypointTypeChanged(selectedType, updatesList);
                dismiss();
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }
}