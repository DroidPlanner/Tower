package org.droidplanner.android.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;

import com.MAVLink.Messages.ardupilotmega.msg_global_position_int;

import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.activities.LocatorActivity;
import org.droidplanner.android.activities.interfaces.OnEditorInteraction;
import org.droidplanner.android.activities.interfaces.OnLocatorListListener;
import org.droidplanner.android.proxy.mission.MissionProxy;
import org.droidplanner.android.proxy.mission.MissionSelection;
import org.droidplanner.android.proxy.mission.item.MissionItemProxy;
import org.droidplanner.android.widgets.adapterViews.LocatorItemAdapter;
import org.droidplanner.android.widgets.adapterViews.MissionItemProxyView;
import org.droidplanner.core.drone.Drone;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;

import java.util.List;

import it.sephiroth.android.library.widget.AdapterView;
import it.sephiroth.android.library.widget.AdapterView.OnItemClickListener;
import it.sephiroth.android.library.widget.AdapterView.OnItemLongClickListener;
import it.sephiroth.android.library.widget.HListView;

public class LocatorListFragment extends Fragment implements OnItemClickListener {

	private HListView list;
	private LocatorItemAdapter adapter;
    private OnLocatorListListener listener;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_locator_list, container,	false);

        final LocatorActivity activity = (LocatorActivity) getActivity();
        adapter = new LocatorItemAdapter(activity, activity.getLastPositions());

		list = (HListView) view.findViewById(R.id.locator_item_list);
        list.setOnItemClickListener(this);
        list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        list.setAdapter(adapter);

		return view;
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
        listener = (OnLocatorListListener) activity;
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
		final msg_global_position_int message = (msg_global_position_int) adapter.getItemAtPosition(position);
		listener.onItemClick(message);
	}

    public void notifyDataSetChanged() {
        list.clearChoices();
        adapter.notifyDataSetChanged();
    }
}
