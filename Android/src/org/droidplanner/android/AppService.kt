package org.droidplanner.android

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.IBinder
import android.support.v4.content.LocalBroadcastManager
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent
import com.o3dr.services.android.lib.drone.attribute.AttributeEventExtra
import org.droidplanner.android.notifications.NotificationHandler

/**
 * Created by Fredia Huya-Kouadio on 9/24/15.
 */
public class AppService : Service() {

    companion object {
        private val filter = initFilter()

        private fun initFilter(): IntentFilter {
            val temp = IntentFilter()
            temp.addAction(AttributeEvent.STATE_CONNECTED)
            temp.addAction(AttributeEvent.STATE_DISCONNECTED)
            temp.addAction(AttributeEvent.AUTOPILOT_ERROR)
            return temp
        }
    }

    private val receiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            when(intent?.action){
                AttributeEvent.STATE_CONNECTED -> notificationHandler?.init()
                AttributeEvent.STATE_DISCONNECTED -> notificationHandler?.terminate()
                AttributeEvent.AUTOPILOT_ERROR -> {
                    val errorName = intent?.getStringExtra(AttributeEventExtra.EXTRA_AUTOPILOT_ERROR_ID)
                    notificationHandler?.onAutopilotError(errorName)
                }
            }
        }
    }

    class BinderHandler : Binder(){}

    private val binder = BinderHandler()

    private var notificationHandler : NotificationHandler? = null

    override fun onCreate(){
        super.onCreate()

        val dpApp = application as DroidPlannerApp
        val drone = dpApp.drone

        notificationHandler = NotificationHandler(applicationContext, drone)

        if(drone.isConnected){
            notificationHandler?.init()
        }

        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(receiver, filter)
    }

    override fun onDestroy(){
        super.onDestroy()
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(receiver)

        notificationHandler?.terminate()
    }

    override fun onBind(intent: Intent?) = binder
}