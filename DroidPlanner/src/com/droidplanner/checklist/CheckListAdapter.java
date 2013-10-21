package com.droidplanner.checklist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.droidplanner.R;
import com.droidplanner.checklist.listadapter.ListXmlAdapter;
import com.droidplanner.checklist.row.ListRow_CheckBox;
import com.droidplanner.checklist.row.ListRow_CheckBox.OnCheckBoxChangeListener;
import com.droidplanner.checklist.row.ListRow_Interface;
import com.droidplanner.checklist.row.ListRow_Level;
import com.droidplanner.checklist.row.ListRow_Note;
import com.droidplanner.checklist.row.ListRow_Radio;
import com.droidplanner.checklist.row.ListRow_Radio.OnRadioGroupCheckedChangeListener;
import com.droidplanner.checklist.row.ListRow_Select;
import com.droidplanner.checklist.row.ListRow_Select.OnSelectChangeListener;
import com.droidplanner.checklist.row.ListRow_Switch;
import com.droidplanner.checklist.row.ListRow_Switch.OnSwitchChangeListener;
import com.droidplanner.checklist.row.ListRow_Toggle;
import com.droidplanner.checklist.row.ListRow_Toggle.OnToggleChangeListener;
import com.droidplanner.checklist.row.ListRow_Type;
import com.droidplanner.checklist.row.ListRow_Value;
import com.droidplanner.checklist.row.ListRow_Value.OnValueChangeListener;

import android.view.LayoutInflater;
import android.widget.RadioGroup;

public class CheckListAdapter extends ListXmlAdapter implements
		OnRadioGroupCheckedChangeListener, OnSelectChangeListener,
		OnCheckBoxChangeListener, OnSwitchChangeListener,
		OnToggleChangeListener, OnValueChangeListener {

	public interface OnCheckListItemUpdateListener {
		public void onRadioGroupUpdate(CheckListItem checkListItem,
				RadioGroup group, int checkId);

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

	public CheckListAdapter(LayoutInflater inflater, List<String> listHeader,
			HashMap<String, List<CheckListItem>> listDataChild) {
		super(inflater, listHeader);
		
		setHeaderLayout(R.layout.list_group_header);
		
		for (String dataHeader : listHeader) {
			List<ListRow_Interface> xmlRows = new ArrayList<ListRow_Interface>();
			for (CheckListItem listItem : listDataChild.get(dataHeader)) {
				if (listItem.getTagName().equalsIgnoreCase("check_item")) {
					ListRow_CheckBox row = new ListRow_CheckBox(this.inflater,
							listItem);
					row.setOnCheckBoxChangeListener(this);
					xmlRows.add(row);
				} else if (listItem.getTagName().equalsIgnoreCase("value_item")) {
					ListRow_Value row = new ListRow_Value(this.inflater,
							listItem);
					row.setOnValueChangeListener(this);
					xmlRows.add(row);
				} else if (listItem.getTagName().equalsIgnoreCase("radio_item")) {
					ListRow_Radio row = new ListRow_Radio(this.inflater,
							listItem);
					row.setOnRadioGroupChackedChangeListener(this);
					xmlRows.add(row);
				} else if (listItem.getTagName().equalsIgnoreCase("select_item")) {
					ListRow_Select row = new ListRow_Select(this.inflater,
							listItem);
					row.setOnSelectChangeListener(this);
					xmlRows.add(row);
				} else if (listItem.getTagName().equalsIgnoreCase("toggle_item")) {
					ListRow_Toggle row = new ListRow_Toggle(this.inflater,
							listItem);
					row.setOnToggleChangeListener(this);
					xmlRows.add(row);
				} else if (listItem.getTagName().equalsIgnoreCase("switch_item")) {
					ListRow_Switch row = new ListRow_Switch(this.inflater,
							listItem);
					row.setOnSwitchChangeListener(this);
					xmlRows.add(row);
				} else if (listItem.getTagName().equalsIgnoreCase("level_item")) {
					xmlRows.add(new ListRow_Level(this.inflater, listItem));
				} else if (listItem.getTagName().equalsIgnoreCase("note_item")) {
					xmlRows.add(new ListRow_Note(this.inflater, listItem));
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
	public int getChildTypeCount(){
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
	public void onRadioGroupCheckedChanged(CheckListItem checkListItem,
			RadioGroup group, int checkId) {
		if (this.listener != null)
			this.listener.onRadioGroupUpdate(checkListItem, group, checkId);
	}

	@Override
	public void onSelectChanged(CheckListItem checkListItem, int selectId) {
		if (this.listener != null)
			this.listener.onSelectUpdate(checkListItem, selectId);
	}

	@Override
	public void onCheckBoxChanged(CheckListItem checkListItem, boolean isChecked) {
		if (this.listener != null)
			this.listener.onCheckBoxUpdate(checkListItem, isChecked);
	}

	@Override
	public void onSwitchChanged(CheckListItem checkListItem, boolean isSwitched) {
		if (this.listener != null)
			this.listener.onSwitchUpdate(checkListItem, isSwitched);
	}

	@Override
	public void onToggleChanged(CheckListItem checkListItem, boolean isToggled) {
		if (this.listener != null)
			this.listener.onToggleUpdate(checkListItem, isToggled);
	}

	@Override
	public void onValueChanged(CheckListItem checkListItem, String newValue) {
		if (this.listener != null)
			this.listener.onValueUpdate(checkListItem, newValue);
	}
}