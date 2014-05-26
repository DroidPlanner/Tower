package org.droidplanner.android.proxy.mission.item.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import org.droidplanner.R;
import org.droidplanner.android.widgets.SeekBarWithText.SeekBarWithText;
import org.droidplanner.android.widgets.SeekBarWithText.SeekBarWithText
        .OnTextSeekBarChangedListener;
import org.droidplanner.core.helpers.units.Altitude;
import org.droidplanner.core.mission.MissionItemType;
import org.droidplanner.core.mission.waypoints.SplineWaypoint;
import org.droidplanner.core.mission.waypoints.Waypoint;

/**
 * This class renders the detail view for a spline waypoint mission item.
 */
public class MissionSplineWaypointFragment extends MissionDetailFragment implements
        OnTextSeekBarChangedListener, OnCheckedChangeListener {

    private SeekBarWithText altitudeSeekBar;
    private SeekBarWithText delaySeekBar;

    @Override
    protected int getResource() {
        return R.layout.fragment_editor_detail_spline_waypoint;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        typeSpinner.setSelection(commandAdapter
                .getPosition(MissionItemType.SPLINE_WAYPOINT));

        SplineWaypoint item = (SplineWaypoint) this.itemRender.getMissionItem();

        altitudeSeekBar = (SeekBarWithText) view.findViewById(R.id.altitudeView);
        altitudeSeekBar.setValue(item.getCoordinate().getAltitude().valueInMeters());
        altitudeSeekBar.setOnChangedListener(this);

        delaySeekBar = (SeekBarWithText) view.findViewById(R.id.waypointDelay);
        delaySeekBar.setValue(item.getDelay());
        delaySeekBar.setOnChangedListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
        onSeekBarChanged();
    }

    @Override
    public void onSeekBarChanged() {
        SplineWaypoint item = (SplineWaypoint) this.itemRender.getMissionItem();
        item.setAltitude(new Altitude(altitudeSeekBar.getValue()));
        item.setDelay((float) delaySeekBar.getValue());
        item.getMission().notifyMissionUpdate();
    }
}
