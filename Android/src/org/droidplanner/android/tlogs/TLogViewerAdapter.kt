package org.droidplanner.android.tlogs

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

/**
 * Return the appropriate fragment for the selected tlog data viewer.
 *
 * @author ne0fhyk (Fredia Huya-Kouadio)
 */
class TLogViewerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
    override fun getItem(position: Int): Fragment? {
        return when(position){
            0 -> TLogPositionViewer()
            1 -> TLogRawViewer()
            else -> throw IllegalStateException("Invalid viewer index.")
        }
    }

    override fun getCount() = 2

    override fun getPageTitle(position: Int): CharSequence? {
        return when(position){
            0 -> "Position"
            1 -> "Raw"
            else -> throw IllegalStateException("Invalid viewer index.")
        }
    }
}