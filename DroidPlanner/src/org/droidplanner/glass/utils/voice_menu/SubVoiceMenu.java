package org.droidplanner.glass.utils.voice_menu;

import android.graphics.drawable.Drawable;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import org.droidplanner.glass.activities.GlassActivity;

/**
 * @author Fredia Huya-Kouadio
 */
public class SubVoiceMenu extends VoiceMenu implements SubMenu {

    private VoiceMenu voiceMenu;
    private VoiceMenuItem voiceMenuItem;

    public SubVoiceMenu(GlassActivity glassActivity, VoiceMenu voiceMenu, VoiceMenuItem item){
        super(glassActivity);

        this.voiceMenu = voiceMenu;
        this.voiceMenuItem = item;
    }

    @Override
    public SubMenu setHeaderTitle(int titleRes) {
        return null;
    }

    @Override
    public SubMenu setHeaderTitle(CharSequence title) {
        return null;
    }

    @Override
    public SubMenu setHeaderIcon(int iconRes) {
        return this;
    }

    @Override
    public SubMenu setHeaderIcon(Drawable icon) {
        return this;
    }

    @Override
    public SubMenu setHeaderView(View view) {
        return this;
    }

    @Override
    public void clearHeader() { }

    @Override
    public SubMenu setIcon(int iconRes) {
        return this;
    }

    @Override
    public SubMenu setIcon(Drawable icon) {
        return this;
    }

    @Override
    public MenuItem getItem() {
        return voiceMenuItem;
    }
}
