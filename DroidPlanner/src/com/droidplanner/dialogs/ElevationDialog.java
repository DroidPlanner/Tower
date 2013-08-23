package com.droidplanner.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.droidplanner.R;
import com.droidplanner.helpers.srtm.SrtmReader;
import com.droidplanner.helpers.srtm.SrtmReader.OnSrtmReaderListner;
import com.droidplanner.widgets.graph.Chart;
import com.droidplanner.widgets.graph.ChartSeries;
import com.google.android.gms.maps.model.LatLng;

public class ElevationDialog implements OnSrtmReaderListner {

	private LinearLayout layout;
	private TextView progressText;
	private ProgressBar progressBar;
	private Chart chart;

	public void build(Context context) {
		AlertDialog dialog = buildDownloadingDialog(context);
		dialog.show();

		SrtmReader srtm = new SrtmReader(this);
		srtm.execute(new LatLng(-29.7026708, -51.1439127));
	}

	@Override
	public void updateSrtmReaderProgress(String values) {
		progressText.setText(values);
	}

	@Override
	public void srtmReadFinished() {
		layout.removeAllViews();
		layout.addView(chart);
		ChartSeries serie = new ChartSeries(80);
		chart.dataRender.setNumPtsToDraw(80);
		serie.enable();
		serie.setColor(Color.BLUE);
		for (int i = 0; i < 80; i++) {
			serie.newData(i%80);			
		}
		chart.series.add(serie);
		chart.update();
	}

	private AlertDialog buildDownloadingDialog(Context context) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(context.getResources().getString(
				R.string.menu_elevation));

		layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.VERTICAL);

		chart = new Chart(context, null);

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
