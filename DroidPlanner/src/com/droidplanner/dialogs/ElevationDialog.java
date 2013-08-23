package com.droidplanner.dialogs;


import com.droidplanner.R;

import android.app.AlertDialog;
import android.content.Context;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ElevationDialog {

	private TextView progressText;
	private ProgressBar progressBar;

	public ElevationDialog() {
	}

	public void build(Context context) {
		AlertDialog dialog = buildDownloadingDialog(context);
		dialog.show();
	}

	private AlertDialog buildDownloadingDialog(Context context) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(context.getResources().getString(R.string.menu_elevation));
		
		LinearLayout layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.VERTICAL);
		
		progressBar = new ProgressBar(context,null,android.R.attr.progressBarStyleLarge);
		
		progressText = new TextView(context,null,android.R.attr.textAppearanceMedium);
		progressText.setText("Temp");
		progressText.setGravity(Gravity.CENTER);
		
		layout.addView(progressBar);
		layout.addView(progressText);
		
		builder.setView(layout);
		AlertDialog dialog = builder.create();
		return dialog;
	}
}
