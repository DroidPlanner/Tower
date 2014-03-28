package org.droidplanner.android.widgets.adapterViews;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.droidplanner.R;
import org.droidplanner.android.activities.ConfigurationActivity;
import org.droidplanner.android.activities.DrawerNavigationUI;
import org.droidplanner.android.activities.EditorActivity;
import org.droidplanner.android.activities.FlightActivity;
import org.droidplanner.android.activities.SettingsActivity;
import org.droidplanner.android.fragments.helpers.HelpDialogFragment;

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

/**
 * Adapter for the navigation drawer items
 */
public class NavigationDrawerAdapter extends BaseExpandableListAdapter {

	/**
	 * Keys used to access the group and child data values.
	 */
	private static final String KEY_SECTION_NAME = "key_section_name";
	private static final String KEY_SECTION_ICON = "key_section_icon";
	private static final String KEY_SECTION_CALLBACK = "key_section_callback";

	/**
	 * Maps the activities used as primary section in the drawer layout to their
	 * index in the expandable list view. Only an activity within the map can
	 * instantiate this adapter.
	 */
	private final static Map<Class<? extends DrawerNavigationUI>, Integer> sActivityGroupIndexMap = new HashMap<Class<? extends DrawerNavigationUI>, Integer>();

	static {
		sActivityGroupIndexMap.put(FlightActivity.class, 0);
		sActivityGroupIndexMap.put(SettingsActivity.class, 1);
	}

	private final static boolean[] sIsGroupExpanded = { false, false, false };

	/**
	 * Delay used to run the callback on selection in the drawer layout. This
	 * delay allows the drawer layout to close smoothly without
	 * stuttering/janking.
	 */
	private final static long CALLBACK_LAUNCH_DELAY = 200l;// milliseconds

	/**
	 * Handler used to launch the selected callback.
	 */
	private final Handler mHandler = new Handler();
	/**
	 * Container activity
	 */
	private final DrawerNavigationUI mActivity;

	/**
	 * Activity's drawer layout. Used to close the drawer on item click.
	 */
	private final DrawerLayout mDrawerLayout;

	/**
	 * Each entry in the list corresponds to one section in the app.
	 */
	private final List<Map<String, ? extends Object>> mGroupData;

	/**
	 * Stores the app subsections' data.
	 */
	private final List<List<Map<String, ? extends Object>>> mChildData;

	/**
	 * Expandable list view this adapter is attached to. We keep a reference in
	 * order to expand/collapse the group(s) when the expand/collapse icon is
	 * clicked.
	 */
	private final ExpandableListView mNavHubView;

	/**
	 * Uses to inflate the expandable list items' views.
	 */
	private final LayoutInflater mInflater;

	public NavigationDrawerAdapter(DrawerNavigationUI activity) {
		if (!sActivityGroupIndexMap.containsKey(activity.getClass())) {
			throw new IllegalArgumentException("Invalid container activity.");
		}

		mActivity = activity;
		mDrawerLayout = activity.getDrawerLayout();
		mNavHubView = activity.getNavHubView();
		mNavHubView
				.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
					@Override
					public boolean onGroupClick(ExpandableListView parent,
							View v, int groupPosition, long id) {
						performCallback((Map<String, Object>) getGroup(groupPosition));
						return true;
					}
				});
		mNavHubView
				.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
					@Override
					public boolean onChildClick(ExpandableListView parent,
							View v, int groupPosition, int childPosition,
							long id) {
						performCallback((Map<String, Object>) getChild(
								groupPosition, childPosition));
						return true;
					}
				});

		mInflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		mChildData = new ArrayList<List<Map<String, ? extends Object>>>();
		initChildData(activity);

		mGroupData = new ArrayList<Map<String, ? extends Object>>();
		initGroupData(activity);

		if (sIsGroupExpanded.length != getGroupCount()) {
			throw new IllegalStateException(
					"Group state cache doesn't match with group data.");
		}
	}

	/**
	 * Updates the hub view this adapter is attached to.
	 */
	public void refreshHubView() {
		// Expand or collapse the group
		final int groupCount = sIsGroupExpanded.length;
		for (int i = 0; i < groupCount; i++) {
			if (sIsGroupExpanded[i])
				mNavHubView.expandGroup(i);
			else
				mNavHubView.collapseGroup(i);
		}

		// Select the container activity's group
		final int groupPosition = sActivityGroupIndexMap.get(mActivity
				.getClass());
		final int flatPosition = mNavHubView
				.getFlatListPosition(ExpandableListView
						.getPackedPositionForGroup(groupPosition));
		mNavHubView.setItemChecked(flatPosition, true);
	}

	private void performCallback(Map<String, Object> itemData) {
		mDrawerLayout.closeDrawer(mNavHubView);

		/*
		 * Run the callback after the drawer is almost closed. Another solution
		 * would be to listen to DrawerListener.onDrawerClosed calls, but that
		 * has a high amount of latency.
		 */
		final Runnable clickCb = (Runnable) itemData.get(KEY_SECTION_CALLBACK);
		mHandler.postDelayed(clickCb, CALLBACK_LAUNCH_DELAY);
	}

	/**
	 * Binds the navigation drawer adapter to the expandable list view passed in
	 * the constructor.
	 */
	public void attachExpandableListView() {
		mNavHubView.setAdapter(this);
		refreshHubView();
	}

	private void initChildData(final DrawerNavigationUI activity) {
		/*
		 * Flight section
		 */
		// Editor activity
		Map<String, Object> editorData = new HashMap<String, Object>();
		editorData.put(KEY_SECTION_NAME, R.string.editor);
		editorData.put(KEY_SECTION_ICON, R.drawable.ic_action_location);
		editorData.put(KEY_SECTION_CALLBACK, new Runnable() {
			@Override
			public void run() {
				activity.startActivity(new Intent(activity,
						EditorActivity.class));
			}
		});

		final List<Map<String, ? extends Object>> flightGroup = new ArrayList<Map<String, ? extends Object>>();
		flightGroup.add(editorData);
		mChildData.add(flightGroup);

		/*
		 * Settings section
		 */
		// Retrieve the elements in the settings section from the
		// ConfigurationActivity
		final List<Map<String, ? extends Object>> setupGroup = new ArrayList<Map<String, ? extends Object>>();

		final int configurationFragmentsCount = ConfigurationActivity.sConfigurationFragments.length;
		for (int i = 0; i < configurationFragmentsCount; i++) {
			final int index = i;
			final Map<String, Object> configData = new HashMap<String, Object>();
			configData.put(KEY_SECTION_NAME,
					ConfigurationActivity.sConfigurationFragmentTitlesRes[i]);
			configData.put(KEY_SECTION_ICON,
					ConfigurationActivity.sConfigurationFragmentIconRes[i]);
			configData.put(KEY_SECTION_CALLBACK, new Runnable() {
				@Override
				public void run() {
					activity.startActivity(new Intent(activity,
							ConfigurationActivity.class).putExtra(
							ConfigurationActivity.EXTRA_CONFIG_SCREEN_INDEX,
							index));
				}
			});

			setupGroup.add(configData);
		}

		mChildData.add(setupGroup);

		/*
		 * Help section
		 */
		// No children for the help section
		mChildData.add(Collections.<Map<String, ? extends Object>> emptyList());
	}

	private void initGroupData(final DrawerNavigationUI activity) {
		// Flight section
		final Map<String, Object> flightData = new HashMap<String, Object>();
		flightData.put(KEY_SECTION_NAME, R.string.flight_data);
		flightData.put(KEY_SECTION_ICON, R.drawable.ic_action_plane);
		flightData.put(KEY_SECTION_CALLBACK, new Runnable() {
			@Override
			public void run() {
				activity.startActivity(new Intent(activity,
						FlightActivity.class));
			}
		});
		mGroupData.add(flightData);

		// Settings section
		final Map<String, Object> droneSetupData = new HashMap<String, Object>();
		droneSetupData.put(KEY_SECTION_NAME, R.string.settings);
		droneSetupData.put(KEY_SECTION_ICON, R.drawable.ic_action_settings);
		droneSetupData.put(KEY_SECTION_CALLBACK, new Runnable() {
			@Override
			public void run() {
				activity.startActivity(new Intent(activity,
						SettingsActivity.class));
			}
		});
		mGroupData.add(droneSetupData);

		// Help section
		final Map<String, Object> helpData = new HashMap<String, Object>();
		helpData.put(KEY_SECTION_NAME, R.string.help);
		helpData.put(KEY_SECTION_ICON, R.drawable.ic_action_help);
		helpData.put(KEY_SECTION_CALLBACK, new Runnable() {
			@Override
			public void run() {
				final HelpDialogFragment helpDialog = HelpDialogFragment
						.newInstance();
				helpDialog.show(activity.getSupportFragmentManager(),
						"Help Dialog");
			}
		});
		mGroupData.add(helpData);
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		View v;
		if (convertView == null) {
			v = newChildView(parent);
		} else {
			v = convertView;
		}
		bindView(v, mChildData.get(groupPosition).get(childPosition),
				R.id.nav_drawer_child);
		return v;
	}

	/**
	 * Instantiates a new View for a child.
	 * 
	 * @param parent
	 *            The eventual parent of this new View.
	 * @return A new child View
	 */
	public View newChildView(ViewGroup parent) {
		return mInflater.inflate(R.layout.adapter_nav_drawer_child, parent,
				false);
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	@Override
	public int getGroupCount() {
		return mGroupData.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return mChildData.get(groupPosition).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return mGroupData.get(groupPosition);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return mChildData.get(groupPosition).get(childPosition);
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public View getGroupView(final int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		View v;
		if (convertView == null) {
			v = newGroupView(parent);
		} else {
			v = convertView;
		}

		final ImageView expandIcon = (ImageView) v
				.findViewById(R.id.nav_drawer_group_expand_icon);
		if (getChildrenCount(groupPosition) == 0) {
			expandIcon.setVisibility(View.GONE);
		} else {
			expandIcon.setVisibility(View.VISIBLE);
			if (isExpanded) {
				expandIcon
						.setImageResource(R.drawable.expandable_listview_icon_expanded);
				expandIcon.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mNavHubView.collapseGroup(groupPosition);
						sIsGroupExpanded[groupPosition] = false;
					}
				});
			} else {
				expandIcon
						.setImageResource(R.drawable.expandable_listview_icon_collapsed);
				expandIcon.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mNavHubView.expandGroup(groupPosition);
						sIsGroupExpanded[groupPosition] = true;
					}
				});
			}
		}

		bindView(v, mGroupData.get(groupPosition), R.id.nav_drawer_group);
		return v;
	}

	/**
	 * Instantiates a new View for a group.
	 * 
	 * @param parent
	 *            The eventual parent of this new View.
	 * @return A new group View
	 */
	public View newGroupView(ViewGroup parent) {
		return mInflater.inflate(R.layout.adapter_nav_drawer_group, parent,
				false);
	}

	private void bindView(View view, Map<String, ? extends Object> data,
			int textViewId) {
		final TextView textView = (TextView) view.findViewById(textViewId);
		if (textView != null) {
			textView.setText((Integer) data.get(KEY_SECTION_NAME));
			textView.setCompoundDrawablesWithIntrinsicBounds(
					(Integer) data.get(KEY_SECTION_ICON), 0, 0, 0);
		}
	}
}
