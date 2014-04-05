package org.droidplanner.activities;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.droidplanner.DroidPlannerApp;
import org.droidplanner.R;
import org.droidplanner.drone.Drone;
import org.droidplanner.utils.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.MAVLink.Messages.ApmModes;
import com.MAVLink.Messages.enums.MAV_TYPE;

public class MenuModesPreference extends DialogPreference {
	static final String TAG = MenuModesPreference.class.getSimpleName();
	
	static class ListItemType {
		final String name;
		final int type;
		
		ListItemType(String n, int t) {
			name = n;
			type = t;
		}
	}
	
	static class ListItem {
		final ApmModes mode;
		boolean selected;
		
		ListItem(ApmModes m, boolean sel) {
			mode = m;
			selected = sel;
		}
		
		public String toString() {
			return mode.getName();
		}
	}
	
	private ListView mListView;
	private int mDroneType = MAV_TYPE.MAV_TYPE_QUADROTOR;
	
	private final OnItemSelectedListener mMavTypeSelectedListener = new OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			ListItemType t = (ListItemType)parent.getAdapter().getItem(position);
			onMavTypeSelected(t.type);
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			onMavTypeSelected(MAV_TYPE.MAV_TYPE_QUADROTOR);
		}
	};
	
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
		
		final DroidPlannerApp app = (DroidPlannerApp)getContext().getApplicationContext();
		
		mListView = (ListView)view.findViewById(R.id.list);
		mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		mListView.setOnItemClickListener(mItemClickListener);
		
		final ListItemType[] types = new ListItemType[] {
			new ListItemType(getContext().getString(R.string.mavtype_quad), MAV_TYPE.MAV_TYPE_QUADROTOR)
		,	new ListItemType(getContext().getString(R.string.mavtype_fixedwing), MAV_TYPE.MAV_TYPE_FIXED_WING)
		,	new ListItemType(getContext().getString(R.string.mavtype_rover), MAV_TYPE.MAV_TYPE_GROUND_ROVER)
		};
		
		final ArrayAdapter<ListItemType> spinAdapter = new ArrayAdapter<ListItemType>(
				getContext(), android.R.layout.simple_spinner_item, android.R.id.text1, types) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View v = super.getView(position, convertView, parent);
				((TextView)v).setText(getItem(position).name);
				return v;
			}

			@Override
			public View getDropDownView(int position, View convertView, ViewGroup parent) {
				View v = super.getDropDownView(position, convertView, parent);
				((TextView)v).setText(getItem(position).name);
				return v;
			}
		};
		
		spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		final Spinner mavTypes = (Spinner)view.findViewById(R.id.spin_uv_type);
		mavTypes.setAdapter(spinAdapter);
		mavTypes.setOnItemSelectedListener(mMavTypeSelectedListener);
		
		final Drone drone = app.getDrone();
		// Default to quadrotor based on popularity
		int droneType = (drone != null && app.isDroneConnected())? 
				drone.type.getType(): MAV_TYPE.MAV_TYPE_QUADROTOR;
		
		if(drone != null) {
			for(int i = 0; i < types.length; ++i) {
				ListItemType lt = types[i];
				if(lt.type == droneType) {
					mavTypes.setSelection(i);
					break;
				}
			}
		}
		
		onMavTypeSelected(droneType);

		// If there's a drone connected, confine edits to the connected type.
		int viz = (app.isDroneConnected())? View.GONE: View.VISIBLE;
		view.findViewById(R.id.mav_type_panel).setVisibility(viz);
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
			prefs.edit().putString(Utils.getMenuModesPrefName(mDroneType), sb.toString()).commit();
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
	
	List<ApmModes> getModesFor(int mavType) {
		final ArrayList<ApmModes> list = new ArrayList<ApmModes>();

		for(ApmModes mm: ApmModes.values()) {
			if(mm.getType() == mavType) {
				list.add(mm);
			}
		}

		return list;
	}
	
	void onMavTypeSelected(int mavType) {
		mDroneType = mavType;
		
        final SharedPreferences prefs = this.getPreferenceManager().getSharedPreferences();
        final String selections = prefs.getString(Utils.getMenuModesPrefName(mavType), null);
        
        String[] parts = (selections != null)? selections.split(","): null;

        final List<ApmModes> flightModes = ApmModes.getModeList(mavType);
        
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
	}
}
