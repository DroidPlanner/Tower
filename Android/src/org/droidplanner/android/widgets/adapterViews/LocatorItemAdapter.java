package org.droidplanner.android.widgets.adapterViews;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.MAVLink.common.msg_global_position_int;

import org.droidplanner.android.R;
import org.droidplanner.android.fragments.LocatorListFragment;

import java.util.List;

/**
 * MissionItem Adapter for the MissionItem horizontal list view. This adapter
 * updates the content of the list view item's view based on the mission item
 * type.
 */
public class LocatorItemAdapter extends RecyclerView.Adapter<LocatorItemAdapter.ViewHolder> {

    // Provide a reference to the views for each data item
    public static class ViewHolder extends RecyclerView.ViewHolder {

        final View viewContainer;
        final TextView titleView;
        final TextView timeView;

        public ViewHolder(View container, TextView titleView, TextView timeView) {
            super(container);
            this.viewContainer = container;
            this.titleView = titleView;
            this.timeView = timeView;
        }
    }

    private int selectedPosition = -1;
    private final List<msg_global_position_int> msgList;
    private final LocatorListFragment.OnLocatorListListener listener;

    public LocatorItemAdapter(List<msg_global_position_int> list, LocatorListFragment.OnLocatorListListener listener) {
        this.msgList = list;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout
                .fragment_locator_list_item, parent, false);

        final TextView titleView = (TextView) view.findViewById(R.id.titleView);
        final TextView timeView = (TextView) view.findViewById(R.id.timeView);

        return new ViewHolder(view, titleView, timeView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final msg_global_position_int msg = msgList.get(position);

        final View container = holder.viewContainer;
        container.setActivated(isSelected(position));
        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelection(position);
                if (listener != null) {
                    if (isSelected(position)) {
                        listener.onItemClick(msg);
                    } else {
                            listener.onItemClick(null);
                    }
                }
            }
        });

        holder.titleView.setText(String.valueOf(position + 1));

        final int min = msg.time_boot_ms / 60000;
        final int sec = (msg.time_boot_ms % 60000) / 1000;
        final int fsec = (msg.time_boot_ms % 1000) / 100;
        holder.timeView.setText(String.format("%dm%02d.%01ds", min, sec, fsec));
    }

    @Override
    public int getItemCount() {
        return msgList.size();
    }

    private void setSelection(int selection) {
        if (selection == selectedPosition)
            clearSelection();
        else
            selectedPosition = selection;
        notifyDataSetChanged();
    }

    private boolean isSelected(int position) {
        return selectedPosition == position;
    }

    public void clearSelection() {
        selectedPosition = -1;
    }
}