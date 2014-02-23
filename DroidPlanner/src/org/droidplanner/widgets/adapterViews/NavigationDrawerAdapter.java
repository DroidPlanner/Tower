package org.droidplanner.widgets.adapterViews;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import org.droidplanner.R;
import org.droidplanner.activities.ConfigurationActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapter for the navigation drawer items
 */
public class NavigationDrawerAdapter extends SimpleExpandableListAdapter {

    /**
     * Keys used to access the group and child data values.
     */
    private static final String KEY_SECTION_NAME = "key_section_name";
    private static final String KEY_SECTION_ICON = "key_section_icon";

    private static final String[] mGroupFrom = {KEY_SECTION_NAME, KEY_SECTION_ICON};
    private static final String[] mChildFrom = mGroupFrom;

    //TODO: complete
    private static final int[] mGroupTo = {R.id.nav_drawer_group};
    private static final int[] mChildTo = {R.id.nav_drawer_child};

    /**
     * Each entry in the list corresponds to one section in the app.
     */
    private static final List<Map<String, Integer>> mGroupData = new ArrayList<Map<String,
            Integer>>();

    static {
        //Flight section
        final Map<String, Integer> flightData = new HashMap<String, Integer>();
        flightData.put(KEY_SECTION_NAME, R.string.flight_data);
        flightData.put(KEY_SECTION_ICON, R.drawable.ic_action_plane);
        mGroupData.add(flightData);

        //Settings section
        final Map<String, Integer> droneSetupData = new HashMap<String, Integer>();
        droneSetupData.put(KEY_SECTION_NAME, R.string.settings);
        droneSetupData.put(KEY_SECTION_ICON, R.drawable.ic_action_settings);
        mGroupData.add(droneSetupData);

        //Help section
        final Map<String, Integer> helpData = new HashMap<String, Integer>();
        helpData.put(KEY_SECTION_NAME, R.string.help);
        helpData.put(KEY_SECTION_ICON, R.drawable.ic_action_help);
        mGroupData.add(helpData);
    }

    /**
     * Stores the app subsections' data.
     */
    private static final List<List<Map<String, Integer>>> mChildData = new
            ArrayList<List<Map<String, Integer>>>();

    static {
        /*
        Flight section
         */
        //Editor activity
        Map<String, Integer> editorData = new HashMap<String, Integer>();
        editorData.put(KEY_SECTION_NAME, R.string.editor);
        editorData.put(KEY_SECTION_ICON, R.drawable.ic_action_location);

        final List<Map<String, Integer>> flightGroup = new ArrayList<Map<String, Integer>>();
        flightGroup.add(editorData);
        mChildData.add(flightGroup);

        /*
        Settings section
         */
        //Retrieve the elements in the settings section from the ConfigurationActivity
        final List<Map<String, Integer>> setupGroup = new ArrayList<Map<String, Integer>>();

        final int configurationFragmentsCount = ConfigurationActivity.sConfigurationFragments
                .length;
        for (int i = 0; i < configurationFragmentsCount; i++) {
            final Map<String, Integer> configData = new HashMap<String, Integer>();
            configData.put(KEY_SECTION_NAME, ConfigurationActivity
                    .sConfigurationFragmentTitlesRes[i]);
            configData.put(KEY_SECTION_ICON, ConfigurationActivity
                    .sConfigurationFragmentIconRes[i]);

            setupGroup.add(configData);
        }

        mChildData.add(setupGroup);

        /*
        Help section
         */
        //No children for the help section
        mChildData.add(Collections.<Map<String, Integer>>emptyList());
    }

    public NavigationDrawerAdapter(Context context) {
        super(context, mGroupData, R.layout.adapter_nav_drawer_group, mGroupFrom, mGroupTo,
                mChildData, R.layout.adapter_nav_drawer_child, mChildFrom, mChildTo);
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                             View convertView, ViewGroup parent) {
        View v;
        if (convertView == null) {
            v = newChildView(isLastChild, parent);
        } else {
            v = convertView;
        }
        bindView(v, mChildData.get(groupPosition).get(childPosition), mChildFrom, mChildTo);
        return v;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                             ViewGroup parent) {
        View v;
        if (convertView == null) {
            v = newGroupView(isExpanded, parent);
        } else {
            v = convertView;
        }

        final ImageView expandIcon = (ImageView) v.findViewById(R.id.nav_drawer_group_expand_icon);
        if (getChildrenCount(groupPosition) == 0) {
            expandIcon.setVisibility(View.GONE);
        } else {
            expandIcon.setVisibility(View.VISIBLE);
            expandIcon.setImageResource(isExpanded
                    ? R.drawable.expandable_listview_icon_expanded
                    : R.drawable.expandable_listview_icon_collapsed);
        }

        bindView(v, mGroupData.get(groupPosition), mGroupFrom, mGroupTo);
        return v;
    }

    private void bindView(View view, Map<String, Integer> data, String[] from, int[] to) {
        int len = to.length;

        for (int i = 0; i < len; i += 2) {
            TextView v = (TextView) view.findViewById(to[i]);
            if (v != null) {
                v.setText(data.get(from[i]));
                v.setCompoundDrawablesWithIntrinsicBounds(data.get(from[i + 1]), 0, 0, 0);
            }
        }
    }
}
