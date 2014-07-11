package org.droidplanner.android.wear;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.support.wearable.view.GridViewPager;
import android.support.wearable.view.WatchViewStub;

import org.droidplanner.android.wear.views.WearUIPagerAdapter;

public class WearUI extends Activity {

    private GridViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear_ui);

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mViewPager = (GridViewPager) stub.findViewById(R.id.grid_view_pager);

                final FragmentGridPagerAdapter pagerAdapter = new WearUIPagerAdapter
                        (getFragmentManager());
                mViewPager.setAdapter(pagerAdapter);
            }
        });
    }

}
