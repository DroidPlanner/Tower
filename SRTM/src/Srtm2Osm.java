/*
 * #%L
 * Osm2garminAPI
 * %%
 * Copyright (C) 2011 Frantisek Mantlik <frantisek at mantlik.cz>
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */
package org.mantlik.osm2garmin;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;
import org.mantlik.osm2garmin.srtm2osm.Contour;
import org.mantlik.osm2garmin.srtm2osm.Contours;
import org.mantlik.osm2garmin.srtm2osm.Point;
import org.mantlik.osm2garmin.srtm2osm.Srtm;

/**
 *
 * @author fm
 */
public class Srtm2Osm extends ThreadProcessor {

    private int lat, lon;
    private String outputFile;
    public boolean allSrtms = true;

    /**
     *
     * @param parameters
     * @param lat
     * @param lon
     * @param outputFile
     */
    public Srtm2Osm(Properties parameters, int lat, int lon, String outputFile) {
        super(parameters, false);
        super.parameters = parameters;
        this.lat = lat;
        this.lon = lon;
        this.outputFile = outputFile;
        start();
    }

    @Override
    public void run() {
        ArrayList<Contour> contours = new ArrayList<Contour>();
        int srtmStep = Integer.parseInt(parameters.getProperty("srtm_step", "2"));
        String coords;
        double offsLat = Double.parseDouble(parameters.getProperty("srtm_offs_lat"));
        double offsLon = Double.parseDouble(parameters.getProperty("srtm_offs_lon"));
        int minorInterval = Integer.parseInt(parameters.getProperty("contour_minor_interval"));
        int mediumInterval = Integer.parseInt(parameters.getProperty("contour_medium_interval"));
        int majorInterval = Integer.parseInt(parameters.getProperty("contour_major_interval"));
        int plotMinorThreshold = Integer.parseInt(parameters.getProperty("plot_minor_threshold"));
        int plotMediumThreshold = Integer.parseInt(parameters.getProperty("plot_medium_threshold"));
        int contoursDensity = Integer.parseInt(parameters.getProperty("contours_density", "1"));
        ArrayList<Contour> gridcont = new ArrayList<Contour>();
        for (int la = 0; la < srtmStep; la++) {
            for (int lo = 0; lo < srtmStep; lo++) {
                coords = Math.abs(lat + la) + (lat + la > 0 ? "N " : "S ") + Math.abs(lon + lo) + (lon + lo > 0 ? "E" : "W");
                Srtm srtm = null;
                if (Srtm.exists(lon + lo, lat + la, parameters)) {
                    int i = 0;
                    while (srtm == null && i <= 5) {
                        i++;
                        setStatus("Contours " + coords + ": Downloading SRTM data - attempt no. " + i);
                        srtm = Srtm.get(lon + lo, lat + la, parameters);
                        if (srtm == null) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException ex) {
                            }
                        }
                    }
                    if (srtm == null) {
                        allSrtms = false;
                    }
                }
                if (srtm == null) {
                    setStatus("Contours " + coords + ": No SRTM data.");
                    continue;
                }
                setStatus("Contours " + coords + ": Preparing data.");
                float[][] data = new float[121][121];
                setStatus("Contours " + coords + ": Making contours.");
                try {
                    gridcont.clear();
                    for (int i = 0; i < 10; i++) {
                        for (int j = 0; j < 10; j++) {
                            int mindata = Integer.MAX_VALUE;
                            int maxdata = Integer.MIN_VALUE;
                            for (int ii = 0; ii < 121; ii++) {
                                for (int jj = 0; jj < 121; jj++) {
                                    int dd = srtm.data[i * 120 + ii][j * 120 + jj];
                                    if ((dd > -10000) && (dd < 10000)) {
                                        mindata = Math.min(mindata, dd);
                                        maxdata = Math.max(maxdata, dd);
                                        data[ii][jj] = dd;
                                    } else {
                                        data[ii][jj] = 32768.0f;
                                    }
                                }
                            }
                            int extent = maxdata - mindata;
                            int interval = extent < plotMinorThreshold ? minorInterval : mediumInterval;
                            interval = extent < plotMediumThreshold ? interval : majorInterval;
                            if (extent < 2 * interval) {
                                if (extent > 15) {
                                    interval = 10;
                                } else if (extent > 10) {
                                    interval = 5;
                                } else if (extent > 5) {
                                    interval = 2;
                                } else {
                                    interval = 1;
                                }
                            }
                            Contours contoursMaker = new Contours(data, 121, 121, 1.0d * lat + la + i / 10d - offsLat,
                                    1.0d * lon + lo + j / 10d - offsLon, 1d / 1200d, interval, 32768.0d);
                            ArrayList c = contoursMaker.makeContours();
                            addContours(gridcont, c, null);
                            setStatus("Contours " + coords + ": Making contours - " + (10 * i + j) + " %");
                        }
                    }
                    setStatus("Contours " + coords + ": Checking contours density.");
                    checkContoursDensity(gridcont, 1201, 1201, 1.0d * lat + la - offsLat,
                            1.0d * lon + lo - offsLon, 1d / 1200d, contoursDensity, majorInterval);
                    String prefix = "Contours " + coords + ": Joining contours "
                            + gridcont.size() + "->" + contours.size() + " ";
                    addContours(contours, gridcont, prefix);
                } catch (Exception ex) {
                    Logger.getLogger(Srtm2Osm.class.getName()).log(Level.SEVERE, "", ex);
                    setStatus("Contours " + coords + ": Contours creation failed.");
                    continue;
                }
            }
        }
        coords = Math.abs(lat) + (lat > 0 ? "N " : "S ") + Math.abs(lon) + (lon > 0 ? "E" : "W");
        if (contours == null || contours.isEmpty()) {
            setStatus("Contours " + coords + ": No contours created.");
            setState(COMPLETED);
            synchronized (this) {
                notify();
            }
            return;
        }
        // export contours to file
        setStatus("Contours " + coords + ": Creating OSM file.");
        PrintStream ss = null;
        try {
            ss = new PrintStream(new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile))));
            ss.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
            ss.println("<osm version=\"0.5\" generator=\"Srtm2Osm.java\">");
            for (int pass = 0; pass < 2; pass++) {
                long id = Long.parseLong(parameters.getProperty("contour_start_id"));
                long wayId = Long.parseLong(parameters.getProperty("contour_start_way_id"));
                boolean nodes = pass == 0;
                boolean way = pass == 1;
                for (int i = 0; i < contours.size(); i++) {
                    Contour contour = contours.get(i);
                    int level = (int) contour.z;
                    boolean major = (level % majorInterval) == 0;
                    contour.outputOsm(wayId, id, major, ss, nodes, way);
                    wayId++;
                    id += contour.data.size();
                    setStatus("Contours " + coords + ": Creating OSM file - " + (int) (50.0 * i / contours.size() + 50.0 * pass) + " %");
                }
            }
            ss.println("</osm>");
            setState(COMPLETED);
        } catch (IOException ex) {
            Logger.getLogger(Srtm2Osm.class.getName()).log(Level.SEVERE, null, ex);
            setStatus("Error creating " + outputFile);
            setState(ERROR);
        } finally {
            ss.close();
        }
        synchronized (this) {
            notify();
        }
    }
    private HashMap<Point, Integer> starts = new HashMap<Point, Integer>();
    private HashMap<Point, Integer> ends = new HashMap<Point, Integer>();

    /*
     * Add contours from list add to list contours, merge where possible
     */
    private void addContours(ArrayList<Contour> contours, ArrayList<Contour> add, String logPrefix) {
        for (int i = 0; i < contours.size(); i++) {
            Contour c = contours.get(i);
            if (c.isClosed() || c.getData().size() < 2) {
                continue;
            }
            starts.put(c.getData().get(0), i);
            ends.put(c.getData().get(c.getData().size() - 1), i);
        }
        for (int i = 0; i < add.size(); i++) {
            Contour c = add.get(i);
            if (c.getData().size() < 2) {
                continue;
            }
            if (contours.isEmpty() || c.isClosed()) {
                contours.add(c);
                continue;
            }
            boolean finished = false;
            Contour cc = null;
            Point newstart = c.getData().get(0);
            Point newend = c.getData().get(c.getData().size() - 1);
            if (newstart == null || newend == null) {
                continue;
            }
            if (starts.containsKey(newend)) {
                cc = contours.get(starts.get(newend));
            } else if (ends.containsKey(newstart)) {
                cc = contours.get(ends.get(newstart));
            }
            if (cc != null && (!cc.isClosed())) {
                Point start = cc.getData().get(0);
                Point end = cc.getData().get(cc.getData().size() - 1);
                if (end.equals(newstart)) {
                    int j;
                    if (ends.containsKey(newstart)) {
                        j = ends.remove(newstart);
                        ends.put(newend, j);
                    }
                    j = contours.indexOf(cc);
                    contours.remove(cc);
                    contours.add(j, joinContours(cc, c));
                    finished = true;
                } else if (newend.equals(start)) {
                    int j = starts.remove(newend);
                    starts.put(newstart, j);
                    j = contours.indexOf(cc);
                    contours.remove(cc);
                    contours.add(j, joinContours(c, cc));
                    finished = true;
                }
            }
            if (!finished) {
                contours.add(c);
                starts.put(newstart, contours.size() - 1);
                ends.put(newend, contours.size() - 1);
            }
            if (logPrefix != null) {
                setStatus(logPrefix + (int) (100.0 * i / add.size()) + " %");
            }
        }
        starts.clear();
        ends.clear();
    }

    /*
     * join c2 to the end of c1 suppose the last point of c1 equals to the first
     * point of c2 (not checked)
     */
    private Contour joinContours(Contour c1, Contour c2) {
        Point end = c1.getData().get(c1.getData().size() - 1);
        Point start = c2.getData().get(0);
        if (!end.equals(start)) {
            System.err.println("Joining contours with non-equal ends: " + end + " and " + start);
        }
        for (int i = 1; i < c2.getData().size(); i++) {
            c1.getData().add(c2.getData().get(i));
        }
        c1.setClosed(c1.getData().get(0).equals(c1.getData().get(c1.getData().size() - 1)));
        return c1;
    }

    /*
     * Delete segments when a cell contains more than contoursDensity segments;
     * keep major contours if they fit maximum density in a cell
     */
    private void checkContoursDensity(ArrayList<Contour> contours, int nlat, int nlon,
            double startlat, double startlon, double delta, int contoursDensity, int majorInterval) {
        if (contours.isEmpty()) {
            return;
        }
        int[][] density = new int[nlat][nlon];
        int[][] majorDensity = new int[nlat][nlon];
        for (Contour contour : contours) {
            for (int i = 1; i < contour.getData().size(); i++) {
                Point p1 = contour.getData().get(i - 1);
                Point p2 = contour.getData().get(i);
                double la = (p1.getX() + p2.getX()) / 2;
                double lo = (p1.getY() + p2.getY()) / 2;
                int ii = (int) ((la - startlat) / delta);
                int jj = (int) ((lo - startlon) / delta);
                density[ii][jj]++;
                if (((int) contour.getZ()) % majorInterval == 0) {
                    majorDensity[ii][jj]++;
                }
            }
        }
        for (int k = 0; k < contours.size(); k++) {
            Contour contour = contours.get(k);
            for (int i = 1; i < contour.getData().size(); i++) {
                Point p1 = contour.getData().get(i - 1);
                Point p2 = contour.getData().get(i);
                double la = (p1.getX() + p2.getX()) / 2;
                double lo = (p1.getY() + p2.getY()) / 2;
                int ii = (int) ((la - startlat) / delta);
                int jj = (int) ((lo - startlon) / delta);
                if ((majorDensity[ii][jj] > contoursDensity)
                        || (density[ii][jj] > contoursDensity
                        && (contour.getZ() % majorInterval != 0))) {
                    // remove segment from contour
                    if (i == 1) { // first segment, delete first point
                        contour.getData().remove(0);
                        i = i - 1;  // next segment replaces deleted one - recheck
                        contour.setClosed(false);
                    } else if (i == (contour.getData().size() - 1)) { //last segment, delete last point
                        contour.getData().remove(i);
                        contour.setClosed(false);
                    } else if (contour.isClosed()) {
                        int id = 1;
                        while (id < i) {
                            contour.getData().add(contour.getData().remove(0));
                            contour.setClosed(false);
                            i = 0;
                        }
                    } else {
                        // middle segment - break contour
                        Contour newContour = new Contour();
                        newContour.setZ(contour.getZ());
                        while (contour.getData().size() > i) {
                            newContour.getData().add(contour.getData().remove(i));
                        }
                        contours.add(newContour);
                    }
                }
            }
        }
    }
}
