package com.droidplanner.glass.utils.voice_menu;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.SubMenu;

import java.util.ArrayList;

import com.droidplanner.glass.activities.GlassActivity;
import android.view.Menu;

/**
 * Implementation of the {@link android.view.Menu} interface for creating a voice menu UI.
 */
public class VoiceMenu implements Menu {

    /**
     * This is the part of an order integer that the user can provide.
     */
    static final int USER_MASK = 0x0000ffff;

    /**
     * This is the part of an order integer that supplies the category of the
     * item.
     */
    static final int CATEGORY_MASK = 0xffff0000;
    /**
     * Bit shift of the category portion of the order integer.
     */
    static final int CATEGORY_SHIFT = 16;

    /**
     * Request code for the speech recognizer result.
     */
    public static final int SPEECH_REQUEST = 0;

    private static final int[]  sCategoryToOrder = new int[] {
            1, /* No category */
            4, /* CONTAINER */
            5, /* SYSTEM */
            3, /* SECONDARY */
            2, /* ALTERNATIVE */
            0, /* SELECTED_ALTERNATIVE */
    };

    /**
     * Glass activity container.
     */
	private final GlassActivity glassActivity;

    /**
     * Contains all of the items for this menu
     */
    private ArrayList<VoiceMenuItem> mItems;
	
	/**
	* Header for speech recognizer prompt.
	*/
	private CharSequence promptHeader;

	public VoiceMenu(GlassActivity activity){
		glassActivity = activity;

        mItems = new ArrayList<VoiceMenuItem>();
	}
	
	public VoiceMenu setPromptHeader(CharSequence header){
		promptHeader = header;
		return this;
	}
	
	/**
     * Start the voice recognizer, displaying the configured MenuItem as prompt options.
     */
    public void openVoiceMenu(){
		//Build the voice menu prompt 
		ArrayList<VoiceMenuItem> visibleItems = getVisibleItems();

        String extraPrompt = "";
		if(promptHeader != null){
			extraPrompt += promptHeader.toString();
		}
		
		for(VoiceMenuItem item : visibleItems){
			String itemTitle = item.getTitle().toString();
			extraPrompt += "\n\t\t\" " + itemTitle + " \""; 
		}

        glassActivity.setRecognizerIntentOriginMenu(this);

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
			.putExtra(RecognizerIntent.EXTRA_PROMPT, extraPrompt);
        glassActivity.startActivityForResult(intent, VoiceMenu.SPEECH_REQUEST);
    }

    /**
     * When the speech recognizer returns, this is used to parse the recognized speech,
     * and identify the corresponding voice menu item.
     * @param recognizedSpeech result from the RecognizerIntent
     * @return true if the corresponding voice menu item was identified and handled.
     */
    public boolean dispatchVoiceMenuItemRecognized(String recognizedSpeech){
        ArrayList<VoiceMenuItem> visibleItems = getVisibleItems();
        for(VoiceMenuItem item: visibleItems){
            String itemTitle = item.getTitle().toString();
            if(itemTitle.equalsIgnoreCase(recognizedSpeech)){
                //Check if the menu item has a sub menu.
                if(item.hasSubMenu()){
                    //Launch the recognizer intent for the sub menu items.
                    SubVoiceMenu subMenu = (SubVoiceMenu)item.getSubMenu();
                    subMenu.openVoiceMenu();
                    return true;
                }
                else {
                    return glassActivity.onOptionsItemSelected(item) || glassActivity
                            .onMenuItemSelected(0, item);
                }
            }
        }
        return false;
    }

    /**
     * @return the container activity's context.
     */
    public Context getContext(){
        return glassActivity.getApplicationContext();
    }

    public ArrayList<VoiceMenuItem> getVisibleItems(){
        ArrayList<VoiceMenuItem> visibleItems = new ArrayList<VoiceMenuItem>();
        for(VoiceMenuItem item: mItems){
            if(item.isVisible()){
                visibleItems.add(item);
            }
        }

        return visibleItems;
    }

    @Override
    public MenuItem add(CharSequence title) {
        return add(0, 0, 0, title);
    }

    @Override
    public MenuItem add(int titleRes) {
        return add(0,0,0, titleRes);
    }

    @Override
    public MenuItem add(int groupId, int itemId, int order, CharSequence title) {
        final int ordering = getOrdering(order);

        final VoiceMenuItem item = new VoiceMenuItem(this, groupId, itemId, order, ordering,
                title);

        mItems.add(findInsertIndex(mItems, ordering), item);
        return item;
    }

    @Override
    public MenuItem add(int groupId, int itemId, int order, int titleRes) {
        return add(groupId, itemId, order, glassActivity.getString(titleRes));
    }

    @Override
    public SubMenu addSubMenu(CharSequence title) {
        return addSubMenu(0, 0, 0, title);
    }

    @Override
    public SubMenu addSubMenu(int titleRes) {
        return addSubMenu(0, 0, 0 , titleRes);
    }

    @Override
    public SubMenu addSubMenu(int groupId, int itemId, int order, CharSequence title) {
        final VoiceMenuItem item = (VoiceMenuItem) add(groupId, itemId, order, title);
        final SubVoiceMenu subMenu = new SubVoiceMenu(glassActivity, this, item);
        subMenu.setPromptHeader(title);
        item.setSubMenu(subMenu);

        return subMenu;
    }

    @Override
    public SubMenu addSubMenu(int groupId, int itemId, int order, int titleRes) {
        return addSubMenu(groupId, itemId, order, glassActivity.getString(titleRes));
    }

    @Override
    public int addIntentOptions(int groupId, int itemId, int order, ComponentName caller,
                                Intent[] specifics, Intent intent, int flags, MenuItem[] outSpecificItems) {
        return 0;
    }

    public int findItemIndex(int id) {
        final int size = size();

        for (int i = 0; i < size; i++) {
            VoiceMenuItem item = mItems.get(i);
            if (item.getItemId() == id) {
                return i;
            }
        }

        return -1;
    }

    public int findGroupIndex(int group) {
        return findGroupIndex(group, 0);
    }

    public int findGroupIndex(int group, int start) {
        final int size = size();

        if (start < 0) {
            start = 0;
        }

        for (int i = start; i < size; i++) {
            final VoiceMenuItem item = mItems.get(i);

            if (item.getGroupId() == group) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public void removeItem(int id) {
        removeItemAtInt(findItemIndex(id));
    }

    /**
     * Remove the item at the given index and optionally forces menu views to
     * update.
     *
     * @param index The index of the item to be removed. If this index is
     *            invalid an exception is thrown.
     */
    private void removeItemAtInt(int index) {
        if ((index < 0) || (index >= mItems.size())) return;

        mItems.remove(index);
    }

    @Override
    public void removeGroup(int groupId) {
        final int i = findGroupIndex(groupId);

        if(i >= 0){
            final int maxRemovable = mItems.size() - i;
            int numRemoved = 0;
            while((numRemoved++ < maxRemovable) && (mItems.get(i).getGroupId() == groupId)){
                removeItemAtInt(i);
            }
        }
    }

    @Override
    public void clear() {
        mItems.clear();
    }

    @Override
    public void setGroupCheckable(int group, boolean checkable, boolean exclusive) { }

    @Override
    public void setGroupVisible(int group, boolean visible) {
        for(VoiceMenuItem item: mItems){
            if(item.getGroupId() == group){
                item.setVisible(visible);
            }
        }
    }

    @Override
    public void setGroupEnabled(int group, boolean enabled) {
        for(VoiceMenuItem item: mItems){
            if(item.getGroupId() == group){
                item.setEnabled(enabled);
            }
        }
    }

    @Override
    public boolean hasVisibleItems() {
        for(VoiceMenuItem item: mItems){
            if(item.isVisible())
                return true;
        }

        return false;
    }

    @Override
    public MenuItem findItem(int id) {
        for(VoiceMenuItem item: mItems){
            if(item.getItemId() == id){
                return item;
            }
            else if(item.hasSubMenu()){
                MenuItem possibleItem = item.getSubMenu().findItem(id);
                if(possibleItem != null){
                    return possibleItem;
                }
            }
        }

        return null;
    }

    @Override
    public int size() {
        return mItems.size();
    }

    @Override
    public MenuItem getItem(int index) {
        return mItems.get(index);
    }

    @Override
    public void close() {}

    @Override
    public boolean performShortcut(int keyCode, KeyEvent event, int flags) {
        return false;
    }

    @Override
    public boolean isShortcutKey(int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    public boolean performIdentifierAction(int id, int flags) {
        return false;
    }

    @Override
    public void setQwertyMode(boolean isQwerty) { }

    private static int findInsertIndex(ArrayList<VoiceMenuItem> items, int ordering) {
        for (int i = items.size() - 1; i >= 0; i--) {
            VoiceMenuItem item = items.get(i);
            if (item.getOrdering() <= ordering) {
                return i + 1;
            }
        }

        return 0;
    }

    /**
     * Returns the ordering across all items. This will grab the category from
     * the upper bits, find out how to order the category with respect to other
     * categories, and combine it with the lower bits.
     *
     * @param categoryOrder The category order for a particular item (if it has
     *            not been or/add with a category, the default category is
     *            assumed).
     * @return An ordering integer that can be used to order this item across
     *         all the items (even from other categories).
     */
    private static int getOrdering(int categoryOrder) {
        final int index = (categoryOrder & CATEGORY_MASK) >> CATEGORY_SHIFT;

        if (index < 0 || index >= sCategoryToOrder.length) {
            throw new IllegalArgumentException("order does not contain a valid category.");
        }

        return (sCategoryToOrder[index] << CATEGORY_SHIFT) | (categoryOrder & USER_MASK);
    }
}
