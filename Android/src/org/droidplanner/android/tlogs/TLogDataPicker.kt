package org.droidplanner.android.tlogs

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.droidplanner.android.R

/**
 * Created by fhuya on 6/12/2016.
 */
class TLogDataPicker : DialogFragment(){

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

        tlogsView?.adapter = TLogsAdapter()
    }
}