package org.droidplanner.android.view.adapterViews;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.MAVLink.common.msg_global_position_int;

import org.droidplanner.android.R;
import org.droidplanner.android.fragments.LocatorListFragment;
import org.droidplanner.android.utils.file.IO.TLogReader;
import org.droidplanner.android.utils.file.IO.TLogReader.Event;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * MissionItem Adapter for the MissionItem horizontal list view. This adapter
 * updates the content of the list view item's view based on the mission item
 * type.
 */
public class LocatorItemAdapter extends RecyclerView.Adapter<LocatorItemAdapter.ViewHolder> {

    // Provide a reference to the views for each data item
    public static class ViewHolder extends RecyclerView.ViewHolder {

        final View viewContainer;
        final TextView timeView;

        public ViewHolder(View container, TextView timeView) {
            super(container);
            this.viewContainer = container;
            this.timeView = timeView;
        }
    }

    private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.US);

    private int selectedPosition = -1;
    private final List<Event> msgList;
    private final LocatorListFragment.OnLocatorListListener listener;

    public LocatorItemAdapter(List<TLogReader.Event> list, LocatorListFragment.OnLocatorListListener listener) {
        this.msgList = list;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_locator_list_item, parent, false);

        final TextView timeView = (TextView) view.findViewById(R.id.timeView);

        return new ViewHolder(view, timeView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final Event msgEvent = msgList.get(position);

        final View container = holder.viewContainer;
        container.setActivated(isSelected(position));
        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelection(position);
                if (listener != null) {
                    if (isSelected(position)) {
                        listener.onItemClick((msg_global_position_int) msgEvent.getMavLinkMessage());
                    } else {
                        listener.onItemClick(null);
                    }
                }
            }
        });

        Date eventDate = new Date(msgEvent.getTimestamp());
        holder.timeView.setText(sdf.format(eventDate));
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