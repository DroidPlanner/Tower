// License: GPL. For details, see Readme.txt file.
package org.openstreetmap.gui.jmapviewer.tilesources;

import java.awt.Image;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class BingAerialTileSource extends AbstractTMSTileSource {

    private static String API_KEY = "Arzdiw4nlOJzRwOz__qailc8NiR31Tt51dN2D7cm57NrnceZnCpgOkmJhNpGoppU";
    private static volatile Future<List<Attribution>> attributions; // volatile is required for getAttribution(), see below.
    private static String imageUrlTemplate;
    private static Integer imageryZoomMax;
    private static String[] subdomains;

    private static final Pattern subdomainPattern = Pattern.compile("\\{subdomain\\}");
    private static final Pattern quadkeyPattern = Pattern.compile("\\{quadkey\\}");
    private static final Pattern culturePattern = Pattern.compile("\\{culture\\}");

    public BingAerialTileSource() {
        super("Bing Aerial Maps", "http://example.com/");
    }

    protected class Attribution {
        String attribution;
        int minZoom;
        int maxZoom;
        Coordinate min;
        Coordinate max;
    }

    @Override
    public String getTileUrl(int zoom, int tilex, int tiley) throws IOException {
        // make sure that attribution is loaded. otherwise subdomains is null.
        if (getAttribution() == null)
            throw new IOException("Attribution is not loaded yet");

        int t = (zoom + tilex + tiley) % subdomains.length;
        String subdomain = subdomains[t];

        String url = imageUrlTemplate;
        url = subdomainPattern.matcher(url).replaceAll(subdomain);
        url = quadkeyPattern.matcher(url).replaceAll(computeQuadTree(zoom, tilex, tiley));

        return url;
    }

    protected URL getAttributionUrl() throws MalformedURLException {
        return new URL("http://dev.virtualearth.net/REST/v1/Imagery/Metadata/Aerial?include=ImageryProviders&output=xml&key="
                + API_KEY);
    }

    protected List<Attribution> parseAttributionText(InputSource xml) throws IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(xml);

            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xpath = xPathFactory.newXPath();
            imageUrlTemplate = xpath.compile("//ImageryMetadata/ImageUrl/text()").evaluate(document);
            imageUrlTemplate = culturePattern.matcher(imageUrlTemplate).replaceAll(Locale.getDefault().toString());
            imageryZoomMax = Integer.parseInt(xpath.compile("//ImageryMetadata/ZoomMax/text()").evaluate(document));

            NodeList subdomainTxt = (NodeList) xpath.compile("//ImageryMetadata/ImageUrlSubdomains/string/text()").evaluate(document, XPathConstants.NODESET);
            subdomains = new String[subdomainTxt.getLength()];
            for(int i = 0; i < subdomainTxt.getLength(); i++) {
                subdomains[i] = subdomainTxt.item(i).getNodeValue();
            }

            XPathExpression attributionXpath = xpath.compile("Attribution/text()");
            XPathExpression coverageAreaXpath = xpath.compile("CoverageArea");
            XPathExpression zoomMinXpath = xpath.compile("ZoomMin/text()");
            XPathExpression zoomMaxXpath = xpath.compile("ZoomMax/text()");
            XPathExpression southLatXpath = xpath.compile("BoundingBox/SouthLatitude/text()");
            XPathExpression westLonXpath = xpath.compile("BoundingBox/WestLongitude/text()");
            XPathExpression northLatXpath = xpath.compile("BoundingBox/NorthLatitude/text()");
            XPathExpression eastLonXpath = xpath.compile("BoundingBox/EastLongitude/text()");

            NodeList imageryProviderNodes = (NodeList) xpath.compile("//ImageryMetadata/ImageryProvider").evaluate(document, XPathConstants.NODESET);
            List<Attribution> attributions = new ArrayList<>(imageryProviderNodes.getLength());
            for (int i = 0; i < imageryProviderNodes.getLength(); i++) {
                Node providerNode = imageryProviderNodes.item(i);

                String attribution = attributionXpath.evaluate(providerNode);

                NodeList coverageAreaNodes = (NodeList) coverageAreaXpath.evaluate(providerNode, XPathConstants.NODESET);
                for(int j = 0; j < coverageAreaNodes.getLength(); j++) {
                    Node areaNode = coverageAreaNodes.item(j);
                    Attribution attr = new Attribution();
                    attr.attribution = attribution;

                    attr.maxZoom = Integer.parseInt(zoomMaxXpath.evaluate(areaNode));
                    attr.minZoom = Integer.parseInt(zoomMinXpath.evaluate(areaNode));

                    Double southLat = Double.parseDouble(southLatXpath.evaluate(areaNode));
                    Double northLat = Double.parseDouble(northLatXpath.evaluate(areaNode));
                    Double westLon = Double.parseDouble(westLonXpath.evaluate(areaNode));
                    Double eastLon = Double.parseDouble(eastLonXpath.evaluate(areaNode));
                    attr.min = new Coordinate(southLat, westLon);
                    attr.max = new Coordinate(northLat, eastLon);

                    attributions.add(attr);
                }
            }

            return attributions;
        } catch (SAXException e) {
            System.err.println("Could not parse Bing aerials attribution metadata.");
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int getMaxZoom() {
        if(imageryZoomMax != null)
            return imageryZoomMax;
        else
            return 22;
    }

    @Override
    public TileUpdate getTileUpdate() {
        return TileUpdate.IfNoneMatch;
    }

    @Override
    public boolean requiresAttribution() {
        return true;
    }

    @Override
    public String getAttributionLinkURL() {
        //return "http://bing.com/maps"
        // FIXME: I've set attributionLinkURL temporarily to ToU URL to comply with bing ToU
        // (the requirement is that we have such a link at the bottom of the window)
        return "http://go.microsoft.com/?linkid=9710837";
    }

    @Override
    public Image getAttributionImage() {
        try {
            return ImageIO.read(JMapViewer.class.getResourceAsStream("images/bing_maps.png"));
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public String getAttributionImageURL() {
        return "http://opengeodata.org/microsoft-imagery-details";
    }

    @Override
    public String getTermsOfUseText() {
        return null;
    }

    @Override
    public String getTermsOfUseURL() {
        return "http://opengeodata.org/microsoft-imagery-details";
    }

    protected Callable<List<Attribution>> getAttributionLoaderCallable() {
        return new Callable<List<Attribution>>() {

            @Override
            public List<Attribution> call() throws Exception {
                int waitTimeSec = 1;
                while (true) {
                    try {
                        InputSource xml = new InputSource(getAttributionUrl().openStream());
                        List<Attribution> r = parseAttributionText(xml);
                        System.out.println("Successfully loaded Bing attribution data.");
                        return r;
                    } catch (IOException ex) {
                        System.err.println("Could not connect to Bing API. Will retry in " + waitTimeSec + " seconds.");
                        Thread.sleep(waitTimeSec * 1000L);
                        waitTimeSec *= 2;
                    }
                }
            }
        };
    }

    protected List<Attribution> getAttribution() {
        if (attributions == null) {
            // see http://www.cs.umd.edu/~pugh/java/memoryModel/DoubleCheckedLocking.html
            synchronized (BingAerialTileSource.class) {
                if (attributions == null) {
                    attributions = Executors.newSingleThreadExecutor().submit(getAttributionLoaderCallable());
                }
            }
        }
        try {
            return attributions.get(1000, TimeUnit.MILLISECONDS);
        } catch (TimeoutException ex) {
            System.err.println("Bing: attribution data is not yet loaded.");
        } catch (ExecutionException ex) {
            throw new RuntimeException(ex.getCause());
        } catch (InterruptedException ign) {
        }
        return null;
    }

    @Override
    public String getAttributionText(int zoom, Coordinate topLeft, Coordinate botRight) {
        try {
            final List<Attribution> data = getAttribution();
            if (data == null)
                return "Error loading Bing attribution data";
            StringBuilder a = new StringBuilder();
            for (Attribution attr : data) {
                if (zoom <= attr.maxZoom && zoom >= attr.minZoom) {
                    if (topLeft.getLon() < attr.max.getLon() && botRight.getLon() > attr.min.getLon()
                            && topLeft.getLat() > attr.min.getLat() && botRight.getLat() < attr.max.getLat()) {
                        a.append(attr.attribution);
                        a.append(" ");
                    }
                }
            }
            return a.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Error loading Bing attribution data";
    }

    static String computeQuadTree(int zoom, int tilex, int tiley) {
        StringBuilder k = new StringBuilder();
        for (int i = zoom; i > 0; i--) {
            char digit = 48;
            int mask = 1 << (i - 1);
            if ((tilex & mask) != 0) {
                digit += 1;
            }
            if ((tiley & mask) != 0) {
                digit += 2;
            }
            k.append(digit);
        }
        return k.toString();
    }
}
