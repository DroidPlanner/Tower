package org.droidplanner.android.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.droidplanner.android.R;
import org.droidplanner.android.activities.WidgetActivity;
import org.droidplanner.android.fragments.widget.TowerWidgets;

public class WidgetsListFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_widgets_list, container, false);

        final ViewGroup widgetsContainer = (ViewGroup) view.findViewById(R.id.widgets_list_container);

        final FragmentManager fm = getChildFragmentManager();
        final TowerWidgets[] towerWidgets = TowerWidgets.values();

        final Context context = getActivity().getApplicationContext();
        for (TowerWidgets towerWidget : towerWidgets) {
            final @IdRes int widgetId = towerWidget.getIdRes();

            //Inflate the widget container
            final View widgetView = inflater.inflate(R.layout.container_telemetry_widget, widgetsContainer, false);

            final View contentHolder = widgetView.findViewById(R.id.widget_container);
            contentHolder.setId(widgetId);

            //Add the widget holder to the container
            widgetsContainer.addView(widgetView);

            //Add the widget fragment to the widget holder
            final Fragment widgetFragment = towerWidget.getMinimizedFragment();
            fm.beginTransaction().add(widgetId, widgetFragment).commit();

            if (towerWidget.canMaximize()) {
                final View maximizeView = widgetView.findViewById(R.id.widget_maximize_button);
                maximizeView.setVisibility(View.VISIBLE);

                maximizeView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(context, WidgetActivity.class)
                                .putExtra(WidgetActivity.EXTRA_WIDGET_ID, widgetId));
                    }
                });
            }
        }

        return view;
    }
}
