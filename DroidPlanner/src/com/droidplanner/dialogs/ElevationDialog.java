package com.droidplanner.dialogs;

import java.util.ArrayList;
import java.util.List;

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

	public void build(Context context, List<LatLng> mockPoints) {
		AlertDialog dialog = buildDownloadingDialog(context);
		dialog.show();

		SrtmReader srtm = new SrtmReader(mockPoints, this);
		srtm.execute(0);
	}

	@Override
	public void updateSrtmReaderProgress(String values) {
		progressText.setText(values);
	}

	@Override
	public void srtmReadFinished(ArrayList<Integer> altitudes) {
		layout.removeAllViews();
		layout.addView(chart);
		ChartSeries serie = new ChartSeries(altitudes);
		chart.dataRender.setNumPtsToDraw(altitudes.size());
		chart.scale.setRange(1000, 10, 1000);
		serie.enable();
		serie.setColor(Color.BLUE);
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
