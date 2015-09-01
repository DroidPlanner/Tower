package org.droidplanner.android.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.MAVLink.common.msg_global_position_int;

import org.droidplanner.android.R;
import org.droidplanner.android.activities.LocatorActivity;
import org.droidplanner.android.view.adapterViews.LocatorItemAdapter;

public class LocatorListFragment extends Fragment {

    private RecyclerView recyclerView;
    private LocatorItemAdapter adapter;
    private OnLocatorListListener listener;

    public interface OnLocatorListListener {
        void onItemClick(msg_global_position_int message);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_locator_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = (RecyclerView) view.findViewById(R.id.locator_item_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        //use a linear layout manager
        final RecyclerView.LayoutManager recyclerLayoutMgr = new LinearLayoutManager(getActivity()
                .getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(recyclerLayoutMgr);

        final LocatorActivity activity = (LocatorActivity) getActivity();
        adapter = new LocatorItemAdapter(activity.getLastPositions(), listener);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof OnLocatorListListener)) {
            throw new IllegalStateException("Parent activity must implement " +
                    OnLocatorListListener.class.getName());
        }

        listener = (OnLocatorListListener) activity;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateViewVisibility();
    }

    public void notifyDataSetChanged() {
        adapter.clearSelection();
        adapter.notifyDataSetChanged();
        updateViewVisibility();
    }

    public void updateViewVisibility() {
        View view = getView();
        if (adapter != null && view != null) {
            if (adapter.getItemCount() > 0)
                view.setVisibility(View.VISIBLE);
            else
                view.setVisibility(View.INVISIBLE);
        }
    }
}