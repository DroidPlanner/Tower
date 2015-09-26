package org.droidplanner.android

import android.annotation.TargetApi
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Binder
import android.os.Build
import android.support.v4.content.LocalBroadcastManager
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent
import com.o3dr.services.android.lib.drone.attribute.AttributeEventExtra
import org.droidplanner.android.notifications.NotificationHandler
import org.droidplanner.android.utils.NetworkUtils
import timber.log.Timber

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

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            when (intent?.action) {
                AttributeEvent.STATE_CONNECTED -> {
                    notificationHandler?.init()

                    if(NetworkUtils.isOnSoloNetwork(context)){
                        bringUpCellularNetwork(context)
                    }
                }

                AttributeEvent.STATE_DISCONNECTED -> {
                    notificationHandler?.terminate()
                    stopSelf()
                }

                AttributeEvent.AUTOPILOT_ERROR -> {
                    val errorName = intent?.getStringExtra(AttributeEventExtra.EXTRA_AUTOPILOT_ERROR_ID)
                    notificationHandler?.onAutopilotError(errorName)
                }
            }
        }
    }

    class BinderHandler : Binder() {}

    private val binder = BinderHandler()

    private var notificationHandler: NotificationHandler? = null

    override fun onCreate() {
        super.onCreate()

        val dpApp = application as DroidPlannerApp
        val drone = dpApp.drone

        notificationHandler = NotificationHandler(applicationContext, drone)

        if (drone.isConnected) {
            notificationHandler?.init()

            if(NetworkUtils.isOnSoloNetwork(applicationContext)){
                bringUpCellularNetwork(applicationContext)
            }
        }

        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(receiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(receiver)

        notificationHandler?.terminate()

        bringDownCellularNetwork()
    }

    override fun onBind(intent: Intent?) = binder

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun bringUpCellularNetwork(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            return

        Timber.i("Setting up cellular network request.")
        val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val builder = NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)

        connMgr.requestNetwork(builder.build(), object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Timber.d("Setting up process default network: %s", network)
                ConnectivityManager.setProcessDefaultNetwork(network)
                DroidPlannerApp.setCellularNetworkAvailability(true)
            }
        })
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun bringDownCellularNetwork() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            return

        Timber.d("Bringing down cellular network access.")
        ConnectivityManager.setProcessDefaultNetwork(null)
        DroidPlannerApp.setCellularNetworkAvailability(false)
    }
}