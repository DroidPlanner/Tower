package org.droidplanner.android.fragments.widget.telemetry

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent
import org.droidplanner.android.R
import org.droidplanner.android.dialogs.SupportYesNoDialog
import org.droidplanner.android.fragments.widget.TowerWidget
import org.droidplanner.android.fragments.widget.TowerWidgets
import java.lang.String

/**
 * Created by Fredia Huya-Kouadio on 9/20/15.
 */
public class MiniWidgetFlightTimer : TowerWidget(), SupportYesNoDialog.Listener {

    companion object {
        private val FLIGHT_TIMER_PERIOD = 1000L; //1 second

        @JvmStatic protected val RESET_TIMER_TAG = "reset_timer_tag"

        private val filter = IntentFilter(AttributeEvent.STATE_UPDATED)
    }

    private val receiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context, intent: Intent){
            when(intent.action){
                AttributeEvent.STATE_UPDATED -> updateFlightTimer()
            }
        }
    }

    private val flightTimeUpdater = object : Runnable{
        override fun run(){
            handler.removeCallbacks(this)
            val drone = drone
            if(!drone.isConnected)
                return

            val timeInSecs = drone.flightTime
            val mins = timeInSecs / 60L
            val secs = timeInSecs % 60L
            flightTimer?.text = String.format("%02d:%02d", mins, secs)

            handler.postDelayed(this, FLIGHT_TIMER_PERIOD)
        }
    }

    private val handler = Handler()

    private var flightTimer : TextView? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View?{
        return inflater?.inflate(R.layout.fragment_mini_widget_flight_timer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)

        val context = activity.applicationContext

        flightTimer = view.findViewById(R.id.flight_timer) as TextView?
        flightTimer?.setOnClickListener {
            //Bring up a dialog allowing the user to reset the timer.
            val resetTimerDialog = SupportYesNoDialog.newInstance(context, RESET_TIMER_TAG,
                    context.getString(R.string.label_widget_flight_timer),
                    context.getString(R.string.description_reset_flight_timer))
            resetTimerDialog.show(childFragmentManager, RESET_TIMER_TAG)
        }
    }

    override fun onDialogNo(dialogTag: kotlin.String?) {
    }

    override fun onDialogYes(dialogTag: kotlin.String?) {
        when(dialogTag){
            RESET_TIMER_TAG -> {
                drone.resetFlightTimer()
                updateFlightTimer()
            }
        }
    }

    override fun getWidgetType() = TowerWidgets.FLIGHT_TIMER

    override fun onApiConnected() {
        updateFlightTimer()
        broadcastManager.registerReceiver(receiver, filter)
    }

    override fun onApiDisconnected() {
        broadcastManager.unregisterReceiver(receiver)
    }

    private fun updateFlightTimer(){
        handler.removeCallbacks(flightTimeUpdater)
        if(drone.isConnected)
            flightTimeUpdater.run()
        else
            flightTimer?.text = "00:00"
    }
}