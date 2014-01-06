package com.droidplanner.glass.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.widget.Toast;
import com.droidplanner.R;
import com.droidplanner.glass.activities.GlassFlightActivity;

import java.util.List;
import com.droidplanner.glass.activities.GlassActivity;

/**
 * This class implements the menu to drive the Glass interface with voice.
 * @author Fredia Huya-Kouadio
 */
public class GlassVoiceMenu {

    public static final int SPEECH_REQUEST = 0;
	
	private final GlassActivity glassActivity;
	
	public GlassVoiceMenu(GlassActivity activity){
		glassActivity = activity;
	}

    public void openVoiceMenu(){
        //Start the voice recognizer. List the available options as prompt.
        String extraPrompt = "Say,\n";
        extraPrompt += glassActivity.drone.MavClient.isConnected()
                ?"\t\t\" Flight Modes \"\n\t\t\" Disconnect \"\n"
                :"\t\t\" Connect \"\n";
        extraPrompt += "\t\t\" Hud \"\n\t\t\" Map \"\n\t\t\" Settings \"";

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                .putExtra(RecognizerIntent.EXTRA_PROMPT, extraPrompt);
        glassActivity.startActivityForResult(intent, SPEECH_REQUEST);
    }

    public void onSpeechComplete(Context context, Intent data){
        List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
        String recognizedText = null;
        if(!results.isEmpty()){
            recognizedText = results.get(0);
            if(context.getString(R.string.menu_connect).equalsIgnoreCase(recognizedText)){
                toggleDroneConnection();
            }
            else if(context.getString(R.string.menu_hud).equalsIgnoreCase(recognizedText)){
                launchHud();
            }
            else if(context.getString(R.string.menu_map).equalsIgnoreCase(recognizedText)){
                launchMap();
            }
            else if(context.getString(R.string.screen_settings).equalsIgnoreCase(recognizedText)){
                launchSettings();
            }
            else{
                recognizedText = null;
            }
        }

        if(recognizedText == null){
            Toast.makeText(context, "Unable to recognize speech", Toast.LENGTH_LONG).show();
        }
    }
}
