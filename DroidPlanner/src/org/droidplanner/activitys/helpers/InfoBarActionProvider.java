package org.droidplanner.activitys.helpers;

import android.content.Context;
import android.os.Handler;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AlignmentSpan;
import android.text.style.RelativeSizeSpan;
import android.view.ActionProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;
import org.droidplanner.R;
import org.droidplanner.drone.Drone;
import org.droidplanner.drone.DroneInterfaces;

/**
 * This implements the info bar displayed on the action bar after connection with the drone.
 * <p/>
 * <b>Note:</b> The parent activity must add instantiations of this class to the list of
 * DroneEvent listeners.
 */
public class InfoBarActionProvider extends ActionProvider implements DroneInterfaces
        .OnDroneListner {

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
                super.updatePopupView(context, drone);

                if (mPopupWindow == null) {
                    throw new IllegalStateException("Unable to initialize popup window for the " +
                            "radio signal info bar item.");
                }

                if (drone == null)
                    return;

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
            if(mDrone == null || mView == null)
                return;

            final TextView flightTimeView = (TextView) mView.findViewById(InfoBarItem.FLIGHT_TIME
                    .getItemId());

            long timeInSeconds = mDrone.state.getFlightTime();
            long minutes = timeInSeconds/60;
            long seconds = timeInSeconds%60;

            SpannableString text = new SpannableString(String.format("   Flight Time\n  %02d:%02d", minutes,seconds));
            text.setSpan(new RelativeSizeSpan(.8f), 0, 14, 0);
            text.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_NORMAL),0, text.length()-1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            flightTimeView.setText(text);
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
        return view;
    }

    /**
     * This is used to update the current drone state.
     * @param drone
     */
    public void setDrone(Drone drone){
        mDrone = drone;
    }

    @Override
    public void onDroneEvent(DroneInterfaces.DroneEventsType event, Drone drone) {
        setDrone(drone);
        switch (event) {
            case BATTERY:
                updateBatteryInfo(drone);
                break;

            case CONNECTED:
                updateInfoBar(drone);
                break;

            case DISCONNECTED:
                setDrone(null);
                updateInfoBar(drone);
                break;

            case GPS_FIX:
            case GPS_COUNT:
                updateGpsInfo(drone);
                break;

            case HOME:
                updateHomeInfo(drone);
                break;

            case RADIO:
                updateRadioInfo(drone);
                break;

            case STATE:
                updateFlightTimeInfo(drone);
                break;

            default:
                break;
        }
    }

    private void setupActionView(View view) {
        final InfoBarItem[] barActions = InfoBarItem.values();

        for (final InfoBarItem action : barActions) {
            final View actionView = view.findViewById(action.getItemId());
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
     *
     * @param drone current drone state
     */
    private void updateBatteryInfo(Drone drone) {
        final InfoBarItem batteryItem = InfoBarItem.BATTERY;
        batteryItem.updatePopupView(mContext, drone);

        if (mView != null) {
            final TextView itemView = (TextView) mView.findViewById(batteryItem.getItemId());
            SpannableString text = new SpannableString(String.format("   Battery\n  %2.1fv, " +
                    "%2.0f%% ", drone.battery.getBattVolt(), drone.battery.getBattRemain()));

            text.setSpan(new RelativeSizeSpan(.8f), 0, 10, 0);
            text.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_NORMAL), 0,
                    text.length() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            itemView.setText(text);
        }
    }

    /**
     * Contains logic to update the gps info bar item.
     *
     * @param drone current drone state
     */
    private void updateGpsInfo(Drone drone) {
        final InfoBarItem gpsItem = InfoBarItem.GPS;
        gpsItem.updatePopupView(mContext, drone);

        if (mView != null) {
            final TextView gpsItemView = (TextView) mView.findViewById(gpsItem.getItemId());
            SpannableString text = new SpannableString(String.format("  Satellite\n  %d, %s", 
                    drone.GPS.getSatCount(), drone.GPS.getFixType()));
            text.setSpan(new RelativeSizeSpan(.8f), 0, 13, 0);
            text.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_NORMAL), 0,
                    text.length() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            gpsItemView.setText(text);
        }
    }

    /**
     * Contains logic to update the radio/signal info bar item.
     * @param drone current drone state
     */
    private void updateRadioInfo(Drone drone) {
        final InfoBarItem radioItem = InfoBarItem.SIGNAL;
        radioItem.updatePopupView(mContext, drone);

        if (mView != null) {
            final TextView radioItemView = (TextView) mView.findViewById(radioItem.getItemId());
            SpannableString text = new SpannableString(String.format("   Signal\n  %d%%",
                    drone.radio.getSignalStrength()));
            text.setSpan(new RelativeSizeSpan(.8f), 0, 9, 0);
            text.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_NORMAL), 0,
                    text.length() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            radioItemView.setText(text);
        }
    }

    /**
     * Contains logic to update the home info bar item.
     * @param drone current drone state
     */
    private void updateHomeInfo(Drone drone) {
        final InfoBarItem homeItem = InfoBarItem.HOME;
        homeItem.updatePopupView(mContext, drone);

        if (mView != null) {
            final TextView homeItemView = (TextView) mView.findViewById(homeItem.getItemId());
            SpannableString text = new SpannableString(String.format("   Home\n  %s",
                    drone.home.getDroneDistanceToHome().toString()));
            text.setSpan(new RelativeSizeSpan(.8f), 0, 7, 0);
            text.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_NORMAL), 0,
                    text.length() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            homeItemView.setText(text);
        }
    }

    /**
     * Contains logic to update the flight time info bar item.
     * @param drone current drone state
     */
    private void updateFlightTimeInfo(Drone drone){
        final InfoBarItem flightTimeItem = InfoBarItem.FLIGHT_TIME;
        flightTimeItem.updatePopupView(mContext, drone);

        mHandler.removeCallbacks(mFlightTimer);

        if(drone != null && mView != null){
            mHandler.postDelayed(mFlightTimer, FLIGHT_TIMER_PERIOD);
        }
    }

    /**
     * This updates the info bar with the current drone state.
     * @param drone current drone state
     */
    public void updateInfoBar(Drone drone){
        updateBatteryInfo(drone);
        updateFlightTimeInfo(drone);
        updateGpsInfo(drone);
        updateHomeInfo(drone);
        updateRadioInfo(drone);
    }
}
