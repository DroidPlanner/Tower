package org.droidplanner.android.tlog

import android.net.Uri
import android.os.AsyncTask
import android.os.Handler
import com.o3dr.android.client.utils.data.tlog.TLogParser
import com.o3dr.android.client.utils.data.tlog.TLogParser.TLogIterator
import timber.log.Timber
import java.io.FileNotFoundException
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * @author ne0fhyk (Fredia Huya-Kouadio)
 */
class TLogDataLoader(activity: TLogActivity, val handler: Handler) : AsyncTask<Uri, Void, Boolean>() {

    private companion object {
        const val EVENT_UPDATE_THRESHOLD = 5000
        const val MIN_UPDATE_DELAY = 1000L //1 second
    }

    private val publishProgress = object: Runnable {
        override fun run() {
            handler.removeCallbacks(this)
            activityRef.get()?.onTLogLoadedData(grabData(), true)
        }

    }

    private val activityRef = WeakReference<TLogActivity>(activity)

    private val allEvents = ConcurrentLinkedQueue<TLogParser.Event>()

    override fun doInBackground(vararg params: Uri): Boolean {
        val context = activityRef.get()?.applicationContext ?: return false
        try {
            for (uri in params) {
                if(isCancelled)
                    break

                val iterator = TLogIterator(context, uri, handler)
                iterator.start()

                var eventCounter = 0
                var lastUpdate = System.currentTimeMillis()
                var event = iterator.blockingNext()
                while(event != null && !isCancelled){
                    allEvents.add(event)
                    eventCounter++
                    if(eventCounter >= EVENT_UPDATE_THRESHOLD){
                        val currentTime = System.currentTimeMillis()
                        if(currentTime - lastUpdate >= MIN_UPDATE_DELAY) {
                            handler.post(publishProgress)
                            lastUpdate = currentTime
                        }
                        eventCounter = 0
                    }

                    event = iterator.blockingNext()
                }

                iterator.finish()
            }
            return true
        }catch(e: FileNotFoundException){
            Timber.e(e, "Error occurred while loading tlog data")
            return false
        }
        finally{
            handler.removeCallbacks(publishProgress)
        }
    }

    private fun grabData(): List<TLogParser.Event> {
        val nextBatch = mutableListOf<TLogParser.Event>()
        var event = allEvents.poll()
        while (event != null) {
            nextBatch.add(event)
            event = allEvents.poll()
        }
        return nextBatch
    }

    override fun onCancelled(){
        activityRef.get()?.onTLogLoadedData(grabData(), false)
    }

    override fun onPostExecute(result: Boolean) {
        activityRef.get()?.onTLogLoadedData(grabData(), false)
    }

}