package org.droidplanner.android.maps;

/**
 * Defines the functionality expected from the map providers.
 */
public interface DPMap {

    /**
     * Adds padding around the edges of the map.
     * @param left the number of pixels of padding to be added on the left of the map.
     * @param top the number of pixels of padding to be added on the top of the map.
     * @param right the number of pixels of padding to be added on the right of the map.
     * @param bottom the number of pixels of padding to be added on the bottom of the map.
     */
    public void setMapPadding(int left, int top, int right, int bottom);
}
