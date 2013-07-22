package com.droidplanner.widgets.paramRow;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.TableRow;
import android.widget.TextView;

import com.droidplanner.drone.variables.waypoint;

public class MissionRow extends TableRow  {
	private TextView nameView;
	private EditText valueView;

	public MissionRow(Context context) {
		super(context);
		createRowViews(context);
	}

	public MissionRow(Context context, AttributeSet attrs) {
		super(context, attrs);
		createRowViews(context);
	}

	public void setHeight(waypoint wp) {
		valueView.setText(wp.getHeight().toString());
	}
	
	public void setNumber(int num) {
		nameView.setText("WP " +num);
	}

	private void createRowViews(Context context) {
		nameView = new TextView(context);
		valueView = new EditText(context);
		valueView.setInputType(InputType.TYPE_CLASS_NUMBER);

		nameView.setWidth(150);
		valueView.setWidth(100);

		addView(nameView);
		addView(valueView);
	}


}
