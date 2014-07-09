package org.droidplanner.android.wear;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.support.wearable.view.GridViewPager;
import android.support.wearable.view.WatchViewStub;

import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;

import org.droidplanner.android.wear.views.WearUIPagerAdapter;

public class WearUI extends Activity implements MessageApi.MessageListener, NodeApi.NodeListener {

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

                final FragmentGridPagerAdapter pagerAdapter = new WearUIPagerAdapter(getFragmentManager());
                mViewPager.setAdapter(pagerAdapter);
            }
        });
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

    }

    @Override
    public void onPeerConnected(Node node) {

    }

    @Override
    public void onPeerDisconnected(Node node) {

    }
}
