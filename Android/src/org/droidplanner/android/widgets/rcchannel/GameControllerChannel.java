package org.droidplanner.android.widgets.rcchannel;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.droidplanner.android.R;
import org.droidplanner.android.dialogs.ControllerAxisKeyPressDialog;
import org.droidplanner.android.dialogs.ControllerAxisKeyPressDialog.ControllerPressListener;
import org.droidplanner.android.utils.rc.RCConstants;

public class GameControllerChannel extends RelativeLayout implements
        OnClickListener, OnLongClickListener, OnCheckedChangeListener, ControllerPressListener {
    private TextView lblTitle;
    private TextView lblValue;
    private boolean firstMode;
    private float value = 0;

    private GameControllerChannelEvents listener = null;
    private CheckBox chkReversed;
    private LinearLayout viewMode1;
    private LinearLayout viewMode2;
    private Button btnAssignKey;
    private Button btnIncrementKey;
    private Button btnDecrementKey;

    public interface GameControllerChannelEvents {

        void OnAssignPressed(GameControllerChannel gameControllerChannel, int id, int key);

        void OnCheckedReverseChanged(GameControllerChannel v, boolean reversed);

        void onSearchJoystickAxisStart();

        void onSearchJoystickAxisFinished();

        void clear(GameControllerChannel channel);
    }

    ;

    public GameControllerChannel(Context context) {
        super(context);

        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.fragment_rc_channel, this);

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
        }
    }

    private void loadViews() {
        lblTitle = (TextView) findViewById(R.id.lblTitle);

        lblValue = (TextView) findViewById(R.id.lblValue);

        btnAssignKey = (Button) findViewById(R.id.btnAssignKey);
        btnAssignKey.setOnClickListener(this);
        btnAssignKey.setOnLongClickListener(this);

        btnIncrementKey = (Button) findViewById(R.id.btnIncrementKey);
        btnIncrementKey.setOnClickListener(this);
        btnIncrementKey.setOnLongClickListener(this);

        btnDecrementKey = (Button) findViewById(R.id.btnDecrementKey);
        btnDecrementKey.setOnClickListener(this);
        btnDecrementKey.setOnLongClickListener(this);

        chkReversed = (CheckBox) findViewById(R.id.chkReversed);
        chkReversed.setOnCheckedChangeListener(this);

        viewMode1 = (LinearLayout) findViewById(R.id.viewMode1);
        viewMode2 = (LinearLayout) findViewById(R.id.viewMode2);

        findViewById(R.id.textholder).setOnClickListener(this);

        setFirstMode(firstMode = true);
    }

    public void setListener(GameControllerChannelEvents listener) {
        this.listener = listener;
    }

    public void setTitle(String title) {
        lblTitle.setText(title);
    }

    public void setValue(float value) {
        this.value = value;
        lblValue.setText(Math.round(value) + "");
    }

    public float getValue() {
        return value;
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
            case R.id.textholder:
                changeMode();
                break;
            case R.id.btnAssignKey:
                showDialog(RCConstants.MODE_SINGLEKEY);
                break;

            case R.id.btnIncrementKey:
                showDialog(RCConstants.MODE_INCREMENTKEY);
                break;

            case R.id.btnDecrementKey:
                showDialog(RCConstants.MODE_DECREMENTKEY);
                break;
        }
    }

    private void changeMode() {
        setFirstMode(!firstMode);
        if (firstMode)
            setCheckedWithoutEvent(false);
    }

    private void showDialog(int id) {
        if (listener != null)
            listener.onSearchJoystickAxisStart();

        ControllerAxisKeyPressDialog dialog = new ControllerAxisKeyPressDialog(this.getContext());
        dialog.setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                if (listener != null)
                    listener.onSearchJoystickAxisFinished();
            }

        });
        dialog.registerListener(this);
        dialog.ID = id;
        dialog.show();
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

    @Override
    public void onControllerPress(ControllerAxisKeyPressDialog dialog, int key, boolean fromJoystick) {
        if (listener != null) {
            if (fromJoystick) {
                listener.OnAssignPressed(this, dialog.ID, key);
                dialog.dismiss();
            } else if (dialog.ID == RCConstants.MODE_SINGLEKEY) {
                listener.OnAssignPressed(this, RCConstants.MODE_JOYSTICK_BUTTON, key);
                dialog.dismiss();
            }
        }
    }

    @Override
    public boolean onLongClick(View v) {
        Toast.makeText(this.getContext(), "Cleared", Toast.LENGTH_SHORT).show();
        Vibrator vibrator = (Vibrator) this.getContext().getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(250);
        if (listener != null)
            listener.clear(this);
        return false;
    }

}
