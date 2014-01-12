package org.droidplanner.glass.utils.voice_menu;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.ActionProvider;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

/**
 * Implementation of the {@link android.view.MenuItem} interface for creating a voice menu ui.
 */
public class VoiceMenuItem implements MenuItem {

    private final int id;
    private final int group;
    private final int categoryOrder;
    private final int ordering;

    /**
     * Menu item title.
     */
    private CharSequence title;

    /**
     * Sets the visibility for this menu item.
     */
    private boolean active = true;

    /**
     * The voice menu to which this item belongs.
     */
    private VoiceMenu voiceMenu;

    /**
     * If this item should launch a sub menu, this is the sub menu to launch.
     */
    private SubVoiceMenu subVoiceMenu;

    VoiceMenuItem(VoiceMenu menu, int group, int id, int categoryOrder, int ordering,
                  CharSequence title){
        this.voiceMenu = menu;
        this.group = group;
        this.id = id;
        this.categoryOrder = categoryOrder;
        this.ordering = ordering;
        this.title = title;
    }

    @Override
    public int getItemId() {
        return id;
    }

    @Override
    public int getGroupId() {
        return group;
    }

    @Override
    public int getOrder() {
        return categoryOrder;
    }

    public int getOrdering(){
        return ordering;
    }

    @Override
    public MenuItem setTitle(CharSequence title) {
        this.title = title;
        return this;
    }

    @Override
    public MenuItem setTitle(int title) {
        return setTitle(voiceMenu.getContext().getString(title));
    }

    @Override
    public CharSequence getTitle() {
        return title;
    }

    @Override
    public MenuItem setTitleCondensed(CharSequence title) {
        return this;
    }

    @Override
    public CharSequence getTitleCondensed() {
        return title;
    }

    @Override
    public MenuItem setIcon(Drawable icon) {
        return this;
    }

    @Override
    public MenuItem setIcon(int iconRes) {
        return this;
    }

    @Override
    public Drawable getIcon() {
        return null;
    }

    @Override
    public MenuItem setIntent(Intent intent) {
        return this;
    }

    @Override
    public Intent getIntent() {
        return null;
    }

    @Override
    public MenuItem setShortcut(char numericChar, char alphaChar) {
        return this;
    }

    @Override
    public MenuItem setNumericShortcut(char numericChar) {
        return this;
    }

    @Override
    public char getNumericShortcut() {
        return 0;
    }

    @Override
    public MenuItem setAlphabeticShortcut(char alphaChar) {
        return this;
    }

    @Override
    public char getAlphabeticShortcut() {
        return 0;
    }

    @Override
    public MenuItem setCheckable(boolean checkable) {
        return this;
    }

    @Override
    public boolean isCheckable() {
        return false;
    }

    @Override
    public MenuItem setChecked(boolean checked) {
        return this;
    }

    @Override
    public boolean isChecked() {
        return false;
    }

    @Override
    public MenuItem setVisible(boolean visible) {
        this.active = visible;
        return this;
    }

    @Override
    public boolean isVisible() {
        return active;
    }

    @Override
    public MenuItem setEnabled(boolean enabled) {
        this.active = enabled;
        return this;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }

    @Override
    public boolean hasSubMenu() {
        return subVoiceMenu != null;
    }

    @Override
    public SubMenu getSubMenu() {
        return subVoiceMenu;
    }

    void setSubMenu(SubVoiceMenu subMenu){
        this.subVoiceMenu = subMenu;
    }

    @Override
    public MenuItem setOnMenuItemClickListener(OnMenuItemClickListener menuItemClickListener) {
        return this;
    }

    @Override
    public ContextMenu.ContextMenuInfo getMenuInfo() {
        return null;
    }

    @Override
    public void setShowAsAction(int actionEnum) { }

    @Override
    public MenuItem setShowAsActionFlags(int actionEnum) {
        return this;
    }

    @Override
    public MenuItem setActionView(View view) {
        return this;
    }

    @Override
    public MenuItem setActionView(int resId) {
        return this;
    }

    @Override
    public View getActionView() {
        return null;
    }

    @Override
    public MenuItem setActionProvider(ActionProvider actionProvider) {
        return this;
    }

    @Override
    public ActionProvider getActionProvider() {
        return null;
    }

    @Override
    public boolean expandActionView() {
        return false;
    }

    @Override
    public boolean collapseActionView() {
        return false;
    }

    @Override
    public boolean isActionViewExpanded() {
        return false;
    }

    @Override
    public MenuItem setOnActionExpandListener(OnActionExpandListener listener) {
        return this;
    }
}
