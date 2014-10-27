package org.droidplanner.android.fragments;

import it.sephiroth.android.library.widget.AdapterView;
import it.sephiroth.android.library.widget.AdapterView.OnItemClickListener;
import it.sephiroth.android.library.widget.AdapterView.OnItemLongClickListener;
import it.sephiroth.android.library.widget.HListView;

import java.util.List;

import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.activities.interfaces.OnEditorInteraction;
import org.droidplanner.android.api.services.DroidPlannerApi;
import org.droidplanner.android.helpers.ApiInterface;
import org.droidplanner.android.proxy.mission.MissionProxy;
import org.droidplanner.android.proxy.mission.MissionSelection;
import org.droidplanner.android.proxy.mission.item.MissionItemProxy;
import org.droidplanner.android.widgets.adapterViews.MissionItemProxyView;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.core.model.Drone;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageButton;

public class EditorListFragment extends Fragment implements OnItemLongClickListener,
		OnItemClickListener, OnDroneListener, MissionSelection.OnSelectionUpdateListener,
        ApiInterface.Subscriber {

	private HListView list;
	private MissionProxy missionProxy;
	private MissionItemProxyView adapter;
	private OnEditorInteraction editorListener;
	private Drone drone;
    private DroidPlannerApi dpApi;

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        if(!(activity instanceof ApiInterface.Provider)){
            throw new IllegalStateException("Parent activity must implement " +
                    ApiInterface.Provider.class.getName());
        }

        if(!(activity instanceof OnEditorInteraction)){
            throw new IllegalStateException("Parent activity must implement " +
                    OnEditorInteraction.class.getName());
        }

        editorListener = (OnEditorInteraction) (activity);
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_editor_list, container, false);

		list = (HListView) view.findViewById(R.id.mission_item_list);
		list.setOnItemClickListener(this);
		list.setOnItemLongClickListener(this);
		list.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);

		return view;
	}

	@Override
	public void onStart() {
		super.onStart();
		updateViewVisibility();

        DroidPlannerApi api = ((ApiInterface.Provider)getActivity()).getApi();
        onApiConnected(api);
	}

	@Override
	public void onStop() {
		super.onStop();
		onApiDisconnected();
	}

    @Override
    public void onApiConnected(DroidPlannerApi api) {
        if(dpApi != null || api == null) return;

        dpApi = api;

        drone = dpApi.getDrone();
        missionProxy = dpApi.getMissionProxy();

        adapter = new MissionItemProxyView(getActivity(), missionProxy.getItems());
        list.setAdapter(adapter);

        dpApi.addDroneListener(this);
        missionProxy.selection.addSelectionUpdateListener(this);
    }

    @Override
    public void onApiDisconnected() {
        if(dpApi == null) return;

        drone.removeDroneListener(this);
        missionProxy.selection.removeSelectionUpdateListener(this);
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
		editorListener.onItemClick(missionItem, true);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> adapter, View view, int position, long id) {
		MissionItemProxy missionItem = (MissionItemProxy) adapter.getItemAtPosition(position);
		return editorListener.onItemLongClick(missionItem);
	}

	/**
	 * Updates the choice mode of the listview containing the mission items.
	 * 
	 * @param choiceMode
	 */
	public void updateChoiceMode(int choiceMode) {
		switch (choiceMode) {
		case AbsListView.CHOICE_MODE_SINGLE:
		case AbsListView.CHOICE_MODE_MULTIPLE:
			list.setChoiceMode(choiceMode);
			break;
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
