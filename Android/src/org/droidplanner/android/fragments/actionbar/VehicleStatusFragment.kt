package org.droidplanner.android.fragments.actionbar

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent
import com.o3dr.services.android.lib.drone.attribute.AttributeType
import com.o3dr.services.android.lib.drone.property.Battery
import com.o3dr.services.android.lib.drone.property.Signal
import com.o3dr.services.android.lib.util.MathUtils
import org.droidplanner.android.R
import org.droidplanner.android.fragments.helpers.ApiListenerFragment
import kotlin.platform.platformStatic
import kotlin.properties.Delegates

/**
 * Created by Fredia Huya-Kouadio on 7/24/15.
 */
public class VehicleStatusFragment : ApiListenerFragment() {

    companion object {
        private val filter = initFilter()

        private fun initFilter(): IntentFilter {
            val filter = IntentFilter()

            filter.addAction(AttributeEvent.STATE_CONNECTED)
            filter.addAction(AttributeEvent.STATE_DISCONNECTED)
            filter.addAction(AttributeEvent.BATTERY_UPDATED)
            filter.addAction(AttributeEvent.SIGNAL_UPDATED)

            return filter
        }
    }

    private val receiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context, intent: Intent) {
            when(intent.getAction()){
                AttributeEvent.STATE_CONNECTED -> updateAllStatus()

                AttributeEvent.STATE_DISCONNECTED -> updateAllStatus()

                AttributeEvent.SIGNAL_UPDATED -> updateSignalStatus()

                AttributeEvent.BATTERY_UPDATED -> updateBatteryStatus()
            }
        }
    }

    private val connectedIcon by Delegates.lazy {
        getView()?.findViewById(R.id.status_vehicle_connection) as ImageView?
    }

    private val batteryIcon by Delegates.lazy {
        getView()?.findViewById(R.id.status_vehicle_battery) as ImageView?
    }

    private val signalIcon by Delegates.lazy {
        getView()?.findViewById(R.id.status_vehicle_signal) as ImageView?
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View?{
        return inflater?.inflate(R.layout.fragment_vehicle_status, container, false)
    }

    override fun onApiConnected() {
        updateAllStatus()
        getBroadcastManager().registerReceiver(receiver, filter)
    }

    override fun onApiDisconnected() {
        getBroadcastManager().unregisterReceiver(receiver)
        updateAllStatus()
    }

    private fun updateAllStatus(){
        updateSignalStatus()
        updateBatteryStatus()
        updateConnectionStatus()
    }

    private fun updateConnectionStatus() {
        val drone = getDrone()
        connectedIcon?.setImageLevel(
                if(drone == null || !drone.isConnected())
                    0
                else
                    1
        )
    }

    private fun updateBatteryStatus() {
        val drone = getDrone()
        batteryIcon?.setImageLevel(
                if(drone == null || !drone.isConnected()){
                    0
                }
                else{
                    val battery: Battery = drone.getAttribute(AttributeType.BATTERY)
                    val battRemain = battery.getBatteryRemain()

                    if (battRemain >= 100) {
                        8
                    } else if (battRemain >= 87.5) {
                        7
                    } else if (battRemain >= 75) {
                        6
                    } else if (battRemain >= 62.5) {
                        5
                    } else if (battRemain >= 50) {
                        4
                    } else if (battRemain >= 37.5) {
                        3
                    } else if (battRemain >= 25) {
                        2
                    } else if (battRemain >= 12.5) {
                        1
                    } else {
                        0
                    }
                }
        )
    }

    private fun updateSignalStatus(){
        val drone = getDrone()
        signalIcon?.setImageLevel(
                if(drone == null || !drone.isConnected()){
                    0
                }
                else{
                    val signal: Signal = drone.getAttribute(AttributeType.SIGNAL)
                    val signalStrength = MathUtils.getSignalStrength(signal.getFadeMargin(), signal.getRemFadeMargin())
                    if (signalStrength >= 100)
                        5
                    else if (signalStrength >= 80)
                        4
                    else if (signalStrength >= 60)
                        3
                    else if (signalStrength >= 40)
                        2
                    else if (signalStrength >= 20)
                        1
                    else
                        0
                }
        )
    }
}