package org.droidplanner.android.tlogs

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.droidplanner.android.DroidPlannerApp
import java.io.File
import java.util.*

/**
 * Created by fhuya on 6/12/2016.
 */
class TLogDataAdapter(app: DroidPlannerApp) : RecyclerView.Adapter<TLogDataAdapter.ViewHolder>() {

    companion object {
        private const val TLOG_FILENAME_EXT = ".tlog"
    }

    class ViewHolder(val container: View, val dataTimestamp: TextView) : RecyclerView.ViewHolder(container)

    private val tlogsFiles = ArrayList<Pair<Date, File>>()

    init {
        // Query the tlogs directory, and populate the adapter with the files present
        val tlogsDir = app.tLogsDirectory
        if(tlogsDir != null && tlogsDir.isDirectory){
            val files = tlogsDir.listFiles { fileDir, filename ->
                // Check if this is a tlog file
                filename.endsWith(TLOG_FILENAME_EXT)
            }

            for(file in files){
                // Parse the filename to retrieve the creation date
                val filename = file.nameWithoutExtension

            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {

    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder? {

    }

    override fun getItemCount() = tlogsFiles.size
}