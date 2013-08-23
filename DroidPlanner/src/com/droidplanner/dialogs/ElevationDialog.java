package com.droidplanner.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.droidplanner.R;
import com.droidplanner.helpers.srtm.Srtm.OnProgressListner;
import com.droidplanner.helpers.srtm.SrtmReader;
import com.google.android.gms.maps.model.LatLng;

public class ElevationDialog implements OnProgressListner {

	private LinearLayout layout;
	private TextView progressText;
	private ProgressBar progressBar;

	public void build(Context context) {
		AlertDialog dialog = buildDownloadingDialog(context);
		dialog.show();

		SrtmReader srtm = new SrtmReader() {
			@Override
			public void update(String values) {
				progressText.setText(values);
			}

			@Override
			public void finish() {
				layout.removeAllViews();
			}
		};
		srtm.execute(new LatLng(-29.7026708, -51.1439127));
	}

	@Override
	public void onProgress(String filename, int percentage) {

	}

	private AlertDialog buildDownloadingDialog(Context context) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(context.getResources().getString(
				R.string.menu_elevation));

		layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.VERTICAL);

		progressBar = new ProgressBar(context, null,
				android.R.attr.progressBarStyleLarge);

		progressText = new TextView(context, null,
				android.R.attr.textAppearanceMedium);
		progressText.setGravity(Gravity.CENTER);
		progressText.setText("Downloading SRTM data");

		layout.addView(progressBar);
		layout.addView(progressText);

		builder.setView(layout);
		AlertDialog dialog = builder.create();
		return dialog;
	}

}
