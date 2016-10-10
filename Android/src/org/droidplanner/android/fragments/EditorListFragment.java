package org.droidplanner.android.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.o3dr.services.android.lib.drone.mission.MissionItemType;

import org.droidplanner.android.R;
import org.droidplanner.android.activities.interfaces.OnEditorInteraction;
import org.droidplanner.android.fragments.helpers.ApiListenerFragment;
import org.droidplanner.android.proxy.mission.MissionProxy;
import org.droidplanner.android.proxy.mission.MissionSelection;
import org.droidplanner.android.proxy.mission.item.MissionItemProxy;
import org.droidplanner.android.view.adapterViews.MissionItemListAdapter;

import java.util.List;

public class EditorListFragment extends ApiListenerFragment implements MissionSelection.OnSelectionUpdateListener {

    private static final long MISSION_UPDATE_BROADCAST_DELAY = 250L; //ms

    private final static IntentFilter eventFilter = new IntentFilter(MissionProxy.ACTION_MISSION_PROXY_UPDATE);

    private final BroadcastReceiver eventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            recyclerAdapter.notifyDataSetChanged();
            updateViewVisibility();
        }
    };

    private final Runnable broadcastMissionUpdate = new Runnable() {
        @Override
        public void run() {
            if (missionProxy != null) {
                missionProxy.selection.notifySelectionUpdate();
                missionProxy.notifyMissionUpdate();
            }
            handler.removeCallbacks(this);
        }
    };

    private final Handler handler = new Handler();

    private MissionProxy missionProxy;
    private OnEditorInteraction editorListener;

    private RecyclerView recyclerView;
    private MissionItemListAdapter recyclerAdapter;

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

        recyclerView = (RecyclerView) view.findViewById(R.id.mission_item_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        final RecyclerView.LayoutManager recyclerLayoutMgr = new LinearLayoutManager(getActivity()
                .getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(recyclerLayoutMgr);

        ItemTouchHelper itHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT,
            ItemTouchHelper.UP|ItemTouchHelper.DOWN) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                recyclerAdapter.swap(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                // Item was deleted, so let's remove it.
                int deletedPosition = viewHolder.getAdapterPosition();
                recyclerAdapter.dismiss(deletedPosition);
            }

            @Override
            public boolean canDropOver(RecyclerView recyclerView, RecyclerView.ViewHolder current, RecyclerView.ViewHolder target){
                // Retrieve the matching mission item
                int currentPosition = current.getAdapterPosition();
                MissionItemType currentMissionItemType = missionProxy.getItems().get(currentPosition).getMissionItem().getType();

                int targetPosition = target.getAdapterPosition();
                MissionItemType targetMissionItemType = missionProxy.getItems().get(targetPosition).getMissionItem().getType();

                return currentMissionItemType != MissionItemType.TAKEOFF
                    && currentMissionItemType != MissionItemType.LAND
                    && currentMissionItemType != MissionItemType.RETURN_TO_LAUNCH
                    && targetMissionItemType != MissionItemType.TAKEOFF
                    && targetMissionItemType != MissionItemType.LAND
                    && targetMissionItemType != MissionItemType.RETURN_TO_LAUNCH;
            }

            @Override
            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
                super.onSelectedChanged(viewHolder, actionState);
                switch(actionState){
                    case ItemTouchHelper.ACTION_STATE_DRAG:
                    case ItemTouchHelper.ACTION_STATE_SWIPE:
                        handler.removeCallbacks(broadcastMissionUpdate);
                        break;
                }
            }

            @Override
            public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                handler.postDelayed(broadcastMissionUpdate, MISSION_UPDATE_BROADCAST_DELAY);
            }
        });
        itHelper.attachToRecyclerView(recyclerView);
    }

    public void enableDeleteMode(boolean isEnabled){
        if(isEnabled)
            recyclerView.setBackgroundResource(android.R.color.holo_red_light);
        else
            recyclerView.setBackgroundResource(android.R.color.transparent);
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

        handler.removeCallbacksAndMessages(null);
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
