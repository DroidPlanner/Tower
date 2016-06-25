package org.droidplanner.android.tlog.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.o3dr.android.client.utils.data.tlog.TLogUtils
import org.droidplanner.android.DroidPlannerApp
import org.droidplanner.android.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by fhuya on 6/12/2016.
 */
class TLogDataAdapter(app: DroidPlannerApp) : RecyclerView.Adapter<TLogDataAdapter.ViewHolder>() {

    companion object {
        private const val TLOG_FILENAME_EXT = ".tlog"
        private val dateFormatter = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US)
    }

    interface TLogSelectionListener {
        fun onTLogSelected(tlogFile: File)
    }

    class ViewHolder(val container: View, val dataTimestamp: TextView) : RecyclerView.ViewHolder(container)

    private var tlogSelectionListener : TLogSelectionListener? = null
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
                val filename = file.name
                val creationDate = TLogUtils.parseTLogConnectionTimestamp(filename) ?: continue

                tlogsFiles.add(Pair(creationDate, file))
            }

            //Sort the tlog files per date
            if(tlogsFiles.isNotEmpty()){
                //TODO: use Collections.sort for the sorting
            }
        }
    }

    fun setTLogSelectionListener(listener: TLogSelectionListener?){
        this.tlogSelectionListener = listener
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (date, tlogFile) = tlogsFiles[position]
        holder.dataTimestamp.text = dateFormatter.format(date)
        holder.dataTimestamp.setOnClickListener {
            // Notify the listener of the selected tlog file
            tlogSelectionListener?.onTLogSelected(tlogFile)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder? {
        val containerView = LayoutInflater.from(parent.context).inflate(R.layout.list_item_tlog_data, parent, false)
        val dataTimestamp = containerView.findViewById(R.id.tlog_data_timestamp) as TextView
        return ViewHolder(containerView, dataTimestamp)
    }

    override fun getItemCount() = tlogsFiles.size
}