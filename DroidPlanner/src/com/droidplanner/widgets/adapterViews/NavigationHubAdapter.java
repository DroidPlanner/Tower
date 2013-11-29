package com.droidplanner.widgets.adapterViews;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
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
import com.droidplanner.fragments.ParametersTableFragment;
import com.droidplanner.fragments.RcSetupFragment;
import com.droidplanner.fragments.SettingsFragment;
import com.droidplanner.fragments.TuningFragment;

import java.util.ArrayList;
import java.util.List;

import static com.droidplanner.utils.Constants.*;

/**
 * ExpandableListAdapter to provide views for the app navigation hub interface.
 *
 * @author fhuya
 * @since 1.2.0
 */
public class NavigationHubAdapter extends BaseExpandableListAdapter {

    /**
     * Class tag, used for logging.
     *
     * @since 1.2.0
     */
    private static final String TAG = NavigationHubAdapter.class.getName();

    /**
     * Represents a navigation hub item.
     *
     * @author fhuya
     * @since 1.2.0
     */
    public enum HubItem {
        FLIGHT_DATA(FlightActivity.class, null, FlightActivity.LABEL_RESOURCE,
                FlightActivity.LOGO_RESOURCE),

        EDITOR(EditorActivity.class, null, EditorActivity.LABEL_RESOURCE,
                EditorActivity.LOGO_RESOURCE),

        CONFIGURATION(ConfigurationActivity.class, null, ConfigurationActivity.LABEL_RESOURCE,
                ConfigurationActivity.LOGO_RESOURCE),

        TUNING(ConfigurationActivity.class, ACTION_CONFIGURATION_TUNING,
                TuningFragment.LABEL_RESOURCE, TuningFragment.LOGO_RESOURCE),

        RC(ConfigurationActivity.class, ACTION_CONFIGURATION_RC, RcSetupFragment.LABEL_RESOURCE,
                RcSetupFragment.LOGO_RESOURCE),

        PARAMETERS(ConfigurationActivity.class, ACTION_CONFIGURATION_PARAMETERS,
                ParametersTableFragment.LABEL_RESOURCE, ParametersTableFragment.LOGO_RESOURCE),

        SETTINGS(ConfigurationActivity.class, ACTION_CONFIGURATION_SETTINGS, SettingsFragment
                .LABEL_RESOURCE, SettingsFragment.LOGO_RESOURCE);

        static {
            int position = 0;
            //Setting the group for the flight data activity.
            HubItem flightData = HubItem.FLIGHT_DATA;
            flightData.setPosition(position++);
            flightData.addChild(HubItem.EDITOR);

            //Setting the group for the Configuration activity.
            HubItem configuration = HubItem.CONFIGURATION;
            configuration.setPosition(position++);
            configuration.addChild(HubItem.TUNING);
            configuration.addChild(HubItem.RC);
            configuration.addChild(HubItem.PARAMETERS);
            configuration.addChild(HubItem.SETTINGS);
        }

        private final int mLabelResource;
        private final int mLogoResource;
        private final Class<? extends SuperUI> mActivityClass;
        private final String mIntentAction;

        private boolean mIsExpanded;
        private int mPosition;
        private HubItem mParent;
        private final List<HubItem> mChildren = new ArrayList<HubItem>();

        private HubItem(Class<? extends SuperUI> activityClass,
                        String intentAction, int labelResource,
                        int logoResource) {
            mActivityClass = activityClass;
            mIntentAction = intentAction;
            mLabelResource = labelResource;
            mLogoResource = logoResource;
        }

        public Class<? extends SuperUI> getActivityClass() {
            return mActivityClass;
        }

        public String getIntentAction() {
            return mIntentAction;
        }

        public int getLabelResource() {
            return mLabelResource;
        }

        public int getLogoResource() {
            return mLogoResource;
        }

        public HubItem getParent() {
            return mParent;
        }

        private void setParent(HubItem parent) {
            if (parent == this) {
                throw new IllegalStateException("Bootstrap Paradox.. Object cannot be its " +
                        "own parent.");
            }

            mParent = parent;
        }

        public List<HubItem> getChildren() {
            return mChildren;
        }

        public void addChild(HubItem child) {
            if (child == this) {
                throw new IllegalStateException("Bootstrap Paradox.. Object cannot be its own " +
                        "child.");
            }

            int position = mChildren.size();

            child.setParent(this);
            child.setPosition(position);
            mChildren.add(position, child);
        }

        void setPosition(int position) {
            mPosition = position;
        }

        public int getPosition() {
            return mPosition;
        }

        boolean isExpanded() {
            return mIsExpanded;
        }

        void setIsExpanded(boolean isExpanded) {
            mIsExpanded = isExpanded;
        }
    }

    /**
     * Delay used to launch the activity on selection in the drawer layout. This delay allows the
     * drawer layout to close smoothly without stuttering/janking.
     */
    private final static long ACTIVITY_LAUNCH_DELAY = 200l;// milliseconds

    /**
     * Handler used to launch the selected activity.
     *
     * @since 1.2.0
     */
    private final Handler mHandler = new Handler();

    /**
     * Context used to start hub activities.
     *
     * @since 1.2.0
     */
    private final Context mContext;

    /**
     * Runnable used to launch the activity selected in the drawer layout.
     *
     * @since 1.2.0
     */
    private final Runnable mLaunchActivity = new Runnable() {
        @Override
        public void run() {
            if (mIntentToLaunch != null) {
                mContext.startActivity(mIntentToLaunch);
                mIntentToLaunch = null;
            }
        }
    };

    /**
     * Drawer layout to which the expandable listview this adapter serves is attached.
     *
     * @since 1.2.0
     */
    private final DrawerLayout mNavDrawer;

    /**
     * Target expandable listview this adapter is attached to.
     *
     * @since 1.2.0
     */
    private ExpandableListView mTargetView;

    /**
     * Use to identify which activity is currently hosting the navigation hub drawer.
     *
     * @since 1.2.0
     */
    private final HubItem mHubItem;

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
    private final List<HubItem> mGroupList = new ArrayList<HubItem>();

    /**
     * Next activity to launch.
     *
     * @since 1.2.0
     */
    private Intent mIntentToLaunch;

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

                    //Update the selected item.
                    int flatPosition = mTargetView.getFlatListPosition(ExpandableListView
                            .getPackedPositionForGroup(groupPosition));
                    mTargetView.setItemChecked(flatPosition, true);

                    HubItem hubGroup = (HubItem) getGroup(groupPosition);
                    launchHubItem(hubGroup);
                    return true;
                }
            };

    /**
     * Set against the expandable listview to handle click events on group child items.
     *
     * @since 1.2.0
     */
    private final ExpandableListView.OnChildClickListener mChildClickListener = new
            ExpandableListView.OnChildClickListener() {


                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                            int childPosition, long id) {
                    //Update the selected item.
                    int flatPosition = mTargetView.getFlatListPosition(ExpandableListView
                            .getPackedPositionForChild(groupPosition, childPosition));
                    mTargetView.setItemChecked(flatPosition, true);

                    HubItem hubChild = (HubItem) getChild(groupPosition, childPosition);
                    launchHubItem(hubChild);
                    return true;
                }
            };

    public NavigationHubAdapter(Context context, HubItem hubItem, DrawerLayout navDrawer,
                                ExpandableListView targetView) {
        mContext = context;
        mHubItem = hubItem;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mNavDrawer = navDrawer;

        mTargetView = targetView;
        mTargetView.setGroupIndicator(null);
        mTargetView.setOnGroupClickListener(mGroupClickListener);
        mTargetView.setOnChildClickListener(mChildClickListener);

        mGroupList.add(HubItem.FLIGHT_DATA);
        mGroupList.add(HubItem.CONFIGURATION);
    }

    private void launchHubItem(HubItem hubItem) {
        //Close the drawer
        mNavDrawer.closeDrawer(mTargetView);

        if (hubItem != mHubItem) {
            /*
            Launch the activity after the drawer is almost closed.
            Another solution would be to listen to DrawerListener.onDrawerClosed calls,
            but this has a high amount of latency.
             */
            mIntentToLaunch = new Intent(mContext, hubItem.getActivityClass()).setAction(hubItem
                    .getIntentAction()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mHandler.postDelayed(mLaunchActivity, ACTIVITY_LAUNCH_DELAY);
        }
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
                             ViewGroup parent) {
        final HubItem hubGroup = (HubItem) getGroup(groupPosition);

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_nav_hub_group, parent, false);
        }

        //Set the title, and icon for the group.
        TextView hubTitleView = (TextView) convertView.findViewById(R.id.nav_hub_group_title);
        hubTitleView.setText(hubGroup.getLabelResource());
        hubTitleView.setCompoundDrawablesWithIntrinsicBounds(hubGroup.getLogoResource(), 0, 0, 0);

        //Update the expand/collapse icon.
        ImageView expandCollapseIcon = (ImageView) convertView.findViewById(R.id
                .nav_hub_group_expand_icon);
        if (hubGroup.getChildren().isEmpty()) {
            expandCollapseIcon.setVisibility(View.INVISIBLE);
        }
        else {
            boolean expandFlag = isExpanded || hubGroup.isExpanded();
            if (isExpanded) {
                //Group is expanded. Show the collapse icon
                expandCollapseIcon.setImageResource(R.drawable.listview_collapse_icon);
                expandCollapseIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Onclick, collapse this group.
                        mTargetView.collapseGroup(groupPosition);
                        hubGroup.setIsExpanded(false);

                        updateItemChecked();
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
                        hubGroup.setIsExpanded(true);

                        updateItemChecked();
                    }
                });
            }
        }

        return convertView;
    }

    private void updateItemChecked() {
        HubItem hubParent = mHubItem.getParent();

        if (hubParent != null) {
            //Selected item is a child.
            if (hubParent.isExpanded()) {
                //Select current item, since it's a child of the group that expanded.
                int flatPosition = mTargetView.getFlatListPosition(ExpandableListView
                        .getPackedPositionForChild(hubParent.getPosition(),
                                mHubItem.getPosition()));
                mTargetView.setItemChecked(flatPosition, true);
            }
            else {
                //Unselect current item since it's a child of the group that collapsed.
                int flatPosition = mTargetView.getFlatListPosition
                        (ExpandableListView.getPackedPositionForGroup(hubParent.getPosition()));
                mTargetView.setItemChecked(flatPosition, true);
            }
        }
        else {
            //Selected item is a group.
            //It's not the current group, so its position might change.
            int flatPosition = mTargetView.getFlatListPosition
                    (ExpandableListView.getPackedPositionForGroup(mHubItem.getPosition()));
            mTargetView.setItemChecked(flatPosition, true);
        }
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View
            convertView, ViewGroup parent) {
        HubItem groupChild = (HubItem) getChild(groupPosition, childPosition);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_nav_hub_child, parent, false);
        }

        //Set the title, and icon for the group child.
        TextView childTitleView = (TextView) convertView.findViewById(R.id.nav_hub_child_title);
        childTitleView.setText(groupChild.getLabelResource());
        childTitleView.setCompoundDrawablesWithIntrinsicBounds(groupChild.getLogoResource(), 0,
                0, 0);

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
