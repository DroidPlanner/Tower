package org.droidplanner.activities;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.droidplanner.DroidPlannerApp;
import org.droidplanner.R;
import org.droidplanner.drone.Drone;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.MAVLink.Messages.ApmModes;
import com.MAVLink.Messages.enums.MAV_TYPE;

public class MenuModesPreference extends DialogPreference {
	private static final String PREF_MENU_MODES = "pref_menu_modes";
	static final String TAG = MenuModesPreference.class.getSimpleName();
	
	static class ListItem {
		ApmModes mode;
		boolean selected = false;
		
		ListItem(ApmModes m, boolean sel) {
			mode = m;
			selected = sel;
		}
		
		public String toString() {
			return mode.getName();
		}
	}
	
	private ListView mListView;
	
	private final OnItemClickListener mItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			ListItem item = (ListItem)parent.getAdapter().getItem(position);
			item.selected = !item.selected;
		}
	};
	
	public MenuModesPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public MenuModesPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	protected void onBindDialogView(View view) {
		super.onBindDialogView(view);
		
        final SharedPreferences prefs = this.getPreferenceManager().getSharedPreferences();
        final String selections = prefs.getString(PREF_MENU_MODES, null);
        
        String[] parts = (selections != null)? selections.split(","): null;
		
		mListView = (ListView)view.findViewById(R.id.list);
		mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		
		final Drone drone = ((DroidPlannerApp)getContext().getApplicationContext()).getDrone();
		// Default to quadrotor based on popularity
		final int droneType = (drone != null)? drone.type.getType(): MAV_TYPE.MAV_TYPE_QUADROTOR;
		
        final List<ApmModes> flightModes = ApmModes.getModeList(droneType);
        
        final ArrayList<ListItem> items = new ArrayList<ListItem>();
        for(ApmModes am: flightModes) {
        	items.add(new ListItem(am, isSelectedIn(parts, am)));
        }
        
		ArrayAdapter<ListItem> adapter = new ArrayAdapter<ListItem>(
				getContext(), 
				android.R.layout.simple_list_item_multiple_choice, 
				android.R.id.text1, 
				items);

		mListView.setAdapter(adapter);
		
		final int size = items.size();
		for(int i = 0; i < size; ++i) {
			mListView.setItemChecked(i, items.get(i).selected);
		}
		
		mListView.setOnItemClickListener(mItemClickListener);
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);
		
		if(positiveResult) {
			final ArrayList<String> selected = new ArrayList<String>();
			
			final SparseBooleanArray checked = mListView.getCheckedItemPositions();
			final int size = mListView.getAdapter().getCount();
			
			for(int i = 0; i < size; ++i) {
				if(checked.get(i)) {
					final ListItem listItem = (ListItem)mListView.getAdapter().getItem(i);
					final ApmModes mode = listItem.mode;
					selected.add(String.valueOf(mode.ordinal()));
				}
			}
			
			final StringBuilder sb = new StringBuilder();
			for(Iterator<String> e = selected.iterator(); e.hasNext();) {
				sb.append(e.next());
				if(e.hasNext()) {
					sb.append(",");
				}
			}
			
			final SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
			prefs.edit().putString(PREF_MENU_MODES, sb.toString()).commit();
		}
	}
	
	/** Return true if the specified ApmModes is selected in the preferences.
	 * If there are no selections, <code>list</code> is null, which means everything
	 * is selected (by default)
	 */
	private boolean isSelectedIn(String[] list, ApmModes mode) {
		if(list == null) {
			return true;
		}
		
		String ord = String.valueOf(mode.ordinal());
		
		for(String s: list) {
			if(ord.equals(s)) {
				return true;
			}
		}
		
		return false;
	}
}
