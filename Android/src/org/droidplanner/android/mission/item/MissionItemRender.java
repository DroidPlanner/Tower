package org.droidplanner.android.mission.item;

import android.view.View;

import org.droidplanner.android.fragments.mission.MissionDetailFragment;
import org.droidplanner.android.graphic.map.MarkerManager;
import org.droidplanner.core.mission.MissionItem;

/**
 * This class is responsible for providing logic to access and interpret the
 * {@link org.droidplanner.core.mission.MissionItem} class on the Android layer,
 * as well as providing methods for rendering it on the Android UI.
 */
public class MissionItemRender implements Comparable<MissionItemRender>{

    /**
     * This is the mission item object this class is built around.
     */
    private final MissionItem mMissionItem;

    public MissionItemRender(MissionItem missionItem){
        mMissionItem = missionItem;
    }

    /**
     * Provides access to the mission item instance.
     * @return {@link org.droidplanner.core.mission.MissionItem} object
     */
    public MissionItem getMissionItem(){
        return mMissionItem;
    }

    public MissionDetailFragment getDetailFragment() {
        return null;
    }

    public MarkerManager.MarkerSource getMarkerSource() {
        return null;
    }

    public View getListViewItemView() {
        return null;
    }

    @Override
    public int compareTo(MissionItemRender another){
        return mMissionItem.compareTo(another.mMissionItem);
    }
}
