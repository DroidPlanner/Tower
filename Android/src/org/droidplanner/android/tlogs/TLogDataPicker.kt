package org.droidplanner.android.tlogs

import android.app.Activity
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.droidplanner.android.DroidPlannerApp
import org.droidplanner.android.R
import org.droidplanner.android.tlogs.TLogDataAdapter.TLogSelectionListener
import java.io.File

/**
 * TLog data picker dialog
 * Created by fhuya on 6/12/2016.
 */
class TLogDataPicker : DialogFragment(){

    private var selectionListener : TLogDataAdapter.TLogSelectionListener? = null

    private val selectionListenerWrapper = object : TLogSelectionListener {
        override fun onTLogSelected(tlogFile: File) {
            selectionListener?.onTLogSelected(tlogFile)
            dismissAllowingStateLoss()
        }
    }

    override fun onAttach(activity: Activity){
        if(activity !is TLogDataAdapter.TLogSelectionListener){
            throw IllegalStateException("Parent activity must implement " +
                    "${TLogDataAdapter.TLogSelectionListener::class.java.name}")
        }

        selectionListener = activity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) : View? {
        return inflater.inflate(R.layout.fragment_tlog_data_picker, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)

        val tlogsView = view.findViewById(R.id.tlogs_selector) as RecyclerView?
        tlogsView?.setHasFixedSize(true)

        // Use a linear layout manager
        val layoutMgr = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        tlogsView?.setLayoutManager(layoutMgr)

        val adapter = TLogDataAdapter(activity.getApplication() as DroidPlannerApp)
        adapter.setTLogSelectionListener(selectionListenerWrapper)
        tlogsView?.adapter = adapter
    }
}