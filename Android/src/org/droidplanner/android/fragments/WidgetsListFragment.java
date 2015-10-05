package org.droidplanner.android.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.droidplanner.android.R;
import org.droidplanner.android.activities.WidgetActivity;
import org.droidplanner.android.fragments.widget.TowerWidget;
import org.droidplanner.android.fragments.widget.TowerWidgets;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;

import timber.log.Timber;

public class WidgetsListFragment extends Fragment {

    private static final IntentFilter filter = new IntentFilter(SettingsFragment.ACTION_WIDGET_PREFERENCE_UPDATED);

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case SettingsFragment.ACTION_WIDGET_PREFERENCE_UPDATED:
                    final String widgetPrefKey = intent.getStringExtra(SettingsFragment.EXTRA_WIDGET_PREF_KEY);
                    final boolean addWidget = intent.getBooleanExtra(SettingsFragment.EXTRA_ADD_WIDGET, false);

                    final TowerWidgets widget = TowerWidgets.getWidgetByPrefKey(widgetPrefKey);
                    if(widget != null){
                        if(addWidget)
                            addWidget(widget.getIdRes());
                        else
                            removeWidget(widget.getIdRes());
                    }
                    break;
            }
        }
    };

    private ViewGroup widgetsContainer;
    private final SparseArray<View> widgetsViews = new SparseArray<>();
    private DroidPlannerPrefs dpPrefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_widgets_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        widgetsContainer = (ViewGroup) view.findViewById(R.id.widgets_list_container);
        dpPrefs = new DroidPlannerPrefs(getActivity().getApplicationContext());
    }

    @Override
    public void onStart(){
        super.onStart();
        generateWidgetsList();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, filter);
    }

    @Override
    public void onStop(){
        super.onStop();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
    }

    private void removeWidget(int widgetId){
        if(!isAdded())
            return;

        final FragmentManager fm = getChildFragmentManager();
        final TowerWidget widgetFrag = (TowerWidget) fm.findFragmentById(widgetId);
        if(widgetFrag != null){
            fm.beginTransaction().remove(widgetFrag).commitAllowingStateLoss();
        }

        final View widgetView = widgetsViews.get(widgetId);
        if(widgetView != null)
            widgetsContainer.removeView(widgetView);

        widgetsViews.remove(widgetId);
    }

    private void addWidget(final int widgetId){
        if(!isAdded())
            return;

        final TowerWidgets widget = TowerWidgets.getWidgetById(widgetId);
        if(widget == null)
            return;

        if (!dpPrefs.isWidgetEnabled(widget)) {
            removeWidget(widgetId);
            return;
        }

        if(widgetsViews.get(widgetId) != null)
            return;

        final FragmentManager fm = getChildFragmentManager();
        final LayoutInflater inflater = getActivity().getLayoutInflater();

        TowerWidget currentFragment = (TowerWidget) fm.findFragmentById(widgetId);

        //Inflate the widget container
        final View widgetView = inflater.inflate(R.layout.container_telemetry_widget, widgetsContainer, false);

        final View contentHolder = widgetView.findViewById(R.id.widget_container);
        contentHolder.setId(widgetId);

        //Add the widget holder to the container
        widgetsContainer.addView(widgetView, Math.min(widget.ordinal(), widgetsContainer.getChildCount()));

        if (currentFragment != null) {
            //Add the widget fragment to the widget holder
            fm.beginTransaction().remove(currentFragment).commitAllowingStateLoss();
            fm.executePendingTransactions();
        }

        //Add the widget fragment to the widget holder
        fm.beginTransaction().replace(widgetId, widget.getMinimizedFragment()).commitAllowingStateLoss();

        widgetsViews.put(widgetId, widgetView);

        if (widget.canMaximize()) {
            final View maximizeView = widgetView.findViewById(R.id.widget_maximize_button);
            maximizeView.setVisibility(View.VISIBLE);

            maximizeView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getActivity().getApplicationContext(), WidgetActivity.class)
                            .putExtra(WidgetActivity.EXTRA_WIDGET_ID, widgetId));
                }
            });
        }
    }

    private void generateWidgetsList() {
        if (!isAdded())
            return;

        final TowerWidgets[] towerWidgets = TowerWidgets.values();
        for (TowerWidgets towerWidget : towerWidgets) {
            final @IdRes int widgetId = towerWidget.getIdRes();
            addWidget(widgetId);
        }
    }
}
