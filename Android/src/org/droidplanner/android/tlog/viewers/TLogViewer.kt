package org.droidplanner.android.tlog.viewers

import android.app.Activity
import org.droidplanner.android.fragments.helpers.ApiListenerFragment
import org.droidplanner.android.tlog.interfaces.TLogDataProvider
import org.droidplanner.android.tlog.interfaces.TLogDataSubscriber

/**
 * @author ne0fhyk (Fredia Huya-Kouadio)
 */
abstract class TLogViewer : ApiListenerFragment(), TLogDataSubscriber {

    private var tlogDataProvider : TLogDataProvider? = null

    override fun onAttach(activity: Activity){
        super.onAttach(activity)

        if(activity !is TLogDataProvider){
            throw IllegalStateException("Parent activity must implement ${TLogDataProvider::class.java.name}")
        }

        tlogDataProvider = activity
    }

    override fun onApiConnected() {
        tlogDataProvider?.registerForTLogDataUpdate(this)
    }

    override fun onApiDisconnected() {
        tlogDataProvider?.unregisterForTLogDataUpdate(this)
        onClearTLogData()
    }

    override fun onDetach(){
        super.onDetach()
        tlogDataProvider = null
    }

}