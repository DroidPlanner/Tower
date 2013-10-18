package com.droidplanner.widgets.ChecklistAdapter;

import com.droidplanner.R;
import com.droidplanner.preflightcheck.CheckListItem;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

public class Radio_XmlRow implements XmlRow {
    private final CheckListItem checkListItem;
    private final LayoutInflater inflater;

    public Radio_XmlRow(LayoutInflater inflater, CheckListItem checkListItem) {
        this.checkListItem = checkListItem;
        this.inflater = inflater;
    }

    public View getView(View convertView) {
        ViewHolder holder;
        View view;
        if (convertView == null) {
            ViewGroup viewGroup = (ViewGroup)inflater.inflate(R.layout.preflight_radio_item, null);
			holder = new ViewHolder(viewGroup);
            viewGroup.setTag(holder);
            view = viewGroup;
        } else {
            view = convertView;
            holder = (ViewHolder)convertView.getTag();
        }
        //TODO - Add information (radio items here)
        return view;
    }

    public int getViewType() {
        return XmlRowType.RADIO_ROW.ordinal();
    }

    private static class ViewHolder {
    	final LinearLayout layoutView;
        final RadioGroup radioGroupView;

        private ViewHolder(ViewGroup viewGroup) {
        	this.layoutView = (LinearLayout) viewGroup.findViewById(R.id.layout_radio);
        	this.radioGroupView = (RadioGroup) viewGroup.findViewById(R.id.chk_radioGroup);
        }
    }
}
