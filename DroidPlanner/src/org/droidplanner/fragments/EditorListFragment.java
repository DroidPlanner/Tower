package org.droidplanner.fragments;

import it.sephiroth.android.library.widget.AdapterView;
import it.sephiroth.android.library.widget.AdapterView.OnItemClickListener;
import it.sephiroth.android.library.widget.AdapterView.OnItemLongClickListener;
import it.sephiroth.android.library.widget.HListView;

import java.util.ArrayList;
import java.util.List;

import org.droidplanner.DroidPlannerApp;
import org.droidplanner.R;
import org.droidplanner.activities.helpers.OnEditorInteraction;
import org.droidplanner.drone.Drone;
import org.droidplanner.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.drone.variables.mission.Mission;
import org.droidplanner.drone.variables.mission.MissionItem;
import org.droidplanner.widgets.adapterViews.MissionItemView;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;

public class EditorListFragment extends Fragment implements  OnItemLongClickListener,  OnItemClickListener, OnDroneListener, OnClickListener{
	private HListView list;
	private Mission mission;
	private MissionItemView adapter;
	private OnEditorInteraction editorListener;
	private ImageButton leftArrow;
	private ImageButton rightArrow;
	private Drone drone;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_editor_list, container,
				false);
		list = (HListView) view.findViewById(R.id.mission_item_list);
		leftArrow = (ImageButton) view.findViewById(R.id.listLeftArrow);
		rightArrow = (ImageButton) view.findViewById(R.id.listRightArrow);
		leftArrow.setOnClickListener(this);
		rightArrow.setOnClickListener(this);

		drone = ((DroidPlannerApp) getActivity().getApplication()).drone;
		mission = drone.mission;
		adapter = new MissionItemView(this.getActivity(), android.R.layout.simple_list_item_1,mission.getItems());
		list.setOnItemClickListener(this);
		list.setOnItemLongClickListener(this);
		list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

		list.setAdapter(adapter);
		return view;
	}

	@Override
	public void onStart() {
		super.onStart();
        updateViewVisibility();
		drone.events.addDroneListener(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		drone.events.removeDroneListener(this);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		editorListener = (OnEditorInteraction) ( activity);
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		if (event == DroneEventsType.MISSION_UPDATE) {
			adapter.notifyDataSetChanged();
            updateViewVisibility();
		}
	}

    /**
     * Updates the fragment view visibility based on the count of stored mission items.
     */
    public void updateViewVisibility(){
    	View view = getView();
        if (adapter != null && view != null) {
            if (adapter.getCount() > 0)
                view.setVisibility(View.VISIBLE);
            else
                view.setVisibility(View.INVISIBLE);
            editorListener.onListVisibilityChanged();
        }
    }

	public void deleteSelected() {
		SparseBooleanArray selected = list.getCheckedItemPositions();
		ArrayList<MissionItem> toRemove = new ArrayList<MissionItem>();

		for( int i = 0; i < selected.size(); i++ ) {
			if( selected.valueAt( i ) ) {
				MissionItem item = adapter.getItem(selected.keyAt(i));
				toRemove.add(item);
			}
		}

		mission.removeWaypoints(toRemove);
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position,
			long id) {
		MissionItem missionItem = (MissionItem) adapter.getItemAtPosition(position);
		editorListener.onItemClick(missionItem);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> adapter, View view,
			int position, long id) {
		MissionItem missionItem = (MissionItem) adapter.getItemAtPosition(position);
		return editorListener.onItemLongClick(missionItem);
	}

	public void setArrowsVisibility(boolean visible) {
		if (visible) {
			leftArrow.setVisibility(View.VISIBLE);
			rightArrow.setVisibility(View.VISIBLE);
		}else{
			leftArrow.setVisibility(View.INVISIBLE);
			rightArrow.setVisibility(View.INVISIBLE);
		}
	}

    /**
     * Updates the choice mode of the listview containing the mission items.
     * @param choiceMode
     */
    public void updateChoiceMode(int choiceMode){
        switch(choiceMode){
            case ListView.CHOICE_MODE_SINGLE:
            case ListView.CHOICE_MODE_MULTIPLE:
                list.setChoiceMode(choiceMode);
                break;
        }
    }

    /**
     * Updates the selected mission items in the list.
     * @param selected list of selected mission items
     */
    public void updateMissionItemSelection(List<MissionItem> selected){
        list.clearChoices();
        for(MissionItem item: selected){
            list.setItemChecked(adapter.getPosition(item), true);
        }
        adapter.notifyDataSetChanged();
    }

	@Override
	public void onClick(View v) {
		if (v == leftArrow ) {
			mission.moveSelection(false);
	        adapter.notifyDataSetChanged();
	        updateMissionItemSelection(mission.getSelected());
		}
		if (v == rightArrow) {
			mission.moveSelection(true);
	        adapter.notifyDataSetChanged();
	        updateMissionItemSelection(mission.getSelected());
		}
	}

}
