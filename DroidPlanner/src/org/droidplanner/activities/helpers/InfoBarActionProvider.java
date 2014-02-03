package org.droidplanner.activities.helpers;

import android.content.Context;
import android.os.Handler;
import android.view.ActionProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import com.MAVLink.Messages.ApmModes;

import org.droidplanner.R;
import org.droidplanner.drone.Drone;
import org.droidplanner.drone.DroneInterfaces;
import org.droidplanner.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.widgets.spinners.ModeAdapter;

import java.util.Collections;
import java.util.List;

/**
 * This implements the info bar displayed on the action bar after connection with the drone.
 * <p/>
 * <b>Note:</b> The parent activity must add instantiations of this class to the list of
 * DroneEvent listeners.
 */
public class InfoBarActionProvider extends ActionProvider implements OnDroneListener {

    /**
     * Set of actions supported by the info bar
     */
    public enum InfoBarItem {

        /**
         * Home info bar item: displays the distance of the drone from its home location.
         */
        HOME(R.id.bar_home){
            @Override
            public void updateItemView(final Context context, final Drone drone){
                if(mItemView != null){
                    String update = drone == null
                            ? "--"
                            : String.format("Home\n%s", drone.home.getDroneDistanceToHome().toString());
                    ((TextView)mItemView).setText(update);
                }
            }
        },

        /**
         * GPS info bar item: displays the count of satellites, and other gps information.
         */
        GPS(R.id.bar_gps){
            @Override
            public void updateItemView(final Context context, final Drone drone){
                if(mItemView != null){
                    String update = drone == null
                            ? "--"
                            : String.format("Satellite\n%d, %s", drone.GPS.getSatCount(),
                            drone.GPS.getFixType());

                    ((TextView)mItemView).setText(update);
                }
            }
        },

        /**
         * Flight time info bar item: displays the amount of time the drone is armed.
         */
        FLIGHT_TIME(R.id.bar_propeller) {

            /**
             * This is the period for the flight time update.
             */
            private final static long FLIGHT_TIMER_PERIOD = 1000l; //1 second

            /**
             * This is the layout resource id for the popup window.
             */
            private static final int sPopupWindowLayoutId = R.layout.popup_info_flight_time;

            /**
             * This popup is used to offer the user the option to reset the flight time.
             */
            private PopupWindow mPopup;

            /**
             * This handler is used to update the flight time value.
             */
            private final Handler mHandler = new Handler();

            @Override
            public  void initItemView(final Context context, View parentView, final Drone drone) {
                super.initItemView(context, parentView, drone);
                mPopup = initPopupWindow(context, sPopupWindowLayoutId);
                updateItemView(context, drone);
            }

            @Override
            public void updateItemView(final Context context, final Drone drone){
                mHandler.removeCallbacksAndMessages(null);

                if(mItemView != null){
                    mItemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mPopup == null)
                                return;

                            final View popupView = mPopup.getContentView();
                            popupView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (drone != null) {
                                        drone.state.resetFlightTimer();
                                    }
                                    mPopup.dismiss();
                                }
                            });

                            mPopup.showAsDropDown(mItemView);
                        }
                    });

                    if(drone != null){
                        new Runnable(){
                            @Override
                            public void run(){
                                mHandler.removeCallbacks(this);
                                if(drone == null)
                                    return;

                                if(mItemView != null){
                                    long timeInSeconds = drone.state.getFlightTime();
                                    long minutes = timeInSeconds / 60;
                                    long seconds = timeInSeconds % 60;

                                    ((TextView)mItemView).setText(String.format("Flight " +
                                            "Time\n%02d:%02d", minutes, seconds));
                                }

                                mHandler.postDelayed(this, FLIGHT_TIMER_PERIOD);
                            }
                        }.run();
                    }
                    else{
                        ((TextView)mItemView).setText("--:--");
                    }
                }
            }
        },

        /**
         * Battery info bar item: displays the drone remaining voltage,
         * and ratio of remaining to full voltage.
         */
        BATTERY(R.id.bar_battery){
            @Override
            public void updateItemView(Context context, Drone drone){
                if(mItemView != null){
                    String update = drone == null
                            ? "--"
                            : String.format("%2.1fv\n%2.0f%%",
                            drone.battery.getBattVolt(), drone.battery.getBattRemain());

                    ((TextView)mItemView).setText(update);
                }
            }
        },

        /**
         * Radio signal info bar item: displays the drone radio signal strength.
         */
        SIGNAL(R.id.bar_signal) {

            /**
             * This is the layout resource id for the popup window.
             */
            private static final int sPopupWindowLayoutId = R.layout.popup_info_signal;

            /**
             * This popup is used to show additional signal info.
             */
            private PopupWindow mPopup;

            @Override
            public void initItemView(Context context, View parentView, Drone drone){
                super.initItemView(context, parentView, drone);
                mPopup = initPopupWindow(context, sPopupWindowLayoutId);
                updateItemView(context, drone);
            }

            @Override
            public void updateItemView(Context context, final Drone drone){
                if(mItemView == null)
                    return;

                mItemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(mPopup == null)
                            return;

                        final View popupView = mPopup.getContentView();
                        ((TextView) popupView.findViewById(R.id.bar_signal_rssi)).setText(String.format
                                ("RSSI %2.0f dB",         drone.radio.getRssi()));
                        ((TextView) popupView.findViewById(R.id.bar_signal_remrssi)).setText(String
                                .format("RemRSSI %2.0f dB",                                    drone.radio.getRemRssi()));
                        ((TextView) popupView.findViewById(R.id.bar_signal_noise)).setText(String.format
                                ("Noise %2.0f dB",                                    drone.radio.getNoise()));
                        ((TextView) popupView.findViewById(R.id.bar_signal_remnoise)).setText(String
                                .format("RemNoise %2.0f dB",                                    drone.radio.getRemNoise()));
                        ((TextView) popupView.findViewById(R.id.bar_signal_fade)).setText(String.format
                                ("Fade %2.0f dB",                                    drone.radio.getFadeMargin()));
                        ((TextView) popupView.findViewById(R.id.bar_signal_remfade)).setText(String
                                .format("RemFade %2.0f dB",                                    drone.radio.getRemFadeMargin()));

                        mPopup.update();
                        mPopup.showAsDropDown(mItemView);
                    }
                });

                String update = drone == null
                        ? "--"
                        : String.format("%d%%", drone.radio.getSignalStrength());
                ((TextView)mItemView).setText(update);
            }
        },

        /**
         * Flight/APM modes info bar item: allows the user to select/view the drone flight mode.
         */
        FLIGHT_MODES(R.id.bar_flight_mode) {

            /**
             * Stores the type of the current drone state.
             */
            private int mLastDroneType = -1;

            /**
             * This is the spinner modes adapter.
             */
            private ModeAdapter mModeAdapter;

            @Override
            public void initItemView(final Context context, View parentView, final Drone drone) {
                super.initItemView(context, parentView, drone);
                updateItemView(context, drone);
            }

            @Override
            public void updateItemView(final Context context, final Drone drone){
                if(mItemView == null)
                    return;

                final Spinner modesSpinner = (Spinner) mItemView;
                modesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position,
                                               long id) {
                        if (drone != null) {
                            ApmModes newMode = (ApmModes) parent.getItemAtPosition(position);
                            drone.state.changeFlightMode(newMode);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });

                if(mModeAdapter == null){
                    mModeAdapter = new ModeAdapter(context, R.layout.spinner_drop_down);
                    modesSpinner.setAdapter(mModeAdapter);
                }

                final int droneType = drone == null ? -1: drone.type.getType();
                if(droneType != mLastDroneType){
                    final List<ApmModes> flightModes = droneType == -1
                            ? Collections.<ApmModes>emptyList()
                            : ApmModes.getModeList(droneType);

                    mModeAdapter.clear();
                    mModeAdapter.addAll(flightModes);
                    mModeAdapter.notifyDataSetChanged();

                    mLastDroneType = droneType;
                }

                if(drone != null)
                    modesSpinner.setSelection(mModeAdapter.getPosition(drone.state.getMode()));
            }
        },

        /**
         * This is used on normal screen devices ( 320dp <= x < 540dp) to reduce the width of the
         * info bar. Items typically on the info bar are moved to the popup displayed by clicking
         * on this item's icon.
         */
        PHONE_EXTRA(R.id.bar_phone_extra) {
            /**
             * This is the layout resource id for the popup window.
             */
            private static final int sPopupWindowLayoutId = R.layout.popup_info_phone_extra;

            /**
             * This is the list of info bar items stored as extra.
             */
            private final InfoBarItem[] mExtraInfoBarItems = {
              HOME, GPS, BATTERY, FLIGHT_TIME
            };

            /**
             * This popup is used to show additional signal info.
             */
            private PopupWindow mPopup;

            @Override
            public void initItemView(Context context, View parentView, Drone drone){
                super.initItemView(context, parentView, drone);

                //Initialize the popup window.
                mPopup = initPopupWindow(context, sPopupWindowLayoutId);
                if(mPopup != null){
                    final View popupView = mPopup.getContentView();

                    for(InfoBarItem infoItem: mExtraInfoBarItems){
                        infoItem.initItemView(context, popupView, drone);
                    }
                }

                updateItemView(context, drone);
            }

            @Override
            public void updateItemView(final Context context, final Drone drone){
                if(mItemView == null)
                    return;

                mItemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(mPopup == null)
                            return;

                        final View popupView = mPopup.getContentView();

                        //Update the popup window content.
                        for(InfoBarItem infoItem: mExtraInfoBarItems){
                            infoItem.updateItemView(context, drone);
                        }

                        mPopup.update();
                        mPopup.showAsDropDown(mItemView);
                    }
                });
            }
        };

        /**
         * Id for the info action.
         */
        protected final int mItemId;

        /**
         * Info bar item view.
         */
        protected View mItemView;

        private InfoBarItem(int itemId) {
            mItemId = itemId;
        }

        /**
         * @return the info bar action id.
         */
        public int getItemId() {
            return mItemId;
        }

        /**
         * This initializes the view backing this info bar item.
         * @param context application context
         * @param parentView parent view for the info bar item
         * @param drone current drone state
         */
        public void initItemView(final Context context, View parentView, Drone drone) {
            mItemView = parentView.findViewById(mItemId);
        }

        /**
         * @return the info bar item view.
         */
        public View getItemView(){
            return mItemView;
        }

        public void updateItemView(Context context, Drone drone){}

        /**
         * This is used, during the creation of the {@link InfoBarActionProvider} class,
         * to initialize the info bar action popup window.
         *
         * @param context application context
         */
        protected static PopupWindow initPopupWindow(Context context, int popupViewRes) {
            if (popupViewRes == 0)
                return null;

            final LayoutInflater inflater = LayoutInflater.from(context);
            final View popupView = inflater.inflate(popupViewRes, null);

            final PopupWindow popup = new PopupWindow(popupView,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, true);
            popup.setBackgroundDrawable(context.getResources().getDrawable(R.drawable
                    .panel_white_bg));

            return popup;
        }
    }

    /**
     * Application context.
     */
    private final Context mContext;

    /**
     * Current drone state.
     */
    private Drone mDrone;

    /**
     * Action provider's view.
     */
    private View mView;

    public InfoBarActionProvider(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public View onCreateActionView() {
        final LayoutInflater inflater = LayoutInflater.from(mContext);
        mView = inflater.inflate(R.layout.action_provider_info_bar, null);

        setupActionView();
        updateInfoBar();

        return mView;
    }

    /**
     * This is used to update the current drone state.
     *
     * @param drone
     */
    public void setDrone(Drone drone) {
        mDrone = drone;
    }

    @Override
    public void onDroneEvent(DroneInterfaces.DroneEventsType event, Drone drone) {
        setDrone(drone);

        boolean updateExtra = true;
        switch (event) {
            case BATTERY:
                ;InfoBarItem.BATTERY.updateItemView(mContext, mDrone);
                break;

            case CONNECTED:
                updateInfoBar();
                updateExtra = false;
                break;

            case DISCONNECTED:
                setDrone(null);
                updateInfoBar();
                updateExtra = false;
                break;

            case GPS_FIX:
            case GPS_COUNT:
                InfoBarItem.GPS.updateItemView(mContext, mDrone);
                break;

            case HOME:
                InfoBarItem.HOME.updateItemView(mContext, mDrone);
                break;

            case RADIO:
                InfoBarItem.SIGNAL.updateItemView(mContext, mDrone);
                break;

            case STATE:
                InfoBarItem.FLIGHT_TIME.updateItemView(mContext, mDrone);
                break;

            case MODE:
            case TYPE:
                InfoBarItem.FLIGHT_MODES.updateItemView(mContext, mDrone);
                break;

            default:
                updateExtra = false;
                break;
        }

        if(updateExtra){
            InfoBarItem.PHONE_EXTRA.updateItemView(mContext, mDrone);
        }
    }

    /**
     * Go through the list of action bar item, and initialize their view.
     */
    private void setupActionView() {
        final InfoBarItem[] barItems = InfoBarItem.values();

        for (final InfoBarItem item : barItems) {
            item.initItemView(mContext, mView, mDrone);
        }
    }

    /**
     * This updates the info bar with the current drone state.
     */
    private void updateInfoBar() {
        final InfoBarItem[] barItems = InfoBarItem.values();

        for (final InfoBarItem item : barItems) {
            item.updateItemView(mContext, mDrone);
        }
    }
}
