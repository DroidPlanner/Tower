package com.droidplanner.widgets.paramRow;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableRow;
import android.widget.TextView;

import com.MAVLink.Messages.ApmCommands;
import com.droidplanner.drone.variables.waypoint;
import com.droidplanner.fragments.MissionFragment;

public class MissionRow extends TableRow implements OnClickListener {

	private Context context;
	private MissionFragment fragment;
	private waypoint waypoint;
	
	private TextView nameView;
	private EditText valueView;
	private TextView cmdView;
	private Button removeButton;

	public MissionRow(Context context) {
		super(context);
	}

	public MissionRow(MissionFragment missionFragment, waypoint waypoint) {
		super(missionFragment.getActivity());
		this.fragment = missionFragment;
		this.context = missionFragment.getActivity();
		this.waypoint = waypoint;
		createRowViews(context);
		update();
	}
	
	public void update(){
		valueView.setText(waypoint.getHeight().toString());
		nameView.setText("WP " + waypoint.getNumber());
		cmdView.setText(waypoint.getCmd().getName());
	}

	
	private void createRowViews(Context context) {
		nameView = new TextView(context);
		valueView = new EditText(context);
		valueView.setInputType(InputType.TYPE_CLASS_NUMBER);
		cmdView = new TextView(context);
		removeButton = new Button(context);
		removeButton.setText("X");

		//Sizes
		nameView.setWidth(50);
		cmdView.setWidth(60);
		valueView.setWidth(80);

		//Listeners
		cmdView.setOnClickListener(this);	
		removeButton.setOnClickListener(this);
		
		addView(nameView);
		addView(cmdView);
		addView(valueView);
		addView(removeButton);
	}

	@Override
	public void onClick(View v) {
		if (v.equals(removeButton)) {
			fragment.onDeleteWaypoint(waypoint);
		}else{
		final String[] list = new String[ApmCommands.getNameList().size()];
		ApmCommands.getNameList().toArray(list);		
		AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		dialog.setTitle("Select type");
		dialog.setItems(list, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface d, int i) {
						waypoint.setCmd(ApmCommands.getCmd(list[i]));
						update();
					}
				});
		dialog.create().show();
		}
	}

}
