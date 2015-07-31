package org.droidplanner.android.proxy.mission.item.fragments;

import android.content.Context;
import android.view.View;

import com.o3dr.services.android.lib.drone.mission.MissionItemType;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;
import com.o3dr.services.android.lib.drone.mission.item.command.DoJump;

import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.R;
import org.droidplanner.android.proxy.mission.MissionProxy;
import org.droidplanner.android.proxy.mission.item.markers.MissionItemMarkerInfo;
import org.droidplanner.android.widgets.spinnerWheel.CardWheelHorizontalView;
import org.droidplanner.android.widgets.spinnerWheel.adapters.NumericWheelAdapter;

/**
 * Created by Toby on 7/31/2015.
 */
public class MissionDoJumpFragment extends MissionDetailFragment implements CardWheelHorizontalView.OnCardWheelScrollListener<Integer> {

    @Override
    protected int getResource() {
        return R.layout.fragment_editor_detail_do_jump;
    }

    @Override
    public void onApiConnected() {
        super.onApiConnected();

        final View view = getView();
        typeSpinner.setSelection(commandAdapter.getPosition(MissionItemType.DO_JUMP));

        final DoJump item = (DoJump) getMissionItems().get(0);
        final Context context = getContext();

        final NumericWheelAdapter adapter = new NumericWheelAdapter(context, R.layout.wheel_text_centered, getFirstWaypoint(), getLastWaypoint(), "%d");
        final CardWheelHorizontalView<Integer> waypointPicker = (CardWheelHorizontalView) view.findViewById(R.id
                .waypoint_picker);
        waypointPicker.setViewAdapter(adapter);
        waypointPicker.addScrollListener(this);
        waypointPicker.setCurrentValue(item.getWaypoint());

        final NumericWheelAdapter repeatCountAdapter = new NumericWheelAdapter(context, R.layout.wheel_text_centered, -1,
                2000, "%d");
        repeatCountAdapter.addValueMap(-1, "inf");
        final CardWheelHorizontalView<Integer> repeatCountPicker = (CardWheelHorizontalView) view.findViewById(R.id.repeat_picker);
        repeatCountPicker.setViewAdapter(repeatCountAdapter);
        repeatCountPicker.addScrollListener(this);
        repeatCountPicker.setCurrentValue(item.getRepeatCount());
    }

    @Override
    public void onScrollingStarted(CardWheelHorizontalView cardWheel, Integer startValue) {

    }

    @Override
    public void onScrollingUpdate(CardWheelHorizontalView cardWheel, Integer oldValue, Integer newValue) {

    }

    @Override
    public void onScrollingEnded(CardWheelHorizontalView wheel, Integer startValue, Integer endValue) {
        switch (wheel.getId()) {
            case R.id.waypoint_picker:
                for (MissionItem missionItem : getMissionItems()) {
                    DoJump item = (DoJump) missionItem;
                    item.setWaypoint(endValue);
                }
                getMissionProxy().notifyMissionUpdate();
                break;

            case R.id.repeat_picker:
                for (MissionItem missionItem : getMissionItems()) {
                    DoJump item = (DoJump) missionItem;
                    item.setRepeatCount(endValue);
                }
                getMissionProxy().notifyMissionUpdate();
                break;
        }
    }

    private int getFirstWaypoint(){
        MissionProxy proxy = ((DroidPlannerApp) getActivity().getApplication()).getMissionProxy();
        if(proxy.getMarkersInfos().size() > 0) {
            MissionItemMarkerInfo info = (MissionItemMarkerInfo) proxy.getMarkersInfos().get(0);
            return proxy.getOrder(info.getMarkerOrigin());
        }
        return 0;
    }

    private int getLastWaypoint(){
        MissionProxy proxy = ((DroidPlannerApp) getActivity().getApplication()).getMissionProxy();
        if(proxy.getMarkersInfos().size() > 0) {
            MissionItemMarkerInfo info = (MissionItemMarkerInfo) proxy.getMarkersInfos().get(proxy.getMarkersInfos().size()-1);
            return proxy.getOrder(info.getMarkerOrigin());
        }
        return 0;
    }
}
