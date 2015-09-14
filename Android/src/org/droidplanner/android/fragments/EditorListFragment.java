package org.droidplanner.android.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.droidplanner.android.R;
import org.droidplanner.android.activities.interfaces.OnEditorInteraction;
import org.droidplanner.android.fragments.helpers.ApiListenerFragment;
import org.droidplanner.android.proxy.mission.MissionProxy;
import org.droidplanner.android.proxy.mission.MissionSelection;
import org.droidplanner.android.proxy.mission.item.MissionItemProxy;
import org.droidplanner.android.utils.ReorderRecyclerView;
import org.droidplanner.android.view.adapterViews.MissionItemListAdapter;

import java.util.List;

public class EditorListFragment extends ApiListenerFragment implements MissionSelection.OnSelectionUpdateListener {

    private final static IntentFilter eventFilter = new IntentFilter(MissionProxy.ACTION_MISSION_PROXY_UPDATE);

    private final BroadcastReceiver eventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            recyclerAdapter.notifyDataSetChanged();
            updateViewVisibility();
        }
    };

    private MissionProxy missionProxy;
    private OnEditorInteraction editorListener;

    private ReorderRecyclerView recyclerView;
    private ReorderRecyclerView.Adapter recyclerAdapter;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof OnEditorInteraction)) {
            throw new IllegalStateException("Parent activity must implement " +
                    OnEditorInteraction.class.getName());
        }

        editorListener = (OnEditorInteraction) (activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_editor_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = (ReorderRecyclerView) view.findViewById(R.id.mission_item_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        //use a linear layout manager
        final RecyclerView.LayoutManager recyclerLayoutMgr = new LinearLayoutManager(getActivity()
                .getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(recyclerLayoutMgr);

    }

    public void enableDeleteMode(boolean isEnabled){
        if(isEnabled)
            recyclerView.setBackgroundResource(android.R.color.holo_red_light);
        else
            recyclerView.setBackgroundResource(R.color.editor_bar);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateViewVisibility();
    }

    @Override
    public void onApiConnected() {
        missionProxy = getMissionProxy();

        recyclerAdapter = new MissionItemListAdapter(getContext(), missionProxy, editorListener);
        recyclerView.setAdapter(recyclerAdapter);

        missionProxy.selection.addSelectionUpdateListener(this);
        getBroadcastManager().registerReceiver(eventReceiver, eventFilter);
    }

    @Override
    public void onApiDisconnected() {
        getBroadcastManager().unregisterReceiver(eventReceiver);
        if (missionProxy != null)
            missionProxy.selection.removeSelectionUpdateListener(this);
    }

    /**
     * Updates the fragment view visibility based on the count of stored mission
     * items.
     */
    public void updateViewVisibility() {
        View view = getView();
        if (recyclerAdapter != null && view != null) {
            if (recyclerAdapter.getItemCount() > 0)
                view.setVisibility(View.VISIBLE);
            else
                view.setVisibility(View.INVISIBLE);
            editorListener.onListVisibilityChanged();
        }
    }

    @Override
    public void onSelectionUpdate(List<MissionItemProxy> selected) {
        recyclerAdapter.notifyDataSetChanged();
    }
}
