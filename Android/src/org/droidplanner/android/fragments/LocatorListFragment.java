package org.droidplanner.android.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.o3dr.services.android.lib.drone.mission.item.raw.GlobalPositionIntMessage;

import org.droidplanner.R;
import org.droidplanner.android.activities.LocatorActivity;
import org.droidplanner.android.widgets.adapterViews.LocatorItemAdapter;

import it.sephiroth.android.library.widget.AdapterView;
import it.sephiroth.android.library.widget.AdapterView.OnItemClickListener;
import it.sephiroth.android.library.widget.HListView;

public class LocatorListFragment extends Fragment implements OnItemClickListener {

	private HListView list;
	private LocatorItemAdapter adapter;
	private OnLocatorListListener listener;

	public interface OnLocatorListListener {
		void onItemClick(GlobalPositionIntMessage message);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_locator_list, container, false);

		final LocatorActivity activity = (LocatorActivity) getActivity();
		adapter = new LocatorItemAdapter(activity, activity.getLastPositions());

		list = (HListView) view.findViewById(R.id.locator_item_list);
		list.setOnItemClickListener(this);
		list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		list.setAdapter(adapter);

		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		listener = (OnLocatorListListener) activity;
	}

    @Override
    public void onStart(){
        super.onStart();
        updateViewVisibility();
    }

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
		final GlobalPositionIntMessage message = (GlobalPositionIntMessage) adapter
				.getItemAtPosition(position);
		listener.onItemClick(message);
	}

	public void notifyDataSetChanged() {
		list.clearChoices();
		adapter.notifyDataSetChanged();
        updateViewVisibility();
	}

    public void updateViewVisibility() {
        View view = getView();
        if (adapter != null && view != null) {
            if (adapter.getCount() > 0)
                view.setVisibility(View.VISIBLE);
            else
                view.setVisibility(View.INVISIBLE);
        }
    }
}