package com.droidplanner.checklist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.droidplanner.R;
import com.droidplanner.checklist.listadapter.ListXmlAdapter;
import com.droidplanner.checklist.row.ListRow_CheckBox;
import com.droidplanner.checklist.row.ListRow_Interface;
import com.droidplanner.checklist.row.ListRow_Interface.OnRowItemChangeListener;
import com.droidplanner.checklist.row.ListRow_Level;
import com.droidplanner.checklist.row.ListRow_Note;
import com.droidplanner.checklist.row.ListRow_Radio;
import com.droidplanner.checklist.row.ListRow_Select;
import com.droidplanner.checklist.row.ListRow_Switch;
import com.droidplanner.checklist.row.ListRow_Toggle;
import com.droidplanner.checklist.row.ListRow_Type;
import com.droidplanner.checklist.row.ListRow_Value;
import com.droidplanner.drone.Drone;

import android.view.LayoutInflater;
import android.view.View;

public class CheckListAdapter extends ListXmlAdapter implements
		OnRowItemChangeListener {

	public interface OnCheckListItemUpdateListener {
		public void onRadioGroupUpdate(CheckListItem checkListItem, int checkId);

		public void onSelectUpdate(CheckListItem checkListItem, int selectId);

		public void onCheckBoxUpdate(CheckListItem checkListItem,
				boolean isChecked);

		public void onSwitchUpdate(CheckListItem checkListItem,
				boolean isSwitched);

		public void onToggleUpdate(CheckListItem checkListItem,
				boolean isToggled);

		public void onValueUpdate(CheckListItem checkListItem, String newValue);
	}

	private OnCheckListItemUpdateListener listener;

	public CheckListAdapter(Drone drone, LayoutInflater inflater,
			List<String> listHeader,
			HashMap<String, List<CheckListItem>> listDataChild) {
		super(inflater, listHeader);

		setHeaderLayout(R.layout.list_group_header);

		for (String dataHeader : listHeader) {
			List<ListRow_Interface> xmlRows = new ArrayList<ListRow_Interface>();
			for (CheckListItem listItem : listDataChild.get(dataHeader)) {
				if (listItem.getTagName().equalsIgnoreCase("check_item")) {
					ListRow_CheckBox row = new ListRow_CheckBox(this.inflater,
							listItem);
					row.setOnRowItemChangeListener(this);
					xmlRows.add(row);
					
				} else if (listItem.getTagName().equalsIgnoreCase("value_item")) {
					ListRow_Value row = new ListRow_Value(this.inflater,
							listItem);
					row.setOnRowItemChangeListener(this);
					xmlRows.add(row);
					
				} else if (listItem.getTagName().equalsIgnoreCase("radio_item")) {
					ListRow_Radio row = new ListRow_Radio(this.inflater,
							listItem);
					row.setOnRowItemChangeListener(this);
					xmlRows.add(row);
					
				} else if (listItem.getTagName().equalsIgnoreCase("select_item")) {
					ListRow_Select row = new ListRow_Select(this.inflater,listItem);
					row.setOnRowItemChangeListener(this);
					xmlRows.add(row);
					
				} else if (listItem.getTagName().equalsIgnoreCase("toggle_item")) {
					ListRow_Toggle row = new ListRow_Toggle(drone, this.inflater, listItem);
					row.setOnRowItemChangeListener(this);
					xmlRows.add(row);
					
				} else if (listItem.getTagName().equalsIgnoreCase("switch_item")) {
					ListRow_Switch row = new ListRow_Switch(drone, this.inflater, listItem);
					row.setOnRowItemChangeListener(this);
					xmlRows.add(row);
					
				} else if (listItem.getTagName().equalsIgnoreCase("level_item")) {
					ListRow_Level row = new ListRow_Level(drone, this.inflater, listItem);
					xmlRows.add(row);
				} else if (listItem.getTagName().equalsIgnoreCase("note_item")) {
					ListRow_Note row = new ListRow_Note(this.inflater, listItem);
					xmlRows.add(row);
					
				}
			}
			listItems.put(dataHeader, xmlRows);
		}
	}

	public void setOnCheckListItemUpdateListener(
			OnCheckListItemUpdateListener listener) {
		this.listener = listener;
	}

	@Override
	public int getChildTypeCount() {
		return ListRow_Type.values().length;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	@Override
	public void onRowItemChanged(View mView, CheckListItem listItem, boolean isChecked) {
		if(this.listener==null)
			return;
		
		if(listItem.getTagName().equalsIgnoreCase("check_item")){
			this.listener.onCheckBoxUpdate(listItem, isChecked);
		}else if(listItem.getTagName().equalsIgnoreCase("switch_item")){
			this.listener.onSwitchUpdate(listItem, listItem.isSys_activated());
		}else if(listItem.getTagName().equalsIgnoreCase("toggle_item")){
			this.listener.onToggleUpdate(listItem, listItem.isSys_activated());
		}else if(listItem.getTagName().equalsIgnoreCase("select_item")){
			this.listener.onSelectUpdate(listItem, listItem.getSelectedIndex());
		}if(listItem.getTagName().equalsIgnoreCase("radio_item")){
			this.listener.onRadioGroupUpdate(listItem, listItem.getSelectedIndex());
		}if(listItem.getTagName().equalsIgnoreCase("value_item")){
			this.listener.onValueUpdate(listItem, listItem.getValue());
		}
	}
}