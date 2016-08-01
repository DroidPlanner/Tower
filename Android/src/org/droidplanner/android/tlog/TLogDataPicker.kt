package org.droidplanner.android.tlog

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
import org.droidplanner.android.droneshare.data.SessionContract
import org.droidplanner.android.tlog.adapters.TLogDataAdapter
import org.droidplanner.android.tlog.adapters.TLogDataAdapter.TLogSelectionListener

/**
 * TLog data picker dialog
 * Created by fhuya on 6/12/2016.
 */
class TLogDataPicker : DialogFragment(){

    private val noTLogMessageView by lazy {
        getView()?.findViewById(R.id.no_tlogs_message)
    }

    private var selectionListener : TLogDataAdapter.TLogSelectionListener? = null

    private val selectionListenerWrapper = object : TLogSelectionListener {
        override fun onTLogSelected(tlogSession: SessionContract.SessionData) {
            selectionListener?.onTLogSelected(tlogSession)
            dismissAllowingStateLoss()
        }
    }

    override fun onAttach(activity: Activity){
        super.onAttach(activity)
        if(activity !is TLogDataAdapter.TLogSelectionListener){
            throw IllegalStateException("Parent activity must implement " +
                    "${TLogDataAdapter.TLogSelectionListener::class.java.name}")
        }

        selectionListener = activity
    }

    override fun onDetach(){
        super.onDetach()
        selectionListener = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) : View? {
        return inflater.inflate(R.layout.fragment_tlog_data_picker, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)

        noTLogMessageView?.setOnClickListener {
            dismissAllowingStateLoss()
        }

        val tlogsView = view.findViewById(R.id.tlogs_selector) as RecyclerView?
        tlogsView?.setHasFixedSize(true)

        // Use a linear layout manager
        val layoutMgr = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        tlogsView?.setLayoutManager(layoutMgr)

        val adapter = TLogDataAdapter(activity.getApplication() as DroidPlannerApp)
        adapter.setTLogSelectionListener(selectionListenerWrapper)
        tlogsView?.adapter = adapter

        if (adapter.itemCount == 0) {
            tlogsView?.visibility = View.GONE
            noTLogMessageView?.visibility = View.VISIBLE
        }
        else {
            tlogsView?.visibility = View.VISIBLE
            noTLogMessageView?.visibility = View.GONE
        }
    }
}