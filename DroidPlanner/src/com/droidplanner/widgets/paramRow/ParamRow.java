package com.droidplanner.widgets.paramRow;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableRow;
import android.widget.TextView;

import com.droidplanner.MAVLink.Parameter;

public class ParamRow extends TableRow implements OnClickListener {

	private TextView nameView;
	private EditText valueView;
	private TextView typeView;
	private TextView indexView;
	private Button sendButton;

	public ParamRow(Context context) {
		super(context);
		createRowViews(context);
	}

	public ParamRow(Context context, AttributeSet attrs) {
		super(context, attrs);
		createRowViews(context);
	}

	public void setParam(Parameter param) {
		nameView.setText(param.name);
		valueView.setText(String.format("%3.3f", param.value));
		typeView.setText(Integer.toString(param.type));
		indexView.setText(Integer.toString(param.index));
		sendButton.setText("Send");
	}

	private void createRowViews(Context context) {
		nameView = new TextView(context);
		valueView = new EditText(context);
		typeView = new TextView(context);
		indexView = new TextView(context);
		sendButton = new Button(context);

		valueView.setInputType(InputType.TYPE_CLASS_NUMBER);

		typeView.setGravity(Gravity.RIGHT);

		indexView.setWidth(50);
		nameView.setWidth(150);
		valueView.setWidth(100);
		typeView.setWidth(50);

		addView(indexView);
		addView(nameView);
		addView(valueView);
		addView(typeView);
		addView(sendButton);
		

		sendButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		if(view == sendButton){
			String param =  nameView.getText().toString();
			String value =  valueView.getText().toString();
			Log.d("PARM", "Send: "+param+" : "+value);
		}
	}

}
