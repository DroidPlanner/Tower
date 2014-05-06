package org.droidplanner.android.maps;

import android.content.res.Resources;
import android.graphics.Bitmap;

import org.droidplanner.core.helpers.coordinates.Coord2D;

/**
 * Defines the methods expected from a MarkerInfo instance.
 * The marker info object is used to gather information to generate a marker.
 */
public interface MarkerInfo {

    /**
     * @return marker's alpha (opacity) value.
     */
    float getAlpha();

    /**
     * @return marker's horizontal distance normalized to [0,1], of the anchor from the left
     * edge.
     */
    float getAnchorU();

    /**
     * @return marker's vertical distance normalized to [0, 1], of the anchor from the top edge.
     */
    float getAnchorV();

    /**
     * @return marker's icon resource id.
     */
    Bitmap getIcon(Resources res);

    /**
     * @return horizontal distance normalized to [0, 1] of the info window anchor from the
     * left edge.
     */
    float getInfoWindowAnchorU();

    /**
     * @return vertical distance normalized to [0,1] of the info window anchor from the top
     * edge.
     */
    float getInfoWindowAnchorV();

    /**
     * @return marker's map coordinate.
     */
    Coord2D getPosition();

    /**
     * Updates the marker info's position.
     * @param coord position update.
     */
    void setPosition(Coord2D coord);

    /**
     * @return marker's rotation.
     */
    float getRotation();

    /**
     * @return string containing the marker's snippet.
     */
    String getSnippet();

    /**
     * @return the marker's title.
     */
    String getTitle();

    /**
     * @return true if the marker's draggable.
     */
    boolean isDraggable();

    /**
     * @return true if the marker's flat.
     */
    boolean isFlat();

    /**
     * @return true if the marker's visible.
     */
    boolean isVisible();

    /**
     * Default implementation of the MarkerInfo interface.
     */
    public static class SimpleMarkerInfo implements MarkerInfo {

        @Override
        public float getAlpha() {
            return 1;
        }

        @Override
        public float getAnchorU() {
            return 0;
        }

        @Override
        public float getAnchorV() {
            return 0;
        }

        @Override
        public Bitmap getIcon(Resources res) {
            return null;
        }

        @Override
        public float getInfoWindowAnchorU() {
            return 0;
        }

        @Override
        public float getInfoWindowAnchorV() {
            return 0;
        }

        @Override
        public Coord2D getPosition() {
            return null;
        }

        @Override
        public void setPosition(Coord2D coord){}

        @Override
        public float getRotation() {
            return 0;
        }

        @Override
        public String getSnippet() {
            return null;
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public boolean isDraggable() {
            return false;
        }

        @Override
        public boolean isFlat() {
            return false;
        }

        @Override
        public boolean isVisible() {
            return false;
        }
    }

}
