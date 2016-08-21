package org.droidplanner.android.maps;

import android.content.res.Resources;
import android.graphics.Bitmap;

import com.o3dr.services.android.lib.coordinate.LatLong;

import org.droidplanner.android.fragments.DroneMap;

/**
 * Defines the methods expected from a MarkerInfo instance. The marker info
 * object is used to gather information to generate a marker.
 */
public abstract class MarkerInfo {

	private ProxyMarker proxyMarker;

	public void setProxyMarker(ProxyMarker proxyMarker){
		this.proxyMarker = proxyMarker;
	}

	public ProxyMarker getProxyMarker(){
		return proxyMarker;
	}

	public final void removeProxyMarker(){
		if(proxyMarker != null){
			proxyMarker.removeMarker();
		}

		this.proxyMarker = null;
	}

	public final void updateMarker(DroneMap droneMap){
		if(proxyMarker == null){
			droneMap.addMarker(this);
		}
		else {
			Resources res = droneMap.getResources();
			proxyMarker.setAlpha(getAlpha());
			proxyMarker.setAnchor(getAnchorU(), getAnchorV());
			proxyMarker.setInfoWindowAnchor(getInfoWindowAnchorU(), getInfoWindowAnchorV());
			proxyMarker.setPosition(getPosition());
			proxyMarker.setRotation(getRotation());
			proxyMarker.setSnippet(getSnippet());
			proxyMarker.setTitle(getTitle());
			proxyMarker.setDraggable(isDraggable());
			proxyMarker.setFlat(isFlat());
			proxyMarker.setVisible(isVisible());
			proxyMarker.setIcon(getIcon(res));
		}
	}

	public final boolean isOnMap(){
		return proxyMarker != null;
	}

	/**
	 * @return marker's alpha (opacity) value.
	 */
    public float getAlpha() {
        return 1;
    }

	/**
	 * @return marker's horizontal distance normalized to [0,1], of the anchor
	 *         from the left edge.
	 */
    public float getAnchorU() {
        return 0.5F;
    }

	/**
	 * @return marker's vertical distance normalized to [0, 1], of the anchor
	 *         from the top edge.
	 */
    public float getAnchorV() {
        return 0.5F;
    }

	/**
	 * @return marker's icon resource id.
	 */
    public abstract Bitmap getIcon(Resources res);

	/**
	 * @return horizontal distance normalized to [0, 1] of the info window
	 *         anchor from the left edge.
	 */
    public float getInfoWindowAnchorU() {
        return 0;
    }

	/**
	 * @return vertical distance normalized to [0,1] of the info window anchor
	 *         from the top edge.
	 */
    public float getInfoWindowAnchorV() {
        return 0;
    }

	/**
	 * @return marker's map coordinate.
	 */
    public abstract LatLong getPosition();

	/**
	 * Updates the marker info's position.
	 *
	 * @param coord
	 *            position update.
	 */
    public void setPosition(LatLong coord) {}

	/**
	 * @return marker's rotation.
	 */
    public float getRotation() {
        return 0;
    }

	/**
	 * @return string containing the marker's snippet.
	 */
    public String getSnippet() {
        return null;
    }

	/**
	 * @return the marker's title.
	 */
    public String getTitle() {
        return null;
    }

	/**
	 * @return true if the marker's draggable.
	 */
    public boolean isDraggable() {
        return false;
    }

	/**
	 * @return true if the marker's flat.
	 */
    public boolean isFlat() {
        return false;
    }

	/**
	 * @return true if the marker's visible.
	 */
    public boolean isVisible() {
        return false;
    }

	/**
     * Proxy interface to the actual map marker implementaton.
     */
	public interface ProxyMarker {
		/**
		 * Sets the alpha (opacity) of the marker.
		 * @param alpha Value from 0 to 1, where 0 means the marker is completely transparent
		 *                    and 1 means the marker is completely opaque.
         */
		void setAlpha(float alpha);

		/**
		 * Sets the anchor point for the marker.
		 *
		 * The anchor specifies the point in the icon image that is anchored to the marker's position on the Earth's surface.
		 * @param anchorU u-coordinate of the anchor, as a ratio of the image width (in the range [0, 1]).
		 * @param anchorV v-coordinate of the anchor, as a ratio of the image height (in the range [0, 1]).
         */
		void setAnchor (float anchorU, float anchorV);

		/**
		 * Sets the draggability of the marker. When a marker is draggable,
		 * it can be moved by the user by long pressing on the marker.
		 * @param draggable
         */
		void setDraggable(boolean draggable);

		/**
		 * Sets whether this marker should be flat against the map true
		 * or a billboard facing the camera false.
		 * @param flat
         */
		void setFlat(boolean flat);

		/**
		 * Sets the icon for the marker.
		 * @param icon
         */
		void setIcon (Bitmap icon);

		/**
		 * Specifies the point in the marker image at which to anchor the info window when it is displayed.
		 * @param anchorU u-coordinate of the info window anchor, as a ratio of the image width (in the range [0, 1]).
		 * @param anchorV v-coordinate of the info window anchor, as a ratio of the image height (in the range [0, 1]).
         */
		void setInfoWindowAnchor (float anchorU, float anchorV);

		/**
		 * Sets the location of the marker.
		 * @param coord
         */
		void setPosition(LatLong coord);

		/**
		 * Sets the rotation of the marker in degrees clockwise about the marker's anchor point.
		 * @param rotation
         */
		void setRotation (float rotation);

		/**
		 * Sets the snippet of the marker.
		 * @param snippet
         */
		void setSnippet (String snippet);

		/**
		 * Sets the title of the marker.
		 * @param title
         */
		void setTitle (String title);

		/**
		 * Sets the visibility of this marker.
		 * @param visible
         */
		void setVisible (boolean visible);

		/**
		 * Remove the marker from the map.
		 */
		void removeMarker();
	}
}
