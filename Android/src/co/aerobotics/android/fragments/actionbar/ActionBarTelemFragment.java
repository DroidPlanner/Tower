package co.aerobotics.android.fragments.actionbar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import co.aerobotics.android.DroidPlannerApp;
import co.aerobotics.android.activities.EditorActivity;
import co.aerobotics.android.data.DJIFlightControllerState;
import co.aerobotics.android.utils.prefs.DroidPlannerPrefs;

import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.util.MathUtils;

import org.beyene.sius.unit.length.LengthUnit;
import co.aerobotics.android.R;
import co.aerobotics.android.fragments.helpers.ApiListenerFragment;

import java.util.Locale;

import dji.common.battery.AggregationState;
import dji.common.battery.BatteryState;
import dji.common.error.DJIError;
import dji.common.flightcontroller.GPSSignalLevel;
import dji.common.flightcontroller.LocationCoordinate3D;
import dji.common.model.LocationCoordinate2D;
import dji.keysdk.AirLinkKey;
import dji.keysdk.BatteryKey;
import dji.keysdk.FlightControllerKey;
import dji.keysdk.KeyManager;
import dji.keysdk.callback.GetCallback;
import dji.keysdk.callback.KeyListener;
import dji.midware.data.model.P3.DataFlycFaultInject;
import dji.sdk.airlink.AirLink;
import dji.sdk.base.BaseProduct;
import dji.sdk.sdkmanager.DJISDKManager;

/**
 * Created by Fredia Huya-Kouadio on 1/14/15.
 */
public class ActionBarTelemFragment extends ApiListenerFragment {
    private static final String TAG = "ActionBarTelemFragment";
    private final static IntentFilter eventFilter = new IntentFilter();
    private Thread loadSignalThread;
    private Handler mHandler;
    static {
        eventFilter.addAction(DroidPlannerApp.FLAG_CONNECTION_CHANGE);
    }

    private final BroadcastReceiver eventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (getActivity() == null)
                return;

            switch (intent.getAction()) {
                case DroidPlannerApp.FLAG_CONNECTION_CHANGE:
                    /*
                    updateAllTelem();
                    BaseProduct mProduct = DroidPlannerApp.getProductInstance();
                    if (mProduct != null && mProduct.isConnected()){
                        showTelemBar();
                        updateAllTelem();
                        if (loadBatteryThread == null || !loadBatteryThread.isAlive()){
                            loadBatteryThread = new Thread(batteryThread);
                            loadBatteryThread.start();
                        }
                        if (loadSignalThread == null || !loadSignalThread.isAlive()){
                            loadSignalThread = new Thread(signalThread);
                            loadSignalThread.start();
                        }
                        djiFlightControllerState = DJIFlightControllerState.getInstance();
                        if (djiFlightControllerState.getFlightControllerState() != null) {
                            updateAllTelem();
                        }
                    }
                    else{
                        updateAllTelem();
                        clearFields();
                    } */

                    BaseProduct mProduct = DJISDKManager.getInstance().getProduct();
                    if (mProduct != null && mProduct.isConnected()){
                        setInitialValues();
                        setUpKeys();
                        if (loadSignalThread == null || !loadSignalThread.isAlive()){
                            loadSignalThread = new Thread(signalThread);
                            loadSignalThread.start();
                        }
                    } else {
                        tearDownKeys();
                        clearFields();
                    }
                    break;

                default:
                    break;
            }
        }
    };

    private TextView homeTelem;
    private TextView altitudeTelem;
    private TextView gpsTelem;
    private PopupWindow gpsPopup;
    private TextView batteryTelem;
    private PopupWindow batteryPopup;
    private TextView signalTelem;
    private PopupWindow signalPopup;
    private TextView flightModeTelem;
    private String emptyString;

    private Double aircraftLatitude;
    private Double aircraftLongitude;
    private LatLong homePosition;
    private Boolean isHomeLocationSet;

    // DJI KEYS
    private BatteryKey batteryPercentageKey = BatteryKey.create(BatteryKey.CHARGE_REMAINING_IN_PERCENT);
    private BatteryKey batteryVoltageKey = BatteryKey.create(BatteryKey.VOLTAGE);
    private BatteryKey batteryChargeKey = BatteryKey.create(BatteryKey.CHARGE_REMAINING);
    private BatteryKey batteryAmpageKey = BatteryKey.create(BatteryKey.CURRENT);
    private FlightControllerKey gpsSignalKey = FlightControllerKey.create(FlightControllerKey.GPS_SIGNAL_LEVEL);
    private FlightControllerKey satelliteCountKey = FlightControllerKey.create(FlightControllerKey.SATELLITE_COUNT);
    private FlightControllerKey flightModeKey = FlightControllerKey.create(FlightControllerKey.FLIGHT_MODE_STRING);
    private FlightControllerKey homeLocationKey = FlightControllerKey.create(FlightControllerKey.HOME_LOCATION);
    private FlightControllerKey isHomeLocationSetKey = FlightControllerKey.create(FlightControllerKey.IS_HOME_LOCATION_SET);
    private FlightControllerKey aircraftLatitudeKey = FlightControllerKey.create(FlightControllerKey.AIRCRAFT_LOCATION_LATITUDE);
    private FlightControllerKey aircraftLongitudeKey = FlightControllerKey.create(FlightControllerKey.AIRCRAFT_LOCATION_LONGITUDE);
    private FlightControllerKey altitudeKey = FlightControllerKey.create(FlightControllerKey.ALTITUDE);

    //Key Listeners
    KeyListener altitudeListener = new KeyListener() {
        @Override
        public void onValueChange(Object o, Object o1) {
            if (o1 instanceof Float) {
                setAltitudeView(o1);
            }
        }
    };

    KeyListener batteryPercentageListener = new KeyListener() {
        @Override
        public void onValueChange(Object o, Object o1) {
            if (o1 instanceof Integer) {
                setBatteryPercentageView((Integer) o1);
            }
        }
    };

    KeyListener batteryVoltageListener = new KeyListener() {
        @Override
        public void onValueChange(Object o, Object o1) {
            if (o1 instanceof Integer) {
                setBatteryVoltageView((Integer) o1);
            }
        }
    };

    KeyListener batteryChargeListener = new KeyListener() {
        @Override
        public void onValueChange(Object o, Object o1) {
            if (o1 instanceof Integer) {
                setBatteryChargeView((Integer) o1);
            }
        }
    };

    KeyListener batteryCurrentListener = new KeyListener() {
        @Override
        public void onValueChange(Object o, Object o1) {
            if (o1 instanceof Integer) {
                setBatteryCurrentView((Integer) o1);
            }
        }
    };

    KeyListener flightModeListener = new KeyListener() {
        @Override
        public void onValueChange(Object o, Object o1) {
            if (o1 instanceof String) {
                setFlightModeView((String) o1);
            }
        }
    };

    KeyListener homeLocationListener = new KeyListener() {
        @Override
        public void onValueChange(Object o, Object o1) {
            if (o1 instanceof LocationCoordinate2D) {
                Double homeLatitude = ((LocationCoordinate2D) o1).getLatitude();
                Double homeLongitude = ((LocationCoordinate2D) o1).getLongitude();
                if (checkGpsCoordination(homeLatitude, homeLongitude)) {
                    homePosition = new LatLong(homeLatitude, homeLongitude);
                    setHomeDistanceView();
                }
            }
        }
    };

    KeyListener homeLocationSetListener = new KeyListener() {
        @Override
        public void onValueChange(Object o, Object o1) {
            if (o1 instanceof Boolean) {
                isHomeLocationSet = (Boolean) o1;
                if (isHomeLocationSet) {
                    getHomePosition();
                    setHomeDistanceView();
                }
            }
        }
    };

    KeyListener aircraftLatitudeListener = new KeyListener() {
        @Override
        public void onValueChange(Object o, Object o1) {
            if (o1 instanceof Double) {
                aircraftLatitude = (Double) o1;
                setHomeDistanceView();
            }
        }
    };

    KeyListener aircraftLongitudeListener = new KeyListener() {
        @Override
        public void onValueChange(Object o, Object o1) {
            if (o1 instanceof Double) {
                aircraftLongitude = (Double) o1;
                setHomeDistanceView();
            }
        }
    };

    KeyListener gpsSignalListener = new KeyListener() {
        @Override
        public void onValueChange(Object o, Object o1) {
            if (o1 instanceof GPSSignalLevel) {
                setGpsView(o1);
            }
        }
    };

    KeyListener satelliteCountListener = new KeyListener() {
        @Override
        public void onValueChange(Object o, Object o1) {
            if (o1 instanceof Integer) {
                setSatelliteCountView((Integer) o1);
            }
        }
    };

    @Override
    public void onStart() {
        hideTelemBar();
        super.onStart();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onApiConnected() {

        BaseProduct drone = DJISDKManager.getInstance().getProduct();
        if (drone != null && drone.isConnected()){
            showTelemBar();
            setInitialValues();
            setUpKeys();
            if (loadSignalThread ==null || !loadSignalThread.isAlive()){
                loadSignalThread = new Thread(signalThread);
                loadSignalThread.start();
            }
        }
        showTelemBar();
        getBroadcastManager().registerReceiver(eventReceiver, eventFilter);
    }

    @Override
    public void onApiDisconnected() {
        getBroadcastManager().unregisterReceiver(eventReceiver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_action_bar_telem, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mHandler = new Handler(Looper.getMainLooper());
        emptyString = getString(R.string.empty_content);

        final Context context = getActivity().getApplicationContext();
        final LayoutInflater inflater = LayoutInflater.from(context);

        final int popupWidth = ViewGroup.LayoutParams.WRAP_CONTENT;
        final int popupHeight = ViewGroup.LayoutParams.WRAP_CONTENT;
        final Drawable popupBg = getResources().getDrawable(android.R.color.transparent);

        homeTelem = (TextView) view.findViewById(R.id.bar_home);
        altitudeTelem = (TextView) view.findViewById(R.id.bar_altitude);

        gpsTelem = (TextView) view.findViewById(R.id.bar_gps);
        final View gpsPopupView = inflater.inflate(R.layout.popup_info_gps, (ViewGroup) view, false);
        gpsPopup = new PopupWindow(gpsPopupView, popupWidth, popupHeight, true);
        gpsPopup.setBackgroundDrawable(popupBg);
        gpsTelem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gpsPopup.showAsDropDown(gpsTelem);
            }
        });

        batteryTelem = (TextView) view.findViewById(R.id.bar_battery);
        final View batteryPopupView = inflater.inflate(R.layout.popup_info_power, (ViewGroup) view, false);
        batteryPopup = new PopupWindow(batteryPopupView, popupWidth, popupHeight, true);
        batteryPopup.setBackgroundDrawable(popupBg);
        batteryTelem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                batteryPopup.showAsDropDown(batteryTelem);
            }
        });

        signalTelem = (TextView) view.findViewById(R.id.bar_signal);
        final View signalPopupView = inflater.inflate(R.layout.popup_info_signal, (ViewGroup) view, false);
        signalPopup = new PopupWindow(signalPopupView, popupWidth, popupHeight, true);
        signalPopup.setBackgroundDrawable(popupBg);
        flightModeTelem = (TextView) view.findViewById(R.id.bar_flight_mode);
    }

    private void setUpKeys() {
        tearDownKeys();
        KeyManager.getInstance().addListener(batteryPercentageKey, batteryPercentageListener);
        KeyManager.getInstance().addListener(batteryVoltageKey, batteryVoltageListener);
        KeyManager.getInstance().addListener(batteryAmpageKey, batteryCurrentListener);
        KeyManager.getInstance().addListener(batteryChargeKey, batteryChargeListener);
        KeyManager.getInstance().addListener(altitudeKey, altitudeListener);
        KeyManager.getInstance().addListener(flightModeKey, flightModeListener);
        KeyManager.getInstance().addListener(homeLocationKey, homeLocationListener);
        KeyManager.getInstance().addListener(isHomeLocationSetKey, homeLocationSetListener);
        KeyManager.getInstance().addListener(aircraftLatitudeKey, aircraftLatitudeListener);
        KeyManager.getInstance().addListener(aircraftLongitudeKey, aircraftLongitudeListener);
        KeyManager.getInstance().addListener(gpsSignalKey, gpsSignalListener);
        KeyManager.getInstance().addListener(satelliteCountKey, satelliteCountListener);
    }

    private void tearDownKeys() {
        KeyManager.getInstance().removeListener(batteryPercentageListener);
        KeyManager.getInstance().removeListener(batteryChargeListener);
        KeyManager.getInstance().removeListener(batteryVoltageListener);
        KeyManager.getInstance().removeListener(batteryCurrentListener);
        KeyManager.getInstance().removeListener(altitudeListener);
        KeyManager.getInstance().removeListener(flightModeListener);
        KeyManager.getInstance().removeListener(homeLocationListener);
        KeyManager.getInstance().removeListener(homeLocationSetListener);
        KeyManager.getInstance().removeListener(aircraftLatitudeListener);
        KeyManager.getInstance().removeListener(aircraftLongitudeListener);
        KeyManager.getInstance().removeListener(gpsSignalListener);
        KeyManager.getInstance().removeListener(satelliteCountListener);
    }

    //Populate telemetry with initial values
    private void setInitialValues() {
        DJISDKManager.getInstance().getKeyManager().getValue(batteryPercentageKey, new GetCallback() {
            @Override
            public void onSuccess(Object o) {
                if (o instanceof Integer) {
                    setBatteryPercentageView((Integer) o);
                }
            }

            @Override
            public void onFailure(DJIError djiError) {

            }
        });

        DJISDKManager.getInstance().getKeyManager().getValue(batteryVoltageKey, new GetCallback() {
           @Override
           public void onSuccess(Object o) {
               if (o instanceof Integer) {
                   setBatteryVoltageView((Integer) o);
               }
           }

           @Override
           public void onFailure(DJIError djiError) {
                Log.d(TAG, djiError.getDescription());
           }
       });

        DJISDKManager.getInstance().getKeyManager().getValue(batteryChargeKey, new GetCallback() {
            @Override
            public void onSuccess(Object o) {
                if (o instanceof Integer) {
                    setBatteryChargeView((Integer) o);
                }
            }

            @Override
            public void onFailure(DJIError djiError) {
                Log.d(TAG, djiError.getDescription());
            }
        });

        DJISDKManager.getInstance().getKeyManager().getValue(batteryAmpageKey, new GetCallback() {
            @Override
            public void onSuccess(Object o) {
                if (o instanceof Integer) {
                    setBatteryCurrentView((Integer) o);
                }
            }

            @Override
            public void onFailure(DJIError djiError) {
                Log.d(TAG, djiError.getDescription());
            }
        });

       KeyManager.getInstance().getValue(flightModeKey, new GetCallback() {
           @Override
           public void onSuccess(Object o) {
               if (o instanceof String) {
                   setFlightModeView((String) o);
               }
           }

           @Override
           public void onFailure(DJIError djiError) {
               Log.d(TAG, djiError.getDescription());

           }
       });

       KeyManager.getInstance().getValue(homeLocationKey, new GetCallback() {
           @Override
           public void onSuccess(Object o) {
               LocationCoordinate2D homeLocation = (LocationCoordinate2D) o;
               if (homeLocation.isValid()) {
                   Double homeLatitude = ((LocationCoordinate2D) o).getLatitude();
                   Double homeLongitude = ((LocationCoordinate2D) o).getLongitude();
                   if (checkGpsCoordination(homeLatitude, homeLongitude)) {
                       homePosition = new LatLong(homeLatitude, homeLongitude);
                       setHomeDistanceView();
                   }
               }
           }

           @Override
           public void onFailure(DJIError djiError) {
               Log.d(TAG, djiError.getDescription());

           }
       });

       KeyManager.getInstance().getValue(isHomeLocationSetKey, new GetCallback() {
           @Override
           public void onSuccess(Object o) {
               if (o instanceof Boolean) {
                   isHomeLocationSet = (Boolean) o;
                   if (isHomeLocationSet) {
                       getHomePosition();
                       setHomeDistanceView();
                   }
               }
           }

           @Override
           public void onFailure(DJIError djiError) {

           }
       });

       KeyManager.getInstance().getValue(aircraftLongitudeKey, new GetCallback() {
           @Override
           public void onSuccess(Object o) {
               if (o instanceof Double) {
                   aircraftLongitude = (double) o;
                   setHomeDistanceView();
               }
           }

           @Override
           public void onFailure(DJIError djiError) {
               Log.d(TAG, djiError.getDescription());

           }
       });

        KeyManager.getInstance().getValue(aircraftLatitudeKey, new GetCallback() {
            @Override
            public void onSuccess(Object o) {
                if (o instanceof Double) {
                    aircraftLatitude = (double) o;
                    setHomeDistanceView();
                }
            }

            @Override
            public void onFailure(DJIError djiError) {
                Log.d(TAG, djiError.getDescription());

            }
        });

       KeyManager.getInstance().getValue(altitudeKey, new GetCallback() {
           @Override
           public void onSuccess(Object o) {
               setAltitudeView(o);
           }

           @Override
           public void onFailure(DJIError djiError) {
               Log.d(TAG, djiError.getDescription());

           }
       });

       KeyManager.getInstance().getValue(gpsSignalKey, new GetCallback() {
           @Override
           public void onSuccess(Object o) {
               setGpsView(o);
           }

           @Override
           public void onFailure(DJIError djiError) {
               Log.d(TAG, djiError.getDescription());

           }
       });

       KeyManager.getInstance().getValue(satelliteCountKey, new GetCallback() {
           @Override
           public void onSuccess(Object o) {
               if (o instanceof Integer) {
                   setSatelliteCountView((Integer) o);

               }
           }

           @Override
           public void onFailure(DJIError djiError) {

           }
       });
    }

    private void getHomePosition() {
        KeyManager.getInstance().getValue(homeLocationKey, new GetCallback() {
            @Override
            public void onSuccess(Object o) {
                LocationCoordinate2D homeLocation = (LocationCoordinate2D) o;
                if (homeLocation.isValid()) {
                    Double homeLatitude = ((LocationCoordinate2D) o).getLatitude();
                    Double homeLongitude = ((LocationCoordinate2D) o).getLongitude();
                    if (checkGpsCoordination(homeLatitude, homeLongitude)) {
                        homePosition = new LatLong(homeLatitude, homeLongitude);
                    }
                }
            }

            @Override
            public void onFailure(DJIError djiError) {
                Log.d(TAG, djiError.getDescription());

            }
        });
    }

    //Update views
    private void setBatteryPercentageView(Integer o1) {
        final double chargeRemainingInPercent = o1;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                String update = String.format(Locale.ENGLISH, "%2.0f%%", chargeRemainingInPercent);
                //batteryPopup.update();
                batteryTelem.setText(update);
            }
        });
    }

    private void setBatteryVoltageView(final Integer o1) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                final int voltage = o1;
                final double voltageVolts = ((double) voltage) / 1000;
                final View batteryPopupView = batteryPopup.getContentView();
                final TextView remainView = (TextView) batteryPopupView.findViewById(R.id.bar_power_remain);
                remainView.setText(String.format(Locale.ENGLISH, "V: %2.1f V", voltageVolts ));
                batteryPopup.update();
            }
        });
    }

    private void setBatteryChargeView(final Integer o1) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                final int chargeRemaining = o1;
                final String dischargeText;
                final View batteryPopupView = batteryPopup.getContentView();
                final TextView dischargeView = (TextView) batteryPopupView.findViewById(R.id.bar_power_discharge);

                if (chargeRemaining == 0) {
                    dischargeText = "D: " + emptyString;
                } else {
                    dischargeText = "D: " + electricChargeToString(chargeRemaining);
                }
                dischargeView.setText(dischargeText);
                batteryPopup.update();
            }
        });
    }

    private void setBatteryCurrentView(final Integer o1) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                final int current = o1;
                final double currentAmps = ((double) current /1000);
                final View batteryPopupView = batteryPopup.getContentView();
                final TextView currentView = (TextView) batteryPopupView.findViewById(R.id.bar_power_current);
                currentView.setText(String.format(Locale.ENGLISH, "C: %2.1f A", currentAmps));
                batteryPopup.update();
            }
        });
    }

    private void setFlightModeView(String o1) {
        final String flightMode = o1;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                int navIcon = R.drawable.ic_navigation_light_blue_a400_18dp;
                flightModeTelem.setText(flightMode);
                flightModeTelem.setCompoundDrawablesWithIntrinsicBounds(navIcon, 0, 0, 0);
            }
        });

        /*
        if (drone != null && drone.isConnected()) {
            navIcon = R.drawable.ic_navigation_light_blue_a400_18dp;
            flightModeTelem.setText(flightMode);
            flightModeTelem.setCompoundDrawablesWithIntrinsicBounds(navIcon, 0, 0, 0);

        } else {
            navIcon = R.drawable.ic_navigation_grey_700_18dp;
            flightModeTelem.setText(emptyString);
            flightModeTelem.setCompoundDrawablesWithIntrinsicBounds(navIcon, 0, 0, 0);
        }*/
    }

    private synchronized void setHomeDistanceView() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                int drawableResId = R.drawable.ic_home_grey_700_18dp;
                if (homePosition != null && aircraftLatitude != null && aircraftLongitude != null) {
                    if (true) {
                        LatLong aircraftPosition = new LatLong(aircraftLatitude, aircraftLongitude);
                        LengthUnit distanceToHome = getLengthUnitProvider().boxBaseValueToTarget
                                (MathUtils.getDistance2D(homePosition, aircraftPosition));
                        String update = String.format("%s", distanceToHome);
                        homeTelem.setCompoundDrawablesWithIntrinsicBounds(drawableResId, 0, 0, 0);
                        homeTelem.setText(update);
                    }
                } else {
                    homeTelem.setCompoundDrawablesWithIntrinsicBounds(drawableResId, 0, 0, 0);
                    homeTelem.setText(emptyString);
                }
            }
        });

    }

    private synchronized void setAltitudeView(Object o1) {
        final float altitude = (float) o1;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                altitudeTelem.setText(String.format(Locale.ENGLISH, "%2.1f m", altitude));
            }
        });
    }

    private void setGpsView(Object o1) {
        GPSSignalLevel droneGps = ((GPSSignalLevel) o1);
        final String fixStatus;
        switch (droneGps.value()){
            case 255:
                fixStatus = "None";
                break;
            case 0:
            case 1:
            case 2:
                fixStatus = "Poor";
                break;
            case 3:
                fixStatus = "Ok";
                break;
            case 4:
            case 5:
                fixStatus = "Good";
                break;
            default:
                fixStatus = "None";
                break;
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                final int gpsIcon;
                final String hdopStatus;
                String update = String.format(Locale.ENGLISH, "%s", fixStatus);
                switch (fixStatus) {
                    case "Ok":
                    case "Good":
                        gpsIcon = R.drawable.ic_gps_fixed_black_24dp;
                        break;

                    case "None":
                    case "Poor":
                    default:
                        gpsIcon = R.drawable.ic_gps_not_fixed_grey_700_18dp;
                        break;
                }

                hdopStatus=String.format(Locale.ENGLISH, "%s", fixStatus);
                final View popupView = gpsPopup.getContentView();
                TextView hdopStatusView = (TextView) popupView.findViewById(R.id.bar_gps_hdop_status);
                hdopStatusView.setVisibility(View.VISIBLE);
                hdopStatusView.setText(hdopStatus);

                gpsTelem.setText(update);
                gpsTelem.setCompoundDrawablesWithIntrinsicBounds(gpsIcon, 0, 0, 0);
                gpsPopup.update();
            }
        });
    }

    private void setSatelliteCountView(final Integer o1){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                final View popupView = gpsPopup.getContentView();
                String satNo = String.format(Locale.ENGLISH, "S: %d", (int) o1);
                TextView satNoView = (TextView) popupView.findViewById(R.id.bar_gps_satno);
                satNoView.setText(satNo);
                gpsPopup.update();
            }
        });
    }

    private void showTelemBar() {
        final View view = getView();
        if (view != null)
            view.setVisibility(View.VISIBLE);
    }

    private void hideTelemBar() {
        final View view = getView();
        if (view != null)
            view.setVisibility(View.GONE);
    }

    private void clearFields(){
        //clear signal
        signalTelem.setText(emptyString);
        signalTelem.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_signal_cellular_null_grey_700_18dp,
                0, 0, 0);
        //clear battery
        final View batteryPopupView = batteryPopup.getContentView();
        final TextView dischargeView = (TextView) batteryPopupView.findViewById(R.id.bar_power_discharge);
        final TextView currentView = (TextView) batteryPopupView.findViewById(R.id.bar_power_current);
        final TextView remainView = (TextView) batteryPopupView.findViewById(R.id.bar_power_remain);
        dischargeView.setText(emptyString);
        remainView.setText(emptyString);
        currentView.setText(emptyString);
        batteryPopup.update();
        batteryTelem.setText(emptyString);
        //clear home
        homeTelem.setText(emptyString);
        //clear altitude
        altitudeTelem.setText(emptyString);
        //clear GPS
        final View popupView = gpsPopup.getContentView();
        TextView satNoView = (TextView) popupView.findViewById(R.id.bar_gps_satno);
        satNoView.setText(emptyString);
        TextView hdopStatusView = (TextView) popupView.findViewById(R.id.bar_gps_hdop_status);
        hdopStatusView.setText(emptyString);
        gpsPopup.update();
        int gpsIcon = R.drawable.ic_gps_off_grey_700_18dp;
        gpsTelem.setCompoundDrawablesWithIntrinsicBounds(gpsIcon, 0, 0, 0);
        gpsTelem.setText(emptyString);
        //clear flight mode
        int navIcon = R.drawable.ic_navigation_grey_700_18dp;
        flightModeTelem.setCompoundDrawablesWithIntrinsicBounds(navIcon, 0, 0, 0);
        flightModeTelem.setText(emptyString);
    }



    private void stopCallbacks(){
        try{
            DroidPlannerApp.getProductInstance().getAirLink().setUplinkSignalQualityCallback(null);
        } catch (Exception ignored){

        }
    }

    private Runnable signalThread = new Runnable(){

        @Override
        public void run() {
            BaseProduct drone = DroidPlannerApp.getProductInstance();
            if (drone != null && drone.isConnected()) {
                AirLink airLink = DroidPlannerApp.getProductInstance().getAirLink();
                if (airLink != null)
                airLink.setUplinkSignalQualityCallback(new SignalQuality() {
                    @Override
                    public void onUpdate(int signalStrength) {
                        Message msg = new Message();
                        Bundle bundle = new Bundle();
                        Log.i(TAG, "ABTF: " + Thread.currentThread().getName());
                        bundle.putInt("percent", signalStrength);
                        msg.setData(bundle);
                        mSignalHandler.sendMessage(msg);

                    }
                });
            }
            else{
                signalTelem.setText(emptyString);
                signalTelem.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_signal_cellular_null_grey_700_18dp,
                        0, 0, 0);
            }
        }
    };

    private Handler mSignalHandler = new Handler(new Handler.Callback(){

        @Override
        public boolean handleMessage(Message msg){
            Bundle bundle = msg.getData();
            int signalStrength = bundle.getInt("percent");
            final int signalIcon;
            if (signalStrength >= 100)
                signalIcon = R.drawable.ic_signal_cellular_4_bar_grey_700_18dp;
            else if (signalStrength >= 75)
                signalIcon = R.drawable.ic_signal_cellular_3_bar_grey_700_18dp;
            else if (signalStrength >= 50)
                signalIcon = R.drawable.ic_signal_cellular_2_bar_grey_700_18dp;
            else if (signalStrength >= 25)
                signalIcon = R.drawable.ic_signal_cellular_1_bar_grey_700_18dp;
            else
                signalIcon = R.drawable.ic_signal_cellular_0_bar_grey_700_18dp;

            signalTelem.setText(String.format(Locale.ENGLISH, "%d%%", signalStrength));
            signalTelem.setCompoundDrawablesWithIntrinsicBounds(signalIcon, 0, 0, 0);

            signalPopup.update();
            return false;
        }
    });

    private String electricChargeToString(double chargeInmAh) {
        double absCharge = Math.abs(chargeInmAh);
        if (absCharge >= 1000) {
            return String.format(Locale.US, "%2.1f Ah", chargeInmAh / 1000);
        } else {
            return String.format(Locale.ENGLISH, "%2.0f mAh", chargeInmAh);
        }
    }

    private static boolean checkGpsCoordination(double latitude, double longitude) {
        if (!Double.isNaN(latitude) && !Double.isNaN(longitude)){
            return (latitude > -90 && latitude < 90 && longitude > -180 && longitude < 180) && (latitude != 0f && longitude != 0f);
        }
        else{
            return false;
        }

    }

}
