package org.droidplanner.android.tlog.adapters

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import org.droidplanner.android.tlog.viewers.TLogPositionViewer
import org.droidplanner.android.tlog.viewers.TLogRawViewer

/**
 * Return the appropriate fragment for the selected tlog data viewer.
 *
 * @author ne0fhyk (Fredia Huya-Kouadio)
 */
class TLogViewerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
    override fun getItem(position: Int): Fragment? {
        return when(position){
            1 -> TLogRawViewer()
            0 -> TLogPositionViewer()
            else -> throw IllegalStateException("Invalid viewer index.")
        }
    }

    override fun getCount() = 2

    override fun getPageTitle(position: Int): CharSequence? {
        return when(position){
            1 -> "All"
            0 -> "Position"
            else -> throw IllegalStateException("Invalid viewer index.")
        }
    }
}