package org.droidplanner.android.mission.item.fragments;

import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import org.droidplanner.R;
import org.droidplanner.android.widgets.SeekBarWithText.SeekBarWithText;
import org.droidplanner.core.mission.MissionItemType;
import org.droidplanner.core.mission.waypoints.Loiter;
import org.droidplanner.core.mission.waypoints.LoiterTurns;

public class MissionLoiterNFragment extends MissionDetailFragment implements
        SeekBarWithText.OnTextSeekBarChangedListener, OnCheckedChangeListener {


    private SeekBarWithText altitudeSeekBar;
    private SeekBarWithText loiterTurnSeekBar;
    private SeekBarWithText loiterRadiusSeekBar;
    private CheckBox loiterCCW;
    private SeekBarWithText yawSeekBar;

    @Override
    protected int getResource() {
        return R.layout.fragment_editor_detail_loitern;
    }

    @Override
    protected void setupViews(View view) {
        super.setupViews(view);
        typeSpinner.setSelection(commandAdapter.getPosition(MissionItemType.LOITERN));

        LoiterTurns item = (LoiterTurns) this.itemRender.getMissionItem();

        loiterCCW = (CheckBox) view.findViewById(R.string.loiter_ccw);
        loiterCCW.setChecked(item.isOrbitCCW());
        loiterCCW.setOnCheckedChangeListener(this);

        altitudeSeekBar = (SeekBarWithText) view.findViewById(R.id.altitudeView);
        altitudeSeekBar.setValue(item.getCoordinate().getAltitude().valueInMeters());
        altitudeSeekBar.setOnChangedListener(this);

        loiterTurnSeekBar = (SeekBarWithText) view.findViewById(R.id.loiterTurn);
        loiterTurnSeekBar.setOnChangedListener(this);
        loiterTurnSeekBar.setValue(item.getTurns());

        loiterRadiusSeekBar = (SeekBarWithText) view.findViewById(R.id.loiterRadius);
        loiterRadiusSeekBar.setAbsValue(item.getOrbitalRadius());
        loiterRadiusSeekBar.setOnChangedListener(this);

        //yawSeekBar = (SeekBarWithText) view.findViewById(R.id.waypointAngle);
        //yawSeekBar.setValue(item.getYawAngle());
        //yawSeekBar.setOnChangedListener(this);
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        ((Loiter) itemRender.getMissionItem()).setOrbitCCW(isChecked);
    }


    @Override
    public void onSeekBarChanged() {
        LoiterTurns item = (LoiterTurns) this.itemRender.getMissionItem();

        item.getCoordinate().getAltitude().set(altitudeSeekBar.getValue());
        item.setTurns((int) loiterTurnSeekBar.getValue());
        item.setOrbitalRadius(loiterRadiusSeekBar.getValue());
        //item.setYawAngle(yawSeekBar.getValue());
    }

}
