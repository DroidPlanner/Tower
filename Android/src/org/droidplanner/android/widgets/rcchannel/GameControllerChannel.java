package org.droidplanner.android.widgets.rcchannel;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.droidplanner.R;

public class GameControllerChannel extends RelativeLayout implements
        OnClickListener, OnCheckedChangeListener {
    private TextView lblTitle;
    private TextView lblValue;
    private boolean firstMode;
    private int value = 1;
    public int IDENTIFIYING_CHANNEL_KEY = -1;

    private GameControllerChannelEvents listener = null;
    private CheckBox chkReversed;
    private LinearLayout viewMode1;
    private LinearLayout viewMode2;
    private Button btnAssignKey;
    private Button btnIncrementKey;
    private Button btnDecrementKey;

    public interface GameControllerChannelEvents {

        void OnSingleKeyPressed(GameControllerChannel gameControllerChannel);

        void OnIncrementPressed(GameControllerChannel gameControllerChannel);

        void OnDecrementPressed(GameControllerChannel gameControllerChannel);

        void OnCheckedReverseChanged(GameControllerChannel v, boolean reversed);

    };

    public GameControllerChannel(Context context) {
        super(context);

        if (!isInEditMode()) {
            loadViews();
            setValue(value);
        }
    }

    public GameControllerChannel(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.fragment_rc_channel, this);

        if (!isInEditMode()) {
            loadViews();

            TypedArray a = context.obtainStyledAttributes(attrs,
                    R.styleable.GameControllerChannel);
            setTitle(a.getString(R.styleable.GameControllerChannel_title));
            setValue(a.getInt(R.styleable.GameControllerChannel_value, -1));
            a.recycle();
        }
    }

    private void loadViews() {
        lblTitle = (TextView) findViewById(R.id.lblTitle);
        lblTitle.setOnClickListener(this);

        lblValue = (TextView) findViewById(R.id.lblValue);
        lblValue.setOnClickListener(this);

        btnAssignKey = (Button) findViewById(R.id.btnAssignKey);
        btnAssignKey.setOnClickListener(this);

        btnIncrementKey = (Button) findViewById(R.id.btnIncrementKey);
        btnIncrementKey.setOnClickListener(this);

        btnDecrementKey = (Button) findViewById(R.id.btnDecrementKey);
        btnDecrementKey.setOnClickListener(this);

        chkReversed = (CheckBox) findViewById(R.id.chkReversed);
        chkReversed.setOnCheckedChangeListener(this);

        viewMode1 = (LinearLayout) findViewById(R.id.viewMode1);
        viewMode2 = (LinearLayout) findViewById(R.id.viewMode2);

        setFirstMode(firstMode = true);
    }

    public void setListener(GameControllerChannelEvents listener) {
        this.listener = listener;
    }

    public void setTitle(String title) {
        lblTitle.setText(title);
    }

    public void setValue(int value) {
        this.value = value;
        lblValue.setText(value + "");
    }

    public void setFirstMode(boolean firstMode) {
        if (this.firstMode == firstMode)
            return;

        this.firstMode = firstMode;
        if (firstMode) {
            viewMode1.setVisibility(View.VISIBLE);
            viewMode2.setVisibility(View.GONE);

        } else {
            viewMode1.setVisibility(View.GONE);
            viewMode2.setVisibility(View.VISIBLE);
        }
    }

    public boolean isFirstMode() {
        return firstMode;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lblTitle:
            case R.id.lblValue:
                setFirstMode(!firstMode);
                break;

            case R.id.btnAssignKey:
                if (listener != null)
                    listener.OnSingleKeyPressed(this);
                break;

            case R.id.btnIncrementKey:
                if (listener != null)
                    listener.OnIncrementPressed(this);
                break;

            case R.id.btnDecrementKey:
                if (listener != null)
                    listener.OnDecrementPressed(this);
                break;
        }
    }

    public void setCheckedWithoutEvent(boolean checked) {
        chkReversed.setOnCheckedChangeListener(null);
        this.chkReversed.setChecked(checked);
        chkReversed.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (listener != null)
            listener.OnCheckedReverseChanged(this, isChecked);
    }

}
