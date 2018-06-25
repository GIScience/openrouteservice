/*
 *  Licensed to GraphHopper GmbH under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for 
 *  additional information regarding copyright ownership.
 * 
 *  GraphHopper GmbH licenses this file to you under the Apache License, 
 *  Version 2.0 (the "License"); you may not use this file except in 
 *  compliance with the License. You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.graphhopper;

import com.graphhopper.routing.util.EdgeAnnotator;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.HintsMap;
import com.graphhopper.routing.util.PathProcessor;
import com.graphhopper.util.Helper;
import com.graphhopper.util.shapes.GHPoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * GraphHopper request wrapper to simplify requesting GraphHopper.
 *
 * @author Peter Karich
 * @author ratrun
 */
public class GHRequest {
    private final List<GHPoint> points;
    private final HintsMap hints = new HintsMap();
    // List of favored start (1st element) and arrival heading (all other).
    // Headings are north based azimuth (clockwise) in (0, 360) or NaN for equal preference
    // MARQ24 MOD START
    //private final List<Double> favoredHeadings;
    private final List<Pair<Double, Double>> favoredHeadings;  // Modification by Maxim Rylov: Double changed to Pair<Double, Double>
    // MARQ24 MOD END
    private List<String> pointHints = new ArrayList<>();
    private String algo = "";
    private boolean possibleToAdd = false;
    private Locale locale = Locale.US;

    // MARQ24 MOD START
    // Modification by Maxim Rylov: Added class members
    private EdgeAnnotator edgeAnnotator;
    private PathProcessor pathProcessor;
    private EdgeFilter edgeFilter;
    private double[] maxSearchDistances;
    private Boolean simplifyGeometry = true;

    public class Pair<A, B> {
        private final A left;
        private final B right;

        public Pair(A left, B right)
        {
            this.left = left;
            this.right = right;
        }
    }
    // MARQ24 MOD END

    public GHRequest() {
        this(5);
    }

    public GHRequest(int size) {
        points = new ArrayList<GHPoint>(size);
        // MARQ24 MOD START
        //favoredHeadings = new ArrayList<Double>(size);
        favoredHeadings = new ArrayList<Pair<Double, Double>>(size);
        // MARQ24 MOD END
        possibleToAdd = true;
    }

    /**
     * Set routing request from specified startPlace (fromLat, fromLon) to endPlace (toLat, toLon)
     * with a preferred start and end heading. Headings are north based azimuth (clockwise) in (0,
     * 360) or NaN for equal preference.
     */
    public GHRequest(double fromLat, double fromLon, double toLat, double toLon,
                     double startHeading, double endHeading) {
        this(new GHPoint(fromLat, fromLon), new GHPoint(toLat, toLon), startHeading, endHeading);
    }

    /**
     * Set routing request from specified startPlace (fromLat, fromLon) to endPlace (toLat, toLon)
     */
    public GHRequest(double fromLat, double fromLon, double toLat, double toLon) {
        this(new GHPoint(fromLat, fromLon), new GHPoint(toLat, toLon));
    }

    /**
     * Set routing request from specified startPlace to endPlace with a preferred start and end
     * heading. Headings are north based azimuth (clockwise) in (0, 360) or NaN for equal preference
     */
    public GHRequest(GHPoint startPlace, GHPoint endPlace, double startHeading, double endHeading) {
        // MARQ24 MOD START
        this(startPlace, endPlace, startHeading, Double.NaN, endHeading, Double.NaN);
        // MARQ24 MOD END
    }

    // MARQ24 MOD START
    public GHRequest(GHPoint startPlace, GHPoint endPlace, double startHeading, double starHeadingDeviation, double endHeading, double endHeadingDeviation) {
        if (startPlace == null)
            throw new IllegalStateException("'from' cannot be null");

        if (endPlace == null)
            throw new IllegalStateException("'to' cannot be null");

        points = new ArrayList<GHPoint>(2);
        points.add(startPlace);
        points.add(endPlace);

        // MARQ24 ORG CODE START
        /*
        favoredHeadings = new ArrayList<Double>(2);
        validateAzimuthValue(startHeading);
        favoredHeadings.add(startHeading);
        validateAzimuthValue(endHeading);
        favoredHeadings.add(endHeading);
        */
        // MARQ24 ORG CODE END
        // MOD START
        favoredHeadings = new ArrayList<Pair<Double, Double>>(2);
        validateAzimuthValue(startHeading);
        favoredHeadings.add(new Pair<Double, Double>(startHeading, starHeadingDeviation));
        validateAzimuthValue(endHeading);
        favoredHeadings.add(new Pair<Double, Double>(endHeading, endHeadingDeviation));
        // MOD END
    }
    // MARQ24 MOD END

    public GHRequest(GHPoint startPlace, GHPoint endPlace) {
        this(startPlace, endPlace, Double.NaN, Double.NaN);
    }

    /**
     * Set routing request
     * <p>
     *
     * @param points          List of stopover points in order: start, 1st stop, 2nd stop, ..., end
     * @param favoredHeadings List of favored headings for starting (start point) and arrival (via
     *                        and end points) Headings are north based azimuth (clockwise) in (0, 360) or NaN for equal
     */
    public GHRequest(List<GHPoint> points, List<Double> favoredHeadings) {
        if (points.size() != favoredHeadings.size())
            throw new IllegalArgumentException("Size of headings (" + favoredHeadings.size()
                    + ") must match size of points (" + points.size() + ")");

        for (Double heading : favoredHeadings) {
            validateAzimuthValue(heading);
        }
        this.points = points;
        // MARQ24 MOD START
        //this.favoredHeadings = favoredHeadings;
        this.favoredHeadings= new ArrayList<Pair<Double, Double>>(favoredHeadings.size());
        for(Double heading : favoredHeadings) {
            this.favoredHeadings.add(new Pair<Double, Double>(heading, Double.NaN));
        }
        // MARQ24 MOD END
    }

    /**
     * Set routing request
     * <p>
     *
     * @param points List of stopover points in order: start, 1st stop, 2nd stop, ..., end
     */
    public GHRequest(List<GHPoint> points) {
        this(points, Collections.nCopies(points.size(), Double.NaN));
    }

    /**
     * Add stopover point to routing request.
     * <p>
     *
     * @param point          geographical position (see GHPoint)
     * @param favoredHeading north based azimuth (clockwise) in (0, 360) or NaN for equal preference
     */
    public GHRequest addPoint(GHPoint point, double favoredHeading) {
        if (point == null)
            throw new IllegalArgumentException("point cannot be null");

        if (!possibleToAdd)
            throw new IllegalStateException("Please call empty constructor if you intent to use "
                    + "more than two places via addPoint method.");

        points.add(point);
        validateAzimuthValue(favoredHeading);
        // MARQ24 MOD START
        //favoredHeadings.add(favoredHeading);
        favoredHeadings.add(new Pair<Double, Double>(favoredHeading, Double.NaN));
        // MARQ24 MOD END
        return this;
    }

    /**
     * Add stopover point to routing request.
     * <p>
     *
     * @param point geographical position (see GHPoint)
     */
    public GHRequest addPoint(GHPoint point) {
        addPoint(point, Double.NaN);
        return this;
    }

    /**
     * @return north based azimuth (clockwise) in (0, 360) or NaN for equal preference
     */
    public double getFavoredHeading(int i) {
        // MARQ24 MOD START
        //return favoredHeadings.get(i);
        return favoredHeadings.get(i).left;
        // MARQ24 MOD END
    }

    // MARQ24 MOD START
    public double getFavoredHeadingDeviation(int i) {
        return  favoredHeadings.get(i).right;
    }
    // MARQ24 MOD END


    /**
     * @return if there exist a preferred heading for start/via/end point i
     */
    public boolean hasFavoredHeading(int i) {
        if (i >= favoredHeadings.size()) {
            return false;
        }
        // MARQ24 MOD START
        //return !Double.isNaN(favoredHeadings.get(i));
        return !Double.isNaN(favoredHeadings.get(i).left);
        // MARQ24 MOD END
    }

    private void validateAzimuthValue(double heading) {
        // heading must be in (0, 360) oder NaN
        if (!Double.isNaN(heading) && (Double.compare(heading, 360) > 0 || Double.compare(heading, 0) < 0))
            throw new IllegalArgumentException("Heading " + heading + " must be in range (0,360) or NaN");
    }

    public List<GHPoint> getPoints() {
        return points;
    }

    public String getAlgorithm() {
        return algo;
    }

    /**
     * For possible values see AlgorithmOptions.*
     */
    public GHRequest setAlgorithm(String algo) {
        if (algo != null)
            this.algo = Helper.camelCaseToUnderScore(algo);
        return this;
    }

    public Locale getLocale() {
        return locale;
    }

    public GHRequest setLocale(Locale locale) {
        if (locale != null)
            this.locale = locale;
        return this;
    }

    public GHRequest setLocale(String localeStr) {
        return setLocale(Helper.getLocale(localeStr));
    }

    public String getWeighting() {
        return hints.getWeighting();
    }

    /**
     * By default it supports fastest and shortest. Or specify empty to use default.
     */
    public GHRequest setWeighting(String w) {
        hints.setWeighting(w);
        return this;
    }

    public String getVehicle() {
        return hints.getVehicle();
    }

    /**
     * Specify car, bike or foot. Or specify empty to use default.
     */
    public GHRequest setVehicle(String vehicle) {
        hints.setVehicle(vehicle);
        return this;
    }

    public HintsMap getHints() {
        return hints;
    }

    public GHRequest setPointHints(List<String> pointHints) {
        this.pointHints = pointHints;
        return this;
    }

    public List<String> getPointHints() {
        return pointHints;
    }

    public boolean hasPointHints() {
        return pointHints.size() == points.size();
    }

    @Override
    public String toString() {
        String res = "";
        for (GHPoint point : points) {
            if (res.isEmpty()) {
                res = point.toString();
            } else {
                res += "; " + point.toString();
            }
        }
        if (!algo.isEmpty())
            res += " (" + algo + ")";

        return res;
    }

    // ****************************************************************
    // MAR24 MOD START
    // ****************************************************************
    // Modification by Maxim Rylov: Added getEdgeFilter method.
    public EdgeFilter getEdgeFilter() {
        return edgeFilter;
    }

    public EdgeAnnotator getEdgeAnnotator() {
        return edgeAnnotator;
    }

    public void setEdgeAnnotator(EdgeAnnotator edgeAnnotator) {
        this.edgeAnnotator = edgeAnnotator;
    }

    public PathProcessor getPathProcessor() {
        return this.pathProcessor;
    }

    public void setPathProcessor(PathProcessor pathProcessor) {
        this.pathProcessor = pathProcessor;
    }

    // Modification by Maxim Rylov: Added getMaxSearchDistances method.
    public double[] getMaxSearchDistances()
    {
        return maxSearchDistances;
    }

    // Modification by Maxim Rylov: Added setMaxSearchDistances method.
    public void setMaxSearchDistance(double[] distances) {
        maxSearchDistances = distances;
    }

    // Modification by Maxim Rylov: Added setMaxSpeed method.
    public void setMaxSpeed(double speed) {
        if (speed > 0) {
            hints.put("max_speed", speed);
        }
    }

    // Modification by Maxim Rylov: Added setEdgeFilter method.
    public GHRequest setEdgeFilter(EdgeFilter edgeFilter)
    {
        if (edgeFilter != null)
            this.edgeFilter = edgeFilter;

        return this;
    }

    // Modification by Maxim Rylov: Added getSimplifyGeometry method.
    public Boolean getSimplifyGeometry() {
        return simplifyGeometry;
    }

    // Modification by Maxim Rylov: Added setSimplifyGeometry method.
    public void setSimplifyGeometry(Boolean value) {
        simplifyGeometry = value;
    }

    // ****************************************************************
    // MAR24 MOD END
    // ****************************************************************
}
