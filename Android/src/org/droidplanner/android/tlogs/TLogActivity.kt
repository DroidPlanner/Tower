package org.droidplanner.android.tlogs

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.view.Menu
import android.view.MenuItem
import org.droidplanner.android.R
import org.droidplanner.android.activities.DrawerNavigationUI

/**
 * Created by fredia on 6/12/16.
 */
class TLogActivity : DrawerNavigationUI() {

    override fun getNavigationDrawerMenuItemId() = R.id.navigation_locator

    override fun getToolbarId() = R.id.toolbar

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tlog)

        val viewPager = findViewById(R.id.container) as ViewPager?
        viewPager?.adapter = TLogViewerAdapter(supportFragmentManager)

        val tabLayout = findViewById(R.id.tabs) as TabLayout?
        tabLayout?.setupWithViewPager(viewPager)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_locator, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
         when(item.itemId){
            R.id.menu_open_tlog_file -> {
                // Open a dialog showing the app generated tlog files
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }
}