package org.droidplanner.android.view.adapterViews;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.droidplanner.android.R;
import org.droidplanner.android.fragments.account.DroneshareAccountFragment;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by Fredia Huya-Kouadio on 1/23/15.
 */
public class UserDataAdapter extends RecyclerView.Adapter<UserDataAdapter.ViewHolder> {

    private static final String TAG = UserDataAdapter.class.getSimpleName();

    public static class ViewHolder extends RecyclerView.ViewHolder {

        final TextView vehicleName;
        final TextView vehicleType;
        final TextView autopilot;
        final TextView missionInfo;

        public ViewHolder(View itemView, TextView vehicleName, TextView vehicleType, TextView autopilot,
                          TextView missionInfo) {
            super(itemView);
            this.vehicleName = vehicleName;
            this.vehicleType = vehicleType;
            this.autopilot = autopilot;
            this.missionInfo = missionInfo;
        }
    }

    private final Context context;
    private JSONArray userVehicleData;

    public UserDataAdapter(Context context) {
        this.context = context;
        userVehicleData = new JSONArray();
    }

    public void updateUserData(JSONObject userData){
        JSONArray temp = userData == null ? null : userData.optJSONArray("vehicles");
        if(temp == null)
            return;

        userVehicleData = temp;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_vehicle_info, parent,
                false);

        final TextView vehicleName = (TextView) view.findViewById(R.id.vehicle_name);
        final TextView vehicleType = (TextView) view.findViewById(R.id.vehicle_type);
        final TextView autopilot = (TextView) view.findViewById(R.id.vehicle_autopilot);
        final TextView missionInfo = (TextView) view.findViewById(R.id.vehicle_mission_info);

        return new ViewHolder(view, vehicleName, vehicleType, autopilot, missionInfo);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final JSONObject vehicleData = userVehicleData.optJSONObject(position);
        if (vehicleData == null)
            return;

        final String vehicleType = vehicleData.optString("vehicleType", "");
        holder.vehicleType.setText("Type: " + vehicleType);

        String vehicleName = vehicleData.optString("name", "");
        if(vehicleName.isEmpty())
            vehicleName = vehicleType;
        holder.vehicleName.setText(vehicleName);

        final String vehicleId = vehicleData.optString("id", null);
        if(vehicleId != null) {
            holder.vehicleName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String vehicleInfoUrl = DroneshareAccountFragment.DRONESHARE_URL + "vehicle/" + vehicleId;
                    context.startActivity(new Intent(Intent.ACTION_VIEW)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            .setData(Uri.parse(vehicleInfoUrl)));
                }
            });
        }

        holder.autopilot.setText("Autopilot: " + vehicleData.optString("autopilotType", ""));

        JSONArray missions = vehicleData.optJSONArray("missions");
        int missionsCount = 0;
        String missionDesc = " mission flown";
        if(missions != null) {
            missionsCount = missions.length();
            if(missionsCount > 1)
                missionDesc = " missions flown";
        }
        holder.missionInfo.setText(missionsCount + missionDesc);
    }

    @Override
    public int getItemCount() {
        return userVehicleData.length();
    }
}
