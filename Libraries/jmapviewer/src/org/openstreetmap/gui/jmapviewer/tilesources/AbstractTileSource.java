// License: GPL. For details, see Readme.txt file.
package org.openstreetmap.gui.jmapviewer.tilesources;

import java.awt.Image;

import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import org.openstreetmap.gui.jmapviewer.Coordinate;

abstract public class AbstractTileSource implements TileSource {

    protected String attributionText;
    protected String attributionLinkURL;
    protected Image attributionImage;
    protected String attributionImageURL;
    protected String termsOfUseText;
    protected String termsOfUseURL;

    @Override
    public boolean requiresAttribution() {
        return attributionText != null || attributionImage != null || termsOfUseText != null || termsOfUseURL != null;
    }

    @Override
    public String getAttributionText(int zoom, Coordinate topLeft, Coordinate botRight) {
        return attributionText;
    }

    @Override
    public String getAttributionLinkURL() {
        return attributionLinkURL;
    }

    @Override
    public Image getAttributionImage() {
        return attributionImage;
    }

    @Override
    public String getAttributionImageURL() {
        return attributionImageURL;
    }

    @Override
    public String getTermsOfUseText() {
        return termsOfUseText;
    }

    @Override
    public String getTermsOfUseURL() {
        return termsOfUseURL;
    }

    public void setAttributionText(String attributionText) {
        this.attributionText = attributionText;
    }

    public void setAttributionLinkURL(String attributionLinkURL) {
        this.attributionLinkURL = attributionLinkURL;
    }

    public void setAttributionImage(Image attributionImage) {
        this.attributionImage = attributionImage;
    }

    public void setAttributionImageURL(String attributionImageURL) {
        this.attributionImageURL = attributionImageURL;
    }

    public void setTermsOfUseText(String termsOfUseText) {
        this.termsOfUseText = termsOfUseText;
    }

    public void setTermsOfUseURL(String termsOfUseURL) {
        this.termsOfUseURL = termsOfUseURL;
    }

}
