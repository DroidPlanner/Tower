package org.droidplanner.android.tlog

import android.net.Uri
import android.os.AsyncTask
import android.os.Handler
import com.MAVLink.common.msg_global_position_int
import com.o3dr.android.client.utils.data.tlog.TLogParser
import com.o3dr.android.client.utils.data.tlog.TLogParser.TLogIterator
import java.io.FileNotFoundException
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * @author ne0fhyk (Fredia Huya-Kouadio)
 */
class TLogDataLoader(activity: TLogActivity, val handler: Handler) : AsyncTask<Uri, Void, Boolean>() {

    private val activityRef = WeakReference<TLogActivity>(activity)

    val allEvents = ConcurrentLinkedQueue<TLogParser.Event>()
    private val positionEvents = ConcurrentLinkedQueue<TLogParser.Event>()

    override fun doInBackground(vararg params: Uri): Boolean {
        try {
            for (uri in params) {
                if(isCancelled)
                    break

                val iterator = TLogIterator(uri, handler)
                iterator.start()

                var event = iterator.blockingNext()
                while(event != null && !isCancelled){
                    allEvents.add(event)
                    if(event.mavLinkMessage is msg_global_position_int){
                        positionEvents.add(event)
                    }

                    event = iterator.blockingNext()
                }

                iterator.finish()
            }
            return true
        }catch(e: FileNotFoundException){
            return false
        }
    }

    override fun onCancelled(){
        activityRef.get()?.onTLogLoadCompleted(false)
    }

    override fun onPostExecute(result: Boolean) {
        activityRef.get()?.onTLogLoadCompleted(result)
    }

}