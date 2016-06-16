package org.droidplanner.android.tlogs

import android.app.Activity
import com.o3dr.android.client.utils.data.tlog.TLogParser
import org.droidplanner.android.fragments.helpers.ApiListenerFragment

/**
 * @author ne0fhyk (Fredia Huya-Kouadio)
 */
abstract class TLogDataSubscriber : ApiListenerFragment() {

    private var tlogDataProvider : TLogDataProvider? = null

    override fun onAttach(activity: Activity){
        super.onAttach(activity)

        if(activity !is TLogDataProvider){
            throw IllegalStateException("Parent activity must implement ${TLogDataProvider::class.java.name}")
        }

        tlogDataProvider = activity
    }

    override fun onApiConnected() {}

    override fun onApiDisconnected() {}

    override fun onDetach(){
        super.onDetach()
        tlogDataProvider = null
    }

    override fun onStart(){
        super.onStart()
        tlogDataProvider?.registerForTLogDataUpdate(this)
    }

    override fun onStop(){
        super.onStop()
        tlogDataProvider?.unregisterForTLogDataUpdate(this)
    }

    abstract fun onTLogDataLoaded(events: List<TLogParser.Event>)
}