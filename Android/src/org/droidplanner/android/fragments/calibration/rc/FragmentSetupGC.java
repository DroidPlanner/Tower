package org.droidplanner.android.fragments.calibration.rc;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import org.droidplanner.R;
import org.droidplanner.android.activities.helpers.ControllerEventCaptureView;
import org.droidplanner.android.activities.interfaces.PhysicalDeviceEvents;
import org.droidplanner.android.utils.rc.RCConstants;
import org.droidplanner.android.utils.rc.RCControlManager;
import org.droidplanner.android.utils.rc.input.GenericInputDevice.IRCEvents;
import org.droidplanner.android.utils.rc.input.GameController.Controller.BaseCommand;
import org.droidplanner.android.utils.rc.input.GameController.Controller.ButtonRemap;
import org.droidplanner.android.utils.rc.input.GameController.Controller.DoubleAxisRemap;
import org.droidplanner.android.utils.rc.input.GameController.Controller.SingleAxisRemap;
import org.droidplanner.android.utils.rc.input.GameController.GameControllerConfig;
import org.droidplanner.android.widgets.rcchannel.GameControllerChannel;

import java.util.ArrayList;
import java.util.List;

public class FragmentSetupGC extends Fragment implements OnClickListener, GameControllerChannel.GameControllerChannelEvents, PhysicalDeviceEvents, IRCEvents {

    private GameControllerConfig gcConfig;
    private List<GameControllerChannel> channels = new ArrayList<GameControllerChannel>();

    private RCControlManager rcOutput;
    
    private ControllerEventCaptureView eventsView;
    private WindowManager wm;
    private boolean assignArmButton = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ScrollView rootView = (ScrollView) inflater.inflate(R.layout.fragment_setup_gc_main, container, false);
        LinearLayout view = (LinearLayout) rootView.findViewById(R.id.setup_gc_container);
        
        gcConfig = GameControllerConfig.getInstance(getActivity());
        
        for(int channelId : RCConstants.rchannels) { //For each channel
            GameControllerChannel current = new GameControllerChannel(this.getActivity());
            current.setTag(channelId);
            current.setTitle(RCConstants.RChannelsTitle[channelId]);
            current.setFirstMode(gcConfig.isSingleAxis(channelId));
            //Change to setView(getView(BaseCommand))
            current.setCheckedWithoutEvent(gcConfig.isReversed(channelId));
            
            view.addView(current);
            channels.add(current);
        }
        
        rcOutput = new RCControlManager(this.getActivity());
        rcOutput.registerListener(this);
        
        view.findViewById(R.id.btnArmAssign).setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();

        for (GameControllerChannel current : channels)
            current.setListener(null);
        
        gcConfig.save();
        removeControllerListener();
    }
    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        float[] channelsValues = new float[channels.size()];
        for(int x = 0; x < channels.size(); x++) {
            channelsValues[x] = channels.get(x).getValue();
        }
        outState.putFloatArray("channelsValues", channelsValues);
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        if(savedInstanceState != null) {
            float[] channelsValues = savedInstanceState.getFloatArray("channelsValues");
            for(int x = 0; x < channels.size(); x++) {
                channels.get(x).setValue(channelsValues[x]);
            }
        }
        
    }

    @Override
    public void onResume() {
        super.onResume();
        
        for (GameControllerChannel current : channels) {
            current.setCheckedWithoutEvent(gcConfig.isReversed((int) current.getTag()));
            current.setListener(this);
        }
        createControllerEventListener();
    }

    @Override
    public void onChannelsChanged(float[] channelsValue) {
        for (GameControllerChannel current : channels) {
            current.setValue(channelsValue[(int) current.getTag()]);
        }
    }

    @Override
    public void physicalJoyMoved(MotionEvent event) {
        rcOutput.onGenericMotionEvent(event);
    }
    
    @Override
    public void OnCheckedReverseChanged(GameControllerChannel v, boolean reversed) {
        int channelId = (int) v.getTag();
        if(!gcConfig.isSingleAxisButton(channelId))
            gcConfig.getSingleRemap(channelId).isReversed = reversed;
    }
    
    public static String getTitle(Context c) {
        return "Controller Setup";
    }

    @Override
    public void OnAssignPressed(GameControllerChannel gameControllerChannel, int mode, int trigger) {
        int channelId = (int) gameControllerChannel.getTag();
        switch(mode) {
            case RCConstants.MODE_SINGLEKEY:
                SingleAxisRemap remap = gcConfig.getSingleRemap(channelId);
                remap.Trigger = trigger;
                break;
            case RCConstants.MODE_INCREMENTKEY:
                DoubleAxisRemap remap1 = gcConfig.getDoubleRemap(channelId);
                remap1.TriggerIncrement = trigger;
                break;
            case RCConstants.MODE_DECREMENTKEY:
                DoubleAxisRemap remap2 = gcConfig.getDoubleRemap(channelId);
                remap2.TriggerDecrement = trigger;
                break;
            case RCConstants.MODE_JOYSTICK_BUTTON:
                ButtonRemap remap3 = gcConfig.getJoystickButtonRemap(channelId);
                remap3.Trigger = trigger;
                break;
        }
    }

    @Override
    public void physicalKeyUp(int keyCode, KeyEvent event) {
        if(assignArmButton) {
            gcConfig.removeButtonRemapAction(1);
            ButtonRemap remap = gcConfig.getButtonRemap(keyCode);
            remap.Action = ButtonRemap.ARM_DISARM;
            assignArmButton = false;
            Toast.makeText(this.getActivity(), "Assigned Arm Button " + keyCode, Toast.LENGTH_SHORT).show();
            Vibrator vibrator = (Vibrator) this.getActivity().getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(250);
        }
        rcOutput.onKeyUp(keyCode, event);
    }

    @Override
    public void onSearchJoystickAxisStart() {
        removeControllerListener(); //Remove overlay so dialog is visible
    }

    @Override
    public void onSearchJoystickAxisFinished() {
        createControllerEventListener();
    }
    
    private void removeControllerListener() {
      if(eventsView != null && wm != null && eventsView.isShown());
          wm.removeView(eventsView);
    }
    
    private void createControllerEventListener() {
        if(eventsView == null)
            eventsView = new ControllerEventCaptureView(this.getActivity());
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                5,
                5,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSPARENT);
        eventsView.registerPhysicalDeviceEventListener(this);
        
        wm = (WindowManager) this.getActivity().getSystemService(Activity.WINDOW_SERVICE);
        wm.addView(eventsView, params);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnArmAssign:
                assignArmButton = true;
                break;
        }
    }

    @Override
    public void clear(GameControllerChannel channel) {
        gcConfig.getController().joystickRemap.remove(channel.getTag());
    }
    
    /*public void onJoystickViewChanged(View gamecontrollerChannel, BaseCommand command) {
        gcConfig.getController().joystickRemap.put(gameControllerChannel.getTag(), command);
    }
    public View getChannelView(BaseCommand command) {
        if(command == null)
            return null;
        else if(command instanceof SingleAxisRemap)
            return null;
        else if(command instanceof ButtonRemap)
            return null;
        else if(command instanceof DoubleAxisRemap)
            return null;
        return null;
    }*/

}
