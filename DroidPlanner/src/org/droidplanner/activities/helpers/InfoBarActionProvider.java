package org.droidplanner.activities.helpers;

import android.content.Context;
import android.os.Handler;
import android.view.ActionProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;
import org.droidplanner.R;
import org.droidplanner.drone.Drone;
import org.droidplanner.drone.DroneInterfaces;
import org.droidplanner.drone.DroneInterfaces.OnDroneListener;

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
        HOME(R.id.bar_home, 0),
        GPS(R.id.bar_gps, 0),

        FLIGHT_TIME(R.id.bar_propeller, R.layout.popup_info_flight_time) {
            @Override
            public void updatePopupView(Context context, final Drone drone) {
                super.updatePopupView(context, drone);

                if (mPopupWindow == null) {
                    throw new IllegalStateException("Unable to initialize popup window for the " +
                            "flight time info bar item.");
                }

                final View popupView = mPopupWindow.getContentView();
                popupView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (drone != null) {
                            drone.state.resetFlightTimer();
                        }
                        mPopupWindow.dismiss();
                    }
                });

                mPopupWindow.update();
            }
        },

        BATTERY(R.id.bar_battery, 0),

        SIGNAL(R.id.bar_signal, R.layout.popup_info_signal) {
            @Override
            public void updatePopupView(Context context, Drone drone) {
                if (drone == null) {
                    mPopupWindow = null;
                }
                else {
                    super.updatePopupView(context, drone);

                    if (mPopupWindow == null) {
                        throw new IllegalStateException("Unable to initialize popup window for " +
                                "the radio signal info bar item.");
                    }

                    final View popupView = mPopupWindow.getContentView();
                    ((TextView) popupView.findViewById(R.id.bar_signal_rssi)).setText(String.format
                            ("RSSI %2.0f dB",
                                    drone.radio.getRssi()));
                    ((TextView) popupView.findViewById(R.id.bar_signal_remrssi)).setText(String
                            .format("RemRSSI %2.0f dB",
                                    drone.radio.getRemRssi()));
                    ((TextView) popupView.findViewById(R.id.bar_signal_noise)).setText(String.format
                            ("Noise %2.0f dB",
                                    drone.radio.getNoise()));
                    ((TextView) popupView.findViewById(R.id.bar_signal_remnoise)).setText(String
                            .format("RemNoise %2.0f dB",
                                    drone.radio.getRemNoise()));
                    ((TextView) popupView.findViewById(R.id.bar_signal_fade)).setText(String.format
                            ("Fade %2.0f dB",
                                    drone.radio.getFadeMargin()));
                    ((TextView) popupView.findViewById(R.id.bar_signal_remfade)).setText(String
                            .format("RemFade %2.0f dB",
                                    drone.radio.getRemFadeMargin()));

                    mPopupWindow.update();
                }
            }
        },

        PHONE_EXTRA(R.id.bar_phone_extra, R.layout.popup_info_phone_extra) {
            @Override
            public void updatePopupView(Context context, Drone drone){
                if(drone == null){
                    mPopupWindow = null;
                }
                else{
                    super.updatePopupView(context, drone);

                    if (mPopupWindow == null) {
                        throw new IllegalStateException("Unable to initialize popup window for " +
                                "the phone extra info bar item.");
                    }

                    final View popupView = mPopupWindow.getContentView();
                    final TextView homeView = (TextView) popupView.findViewById(R.id.bar_home);
                    updateHomeViewHelper(drone, homeView);

                    final TextView gpsView = (TextView) popupView.findViewById(R.id.bar_gps);
                    updateGpsInfoHelper(drone, gpsView);

                    mPopupWindow.update();
                }
            }
        };

        /**
         * Id for the info action.
         */
        protected final int mActionId;

        /**
         * Info bar action popup window.
         */
        protected PopupWindow mPopupWindow;

        /**
         * Resource layout for the info popup view.
         */
        protected final int mPopupViewRes;

        private InfoBarItem(int actionId, int popupViewRes) {
            mActionId = actionId;
            mPopupViewRes = popupViewRes;
        }

        /**
         * @return the info bar action id.
         */
        public int getItemId() {
            return mActionId;
        }

        /**
         * @return the popup window for this info bar action
         */
        public PopupWindow getPopupWindow(Context context) {
            if (mPopupWindow == null) {
                initPopupWindow(context);
                updatePopupView(context, null);
            }

            return mPopupWindow;
        }

        /**
         * This is used, during the creation of the {@link InfoBarActionProvider} class,
         * to initialize the info bar action popup window.
         *
         * @param context application context
         */
        protected void initPopupWindow(Context context) {
            mPopupWindow = null;

            int popupViewRes = mPopupViewRes;
            if (popupViewRes == 0)
                return;

            final LayoutInflater inflater = LayoutInflater.from(context);
            final View popupView = inflater.inflate(popupViewRes, null);

            final PopupWindow popup = new PopupWindow(popupView,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, true);
            popup.setBackgroundDrawable(context.getResources().getDrawable(R.drawable
                    .panel_white_bg));

            mPopupWindow = popup;
        }

        /**
         * This is used to update the popup window view to match the current drone state
         *
         * @param context application context
         * @param drone   currently connected drone
         */
        public void updatePopupView(Context context, Drone drone) {
            if (mPopupWindow == null) {
                initPopupWindow(context);
            }
        }
    }

    private final Context mContext;

    /**
     * Current drone state.
     */
    private Drone mDrone;

    /**
     * Action provider's view.
     */
    private View mView;

    private final Handler mHandler = new Handler();

    private final static long FLIGHT_TIMER_PERIOD = 1000l; //1 second

    private final Runnable mFlightTimer = new Runnable() {
        @Override
        public void run() {
            mHandler.removeCallbacks(this);
            if (mDrone == null)
                return;

            if (mView != null) {
                final TextView flightTimeView = (TextView) mView.findViewById(InfoBarItem
                        .FLIGHT_TIME.getItemId());
                if (flightTimeView != null) {

                    long timeInSeconds = mDrone.state.getFlightTime();
                    long minutes = timeInSeconds / 60;
                    long seconds = timeInSeconds % 60;

                    flightTimeView.setText(String.format("Flight Time\n%02d:%02d", minutes,
                            seconds));
                }
            }

            mHandler.postDelayed(this, FLIGHT_TIMER_PERIOD);
        }
    };

    public InfoBarActionProvider(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public View onCreateActionView() {
        final LayoutInflater inflater = LayoutInflater.from(mContext);
        final View view = inflater.inflate(R.layout.action_provider_info_bar, null);
        setupActionView(view);

        mView = view;
        updateInfoBar();

        return view;
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
        switch (event) {
            case BATTERY:
                updateBatteryInfo();
                break;

            case CONNECTED:
                updateInfoBar();
                break;

            case DISCONNECTED:
                setDrone(null);
                updateInfoBar();
                break;

            case GPS_FIX:
            case GPS_COUNT:
                updateGpsInfo();
                updatePhoneExtraInfo();
                break;

            case HOME:
                updateHomeInfo();
                updatePhoneExtraInfo();
                break;

            case RADIO:
                updateRadioInfo();
                break;

            case STATE:
                updateFlightTimeInfo();
                break;

            default:
                break;
        }
    }

    private void setupActionView(View view) {
        final InfoBarItem[] barActions = InfoBarItem.values();

        for (final InfoBarItem action : barActions) {
            final View actionView = view.findViewById(action.getItemId());
            if (actionView == null)
                continue;

            actionView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final PopupWindow popup = action.getPopupWindow(mContext);
                    if (popup == null)
                        return;

                    popup.showAsDropDown(actionView);
                }
            });
        }
    }

    /**
     * Contains logic to update the battery info bar item.
     */
    private void updateBatteryInfo() {
        final InfoBarItem batteryItem = InfoBarItem.BATTERY;
        batteryItem.updatePopupView(mContext, mDrone);

        if (mView != null) {
            final TextView itemView = (TextView) mView.findViewById(batteryItem.getItemId());
            if (itemView != null) {
                String update = mDrone == null
                        ? "--"
                        : String.format("%2.1fv\n%2.0f%%",
                        mDrone.battery.getBattVolt(), mDrone.battery.getBattRemain());

                itemView.setText(update);
            }
        }
    }

    /**
     * Contains logic to update the gps info bar item.
     */
    private void updateGpsInfo() {
        final InfoBarItem gpsItem = InfoBarItem.GPS;
        gpsItem.updatePopupView(mContext, mDrone);

        if (mView != null) {
            final TextView gpsItemView = (TextView) mView.findViewById(gpsItem.getItemId());
            updateGpsInfoHelper(mDrone, gpsItemView);
        }
    }

    /**
     * Abstracts the logic to update the gps info bar view.
     * @param drone current drone state
     * @param gpsView {@link TextView} to update
     */
    private static void updateGpsInfoHelper(Drone drone, TextView gpsView){
        if (gpsView != null) {
            String update = drone == null
                    ? "--"
                    : String.format("Satellite\n%d, %s", drone.GPS.getSatCount(),
                    drone.GPS.getFixType());

            gpsView.setText(update);
        }
    }

    /**
     * Contains logic to update the radio/signal info bar item.
     */
    private void updateRadioInfo() {
        final InfoBarItem radioItem = InfoBarItem.SIGNAL;
        radioItem.updatePopupView(mContext, mDrone);

        if (mView != null) {
            final TextView radioItemView = (TextView) mView.findViewById(radioItem.getItemId());
            if (radioItemView != null) {
                String update = mDrone == null
                        ? "--"
                        : String.format("%d%%", mDrone.radio.getSignalStrength());


                radioItemView.setText(update);
            }
        }
    }

    /**
     * Contains logic to update the home info bar item.
     */
    private void updateHomeInfo() {
        final InfoBarItem homeItem = InfoBarItem.HOME;
        homeItem.updatePopupView(mContext, mDrone);

        if (mView != null) {
            final TextView homeItemView = (TextView) mView.findViewById(homeItem.getItemId());
            updateHomeViewHelper(mDrone, homeItemView);
        }
    }

    /**
     * Abstracts the logic to update the home info bar view.
     * @param drone current drone state
     * @param homeView {@link TextView} to update
     */
    private static void updateHomeViewHelper(Drone drone, TextView homeView){
        if (homeView != null) {
            String update = drone == null
                    ? "--"
                    : String.format("Home\n%s", drone.home.getDroneDistanceToHome().toString());

            homeView.setText(update);
        }
    }

    /**
     * Contains logic to update the flight time info bar item.
     */
    private void updateFlightTimeInfo() {
        final InfoBarItem flightTimeItem = InfoBarItem.FLIGHT_TIME;
        flightTimeItem.updatePopupView(mContext, mDrone);

        mHandler.removeCallbacks(mFlightTimer);

        if (mDrone != null) {
            mFlightTimer.run();
        }
        else if (mView != null) {
            final TextView flightTimeView = (TextView) mView.findViewById(flightTimeItem
                    .getItemId());
            if (flightTimeView != null)
                flightTimeView.setText("--:--");
        }
    }

    private void updatePhoneExtraInfo(){
        final InfoBarItem phoneExtraItem = InfoBarItem.PHONE_EXTRA;
        phoneExtraItem.updatePopupView(mContext, mDrone);
    }

    /**
     * This updates the info bar with the current drone state.
     */
    private void updateInfoBar() {
        updateBatteryInfo();
        updateFlightTimeInfo();
        updateGpsInfo();
        updateHomeInfo();
        updatePhoneExtraInfo();
        updateRadioInfo();
    }
}
