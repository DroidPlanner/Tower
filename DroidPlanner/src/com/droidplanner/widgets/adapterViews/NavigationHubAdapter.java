package com.droidplanner.widgets.adapterViews;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import com.droidplanner.R;
import com.droidplanner.activitys.ConfigurationActivity;
import com.droidplanner.activitys.EditorActivity;
import com.droidplanner.activitys.FlightActivity;
import com.droidplanner.activitys.helpers.SuperUI;

import java.util.ArrayList;
import java.util.List;

/**
 * ExpandableListAdapter to provide views for the app navigation hub interface.
 *
 * @author fhuya
 * @since 1.2.0
 */
public class NavigationHubAdapter extends BaseExpandableListAdapter {

    private static final String TAG = NavigationHubAdapter.class.getName();

    private final DrawerLayout mNavDrawer;

    /**
     * Target expandable listview this adapter is attached to.
     *
     * @since 1.2.0
     */
    private final ExpandableListView mTargetView;

    /**
     * Use to identify which activity is currently hosting the navigation hub drawer.
     *
     * @since 1.2.0
     */
    private final SuperUI mUIActivity;

    /**
     * Use to inflate the layout use by the listview item.
     *
     * @since 1.2.0
     */
    private final LayoutInflater mInflater;

    /**
     * Contains the list of groups displayed by the expandable list view.
     *
     * @since 1.2.0
     */
    private final List<HubGroup> mGroupList;

    /**
     * Set against the expandable listview to handle click on group item.
     * Prevent the group from expanding or collapsing, until the expand icon is pressed.
     *
     * @since 1.2.0
     */
    private final ExpandableListView.OnGroupClickListener mGroupClickListener = new
            ExpandableListView.OnGroupClickListener() {

                @Override
                public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition,
                                            long id) {
                    HubGroup hubGroup = (HubGroup) getGroup(groupPosition);
                    Class<? extends SuperUI> hubClass = hubGroup.getActivityClass();
                    launchActivity(hubClass);
                    return true;
                }
            };

    /**
     * Set agains the expandable listview to handle click events on group child items.
     */
    private final ExpandableListView.OnChildClickListener mChildClickListener = new
            ExpandableListView.OnChildClickListener() {


                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                            int childPosition, long id) {
                    HubGroupItem hubChild = (HubGroupItem) getChild(groupPosition, childPosition);
                    Class<? extends SuperUI> childClass = hubChild.getActivityClass();
                    launchActivity(childClass);
                    return true;
                }
            };

    public NavigationHubAdapter(SuperUI uiActivity, DrawerLayout navDrawer,
                                ExpandableListView targetView) {
        mNavDrawer = navDrawer;
        mUIActivity = uiActivity;
        mInflater = mUIActivity.getLayoutInflater();

        final Context context = uiActivity.getApplicationContext();

        //Add the elements for the supported expandable view.
        mGroupList = new ArrayList<HubGroup>(3);

        //Adding the group for the flight data activity.
        HubGroup flightDataGroup = new HubGroup(context, FlightActivity.class);

        //Adding the editor activity as child of the flight data group.
        flightDataGroup.addChild(new HubGroupItem(context, EditorActivity.class));

        //Adding the group for the Configuration activity.
        HubGroup configGroup = new HubGroup(context, ConfigurationActivity.class);

        mGroupList.add(flightDataGroup);
        mGroupList.add(configGroup);

        mTargetView = targetView;
        mTargetView.setGroupIndicator(null);
        mTargetView.setOnGroupClickListener(mGroupClickListener);
        mTargetView.setOnChildClickListener(mChildClickListener);
    }

    private void launchActivity(Class<? extends SuperUI> activityClass) {
        if (!activityClass.isInstance(mUIActivity)) {
            mUIActivity.startActivity(new Intent(mUIActivity, activityClass));
        }

        mNavDrawer.closeDrawer(mTargetView);
    }

    @Override
    public int getGroupCount() {
        return mGroupList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mGroupList.get(groupPosition).getChildren().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mGroupList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mGroupList.get(groupPosition).getChildren().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return groupPosition * 100 + childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(final int groupPosition, boolean isExpanded, View convertView,
                             ViewGroup
                                     parent) {
        HubGroup hubGroup = (HubGroup) getGroup(groupPosition);

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_nav_hub_group, parent, false);
        }

        //Set the title, and icon for the group.
        TextView hubTitleView = (TextView) convertView.findViewById(R.id.nav_hub_group_title);
        hubTitleView.setText(hubGroup.getHubName());
        hubTitleView.setCompoundDrawablesWithIntrinsicBounds(hubGroup.getIconResource(), 0, 0, 0);

        //Update the expand/collapse icon.
        ImageView expandCollapseIcon = (ImageView) convertView.findViewById(R.id
                .nav_hub_group_expand_icon);
        if (hubGroup.getChildren().isEmpty()) {
            expandCollapseIcon.setVisibility(View.INVISIBLE);
        }
        else {
            if (isExpanded) {
                //Group is expanded. Show the collapse icon
                expandCollapseIcon.setImageResource(R.drawable.listview_collapse_icon);
                expandCollapseIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Onclick, collapse this group.
                        mTargetView.collapseGroup(groupPosition);
                    }
                });
            }
            else {
                //Group is collapsed. Show the expand icon
                expandCollapseIcon.setImageResource(R.drawable.listview_expand_icon);
                expandCollapseIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Onclick, expand this group
                        mTargetView.expandGroup(groupPosition, true);
                    }
                });
            }
        }

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View
            convertView, ViewGroup parent) {
        HubGroupItem groupChild = (HubGroupItem) getChild(groupPosition, childPosition);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_nav_hub_child, parent, false);
        }

        //Set the title, and icon for the group child.
        TextView childTitleView = (TextView) convertView.findViewById(R.id.nav_hub_child_title);
        childTitleView.setText(groupChild.getHubName());
        childTitleView.setCompoundDrawablesWithIntrinsicBounds(groupChild.getIconResource(), 0,
                0, 0);

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    /**
     * Represent a top level hub navigation item.
     *
     * @author fhuya
     * @since 1.2.0
     */
    private static class HubGroup {

        private String mHubName;
        private int mIconResource;
        private final Class<? extends SuperUI> mHubActivityClass;
        private final List<HubGroupItem> mHubChildren;

        public HubGroup(Context context, Class<? extends SuperUI> hubActivityClass) {
            mHubActivityClass = hubActivityClass;
            mHubChildren = new ArrayList<HubGroupItem>();

            try {
                final PackageManager pm = context.getPackageManager();
                final ActivityInfo ai = pm.getActivityInfo(new ComponentName(context,
                        hubActivityClass), 0);
                mHubName = ai.loadLabel(pm).toString();
                mIconResource = ai.getIconResource();
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }

        public void addChild(HubGroupItem child) {
            mHubChildren.add(child);
        }

        public Class<? extends SuperUI> getActivityClass() {
            return mHubActivityClass;
        }

        public String getHubName() {
            return mHubName;
        }

        public int getIconResource() {
            return mIconResource;
        }

        public List<HubGroupItem> getChildren() {
            return mHubChildren;
        }

    }

    /**
     * Represents a sub level hub navigation item.
     *
     * @author fhuya
     * @since 1.2.0
     */
    private static class HubGroupItem {

        private String mHubName;
        private int mIconResource;
        private final Class<? extends SuperUI> mHubActivityClass;

        public HubGroupItem(Context context, Class<? extends SuperUI> hubActivityClass) {
            mHubActivityClass = hubActivityClass;

            try {
                final PackageManager pm = context.getPackageManager();
                final ActivityInfo ai = pm.getActivityInfo(new ComponentName(context,
                        hubActivityClass), 0);
                mHubName = ai.loadLabel(pm).toString();
                mIconResource = ai.getIconResource();
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }

        public Class<? extends SuperUI> getActivityClass() {
            return mHubActivityClass;
        }

        public String getHubName() {
            return mHubName;
        }

        public int getIconResource() {
            return mIconResource;
        }
    }
}
