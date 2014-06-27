package org.droidplanner.android.widgets.graph;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class ChartCheckBox extends CheckBox implements OnCheckedChangeListener {

    /**
     * Prefix for a chart check box object preference key.
     * The full preference key is obtained by adding this prefix to the check box label.
     *
     * @since 1.2.0
     */
    private static final String PREF_PREFIX = "pref_chart_check_box_";

    /**
     * Default value for the check box check state.
     *
     * @since 1.2.0
     */
    private static final boolean DEFAULT_CHECK_STATE = false;

    /**
     * Preference key for this check box.
     * @since 1.2.0
     */
    private final String mPrefKey;
    private final Chart chart;

    public ChartCheckBox(Context context, String label, Chart chart) {
        super(context);

        ChartSeries serie = new ChartSeries(800);
        this.chart = chart;
        this.chart.series.add(serie);
        setTag(serie);
        setText(label);
        setGravity(Gravity.LEFT);

        mPrefKey = PREF_PREFIX + label;
        //Restore the value for this check box
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        final boolean isChecked = settings.getBoolean(mPrefKey, DEFAULT_CHECK_STATE);
        setChecked(isChecked);
        if (isChecked)
            onCheckBoxEnabled();
        else
            serie.disable();

        setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton checkBox, boolean isChecked) {

        if (isChecked) {
            onCheckBoxEnabled();
        }
        else {
            onCheckBoxDisabled();
        }

        //Store the check state value
        final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences
                (getContext()).edit();
        editor.putBoolean(mPrefKey, isChecked).apply();
        chart.update();
    }

    private void onCheckBoxEnabled(){
        ChartSeries serie = (ChartSeries) getTag();
        serie.enable();
        Integer color = chart.colors.retriveColor();
        serie.setColor(color);
        setTextColor(color);
    }

    private void onCheckBoxDisabled(){
        ChartSeries serie = (ChartSeries) getTag();
        serie.disable();
        chart.colors.depositColor(serie.getColor());
        setTextColor(Color.WHITE);
    }
}
