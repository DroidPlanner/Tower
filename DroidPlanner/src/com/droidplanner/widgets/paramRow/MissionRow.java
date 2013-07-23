package com.droidplanner.widgets.paramRow;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TableRow;
import android.widget.TextView;

import com.MAVLink.Messages.ApmCommands;
import com.droidplanner.drone.variables.waypoint;

public class MissionRow extends TableRow implements OnClickListener {
	private Context context;
	private TextView nameView;
	private EditText valueView;
	private TextView cmdView;

	public MissionRow(Context context) {
		super(context);
		this.context = context;
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
		nameView.setText("WP " + num);
	}

	public void setCmd(ApmCommands cmd) {
		cmdView.setText(cmd.getName());
	}

	private void createRowViews(Context context) {
		nameView = new TextView(context);
		valueView = new EditText(context);
		valueView.setInputType(InputType.TYPE_CLASS_NUMBER);
		cmdView = new TextView(context);

		nameView.setWidth(50);
		cmdView.setWidth(60);
		valueView.setWidth(100);

		cmdView.setOnClickListener(this);

		addView(nameView);
		addView(cmdView);
		addView(valueView);
	}

	@Override
	public void onClick(View v) {
		
		String[] list = new String[ApmCommands.getNameList().size()];
		ApmCommands.getNameList().toArray(list);		
		AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		dialog.setTitle("Select type");
		dialog.setItems(list, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface d, int arg1) {
						Log.d("PLAN", ApmCommands.getNameList().get(arg1));
						setCmd(ApmCommands.getCmd(ApmCommands.getNameList().get(arg1)));
					}
				});
		dialog.create().show();
	}

}
