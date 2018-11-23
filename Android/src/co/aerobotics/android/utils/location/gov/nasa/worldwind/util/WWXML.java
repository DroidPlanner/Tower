/*
 * Copyright (C) 2011 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package co.aerobotics.android.utils.location.gov.nasa.worldwind.util;

import co.aerobotics.android.utils.location.gov.nasa.worldwind.avlist.AVKey;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.avlist.*;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.exception.WWRuntimeException;
import co.aerobotics.android.utils.location.gov.nasa.worldwind.geom.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.text.*;
import java.util.*;
import java.util.List;

/**
 * A collection of static methods use for opening, reading and otherwise working with XML files.
 *
 * @author dcollins
 * @version $Id$
 */
// TODO: getUnqualifiedName(StartElement event) not yet implemented on Android because javax.xml.stream is not available
// TODO: in Android SDK.
public class WWXML
{
    /**
     * Create a DOM builder.
     *
     * @param isNamespaceAware true if the builder is to be namespace aware, otherwise false.
     *
     * @return a {@link javax.xml.parsers.DocumentBuilder}.
     *
     * @throws WWRuntimeException if an error occurs.
     */
    public static DocumentBuilder createDocumentBuilder(boolean isNamespaceAware)
    {
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();

        docBuilderFactory.setNamespaceAware(isNamespaceAware);

        //if (Configuration.getJavaVersion() >= 1.6)
        //{
        //    try
        //    {
        //        docBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd",
        //            false);
        //    }
        //    catch (ParserConfigurationException e)
        //    {   // Note it and continue on. Some Java5 parsers don't support the feature.
        //        Logging.logger().finest(Logging.getMessage("XML.NonvalidatingNotSupported"));
        //    }
        //}

        try
        {
            return docBuilderFactory.newDocumentBuilder();
        }
        catch (ParserConfigurationException e)
        {
            String msg = Logging.getMessage("XML.ParserConfigurationException");
            Logging.verbose(msg);
            throw new WWRuntimeException(msg, e);
        }
    }

    /**
     * Open and XML document from a general source. The source type may be one of the following: <ul> <li>a {@link
     * java.net.URL}</li> <li>an {@link java.io.InputStream}</li> <li>a {@link java.io.File}</li> <li>a {@link String}
     * containing a valid URL description or a file or resource name available on the classpath.</li> </ul>
     *
     * @param source the source of the XML document.
     *
     * @return the source document as a {@link org.w3c.dom.Document}, or null if the source object is a string that does
     *         not identify a URL, a file or a resource available on the classpath.
     */
    public static Document openDocument(Object source)
    {
        if (source == null || WWUtil.isEmpty(source))
        {
            throw new IllegalArgumentException(Logging.getMessage("nullValue.SourceIsNull"));
        }

        if (source instanceof URL)
        {
            return openDocumentURL((URL) source);
        }
        else if (source instanceof InputStream)
        {
            return openDocumentStream((InputStream) source);
        }
        else if (source instanceof File)
        {
            return openDocumentFile(((File) source).getPath(), null);
        }
        else if (!(source instanceof String))
        {
            throw new IllegalArgumentException(Logging.getMessage("generic.SourceTypeUnrecognized", source));
        }

        String sourceName = (String) source;

        URL url = WWIO.makeURL(sourceName);
        if (url != null)
            return openDocumentURL(url);

        return openDocumentFile(sourceName, null);
    }

    /**
     * Opens an XML file given the file's location in the file system or on the classpath.
     *
     * @param path the path to the file. Must be an absolute path or a path relative to a location in the classpath.
     * @param c    the class that is used to find a path relative to the classpath.
     *
     * @return a DOM for the file, or null if the specified cannot be found.
     *
     * @throws IllegalArgumentException if the file path is null.
     * @throws WWRuntimeException       if an exception or error occurs while opening and parsing the file. The causing
     *                                  exception is included in this exception's {@link Throwable#initCause(Throwable)}
     *                                  .
     */
    public static Document openDocumentFile(String path, Class c)
    {
        if (path == null)
        {
            throw new IllegalArgumentException(Logging.getMessage("nullValue.PathIsNull"));
        }

        InputStream inputStream = WWIO.openFileOrResourceStream(path, c);

        return inputStream != null ? openDocumentStream(inputStream) : null;
    }

    /**
     * Opens an XML document given a generic {@link java.net.URL} reference.
     *
     * @param url the URL to the document.
     *
     * @return a DOM for the URL.
     *
     * @throws IllegalArgumentException if the url is null.
     * @throws WWRuntimeException       if an exception or error occurs while opening and parsing the url. The causing
     *                                  exception is included in this exception's {@link Throwable#initCause(Throwable)}
     *                                  .
     */
    public static Document openDocumentURL(URL url)
    {
        if (url == null)
        {
            throw new IllegalArgumentException(Logging.getMessage("nullValue.UrlIsNull"));
        }

        try
        {
            InputStream inputStream = url.openStream();
            return openDocumentStream(inputStream);
        }
        catch (IOException e)
        {
            throw new WWRuntimeException(Logging.getMessage("XML.ExceptionParsingXml", url), e);
        }
    }

    /**
     * Opens an XML document given an input stream.
     *
     * @param inputStream the document as an input stream.
     *
     * @return a DOM for the stream content.
     *
     * @throws IllegalArgumentException if the input stream is null.
     * @throws WWRuntimeException       if an exception or error occurs while parsing the stream. The causing exception
     *                                  is included in this exception's {@link Throwable#initCause(Throwable)}
     */
    public static Document openDocumentStream(InputStream inputStream)
    {
        return openDocumentStream(inputStream, true);
    }

    public static Document openDocumentStream(InputStream inputStream, boolean isNamespaceAware)
    {
        if (inputStream == null)
        {
            throw new IllegalArgumentException(Logging.getMessage("nullValue.InputStreamIsNull"));
        }

        try
        {
            return WWXML.createDocumentBuilder(isNamespaceAware).parse(inputStream);
        }
        catch (SAXException e)
        {
            throw new WWRuntimeException(Logging.getMessage("XML.ExceptionParsingXml", inputStream), e);
        }
        catch (IOException e)
        {
            throw new WWRuntimeException(Logging.getMessage("XML.ExceptionParsingXml", inputStream), e);
        }
    }

    /**
     * Writes an XML document to a location in the file system.
     *
     * @param doc      the DOM document to save.
     * @param filePath the path to the file. Must be an absolute path in the file system.
     *
     * @throws IllegalArgumentException if either the document or file path is null.
     * @throws WWRuntimeException       if an exception or error occurs while writing the document. The causing
     *                                  exception is included in this exception's {@link Throwable#initCause(Throwable)}
     */
    public static void saveDocumentToFile(Document doc, String filePath)
    {
        if (doc == null)
        {
            String message = Logging.getMessage("nullValue.DocumentIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (filePath == null)
        {
            String message = Logging.getMessage("nullValue.FilePathIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        try
        {
            java.io.FileOutputStream outputStream = new java.io.FileOutputStream(filePath);

            saveDocumentToStream(doc, outputStream);
        }
        catch (IOException e)
        {
            String message = Logging.getMessage("generic.ExceptionAttemptingToWriteXml", filePath);
            Logging.error(message);
            throw new WWRuntimeException(e);
        }
    }

    /**
     * Writes an XML document to a specified outputstream stream.
     *
     * @param doc          the DOM document to save.
     * @param outputStream the outputstream to save the document contents to.
     *
     * @throws IllegalArgumentException if either the document or input stream is null.
     * @throws WWRuntimeException       if an exception or error occurs while writing the document. The causing
     *                                  exception is included in this exception's {@link Throwable#initCause(Throwable)}
     */
    public static void saveDocumentToStream(Document doc, OutputStream outputStream)
    {
        if (doc == null)
        {
            String message = Logging.getMessage("nullValue.DocumentIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (outputStream == null)
        {
            String message = Logging.getMessage("nullValue.OutputStreamIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        Source source = new DOMSource(doc);
        Result result = new StreamResult(outputStream);

        try
        {
            Transformer transformer = createTransformer();
            transformer.transform(source, result);
        }
        catch (TransformerException e)
        {
            String message = Logging.getMessage("generic.ExceptionAttemptingToWriteXml", outputStream);
            Logging.error(message);
            throw new WWRuntimeException(e);
        }
    }

    /**
     * Shortcut method to create an {@link javax.xml.xpath.XPath}.
     *
     * @return a new XPath.
     */
    public static XPath makeXPath()
    {
        XPathFactory xpFactory = XPathFactory.newInstance();
        return xpFactory.newXPath();
    }

    /**
     * Create a XML transformer.
     *
     * @return a {@link javax.xml.transform.Transformer}
     *
     * @throws WWRuntimeException if an error occurs.
     */
    public static Transformer createTransformer()
    {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();

        try
        {
            return transformerFactory.newTransformer();
        }
        catch (TransformerConfigurationException e)
        {
            String message = Logging.getMessage("XML.TransformerConfigurationException");
            Logging.verbose(message);
            throw new WWRuntimeException(e);
        }
    }

    /**
     * Returns the element node's unqualified name. If the element is qualified with a namespace, this returns the local
     * part of the qualified name. Otherwise, this returns the element's unqualified tag name.
     *
     * @param context the element who's unqualified name is returned.
     *
     * @return the unqualified tag name of the specified element.
     *
     * @throws IllegalArgumentException if the context is null.
     */
    public static String getUnqualifiedName(Element context)
    {
        if (context == null)
        {
            String msg = Logging.getMessage("nullValue.ContextIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        return (context.getLocalName() != null) ? context.getLocalName() : context.getTagName();
    }

    /**
     * Returns the element identified by an XPath expression.
     *
     * @param context the context from which to start the XPath search.
     * @param path    the XPath expression.
     *
     * @return the text of an element matching the XPath expression, or null if no match is found.
     *
     * @throws IllegalArgumentException if the context or XPath expression are null.
     */
    public static String getText(Element context, String path)
    {
        return getText(context, path, null);
    }

    /**
     * Returns the text of the element identified by an XPath expression.
     *
     * @param context the context from which to start the XPath search.
     * @param path    the XPath expression.
     * @param xpath   an {@link XPath} object to use for the search. This allows the caller to re-use XPath objects when
     *                performing multiple searches. May be null.
     *
     * @return the element matching the XPath expression, or null if no element matches.
     *
     * @throws IllegalArgumentException if the context or XPath expression are null.
     */
    public static Element getElement(Element context, String path, XPath xpath)
    {
        if (context == null)
        {
            String msg = Logging.getMessage("nullValue.ContextIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (path == null)
        {
            String msg = Logging.getMessage("nullValue.PathIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (xpath == null)
            xpath = makeXPath();

        try
        {
            Node node = (Node) xpath.evaluate(path, context, XPathConstants.NODE);
            if (node == null)
                return null;

            return node instanceof Element ? (Element) node : null;
        }
        catch (XPathExpressionException e)
        {
            Logging.warning(Logging.getMessage("XML.InvalidXPathExpression", "internal expression"));
            return null;
        }
    }

    /**
     * Returns all elements identified by an XPath expression.
     *
     * @param context the context from which to start the XPath search.
     * @param path    the XPath expression.
     * @param xpath   an {@link XPath} object to use for the search. This allows the caller to re-use XPath objects when
     *                performing multiple searches. May be null.
     *
     * @return an array containing the elements matching the XPath expression.
     *
     * @throws IllegalArgumentException if the context or XPath expression are null.
     */
    public static List<Element> getElements(Element context, String path, XPath xpath)
    {
        if (context == null)
        {
            String msg = Logging.getMessage("nullValue.ContextIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (path == null)
        {
            String msg = Logging.getMessage("nullValue.PathIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (xpath == null)
            xpath = makeXPath();

        try
        {
            NodeList nodes = (NodeList) xpath.evaluate(path, context, XPathConstants.NODESET);
            if (nodes == null || nodes.getLength() == 0)
                return null;

            ArrayList<Element> elements = new ArrayList<Element>();
            for (int i = 0; i < nodes.getLength(); i++)
            {
                Node node = nodes.item(i);
                if (node instanceof Element)
                    elements.add((Element) node);
            }

            return elements;
        }
        catch (XPathExpressionException e)
        {
            Logging.warning(Logging.getMessage("XML.InvalidXPathExpression", "internal expression"), e);
            return null;
        }
    }

    /**
     * Returns the text of the element identified by an XPath expression.
     *
     * @param context the context from which to start the XPath search.
     * @param path    the XPath expression.
     * @param xpath   an {@link XPath} object to use for the search. This allows the caller to re-use XPath objects when
     *                performing multiple searches. May be null.
     *
     * @return the text of an element matching the XPath expression, or null if no match is found.
     *
     * @throws IllegalArgumentException if the context or XPath expression are null.
     */
    public static String getText(Element context, String path, XPath xpath)
    {
        if (context == null)
        {
            String msg = Logging.getMessage("nullValue.ContextIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (path == null)
        {
            String msg = Logging.getMessage("nullValue.PathIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (xpath == null)
            xpath = makeXPath();

        try
        {
            return xpath.evaluate(path, context);
        }
        catch (XPathExpressionException e)
        {
            return null;
        }
    }

    /**
     * Returns the {@link Integer} value of an element identified by an XPath expression.
     *
     * @param context the context from which to start the XPath search.
     * @param path    the XPath expression.
     * @param xpath   an {@link XPath} object to use for the search. This allows the caller to re-use XPath objects when
     *                performing multiple searches. May be null.
     *
     * @return the value of an element matching the XPath expression, or null if no match is found or the match does not
     *         contain a {@link Integer}.
     *
     * @throws IllegalArgumentException if the context or XPath expression are null.
     */
    public static Integer getInteger(Element context, String path, XPath xpath)
    {
        if (context == null)
        {
            String msg = Logging.getMessage("nullValue.ContextIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (path == null)
        {
            String msg = Logging.getMessage("nullValue.PathIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        String s = null;

        try
        {
            s = getText(context, path, xpath);
            if (WWUtil.isEmpty(s))
                return null;

            return Integer.valueOf(s);
        }
        catch (NumberFormatException e)
        {
            Logging.error(Logging.getMessage("generic.ConversionError", s));
            return null;
        }
    }

    /**
     * Returns the {@link Double} value of an element identified by an XPath expression.
     *
     * @param context the context from which to start the XPath search.
     * @param path    the XPath expression.
     * @param xpath   an {@link XPath} object to use for the search. This allows the caller to re-use XPath objects when
     *                performing multiple searches. May be null.
     *
     * @return the value of an element matching the XPath expression, or null if no match is found or the match does not
     *         contain a {@link Double}.
     *
     * @throws IllegalArgumentException if the context or XPath expression are null.
     */
    public static Double getDouble(Element context, String path, XPath xpath)
    {
        if (context == null)
        {
            String msg = Logging.getMessage("nullValue.ContextIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (path == null)
        {
            String msg = Logging.getMessage("nullValue.PathIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        String s = null;

        try
        {
            s = getText(context, path, xpath);
            if (WWUtil.isEmpty(s))
                return null;

            return Double.valueOf(s);
        }
        catch (NumberFormatException e)
        {
            Logging.error(Logging.getMessage("generic.ConversionError", s));
            return null;
        }
    }

    /**
     * Returns the {@link Boolean} value of an element identified by an XPath expression.
     *
     * @param context the context from which to start the XPath search.
     * @param path    the XPath expression.
     * @param xpath   an {@link XPath} object to use for the search. This allows the caller to re-use XPath objects when
     *                performing multiple searches. May be null.
     *
     * @return the value of an element matching the XPath expression, or null if no match is found or the match does not
     *         contain a {@link Boolean}.
     *
     * @throws IllegalArgumentException if the context or XPath expression are null.
     */
    public static Boolean getBoolean(Element context, String path, XPath xpath)
    {
        if (context == null)
        {
            String msg = Logging.getMessage("nullValue.ContextIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (path == null)
        {
            String msg = Logging.getMessage("nullValue.PathIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        String s = null;

        try
        {
            s = getText(context, path, xpath);
            if (WWUtil.isEmpty(s))
                return null;

            return Boolean.valueOf(s);
        }
        catch (NumberFormatException e)
        {
            Logging.error(Logging.getMessage("generic.ConversionError", s));
            return null;
        }
    }

    /**
     * Returns the {@link Long} value of an element identified by an XPath expression.
     *
     * @param context the context from which to start the XPath search.
     * @param path    the XPath expression.
     * @param xpath   an {@link XPath} object to use for the search. This allows the caller to re-use XPath objects when
     *                performing multiple searches. May be null.
     *
     * @return the value of an element matching the XPath expression, or null if no match is found or the match does not
     *         contain a {@link Integer}.
     *
     * @throws IllegalArgumentException if the context or XPath expression are null.
     */
    public static Long getLong(Element context, String path, XPath xpath)
    {
        if (context == null)
        {
            String message = Logging.getMessage("nullValue.ContextIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (path == null)
        {
            String message = Logging.getMessage("nullValue.PathIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        String s = null;

        try
        {
            s = getText(context, path, xpath);
            if (s == null || s.length() == 0)
                return null;

            return Long.valueOf(s);
        }
        catch (NumberFormatException e)
        {
            String message = Logging.getMessage("generic.ConversionError", s);
            Logging.error(message, e);
            return null;
        }
    }

    /**
     * Returns the {@link gov.nasa.worldwind.geom.LatLon} value of an element identified by an XPath expression.
     *
     * @param context the context from which to start the XPath search.
     * @param path    the XPath expression. If null, indicates that the context is the LatLon element itself. If
     *                non-null, the context is searched for a LatLon element using the expression.
     * @param xpath   an {@link XPath} object to use for the search. This allows the caller to re-use XPath objects when
     *                performing multiple searches. May be null.
     *
     * @return the value of an element matching the XPath expression, or null if no match is found or the match does not
     *         contain a {@link gov.nasa.worldwind.geom.LatLon}.
     *
     * @throws IllegalArgumentException if the context is null.
     */
    public static LatLon getLatLon(Element context, String path, XPath xpath)
    {
        if (context == null)
        {
            String msg = Logging.getMessage("nullValue.ContextIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        try
        {
            Element el = path == null ? context : getElement(context, path, xpath);
            if (el == null)
                return null;

            String units = getText(el, "@units", xpath);
            Double lat = getDouble(el, "@latitude", xpath);
            Double lon = getDouble(el, "@longitude", xpath);

            if (lat == null || lon == null)
                return null;

            if (units == null || units.equals("degrees"))
                return LatLon.fromDegrees(lat, lon);

            if (units.equals("radians"))
                return LatLon.fromRadians(lat, lon);

            // Warn that units are not recognized
            Logging.warning(Logging.getMessage("generic.UnitsUnrecognized", units));

            return null;
        }
        catch (NumberFormatException e)
        {
            Logging.warning(Logging.getMessage("generic.ConversionError", path));
            return null;
        }
    }

    ///**
    // * Returns the {@link gov.nasa.worldwind.geom.Color} value of an element identified by an XPath expression.
    // *
    // * @param context the context from which to start the XPath search.
    // * @param path    the XPath expression. If null, indicates that the context is the Color element itself. If
    // *                non-null, the context is searched for a Color element using the expression.
    // * @param xpath   an {@link XPath} object to use for the search. This allows the caller to re-use XPath objects when
    // *                performing multiple searches. May be null.
    // *
    // * @return the value of an element matching the XPath expression, or null if no match is found or the match does not
    // *         contain a {@link gov.nasa.worldwind.geom.Color}.
    // *
    // * @throws IllegalArgumentException if the context is null.
    // */
    //public static Color getColor(Element context, String path, XPath xpath)
    //{
    //    if (context == null)
    //    {
    //        String message = Logging.getMessage("nullValue.ContextIsNull");
    //        Logging.error(message);
    //        throw new IllegalArgumentException(message);
    //    }
    //
    //    try
    //    {
    //        Element el = path == null ? context : getElement(context, path, xpath);
    //        if (el == null)
    //            return null;
    //
    //        Integer r = getInteger(el, "@red", xpath);
    //        Integer g = getInteger(el, "@green", xpath);
    //        Integer b = getInteger(el, "@blue", xpath);
    //        Integer a = getInteger(el, "@alpha", xpath);
    //
    //        return Color.fromRGBAInt(r != null ? r : 0, g != null ? g : 0, b != null ? b : 0, a != null ? a : 255);
    //    }
    //    catch (NumberFormatException e)
    //    {
    //        String message = Logging.getMessage("generic.ConversionError", path);
    //        Logging.error(message, e);
    //        return null;
    //    }
    //}

    /**
     * Returns the {@link gov.nasa.worldwind.geom.Sector} value of an element identified by an XPath expression.
     *
     * @param context the context from which to start the XPath search.
     * @param path    the XPath expression. If null, indicates that the context is the Sector element itself. If
     *                non-null, the context is searched for a Sector element using the expression.
     * @param xpath   an {@link XPath} object to use for the search. This allows the caller to re-use XPath objects when
     *                performing multiple searches. May be null.
     *
     * @return the value of an element matching the XPath expression, or null if no match is found or the match does not
     *         contain a {@link gov.nasa.worldwind.geom.Sector}.
     *
     * @throws IllegalArgumentException if the context is null.
     */
    public static Sector getSector(Element context, String path, XPath xpath)
    {
        if (context == null)
        {
            String msg = Logging.getMessage("nullValue.ContextIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        Element el = path == null ? context : getElement(context, path, xpath);
        if (el == null)
            return null;

        LatLon sw = getLatLon(el, "SouthWest/LatLon", xpath);
        LatLon ne = getLatLon(el, "NorthEast/LatLon", xpath);

        if (sw == null || ne == null)
            return null;

        return new Sector(sw.latitude, ne.latitude, sw.longitude, ne.longitude);
    }

    /**
     * Returns the time in milliseconds of an element identified by an XPath expression.
     *
     * @param context the context from which to start the XPath search.
     * @param path    the XPath expression. If null, indicates that the context is the Time element itself. If non-null,
     *                the context is searched for a Time element using the expression.
     * @param xpath   an {@link XPath} object to use for the search. This allows the caller to re-use XPath objects when
     *                performing multiple searches. May be null.
     *
     * @return the value of an element matching the XPath expression, or null if no match is found or the match does not
     *         contain a {@link gov.nasa.worldwind.geom.LatLon}.
     *
     * @throws IllegalArgumentException if the context is null.
     */
    public static Long getTimeInMillis(Element context, String path, XPath xpath)
    {
        if (context == null)
        {
            String message = Logging.getMessage("nullValue.ContextIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        try
        {
            Element el = path == null ? context : getElement(context, path, xpath);
            if (el == null)
                return null;

            String units = getText(el, "@units", xpath);
            Double value = getDouble(el, "@value", xpath);

            if (value == null)
                return null;

            if (units == null || units.equals("milliseconds"))
                return value.longValue();

            if (units.equals("seconds"))
                return (long) WWMath.convertSecondsToMillis(value);

            if (units.equals("minutes"))
                return (long) WWMath.convertMinutesToMillis(value);

            if (units.equals("hours"))
                return (long) WWMath.convertHoursToMillis(value);

            // Warn that units are not recognized
            String message = Logging.getMessage("XML.UnitsUnrecognized", units);
            Logging.warning(message);

            return null;
        }
        catch (NumberFormatException e)
        {
            String message = Logging.getMessage("generic.ConversionError", path);
            Logging.error(message, e);
            return null;
        }
    }

    /**
     * Returns the date and time in milliseconds of an element identified by an XPath expression.
     *
     * @param context the context from which to start the XPath search.
     * @param path    the XPath expression. If null, indicates that the context is the element itself. If non-null, the
     *                context is searched for an element matching the expression.
     * @param pattern the format pattern of the date. See {@link java.text.DateFormat} for the pattern symbols. The
     *                element content must either match the pattern or be directly convertible to a long.
     * @param xpath   an {@link XPath} object to use for the search. This allows the caller to re-use XPath objects when
     *                performing multiple searches. May be null.
     *
     * @return the value of an element matching the XPath expression, or null if no match is found.
     *
     * @throws IllegalArgumentException if the context or pattern is null.
     */
    public static Long getDateTimeInMillis(Element context, String path, String pattern, XPath xpath)
    {
        if (context == null)
        {
            String message = Logging.getMessage("nullValue.ContextIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (WWUtil.isEmpty(pattern))
        {
            String message = Logging.getMessage("nullValue.PatternIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        try
        {
            Element el = path == null ? context : getElement(context, path, xpath);
            if (el == null)
                return null;

            String s = getText(context, path, xpath);
            if (s == null || s.length() == 0)
                return null;

            // See if the value is already a long
            Long longValue = WWUtil.makeLong(s);
            if (longValue != null)
                return longValue;

            return new SimpleDateFormat(pattern).parse(s).getTime();
        }
        catch (ParseException e)
        {
            String message = Logging.getMessage("generic.ConversionError", path);
            Logging.error(message, e);
            return null;
        }
    }

    /**
     * Returns the text of all elements identified by an XPath expression.
     *
     * @param context the context from which to start the XPath search.
     * @param path    the XPath expression.
     * @param xpath   an {@link XPath} object to use for the search. This allows the caller to re-use XPath objects when
     *                performing multiple searches. May be null.
     *
     * @return an array containing the text of each element matching the XPath expression.
     *
     * @throws IllegalArgumentException if the context or XPath expression are null.
     */
    public static String[] getTextArray(Element context, String path, XPath xpath)
    {
        if (context == null)
        {
            String message = Logging.getMessage("nullValue.ContextIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (path == null)
        {
            String message = Logging.getMessage("nullValue.PathIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (xpath == null)
            xpath = makeXPath();

        try
        {
            NodeList nodes = (NodeList) xpath.evaluate(path, context,
                XPathConstants.NODESET);
            if (nodes == null || nodes.getLength() == 0)
                return null;

            String[] strings = new String[nodes.getLength()];
            for (int i = 0; i < nodes.getLength(); i++)
            {
                strings[i] = nodes.item(i).getTextContent();
            }
            return strings;
        }
        catch (XPathExpressionException e)
        {
            return null;
        }
    }

    /**
     * Returns the text of all unique elements identified by an XPath expression.
     *
     * @param context the context from which to start the XPath search.
     * @param path    the XPath expression.
     * @param xpath   an {@link XPath} object to use for the search. This allows the caller to re-use XPath objects when
     *                performing multiple searches. May be null.
     *
     * @return an array containing the text of each element matching the XPath expression and containing unique text. If
     *         multiple elements contain the same text only the first one found is returned. Returns null if no matching
     *         element is found.
     *
     * @throws IllegalArgumentException if the context or XPath expression are null.
     */
    public static String[] getUniqueText(Element context, String path, XPath xpath)
    {
        if (context == null)
        {
            String message = Logging.getMessage("nullValue.ContextIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (path == null)
        {
            String message = Logging.getMessage("nullValue.PathIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (xpath == null)
            xpath = makeXPath();

        String[] strings = getTextArray(context, path, xpath);
        if (strings == null)
            return null;

        ArrayList<String> sarl = new ArrayList<String>();
        for (String s : strings)
        {
            if (!sarl.contains(s))
                sarl.add(s);
        }

        return sarl.toArray(new String[1]);
    }

    /**
     * Returns the {@link LevelSet.SectorResolution} value of an element identified by an XPath
     * expression.
     *
     * @param context the context from which to start the XPath search.
     * @param path    the XPath expression. If null, indicates that the context is the SectorResolution element itself.
     *                If non-null, the context is searched for a SectorResolution element using the expression.
     * @param xpath   an {@link XPath} object to use for the search. This allows the caller to re-use XPath objects when
     *                performing multiple searches. May be null.
     *
     * @return the value of an element matching the XPath expression, or null if no match is found or the match does not
     *         contain a {@link LevelSet.SectorResolution}.
     *
     * @throws IllegalArgumentException if the context is null.
     */
    public static LevelSet.SectorResolution getSectorResolutionLimit(Element context, String path, XPath xpath)
    {
        if (context == null)
        {
            String message = Logging.getMessage("nullValue.ContextIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        Element el = path == null ? context : getElement(context, path, xpath);
        if (el == null)
            return null;

        Integer maxLevelNum = getInteger(el, "@maxLevelNum", xpath);
        Sector sector = getSector(el, "Sector", xpath);

        if (maxLevelNum == null || sector == null)
            return null;

        return new LevelSet.SectorResolution(sector, maxLevelNum);
    }

    /**
     * Sets the element's attribute with the specified name to the specified String value. If the element already has an
     * attribute with this name, its value is replaced with the specified value.
     *
     * @param context the element on which to set the attribute.
     * @param name    the attribute's name.
     * @param value   the attribute's value.
     *
     * @throws IllegalArgumentException if the context is null, if the name is null, or if the name is empty.
     */
    public static void setTextAttribute(Element context, String name, String value)
    {
        if (context == null)
        {
            String message = Logging.getMessage("nullValue.ContextIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (WWUtil.isEmpty(name))
        {
            String message = Logging.getMessage("nullValue.NameIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        // Create a namespace-aware attribute node, which supports DOM Level 1 and Level 2 features. This ensures the
        // constructed DOM node is consistent with attribute nodes created by parsing an XML document in namespace-aware
        // mode.
        context.setAttributeNS(null, name, value);
    }

    /**
     * Sets the specified document's root element to a new element node with the specified name. If the document already
     * has a root element, this replaces the existing root node with the new element node.
     *
     * @param doc  the document which receives the new root element.
     * @param name the name of the document's new root element node.
     *
     * @return the document's new root element node.
     *
     * @throws IllegalArgumentException if the document is null, if the name is null, or if the name is empty.
     */
    public static Element setDocumentElement(Document doc, String name)
    {
        if (doc == null)
        {
            String message = Logging.getMessage("nullValue.DocumentIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (WWUtil.isEmpty(name))
        {
            String message = Logging.getMessage("nullValue.NameIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        // Create a namespace-aware Element node, which supports DOM Level 1 and Level 2 features. This ensures the
        // constructed DOM node is consistent with Element nodes created by parsing an XML document in namespace-aware
        // mode.
        Element el = doc.createElementNS(null, name);

        if (doc.getDocumentElement() != null)
        {
            doc.replaceChild(el, doc.getDocumentElement());
        }
        else
        {
            doc.appendChild(el);
        }

        return el;
    }

    /**
     * Sets the element's attribute with the specified name to the specified integer value, converted to a String. If
     * the element already has an attribute with this name, its value is repaced with the specified value.
     *
     * @param context the element on which to set the attribute.
     * @param name    the attribute's name.
     * @param value   the attribute's value.
     *
     * @throws IllegalArgumentException if the context is null, if the name is null, or if the name is empty.
     */
    public static void setIntegerAttribute(Element context, String name, int value)
    {
        if (context == null)
        {
            String message = Logging.getMessage("nullValue.ContextIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (name == null)
        {
            String message = Logging.getMessage("nullValue.NameIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        setTextAttribute(context, name, Integer.toString(value));
    }

    /**
     * Sets the element's attribute with the specified name to the specified long integer value, converted to a String.
     * If the element already has an attribute with this name, its value is repaced with the specified value.
     *
     * @param context the element on which to set the attribute.
     * @param name    the attribute's name.
     * @param value   the attribute's value.
     *
     * @throws IllegalArgumentException if the context is null, if the name is null, or if the name is empty.
     */
    public static void setLongAttribute(Element context, String name, long value)
    {
        if (context == null)
        {
            String message = Logging.getMessage("nullValue.ContextIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (name == null)
        {
            String message = Logging.getMessage("nullValue.NameIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        setTextAttribute(context, name, Long.toString(value));
    }

    /**
     * Sets the element's attribute with the specified name to the specified double value, converted to a String. If the
     * element already has an attribute with this name, its value is repaced with the specified value.
     *
     * @param context the Element on which to set the attribute.
     * @param name    the attribute's name.
     * @param value   the attribute's value.
     *
     * @throws IllegalArgumentException if the context is null, if the name is null, or if the name is empty.
     */
    public static void setDoubleAttribute(Element context, String name, double value)
    {
        if (context == null)
        {
            String message = Logging.getMessage("nullValue.ContextIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (name == null)
        {
            String message = Logging.getMessage("nullValue.NameIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        setTextAttribute(context, name, Double.toString(value));
    }

    /**
     * Append a heirarcy of new elements with a path to a context element, ending with a text element with a specified
     * value. Elements are added to the context as in {@link #appendElementPath(org.w3c.dom.Element, String)}, except
     * that a terminating text element is appended to the last element.
     *
     * @param context the context on which to append new elements.
     * @param path    the element path to append.
     * @param value   the text element value.
     *
     * @return the new element appended to the context, or the context if the element path is null or empty.
     *
     * @throws IllegalArgumentException if the context is null.
     */
    public static Element appendBoolean(Element context, String path, boolean value)
    {
        if (context == null)
        {
            String message = Logging.getMessage("nullValue.ContextIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        return appendText(context, path, Boolean.toString(value));
    }

    /**
     * Append a heirarcy of new elements with a path to a context element, ending with a text element with a specified
     * value. Elements are added to the context as in {@link #appendElementPath(org.w3c.dom.Element, String)}, except
     * that a terminating text element is appended to the last element.
     *
     * @param context the context on which to append new elements.
     * @param path    the element path to append.
     * @param value   the text element value.
     *
     * @return the new element appended to the context, or the context if the element path is null or empty.
     *
     * @throws IllegalArgumentException if the context is null.
     */
    public static Element appendInteger(Element context, String path, int value)
    {
        if (context == null)
        {
            String message = Logging.getMessage("nullValue.ContextIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        return appendText(context, path, Integer.toString(value));
    }

    /**
     * Append a heirarcy of new elements with a path to a context element, ending with a text element with a specified
     * value. Elements are added to the context as in {@link #appendElementPath(org.w3c.dom.Element, String)}, except
     * that a terminating text element is appended to the last element.
     *
     * @param context the context on which to append new elements.
     * @param path    the element path to append.
     * @param value   the text element value.
     *
     * @return the new element appended to the context, or the context if the element path is null or empty.
     *
     * @throws IllegalArgumentException if the context is null.
     */
    public static Element appendLong(Element context, String path, long value)
    {
        if (context == null)
        {
            String message = Logging.getMessage("nullValue.ContextIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        return appendText(context, path, Long.toString(value));
    }

    /**
     * Append a heirarcy of new elements with a path to a context element, ending with a text element with a specified
     * value. Elements are added to the context as in {@link #appendElementPath(org.w3c.dom.Element, String)}, except
     * that a terminating text element is appended to the last element.
     *
     * @param context the context on which to append new elements.
     * @param path    the element path to append.
     * @param value   the text element value.
     *
     * @return the new element appended to the context, or the context if the element path is null or empty.
     *
     * @throws IllegalArgumentException if the context is null.
     */
    public static Element appendDouble(Element context, String path, double value)
    {
        if (context == null)
        {
            String message = Logging.getMessage("nullValue.ContextIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        return appendText(context, path, Double.toString(value));
    }

    /**
     * Append a heirarcy of new elements with a path to a context element. The path is treated as a list of descendant
     * elements which created and appended to the context. Each new element in the path is delimited by the "/"
     * character. For example, the path "A/B/C" appends A to context, B to A, and C to B. If the path is null or empty,
     * this returns the context element, and does not make any modifications to the context. If any element name in the
     * path is empty, that element is skipped.
     *
     * @param context the context on which to append new elements.
     * @param path    the element path to append.
     *
     * @return the new element appended to the context, or the context if the element path is null or empty.
     *
     * @throws IllegalArgumentException if the context is null.
     */
    public static Element appendElementPath(Element context, String path)
    {
        if (context == null)
        {
            String message = Logging.getMessage("nullValue.ContextIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (WWUtil.isEmpty(path))
        {
            return context;
        }

        String[] names = path.split("/");
        if (names == null || names.length == 0)
        {
            return context;
        }

        Document doc = context.getOwnerDocument();
        Element cur = context;

        for (String s : names)
        {
            if (s != null && s.length() > 0)
            {
                // Create a namespace-aware element node, which supports DOM Level 1 and Level 2 features. This ensures
                // the constructed DOM node is consistent with element nodes created by parsing an XML document in
                // namespace-aware mode.
                Element el = doc.createElementNS(null, s);
                cur.appendChild(el);
                cur = el;
            }
        }

        return cur;
    }

    /**
     * Append a heirarcy of new elements with a path to a context element, ending with a text element with a specified
     * value. Elements are added to the context as in {@link #appendElementPath(org.w3c.dom.Element, String)}, except
     * that a terminating text element is appended to the last element.
     *
     * @param context the context on which to append new elements.
     * @param path    the element path to append.
     * @param string  the text element value.
     *
     * @return the new element appended to the context, or the context if the element path is null or empty.
     *
     * @throws IllegalArgumentException if the context is null.
     */
    public static Element appendText(Element context, String path, String string)
    {
        if (context == null)
        {
            String message = Logging.getMessage("nullValue.ContextIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (string == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        Element el = appendElementPath(context, path);

        Document doc = context.getOwnerDocument();
        Text text = doc.createTextNode(string);
        el.appendChild(text);

        return el;
    }

    /**
     * Append a heirarcy of new elements with a path to a context element, ending with an element formatted as a time in
     * milliseconds. Elements are added to the context as in {@link #appendElementPath(org.w3c.dom.Element, String)},
     * except that a terminating text element is appended to the last element.
     *
     * @param context      the context on which to append new elements.
     * @param path         the element path to append.
     * @param timeInMillis the time value in milliseconds to create.
     *
     * @return the new element appended to the context, or the context if the element path is null or empty.
     *
     * @throws IllegalArgumentException if the context is null.
     */
    public static Element appendTimeInMillis(Element context, String path, long timeInMillis)
    {
        if (context == null)
        {
            String message = Logging.getMessage("nullValue.ContextIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        Element el = appendElementPath(context, path);
        setTextAttribute(el, "units", "milliseconds");
        setLongAttribute(el, "value", timeInMillis);

        return el;
    }

    /**
     * For each non-null string in a specified array, appends a heirarcy of new elements with a path to a context
     * element, ending with a text element with a specified value. Elements are added to the context as in {@link
     * #appendElementPath(org.w3c.dom.Element, String)}, except that a terminating text element is appended to the last
     * element.
     *
     * @param context the context on which to append new elements.
     * @param path    the element path to append.
     * @param strings the text element values.
     *
     * @return array of new elements appended to the context.
     *
     * @throws IllegalArgumentException if the context is null.
     */
    public static Element[] appendTextArray(Element context, String path, String[] strings)
    {
        if (context == null)
        {
            String message = Logging.getMessage("nullValue.ContextIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (strings == null)
        {
            String message = Logging.getMessage("nullValue.ArrayIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        Element[] els = new Element[strings.length];

        for (int i = 0; i < strings.length; i++)
        {
            String s = strings[i];

            if (s != null && s.length() > 0)
            {
                els[i] = appendText(context, path, s);
            }
        }

        return els;
    }

    /**
     * Append a heirarcy of new elements with a path to a context element, ending with an element formatted as a LatLon.
     * Elements are added to the context as in {@link #appendElementPath(org.w3c.dom.Element, String)}, except that a
     * terminating text element is appended to the last element.
     *
     * @param context the context on which to append new elements.
     * @param path    the element path to append.
     * @param ll      the LatLon value to create.
     *
     * @return the new element appended to the context, or the context if the element path is null or empty.
     *
     * @throws IllegalArgumentException if the context is null.
     */
    public static Element appendLatLon(Element context, String path, LatLon ll)
    {
        if (context == null)
        {
            String message = Logging.getMessage("nullValue.ContextIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (ll == null)
        {
            String message = Logging.getMessage("nullValue.LatLonIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        Element el = appendElementPath(context, path);
        setTextAttribute(el, "units", "degrees");
        setDoubleAttribute(el, "latitude", ll.latitude.degrees);
        setDoubleAttribute(el, "longitude", ll.longitude.degrees);

        return el;
    }

    /**
     * Append a heirarcy of new elements with a path to a context element, ending with an element formatted as a Sector.
     * Elements are added to the context as in {@link #appendElementPath(org.w3c.dom.Element, String)}, except that a
     * terminating text element is appended to the last element.
     *
     * @param context the context on which to append new elements.
     * @param path    the element path to append.
     * @param sector  the Sector value to create.
     *
     * @return the new element appended to the context, or the context if the element path is null or empty.
     *
     * @throws IllegalArgumentException if the context is null.
     */
    public static Element appendSector(Element context, String path, Sector sector)
    {
        if (context == null)
        {
            String message = Logging.getMessage("nullValue.ContextIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (sector == null)
        {
            String message = Logging.getMessage("nullValue.SectorIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        Element el = appendElementPath(context, path);
        appendLatLon(el, "SouthWest/LatLon", new LatLon(sector.minLatitude, sector.minLongitude));
        appendLatLon(el, "NorthEast/LatLon", new LatLon(sector.maxLatitude, sector.maxLongitude));

        return el;
    }

    /**
     * Append a heirarcy of new elements with a path to a context element, ending with an element formatted as a
     * LevelSet.SectorResolutionLimit. Elements are added to the context as in {@link
     * #appendElementPath(org.w3c.dom.Element, String)}, except that a terminating text element is appended to the last
     * element.
     *
     * @param context          the context on which to append new elements.
     * @param path             the element path to append.
     * @param sectorResolution the LevelSet.SectorResolutionLimit value to create.
     *
     * @return the new element appended to the context, or the context if the element path is null or empty.
     *
     * @throws IllegalArgumentException if the context is null.
     */
    public static Element appendSectorResolutionLimit(Element context, String path,
        LevelSet.SectorResolution sectorResolution)
    {
        if (context == null)
        {
            String message = Logging.getMessage("nullValue.ContextIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (sectorResolution == null)
        {
            String message = Logging.getMessage("nullValue.LevelSet.SectorResolutionIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        Element el = appendElementPath(context, path);
        setIntegerAttribute(el, "maxLevelNum", sectorResolution.getLevelNumber());
        appendSector(el, "Sector", sectorResolution.getSector());

        return el;
    }

    /**
     * Checks a parameter list for a specified key and if present attempts to append new elements represeting the
     * parameter to a specified context.
     *
     * @param context  the context on which to append new elements.
     * @param params   the parameter list.
     * @param paramKey the key used to identify the paramater in the parameter list.
     * @param path     the element path to append.
     *
     * @throws IllegalArgumentException if either the parameter list  parameter key, or context are null.
     */
    public static void checkAndAppendTextElement(AVList params, String paramKey, Element context, String path)
    {
        if (params == null)
        {
            String message = Logging.getMessage("nullValue.ParametersIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (paramKey == null)
        {
            String message = Logging.getMessage("nullValue.ParameterKeyIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (context == null)
        {
            String message = Logging.getMessage("nullValue.ElementIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        String s = params.getStringValue(paramKey);
        if (s != null && s.length() > 0)
        {
            appendText(context, path, s.trim());
        }
    }

    /**
     * Checks a parameter list for a specified key and if present attempts to append new elements represeting the
     * parameter to a specified context.
     *
     * @param context  the context on which to append new elements.
     * @param params   the parameter list.
     * @param paramKey the key used to identify the paramater in the parameter list.
     * @param path     the element path to append.
     *
     * @throws IllegalArgumentException if either the parameter list  parameter key, or context are null.
     */
    public static void checkAndAppendTimeElement(AVList params, String paramKey, Element context,
        String path)
    {
        if (params == null)
        {
            String message = Logging.getMessage("nullValue.ParametersIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (paramKey == null)
        {
            String message = Logging.getMessage("nullValue.ParameterKeyIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (context == null)
        {
            String message = Logging.getMessage("nullValue.ElementIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        Object o = params.getValue(paramKey);
        if (o != null && o instanceof Number)
        {
            Number num = (Number) o;
            appendTimeInMillis(context, path, num.longValue());
        }
    }

    /**
     * Checks a parameter list for a specified key and if present attempts to append new elements represeting the
     * parameter to a specified context.
     *
     * @param context  the context on which to append new elements.
     * @param params   the parameter list.
     * @param paramKey the key used to identify the paramater in the parameter list.
     * @param path     the element path to append.
     *
     * @throws IllegalArgumentException if either the parameter list  parameter key, or context are null.
     */
    public static void checkAndAppendBooleanElement(AVList params, String paramKey, Element context, String path)
    {
        if (params == null)
        {
            String message = Logging.getMessage("nullValue.ParametersIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (paramKey == null)
        {
            String message = Logging.getMessage("nullValue.ParameterKeyIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (context == null)
        {
            String message = Logging.getMessage("nullValue.ElementIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        Object o = params.getValue(paramKey);
        if (o != null && o instanceof Boolean)
        {
            appendBoolean(context, path, (Boolean) o);
        }
    }

    /**
     * Checks a parameter list for a specified key and if present attempts to append new elements represeting the
     * parameter to a specified context.
     *
     * @param context  the context on which to append new elements.
     * @param params   the parameter list.
     * @param paramKey the key used to identify the paramater in the parameter list.
     * @param path     the element path to append.
     *
     * @throws IllegalArgumentException if either the parameter list  parameter key, or context are null.
     */
    public static void checkAndAppendDoubleElement(AVList params, String paramKey, Element context, String path)
    {
        if (params == null)
        {
            String message = Logging.getMessage("nullValue.ParametersIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (paramKey == null)
        {
            String message = Logging.getMessage("nullValue.ParameterKeyIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (context == null)
        {
            String message = Logging.getMessage("nullValue.ElementIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        Double d = AVListImpl.getDoubleValue(params, paramKey);
        if (d != null)
        {
            appendDouble(context, path, d);
        }
    }

    /**
     * Checks a parameter list for a specified key and if present attempts to append new elements represeting the
     * parameter to a specified context.
     *
     * @param context  the context on which to append new elements.
     * @param params   the parameter list.
     * @param paramKey the key used to identify the paramater in the parameter list.
     * @param path     the element path to append.
     *
     * @throws IllegalArgumentException if either the parameter list  parameter key, or context are null.
     */
    public static void checkAndAppendLongElement(AVList params, String paramKey, Element context, String path)
    {
        if (params == null)
        {
            String message = Logging.getMessage("nullValue.ParametersIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (paramKey == null)
        {
            String message = Logging.getMessage("nullValue.ParameterKeyIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (context == null)
        {
            String message = Logging.getMessage("nullValue.ElementIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        Long l = AVListImpl.getLongValue(params, paramKey);
        if (l != null)
        {
            appendLong(context, path, l);
        }
    }

    /**
     * Checks a parameter list for a specified key and if not present attempts to find a value for the key from an
     * element matched by an XPath expression. If found, the key and value are added to the parameter list.
     *
     * @param context   the context from which to start the XPath search.
     * @param params    the parameter list.
     * @param paramKey  the key used to identify the paramater in the parameter list.
     * @param paramName the Xpath expression identifying the parameter value within the specified context.
     * @param xpath     an {@link XPath} object to use for the search. This allows the caller to re-use XPath objects
     *                  when performing multiple searches. May be null.
     *
     * @throws IllegalArgumentException if either the context, parameter list, parameter key or parameter name are
     *                                  null.
     */
    public static void checkAndSetBooleanParam(Element context, AVList params, String paramKey, String paramName,
        XPath xpath)
    {
        if (context == null)
        {
            String message = Logging.getMessage("nullValue.ElementIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (params == null)
        {
            String message = Logging.getMessage("nullValue.ParametersIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (paramKey == null)
        {
            String message = Logging.getMessage("nullValue.ParameterKeyIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (paramName == null)
        {
            String message = Logging.getMessage("nullValue.ParameterNameIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        Object o = params.getValue(paramKey);
        if (o == null)
        {
            Boolean d = getBoolean(context, paramName, xpath);
            if (d != null)
                params.setValue(paramKey, d);
        }
    }

    /**
     * Checks a parameter list for a specified key and if not present attempts to find a value for the key from an
     * element matched by an XPath expression. If found, the key and value are added to the parameter list.
     *
     * @param context   the context from which to start the XPath search.
     * @param params    the parameter list.
     * @param paramKey  the key used to identify the paramater in the parameter list.
     * @param paramName the Xpath expression identifying the parameter value within the specified context.
     * @param xpath     an {@link XPath} object to use for the search. This allows the caller to re-use XPath objects
     *                  when performing multiple searches. May be null.
     *
     * @throws IllegalArgumentException if either the context, parameter list, parameter key or parameter name are
     *                                  null.
     */
    public static void checkAndSetStringParam(Element context, AVList params, String paramKey, String paramName,
        XPath xpath)
    {
        if (context == null)
        {
            String message = Logging.getMessage("nullValue.ElementIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (params == null)
        {
            String message = Logging.getMessage("nullValue.ParametersIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (paramKey == null)
        {
            String message = Logging.getMessage("nullValue.ParameterKeyIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (paramName == null)
        {
            String message = Logging.getMessage("nullValue.ParameterNameIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        String s = params.getStringValue(paramKey);
        if (s == null)
        {
            s = getText(context, paramName, xpath);
            if (s != null && s.length() > 0)
                params.setValue(paramKey, s.trim());
        }
    }

    /**
     * Checks a parameter list for a specified key and if not present attempts to find a value for the key from an
     * element matched by an XPath expression. If found, the key and value are added to the parameter list.
     *
     * @param context   the context from which to start the XPath search.
     * @param params    the parameter list.
     * @param paramKey  the key used to identify the paramater in the parameter list.
     * @param paramName the Xpath expression identifying the parameter value within the specified context.
     * @param xpath     an {@link XPath} object to use for the search. This allows the caller to re-use XPath objects
     *                  when performing multiple searches. May be null.
     *
     * @throws IllegalArgumentException if either the context, parameter list, parameter key or parameter name are
     *                                  null.
     */
    public static void checkAndSetIntegerParam(Element context, AVList params, String paramKey, String paramName,
        XPath xpath)
    {
        if (context == null)
        {
            String message = Logging.getMessage("nullValue.ElementIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (params == null)
        {
            String message = Logging.getMessage("nullValue.ParametersIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (paramKey == null)
        {
            String message = Logging.getMessage("nullValue.ParameterKeyIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (paramName == null)
        {
            String message = Logging.getMessage("nullValue.ParameterNameIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        Object o = params.getValue(paramKey);
        if (o == null)
        {
            Integer d = getInteger(context, paramName, xpath);
            if (d != null)
                params.setValue(paramKey, d);
        }
    }

    /**
     * Checks a parameter list for a specified key and if not present attempts to find a value for the key from an
     * element matched by an XPath expression. If found, the key and value are added to the parameter list.
     *
     * @param context   the context from which to start the XPath search.
     * @param params    the parameter list.
     * @param paramKey  the key used to identify the paramater in the parameter list.
     * @param paramName the Xpath expression identifying the parameter value within the specified context.
     * @param xpath     an {@link XPath} object to use for the search. This allows the caller to re-use XPath objects
     *                  when performing multiple searches. May be null.
     *
     * @throws IllegalArgumentException if either the context, parameter list, parameter key or parameter name are
     *                                  null.
     */
    public static void checkAndSetLongParam(Element context, AVList params, String paramKey, String paramName,
        XPath xpath)
    {
        if (context == null)
        {
            String message = Logging.getMessage("nullValue.ElementIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (params == null)
        {
            String message = Logging.getMessage("nullValue.ParametersIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (paramKey == null)
        {
            String message = Logging.getMessage("nullValue.ParameterKeyIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (paramName == null)
        {
            String message = Logging.getMessage("nullValue.ParameterNameIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        Object o = params.getValue(paramKey);
        if (o == null)
        {
            Long d = getLong(context, paramName, xpath);
            if (d != null)
                params.setValue(paramKey, d);
        }
    }

    /**
     * Checks a parameter list for a specified key and if not present attempts to find a value for the key from an
     * element matched by an XPath expression. If found, the key and value are added to the parameter list.
     *
     * @param context   the context from which to start the XPath search.
     * @param params    the parameter list.
     * @param paramKey  the key used to identify the paramater in the parameter list.
     * @param paramName the Xpath expression identifying the parameter value within the specified context.
     * @param pattern   the format pattern of the date. See {@link java.text.DateFormat} for the pattern symbols. The
     *                  element content must either match the pattern or be directly convertible to a long.
     * @param xpath     an {@link XPath} object to use for the search. This allows the caller to re-use XPath objects
     *                  when performing multiple searches. May be null.
     *
     * @throws IllegalArgumentException if either the context, parameter list, parameter key, pattern or parameter name
     *                                  are null.
     */
    public static void checkAndSetDateTimeParam(Element context, AVList params, String paramKey, String paramName,
        String pattern, XPath xpath)
    {
        if (context == null)
        {
            String message = Logging.getMessage("nullValue.ElementIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (params == null)
        {
            String message = Logging.getMessage("nullValue.ParametersIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (paramKey == null)
        {
            String message = Logging.getMessage("nullValue.ParameterKeyIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (paramName == null)
        {
            String message = Logging.getMessage("nullValue.ParameterNameIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (WWUtil.isEmpty(pattern))
        {
            String message = Logging.getMessage("nullValue.PatternIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        Object o = params.getValue(paramKey);
        if (o == null)
        {
            Long d = getDateTimeInMillis(context, paramName, pattern, xpath);
            if (d != null)
                params.setValue(paramKey, d);
        }
    }

    //public static void checkAndSetColorArrayParam(Element context, AVList params, String paramKey, String paramName,
    //    XPath xpath)
    //{
    //    if (context == null)
    //    {
    //        String message = Logging.getMessage("nullValue.ElementIsNull");
    //        Logging.error(message);
    //        throw new IllegalArgumentException(message);
    //    }
    //
    //    if (params == null)
    //    {
    //        String message = Logging.getMessage("nullValue.ParametersIsNull");
    //        Logging.error(message);
    //        throw new IllegalArgumentException(message);
    //    }
    //
    //    if (paramKey == null)
    //    {
    //        String message = Logging.getMessage("nullValue.ParameterKeyIsNull");
    //        Logging.error(message);
    //        throw new IllegalArgumentException(message);
    //    }
    //
    //    if (paramName == null)
    //    {
    //        String message = Logging.getMessage("nullValue.ParameterNameIsNull");
    //        Logging.error(message);
    //        throw new IllegalArgumentException(message);
    //    }
    //
    //    Object o = params.getValue(paramKey);
    //    if (o == null)
    //    {
    //        List<Element> els = getElements(context, paramName, xpath);
    //        if (els == null || els.size() == 0)
    //            return;
    //
    //        int size = els.size();
    //        int[] colors = new int[size];
    //
    //        for (int i = 0; i < size; i++)
    //        {
    //            Color color = getColor(context, paramName, xpath);
    //            if (color != null)
    //                colors[i] = color.getRGB();
    //        }
    //
    //        params.setValue(paramKey, colors);
    //    }
    //}

    /**
     * Checks a parameter list for a specified key and if not present attempts to find a value for the key from an
     * element matched by an XPath expression. If found, the key and value are added to the parameter list.
     *
     * @param context   the context from which to start the XPath search.
     * @param params    the parameter list.
     * @param paramKey  the key used to identify the paramater in the parameter list.
     * @param paramName the Xpath expression identifying the parameter value within the specified context.
     * @param xpath     an {@link XPath} object to use for the search. This allows the caller to re-use XPath objects
     *                  when performing multiple searches. May be null.
     *
     * @throws IllegalArgumentException if either the context, parameter list, parameter key or parameter name are
     *                                  null.
     */
    public static void checkAndSetSectorParam(Element context, AVList params, String paramKey, String paramName,
        XPath xpath)
    {
        if (context == null)
        {
            String message = Logging.getMessage("nullValue.ElementIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (params == null)
        {
            String message = Logging.getMessage("nullValue.ParametersIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (paramKey == null)
        {
            String message = Logging.getMessage("nullValue.ParameterKeyIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (paramName == null)
        {
            String message = Logging.getMessage("nullValue.ParameterNameIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        Object o = params.getValue(paramKey);
        if (o == null)
        {
            Sector sector = getSector(context, paramName, xpath);
            if (sector != null)
                params.setValue(paramKey, sector);
        }
    }

    /**
     * Checks a parameter list for a specified key and if not present attempts to find a value for the key from an
     * element matched by an XPath expression. If found, the key and value are added to the parameter list.
     *
     * @param context   the context from which to start the XPath search.
     * @param params    the parameter list.
     * @param paramKey  the key used to identify the paramater in the parameter list.
     * @param paramName the Xpath expression identifying the parameter value within the specified context.
     * @param xpath     an {@link XPath} object to use for the search. This allows the caller to re-use XPath objects
     *                  when performing multiple searches. May be null.
     *
     * @throws IllegalArgumentException if either the context, parameter list, parameter key or parameter name are
     *                                  null.
     */
    public static void checkAndSetSectorResolutionParam(Element context, AVList params, String paramKey,
        String paramName, XPath xpath)
    {
        if (context == null)
        {
            String message = Logging.getMessage("nullValue.ElementIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (params == null)
        {
            String message = Logging.getMessage("nullValue.ParametersIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (paramKey == null)
        {
            String message = Logging.getMessage("nullValue.ParameterKeyIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (paramName == null)
        {
            String message = Logging.getMessage("nullValue.ParameterNameIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        Object o = params.getValue(paramKey);
        if (o == null)
        {
            List<Element> els = getElements(context, paramName, xpath);
            if (els == null || els.size() == 0)
                return;

            LevelSet.SectorResolution[] srs = new LevelSet.SectorResolution[els.size()];

            for (int i = 0; i < els.size(); i++)
            {
                LevelSet.SectorResolution sr = getSectorResolutionLimit(els.get(i), null, xpath);
                if (sr != null)
                    srs[i] = sr;
            }

            params.setValue(paramKey, srs);
        }
    }

    /**
     * Checks a parameter list for a specified key and if not present attempts to find a value for the key from an
     * element matched by an XPath expression. If found, the key and value are added to the parameter list.
     *
     * @param context   the context from which to start the XPath search.
     * @param params    the parameter list.
     * @param paramKey  the key used to identify the parameter in the parameter list.
     * @param paramName the Xpath expression identifying the parameter value within the specified context.
     * @param xpath     an {@link XPath} object to use for the search. This allows the caller to re-use XPath objects
     *                  when performing multiple searches. May be null.
     *
     * @throws IllegalArgumentException if either the context, parameter list, parameter key or parameter name are
     *                                  null.
     */
    public static void checkAndSetLatLonParam(Element context, AVList params, String paramKey, String paramName,
        XPath xpath)
    {
        if (context == null)
        {
            String message = Logging.getMessage("nullValue.ElementIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (params == null)
        {
            String message = Logging.getMessage("nullValue.ParametersIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (paramKey == null)
        {
            String message = Logging.getMessage("nullValue.ParameterKeyIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (paramName == null)
        {
            String message = Logging.getMessage("nullValue.ParameterNameIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        Object o = params.getValue(paramKey);
        if (o == null)
        {
            LatLon ll = getLatLon(context, paramName, xpath);
            if (ll != null)
                params.setValue(paramKey, ll);
        }
    }

    /**
     * Checks a parameter list for a specified key and if not present attempts to find a value for the key from an
     * element matched by an XPath expression. If found, the key and value are added to the parameter list.
     *
     * @param context   the context from which to start the XPath search.
     * @param params    the parameter list.
     * @param paramKey  the key used to identify the paramater in the parameter list.
     * @param paramName the Xpath expression identifying the parameter value within the specified context.
     * @param xpath     an {@link XPath} object to use for the search. This allows the caller to re-use XPath objects
     *                  when performing multiple searches. May be null.
     *
     * @throws IllegalArgumentException if either the context, parameter list, parameter key or parameter name are
     *                                  null.
     */
    public static void checkAndSetTimeParam(Element context, AVList params, String paramKey, String paramName,
        XPath xpath)
    {
        if (context == null)
        {
            String message = Logging.getMessage("nullValue.ElementIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (params == null)
        {
            String message = Logging.getMessage("nullValue.ParametersIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (paramKey == null)
        {
            String message = Logging.getMessage("nullValue.ParameterKeyIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (paramName == null)
        {
            String message = Logging.getMessage("nullValue.ParameterNameIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        Object o = params.getValue(paramKey);
        if (o == null)
        {
            Long d = getTimeInMillis(context, paramName, xpath);
            if (d != null)
                params.setValue(paramKey, d);
        }
    }

    /**
     * Checks a parameter list for a specified key and if not present attempts to find a value for the key from an
     * element matched by an XPath expression. If found, the key and value are added to the parameter list.
     *
     * @param context   the context from which to start the XPath search.
     * @param params    the parameter list.
     * @param paramKey  the key used to identify the parameter in the parameter list.
     * @param paramName the Xpath expression identifying the parameter value within the specified context.
     * @param xpath     an {@link XPath} object to use for the search. This allows the caller to re-use XPath objects
     *                  when performing multiple searches. May be null.
     *
     * @throws IllegalArgumentException if either the context, parameter list, parameter key or parameter name are
     *                                  null.
     */
    public static void checkAndSetUniqueStringsParam(Element context, AVList params, String paramKey, String paramName,
        XPath xpath)
    {
        if (context == null)
        {
            String message = Logging.getMessage("nullValue.ElementIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (params == null)
        {
            String message = Logging.getMessage("nullValue.ParametersIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (paramKey == null)
        {
            String message = Logging.getMessage("nullValue.ParameterKeyIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (paramName == null)
        {
            String message = Logging.getMessage("nullValue.ParameterNameIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        Object o = params.getValue(paramKey);
        if (o == null)
        {
            String[] strings = getUniqueText(context, paramName, xpath);
            if (strings != null && strings.length > 0)
                params.setValue(paramKey, strings);
        }
    }

    /**
     * Checks a parameter list for a specified key and if not present attempts to find a value for the key from an
     * element matched by an XPath expression. If found, the key and value are added to the parameter list.
     *
     * @param context   the context from which to start the XPath search.
     * @param params    the parameter list.
     * @param paramKey  the key used to identify the paramater in the parameter list.
     * @param paramName the Xpath expression identifying the parameter value within the specified context.
     * @param xpath     an {@link XPath} object to use for the search. This allows the caller to re-use XPath objects
     *                  when performing multiple searches. May be null.
     *
     * @throws IllegalArgumentException if either the context, parameter list, parameter key or parameter name are
     *                                  null.
     */
    public static void checkAndSetDoubleParam(Element context, AVList params, String paramKey, String paramName,
        XPath xpath)
    {
        if (context == null)
        {
            String message = Logging.getMessage("nullValue.ElementIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (params == null)
        {
            String message = Logging.getMessage("nullValue.ParametersIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (paramKey == null)
        {
            String message = Logging.getMessage("nullValue.ParameterKeyIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (paramName == null)
        {
            String message = Logging.getMessage("nullValue.ParameterNameIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        Object o = params.getValue(paramKey);
        if (o == null)
        {
            Double d = getDouble(context, paramName, xpath);
            if (d != null)
                params.setValue(paramKey, d);
        }
    }

    /**
     * Checks a parameter list for a specified key and if not present attempts to find a value for the key from an
     * element matched by an XPath expression. If found, the key and value are added to the parameter list.
     *
     * @param context   the context from which to start the XPath search.
     * @param params    the parameter list.
     * @param paramKey  the key used to identify the paramater in the parameter list.
     * @param paramName the Xpath expression identifying the parameter value within the specified context.
     * @param xpath     an {@link XPath} object to use for the search. This allows the caller to re-use XPath objects
     *                  when performing multiple searches. May be null.
     *
     * @throws IllegalArgumentException if either the context, parameter list, parameter key or parameter name are
     *                                  null.
     */
    public static void checkAndSetTimeParamAsInteger(Element context, AVList params, String paramKey, String paramName,
        XPath xpath)
    {
        if (context == null)
        {
            String message = Logging.getMessage("nullValue.ElementIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (params == null)
        {
            String message = Logging.getMessage("nullValue.ParametersIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (paramKey == null)
        {
            String message = Logging.getMessage("nullValue.ParameterKeyIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (paramName == null)
        {
            String message = Logging.getMessage("nullValue.ParameterNameIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        Object o = params.getValue(paramKey);
        if (o == null)
        {
            Long d = WWXML.getTimeInMillis(context, paramName, xpath);
            if (d != null)
                params.setValue(paramKey, d.intValue());
        }
    }

    /**
     * Checks a parameter list for a specified key and if present attempts to append new elements represeting the
     * parameter to a specified context.
     *
     * @param context  the context on which to append new elements.
     * @param params   the parameter list.
     * @param paramKey the key used to identify the paramater in the parameter list.
     * @param path     the element path to append.
     *
     * @throws IllegalArgumentException if either the parameter list  parameter key, or context are null.
     */
    public static void checkAndAppendSectorElement(AVList params, String paramKey, Element context, String path)
    {
        if (params == null)
        {
            String message = Logging.getMessage("nullValue.ParametersIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (paramKey == null)
        {
            String message = Logging.getMessage("nullValue.ParameterKeyIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (context == null)
        {
            String message = Logging.getMessage("nullValue.ElementIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        Object o = params.getValue(paramKey);
        if (o != null && o instanceof Sector)
        {
            appendSector(context, path, (Sector) o);
        }
    }

    /**
     * Checks a parameter list for a specified key and if present attempts to append new elements represeting the
     * parameter to a specified context.
     *
     * @param context  the context on which to append new elements.
     * @param params   the parameter list.
     * @param paramKey the key used to identify the paramater in the parameter list.
     * @param path     the element path to append.
     *
     * @throws IllegalArgumentException if either the parameter list  parameter key, or context are null.
     */
    public static void checkAndAppendSectorResolutionElement(AVList params, String paramKey, Element context,
        String path)
    {
        if (params == null)
        {
            String message = Logging.getMessage("nullValue.ParametersIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (paramKey == null)
        {
            String message = Logging.getMessage("nullValue.ParameterKeyIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (context == null)
        {
            String message = Logging.getMessage("nullValue.ElementIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        Object o = params.getValue(paramKey);
        if (o != null && o instanceof LevelSet.SectorResolution[])
        {
            LevelSet.SectorResolution[] srs = (LevelSet.SectorResolution[]) o;

            for (LevelSet.SectorResolution sr : srs)
            {
                if (sr != null)
                {
                    appendSectorResolutionLimit(context, path, sr);
                }
            }
        }
    }

    /**
     * Checks a parameter list for a specified key and if present attempts to append new elements represeting the
     * parameter to a specified context.
     *
     * @param context  the context on which to append new elements.
     * @param params   the parameter list.
     * @param paramKey the key used to identify the paramater in the parameter list.
     * @param path     the element path to append.
     *
     * @throws IllegalArgumentException if either the parameter list  parameter key, or context are null.
     */
    public static void checkAndAppendLatLonElement(AVList params, String paramKey, Element context, String path)
    {
        if (params == null)
        {
            String message = Logging.getMessage("nullValue.ParametersIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (paramKey == null)
        {
            String message = Logging.getMessage("nullValue.ParameterKeyIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (context == null)
        {
            String message = Logging.getMessage("nullValue.ElementIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        Object o = params.getValue(paramKey);
        if (o != null && o instanceof LatLon)
        {
            appendLatLon(context, path, (LatLon) o);
        }
    }

    /**
     * Checks a parameter list for a specified key and if present attempts to append new elements represeting the
     * parameter to a specified context.
     *
     * @param context  the context on which to append new elements.
     * @param params   the parameter list.
     * @param paramKey the key used to identify the paramater in the parameter list.
     * @param path     the element path to append.
     *
     * @throws IllegalArgumentException if either the parameter list  parameter key, or context are null.
     */
    public static void checkAndAppendIntegerElement(AVList params, String paramKey, Element context, String path)
    {
        if (params == null)
        {
            String message = Logging.getMessage("nullValue.ParametersIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (paramKey == null)
        {
            String message = Logging.getMessage("nullValue.ParameterKeyIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (context == null)
        {
            String message = Logging.getMessage("nullValue.ElementIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        Integer i = AVListImpl.getIntegerValue(params, paramKey);
        if (i != null)
        {
            appendInteger(context, path, i);
        }
    }

    /**
     * Uses reflection to invoke property methods on an object, with the properties specified in an XML document. For
     * each element named "Property" in the document, the corresponding <i>set</i> method is called on the specified
     * object, if such a method exists.
     *
     * @param parent     the object on which to set the properties.
     * @param domElement the XML document containing the properties.
     *
     * @throws IllegalArgumentException if the specified object or XML document element is null.
     * @see WWUtil#invokePropertyMethod(Object, String, String)
     */
    public static void invokePropertySetters(Object parent, Element domElement)
    {
        if (parent == null)
        {
            String msg = Logging.getMessage("nullValue.ParentIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (domElement == null)
        {
            String msg = Logging.getMessage("nullValue.ElementIsNull");
            Logging.error(msg);
            throw new IllegalArgumentException(msg);
        }

        List<Element> elements = getElements(domElement, "Property", null);
        if (elements == null || elements.size() == 0)
            return;

        for (Element element : elements)
        {
            String propertyName = element.getAttribute("name");
            if (WWUtil.isEmpty(propertyName))
                continue;

            String propertyValue = element.getAttribute("value");

            try
            {
                WWUtil.invokePropertyMethod(parent, propertyName, propertyValue);
            }
            catch (NoSuchMethodException e)
            {
                // No property method, so just add the property to the object's AVList if it has one.
                if (parent instanceof AVList)
                    ((AVList) parent).setValue(propertyName, propertyValue);
                // This is a benign exception; not all properties have set methods.
            }
            catch (InvocationTargetException e)
            {
                Logging.warning(Logging.getMessage("generic.ExceptionInvokingPropertySetter", propertyName), e);
            }
            catch (IllegalAccessException e)
            {
                Logging.warning(Logging.getMessage("generic.ExceptionInvokingPropertySetter", propertyName), e);
            }
        }
    }

    /**
     * Returns the data type constant for a specified string. This performs a mapping between text and an AVKey
     * constant: <table> <th><td>Text</td><td>Constant</td></th> <tr><td>Float32</td><td>{@link AVKey#FLOAT32}</td></tr>
     * <tr><td>Int32</td><td>{@link AVKey#INT32}</td></tr> <tr><td>Int16</td><td>{@link AVKey#INT16}</td></tr>
     * <tr><td>Int8</td><td>{@link AVKey#INT8}</td></tr> </table>
     *
     * @param s the string to parse as a data type.
     *
     * @return a data type constant, or null if the string text is not recognized.
     *
     * @throws IllegalArgumentException if the string is null.
     */
    public static String parseDataType(String s)
    {
        if (s == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (s.equals("Float32"))
            return AVKey.FLOAT32;
        else if (s.equals("Int32"))
            return AVKey.INT32;
        else if (s.equals("Int16"))
            return AVKey.INT16;
        else if (s.equals("Int8"))
            return AVKey.INT8;

        // Warn that the data type is unrecognized.
        String message = Logging.getMessage("generic.UnrecognizedDataType", s);
        Logging.warning(message);

        return null;
    }

    /**
     * Returns the string text for a specified byte order constant. This performs a mapping between text and an AVKey
     * constant: <table> <th><td>Text</td><td>Constant</td></th> <tr><td>LittleEndian</td><td>{@link
     * AVKey#LITTLE_ENDIAN}</td></tr> <tr><td>BigEndian</td><td>{@link AVKey#BIG_ENDIAN}</td></tr> </table>
     *
     * @param byteOrder the byte order constant to encode as a string.
     *
     * @return a string representing the byte order constant, or null if the byte order constant is not recognized.
     *
     * @throws IllegalArgumentException if the byte order is null.
     */
    public static String byteOrderAsText(String byteOrder)
    {
        if (byteOrder == null)
        {
            String message = Logging.getMessage("nullValue.ByteOrderIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (byteOrder.equals(AVKey.LITTLE_ENDIAN))
            return "LittleEndian";
        else if (byteOrder.equals(AVKey.BIG_ENDIAN))
            return "BigEndian";

        // Warn that the byte order is unrecognized.
        String message = Logging.getMessage("generic.UnrecognizedByteOrder", byteOrder);
        Logging.warning(message);

        return null;
    }

    /**
     * Returns the string text for a specified data type constant. This performs a mapping between text and an AVKey
     * constant: <table> <th><td>Text</td><td>Constant</td></th> <tr><td>Float32</td><td>{@link AVKey#FLOAT32}</td></tr>
     * <tr><td>Int32</td><td>{@link AVKey#INT32}</td></tr> <tr><td>Int16</td><td>{@link AVKey#INT16}</td></tr>
     * <tr><td>Int8</td><td>{@link AVKey#INT8}</td></tr> </table>
     *
     * @param dataType the data type constant to encode as a string.
     *
     * @return a string representing the data type constant, or null if the data type constant is not recognized.
     *
     * @throws IllegalArgumentException if the data type is null.
     */
    public static String dataTypeAsText(String dataType)
    {
        if (dataType == null)
        {
            String message = Logging.getMessage("nullValue.DataTypeIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        if (dataType.equals(AVKey.FLOAT32))
            return "Float32";
        else if (dataType.equals(AVKey.INT32))
            return "Int32";
        else if (dataType.equals(AVKey.INT16))
            return "Int16";
        else if (dataType.equals(AVKey.INT8))
            return "Int8";

        // Warn that the data type is unrecognized.
        String message = Logging.getMessage("generic.UnrecognizedDataType", dataType);
        Logging.warning(message);

        return null;
    }

    /**
     * Returns the byte order constant for a specified string. This performs a mapping between text and an AVKey
     * constant: <table> <th><td>Text</td><td>Constant</td></th> <tr><td>LittleEndian</td><td>{@link
     * AVKey#LITTLE_ENDIAN}</td></tr> <tr><td>BigEndian</td><td>{@link AVKey#BIG_ENDIAN}</td></tr> </table>
     *
     * @param s the string to parse as a byte order.
     *
     * @return a byte order constant, or null if the string text is not recognized.
     *
     * @throws IllegalArgumentException if the string is null.
     */
    public static String parseByteOrder(String s)
    {
        if (s == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        s = s.trim().toLowerCase();
        if (s.startsWith("little"))
            return AVKey.LITTLE_ENDIAN;
        else if (s.startsWith("big"))
            return AVKey.BIG_ENDIAN;

        // Warn that the byte order is unrecognized.
        String message = Logging.getMessage("generic.UnrecognizedByteOrder", s);
        Logging.warning(message);

        return null;
    }

    public static String fixGetMapString(String gms)
    {
        if (gms == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.error(message);
            throw new IllegalArgumentException(message);
        }

        gms = gms.trim();
        int qMarkIndex = gms.indexOf("?");
        if (qMarkIndex < 0)
            gms += "?";
        else if (qMarkIndex != gms.length() - 1)
            if (gms.lastIndexOf("&") != gms.length() - 1)
                gms += "&";

        return gms;
    }
}
