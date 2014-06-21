package org.droidplanner.android.glass.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MotionEvent;

import org.droidplanner.R;
import org.droidplanner.android.glass.fragments.GlassEditorMapFragment;

/**
 * Used on glass to generate, and edit drone missions.
 */
public class GlassEditorActivity extends GlassUI{

    /**
     * Reference to the menu so it can be updated when used with contextual voice commands.
     */
    protected Menu mMenu;

    private GlassEditorMapFragment mMapFragment;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glass_editor);

        mMapFragment = (GlassEditorMapFragment) getSupportFragmentManager().findFragmentById(R.id
                .glass_editor_map_fragment);
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event){
        return mMapFragment != null && mMapFragment.onGenericMotionEvent(event)
                || super.onGenericMotionEvent(event);
    }
}
