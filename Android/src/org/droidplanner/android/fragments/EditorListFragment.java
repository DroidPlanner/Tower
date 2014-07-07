package org.droidplanner.android.fragments;

import it.sephiroth.android.library.widget.AdapterView;
import it.sephiroth.android.library.widget.AdapterView.OnItemClickListener;
import it.sephiroth.android.library.widget.AdapterView.OnItemLongClickListener;
import it.sephiroth.android.library.widget.HListView;

import java.util.List;

import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.activities.interfaces.OnEditorInteraction;
import org.droidplanner.android.proxy.mission.MissionSelection;
import org.droidplanner.android.proxy.mission.item.MissionItemProxy;
import org.droidplanner.android.proxy.mission.MissionProxy;
import org.droidplanner.android.widgets.adapterViews.MissionItemProxyView;
import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;

public class EditorListFragment extends Fragment implements
		OnItemLongClickListener, OnItemClickListener, OnDroneListener,
		OnClickListener, MissionSelection.OnSelectionUpdateListener {

	private HListView list;
	private MissionProxy missionProxy;
	private MissionItemProxyView adapter;
	private OnEditorInteraction editorListener;
	private ImageButton leftArrow;
	private ImageButton rightArrow;
	private Drone drone;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_editor_list, container,	false);

        DroidPlannerApp app = ((DroidPlannerApp) getActivity().getApplication());
        drone = app.getDrone();
        missionProxy = app.missionProxy;
        adapter = new MissionItemProxyView(getActivity(), missionProxy.getItems());

		list = (HListView) view.findViewById(R.id.mission_item_list);
        list.setOnItemClickListener(this);
        list.setOnItemLongClickListener(this);
        list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        list.setAdapter(adapter);

		leftArrow = (ImageButton) view.findViewById(R.id.listLeftArrow);
		rightArrow = (ImageButton) view.findViewById(R.id.listRightArrow);
		leftArrow.setOnClickListener(this);
		rightArrow.setOnClickListener(this);

		return view;
	}

	@Override
	public void onStart() {
		super.onStart();
		updateViewVisibility();
		drone.events.addDroneListener(this);
        missionProxy.selection.addSelectionUpdateListener(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		drone.events.removeDroneListener(this);
        missionProxy.selection.removeSelectionUpdateListener(this);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		editorListener = (OnEditorInteraction) (activity);
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		if (event == DroneEventsType.MISSION_UPDATE) {
			adapter.notifyDataSetChanged();
			updateViewVisibility();
		}
	}

	/**
	 * Updates the fragment view visibility based on the count of stored mission
	 * items.
	 */
	public void updateViewVisibility() {
		View view = getView();
		if (adapter != null && view != null) {
			if (adapter.getCount() > 0)
				view.setVisibility(View.VISIBLE);
			else
				view.setVisibility(View.INVISIBLE);
			editorListener.onListVisibilityChanged();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
		MissionItemProxy missionItem = (MissionItemProxy) adapter.getItemAtPosition(position);
		editorListener.onItemClick(missionItem);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> adapter, View view, int position, long id) {
		MissionItemProxy missionItem = (MissionItemProxy) adapter.getItemAtPosition(position);
		return editorListener.onItemLongClick(missionItem);
	}

	public void setArrowsVisibility(boolean visible) {
		if (visible) {
			leftArrow.setVisibility(View.VISIBLE);
			rightArrow.setVisibility(View.VISIBLE);
		} else {
			leftArrow.setVisibility(View.INVISIBLE);
			rightArrow.setVisibility(View.INVISIBLE);
		}
	}

	/**
	 * Updates the choice mode of the listview containing the mission items.
	 * 
	 * @param choiceMode
	 */
	public void updateChoiceMode(int choiceMode) {
		switch (choiceMode) {
		case ListView.CHOICE_MODE_SINGLE:
		case ListView.CHOICE_MODE_MULTIPLE:
			list.setChoiceMode(choiceMode);
			break;
		}
	}

	@Override
	public void onClick(View v) {
		if (v == leftArrow) {
			missionProxy.moveSelection(false);
			adapter.notifyDataSetChanged();
		}
		if (v == rightArrow) {
			missionProxy.moveSelection(true);
			adapter.notifyDataSetChanged();
		}
	}

    @Override
    public void onSelectionUpdate(List<MissionItemProxy> selected) {
        list.clearChoices();
        for (MissionItemProxy item : selected) {
            list.setItemChecked(adapter.getPosition(item), true);
        }
        adapter.notifyDataSetChanged();
    }
}
