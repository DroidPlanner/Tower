package org.droidplanner.android.wear.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.MAVLink.Messages.ApmModes;

import org.droidplanner.R;
import org.droidplanner.android.lib.parcelables.ParcelableApmMode;
import org.droidplanner.android.lib.utils.WearUtils;
import org.droidplanner.android.wear.services.DroidPlannerWearService;

import java.util.List;

/**
 * Allow to select the flight mode for the drone.
 */
public class FlightModesSelectionActivity extends Activity implements WearableListView
        .ClickListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actiivty_flight_modes_selection);

        handleIntent(getIntent());
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        ParcelableApmMode flightMode = null;
        if (intent != null) {
            flightMode = intent.getParcelableExtra(DroidPlannerWearService
                    .EXTRA_CURRENT_FLIGHT_MODE);
        }

        if (flightMode == null || flightMode.getApmMode() == null) {
            flightMode = new ParcelableApmMode(ApmModes.UNKNOWN);
        }

        ApmModes apmMode = flightMode.getApmMode();
        List<ApmModes> apmList = ApmModes.getModeList(apmMode.getType());

        WearableListView listView = (WearableListView) findViewById(R.id.list);
        listView.setAdapter(new Adapter(this, apmList));
        listView.setClickListener(this);
        RecyclerView.LayoutManager layoutMgr = listView.getLayoutManager();
        if(layoutMgr != null){
            int currentPos = apmList.indexOf(apmMode);
            if(currentPos != -1){
                layoutMgr.scrollToPosition(currentPos);
            }
        }
    }

    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {
        final ApmModes selected = (ApmModes) viewHolder.itemView.getTag();
        startService(new Intent(getApplicationContext(), DroidPlannerWearService.class)
                .setAction(WearUtils.SET_DRONE_FLIGHT_MODE_PATH)
                .putExtra(WearUtils.SET_DRONE_FLIGHT_MODE_PATH, new ParcelableApmMode(selected)));
        finish();
    }

    @Override
    public void onTopEmptyRegionClick() {}

    private static final class Adapter extends WearableListView.Adapter {

        private final LayoutInflater mInflater;
        private final List<ApmModes> mFlightModes;

        private Adapter(Context context, List<ApmModes> flightModes) {
            mInflater = LayoutInflater.from(context);
            mFlightModes = flightModes;
        }

        @Override
        public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new WearableListView.ViewHolder(
                    mInflater.inflate(R.layout.list_item_flight_mode, null));
        }


        @Override
        public void onBindViewHolder(WearableListView.ViewHolder holder, int position) {
            final ApmModes flightMode = mFlightModes.get(position);
            TextView view = (TextView) holder.itemView.findViewById(R.id.name);
            view.setText(flightMode.getName());
            holder.itemView.setTag(flightMode);
        }

        @Override
        public int getItemCount() {
            return mFlightModes.size();
        }
    }
}
