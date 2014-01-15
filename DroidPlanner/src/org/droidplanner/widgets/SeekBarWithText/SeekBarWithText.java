package org.droidplanner.widgets.SeekBarWithText;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.text.InputType;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.view.View;
import android.widget.TextView;

import org.droidplanner.R;

public class SeekBarWithText extends LinearLayout implements
		OnSeekBarChangeListener {

	public interface OnTextSeekBarChangedListner {
		public void onSeekBarChanged();
	}

	private TextView textView;
	private SeekBar seekBar;
	private double min = 0;
	private double inc = 1;
	private String title = "";
	private String unit = "";
	private String formatString = "%2.1f";
	private OnTextSeekBarChangedListner listner;

	public SeekBarWithText(Context context) {
		super(context);
		createViews(context);
	}

	public SeekBarWithText(Context context, AttributeSet attrs) {
		super(context, attrs);
		createViews(context);
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
				R.styleable.SeekBarWithText, 0, 0);

		try {
			setTitle(a.getString(R.styleable.SeekBarWithText_title));
			setUnit(a.getString(R.styleable.SeekBarWithText_unit));
			setMinMaxInc(a.getFloat(R.styleable.SeekBarWithText_min, 0),
					a.getFloat(R.styleable.SeekBarWithText_max, 100),
					a.getFloat(R.styleable.SeekBarWithText_inc, 1));
			setFormat(a.getString(R.styleable.SeekBarWithText_formatString));
		} finally {
			a.recycle();
		}
	}

	private void setFormat(String string) {
		if (string != null) {
			formatString = string;
		}
	}

	private void createViews(final Context context) {
		setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		setOrientation(VERTICAL);
		textView = new TextView(context);
		seekBar = new SeekBar(context);
		seekBar.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		seekBar.setOnSeekBarChangeListener(this);

		addView(textView);
		addView(seekBar);

		textView.setOnLongClickListener(new View.OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				AlertDialog.Builder alert = new AlertDialog.Builder(context);
				alert.setTitle(String.format(
						getResources().getString(R.string.seekbar_edit_title),
						title));
				alert.setMessage(unit.isEmpty() ? "" : (String.format(
						getResources().getString(R.string.seekbar_edit_unit),
						unit)));

				final EditText input = new EditText(context);

				input.setInputType(InputType.TYPE_CLASS_NUMBER
						| InputType.TYPE_NUMBER_FLAG_DECIMAL);
				input.setText(String.format(formatString, getValue()));
				alert.setView(input);

				alert.setPositiveButton("OK",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								try {
									setValue(Double.valueOf(input
											.getEditableText().toString()));
								} catch (NumberFormatException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						});
				alert.setNegativeButton("CANCEL",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								dialog.cancel();
							}
						});
				AlertDialog alertDialog = alert.create();
				alertDialog.show();
				return false;
			}
		});
	}

	public void setMinMaxInc(double min, double max, double inc) {
		this.min = min;
		this.inc = inc;
		seekBar.setMax((int) ((max - min) / inc));
	}

	public void setUnit(String unit) {
		if (unit != null) {
			this.unit = unit;
		}
	}

	public void setTitle(CharSequence text) {
		if (text != null) {
			title = text.toString();
			updateTitle();
		}
	}

	private void updateTitle() {
		textView.setText(String.format("%s\t" + formatString + " %s", title,
				getValue(), unit));
	}

	public double getValue() {
		return (seekBar.getProgress() * inc + min);
	}

	public void setValue(double value) {
		seekBar.setProgress((int) (Math.round((value - min) / inc)));
	}

	public void setAbsValue(double value) {
		if (value < 0)
			value *= -1.0;
		seekBar.setProgress((int) ((value - min) / inc));
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		updateTitle();
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		if (listner != null) {
			listner.onSeekBarChanged();
		}
	}

	public void setOnChangedListner(OnTextSeekBarChangedListner listner) {
		this.listner = listner;
	}

}
