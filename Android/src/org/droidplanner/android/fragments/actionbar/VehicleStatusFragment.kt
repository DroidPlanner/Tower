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
import android.widget.TextView
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent
import com.o3dr.services.android.lib.drone.attribute.AttributeType
import com.o3dr.services.android.lib.drone.property.Battery
import com.o3dr.services.android.lib.drone.property.State
import org.droidplanner.android.R
import org.droidplanner.android.fragments.helpers.ApiListenerFragment
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
            filter.addAction(AttributeEvent.HEARTBEAT_TIMEOUT)
            filter.addAction(AttributeEvent.HEARTBEAT_RESTORED)
            filter.addAction(AttributeEvent.BATTERY_UPDATED)

            return filter
        }
    }

    private val receiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context, intent: Intent) {
            when(intent.action){
                AttributeEvent.STATE_CONNECTED -> updateAllStatus()

                AttributeEvent.STATE_DISCONNECTED -> updateAllStatus()

                AttributeEvent.HEARTBEAT_RESTORED -> updateConnectionStatus()

                AttributeEvent.HEARTBEAT_TIMEOUT -> updateConnectionStatus()

                AttributeEvent.BATTERY_UPDATED -> updateBatteryStatus()
            }
        }
    }

    private var title: CharSequence = ""

    private val connectedIcon by lazy(LazyThreadSafetyMode.NONE) {
        view?.findViewById(R.id.status_vehicle_connection) as ImageView?
    }

    private val batteryIcon by lazy(LazyThreadSafetyMode.NONE) {
        view?.findViewById(R.id.status_vehicle_battery) as ImageView?
    }

    private var titleView: TextView? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View?{
        return inflater?.inflate(R.layout.fragment_vehicle_status, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)

        titleView = view.findViewById(R.id.status_actionbar_title) as TextView?
        titleView?.text = title
    }

    override fun onApiConnected() {
        updateAllStatus()
        broadcastManager.registerReceiver(receiver, filter)
    }

    override fun onApiDisconnected() {
        broadcastManager.unregisterReceiver(receiver)
        updateAllStatus()
    }

    private fun updateAllStatus(){
        updateBatteryStatus()
        updateConnectionStatus()
    }

    private fun updateConnectionStatus() {
        val drone = drone
        connectedIcon?.setImageLevel(
                if(drone == null || !drone.isConnected)
                    0
                else {
                    val state: State = drone.getAttribute(AttributeType.STATE)
                    if (state.isTelemetryLive)
                        2
                    else
                        1
                }
        )
    }

    fun setTitle(title: CharSequence){
        this.title = title
        titleView?.text = title
    }

    private fun updateBatteryStatus() {
        val drone = drone
        batteryIcon?.setImageLevel(
                if(drone == null || !drone.isConnected){
                    0
                }
                else{
                    val battery: Battery = drone.getAttribute(AttributeType.BATTERY)
                    val battRemain = battery.batteryRemain

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
}