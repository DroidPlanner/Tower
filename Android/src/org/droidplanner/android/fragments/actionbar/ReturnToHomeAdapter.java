package org.droidplanner.android.fragments.actionbar;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.o3dr.android.client.Drone;
import com.o3dr.android.client.apis.VehicleApi;
import com.o3dr.services.android.lib.coordinate.LatLongAlt;
import com.o3dr.services.android.lib.drone.attribute.AttributeType;
import com.o3dr.services.android.lib.gcs.returnToMe.ReturnToMeState;
import com.o3dr.services.android.lib.model.AbstractCommandListener;

import org.droidplanner.android.R;
import org.droidplanner.android.dialogs.SelectionListDialog;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;

import timber.log.Timber;

/**
 * Created by Fredia Huya-Kouadio on 9/25/15.
 */
class ReturnToHomeAdapter extends SelectionListAdapter<Integer> {

    @StringRes
    private final int[] rthLabels = {
            R.string.label_rtl,
            R.string.label_rtm
    };

    @DrawableRes
    private final int[] rthIcons = {
            R.drawable.ic_home_grey_700_18dp,
            R.drawable.ic_person_grey_700_18dp
    };

    private int selectedLabel = 0;

    private final Context context;
    private final Drone drone;
    private final DroidPlannerPrefs dpPrefs;

    public ReturnToHomeAdapter(Context context, Drone drone, DroidPlannerPrefs dpPrefs) {
        super(context);
        this.context = context;
        this.drone = drone;
        this.dpPrefs = dpPrefs;

        selectedLabel = dpPrefs.isReturnToMeEnabled() ? 1 : 0;
    }

    @Override
    public int getCount() {
        return rthLabels.length;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent){
        if(convertView == null){
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_selection, parent, false);
        }

        ViewHolder holder = (ViewHolder) convertView.getTag();
        if(holder == null) {
            holder = new ViewHolder((TextView) convertView.findViewById(R.id.item_selectable_option),
                    (RadioButton) convertView.findViewById(R.id.item_selectable_check));
        }

        final View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedLabel = position;

                final boolean isReturnToMeEnabled = position == 1;
                dpPrefs.enableReturnToMe(isReturnToMeEnabled);
                if (isReturnToMeEnabled) {
                    //Start return to me
                    VehicleApi.getApi(drone).enableReturnToMe(true, new AbstractCommandListener() {
                        @Override
                        public void onSuccess() {
                            Timber.i("Started return to me.");
                            Toast.makeText(context, "Return to me started", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(int i) {
                            Timber.e("Unable to start return to me.");
                        }

                        @Override
                        public void onTimeout() {
                            Timber.w("Starting return to me timed out.");
                        }
                    });
                } else {
                    final ReturnToMeState rtmState = drone.getAttribute(AttributeType.RETURN_TO_ME_STATE);
                    final LatLongAlt originalHome = rtmState == null ? null : rtmState.getOriginalHomeLocation();

                    final VehicleApi vehicleApi = VehicleApi.getApi(drone);
                    //Stop return to me.
                    vehicleApi.enableReturnToMe(false, new AbstractCommandListener() {
                        @Override
                        public void onSuccess() {
                            Timber.i("Stopped return to me.");
                            Toast.makeText(context, "Return to me stopped.", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(int i) {
                            Timber.e("Unable to stop return to me.");
                        }

                        @Override
                        public void onTimeout() {
                            Timber.w("Stopping return to me timed out.");
                        }
                    });

                    //Set home position back to its original
                    if (originalHome != null) {
                        vehicleApi.setVehicleHome(originalHome, new AbstractCommandListener() {
                            @Override
                            public void onSuccess() {
                                Timber.i("Restored original home location.");
                                Toast.makeText(context, "Restored original home location", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onError(int i) {
                                Timber.e("Unable to restore original home location.");
                            }

                            @Override
                            public void onTimeout() {
                                Timber.w("Timed out while attempting to restore the home location.");
                            }
                        });
                    }
                }

                if(listener != null)
                    listener.onSelection();
            }
        };

        holder.rthCheck.setChecked(position == selectedLabel);
        holder.rthCheck.setOnClickListener(clickListener);

        holder.rthOption.setText(rthLabels[position]);
        holder.rthOption.setOnClickListener(clickListener);
        holder.rthOption.setCompoundDrawablesWithIntrinsicBounds(rthIcons[position], 0, 0, 0);

        convertView.setOnClickListener(clickListener);
        convertView.setTag(holder);
        return convertView;
    }

    @Override
    public int getSelection(){
        return selectedLabel;
    }

    public static class ViewHolder {

        final TextView rthOption;
        final RadioButton rthCheck;

        public ViewHolder(TextView rthView, RadioButton check) {
            this.rthOption = rthView;
            this.rthCheck = check;
        }
    }
}
