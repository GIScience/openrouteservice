/*  This file is part of Openrouteservice.
 *
 *  Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the
 *  GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.

 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Lesser General Public License for more details.

 *  You should have received a copy of the GNU Lesser General Public License along with this library;
 *  if not, see <https://www.gnu.org/licenses/>.
 */

package org.heigit.ors.routing.graphhopper.extensions.storages.builders;

import com.carrotsearch.hppc.LongArrayList;
import com.carrotsearch.hppc.LongIntHashMap;
import com.graphhopper.GraphHopper;
import com.graphhopper.coll.LongIntMap;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.storage.GraphExtension;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.FetchMode;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.apache.log4j.Logger;
import org.geotools.data.DataUtilities;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geojson.geom.GeometryJSON;
import org.heigit.ors.routing.graphhopper.extensions.ORSGraphHopper;
import org.heigit.ors.routing.graphhopper.extensions.reader.ubertraffic.UberTrafficData;
import org.heigit.ors.routing.graphhopper.extensions.reader.ubertraffic.UberTrafficPattern;
import org.heigit.ors.routing.graphhopper.extensions.reader.ubertraffic.UberTrafficReader;
import org.heigit.ors.routing.graphhopper.extensions.storages.UberTrafficGraphStorage;
import org.heigit.ors.util.ErrorLoggingUtility;
import org.json.JSONObject;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class UberTrafficGraphStorageBuilder extends AbstractGraphStorageBuilder {
    static final Logger LOGGER = Logger.getLogger(UberTrafficGraphStorageBuilder.class.getName());

    private static final String PARAM_KEY_OUTPUT_LOG = "output_log";
    private static boolean outputLog = false;

    public static final String BUILDER_NAME = "UberTraffic";

    private static final Date date = Calendar.getInstance().getTime();
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_hh:mm");

    private static final String ENABLED = "enabled";
    private static final String PARAM_KEY_UBER_MOVEMENT = "movement_data";

    private boolean enabled = false;
    String uberMovementFile = "";

    private UberTrafficGraphStorage storage;
    private com.carrotsearch.hppc.LongIntMap osmNodeId2InternalIdMapping;

    private UberTrafficData uberTrafficData;

    private int missCounter = 0;
    private int hitCounterMinTwo = 0;
    private int hitCounterMinOne = 0;
    private int totalORSEdges = 0;

    private ArrayList<String> matchedOSMEdges = new ArrayList<>();
    private ArrayList<String> partiallyMatchedOSMEdges = new ArrayList<>();
    private ArrayList<String> missedOSMEdges = new ArrayList<>();


    public UberTrafficGraphStorageBuilder() {
    }

    /**
     * Initialize the Here Traffic graph extension <br/><br/>
     * Files required for the process are obtained from the app.config and passed to a CountryBordersReader object
     * which stores information required for the process (i.e. country geometries and border types)
     *
     * @param graphhopper Provide a graphhopper object.
     * @throws Exception Throws an exception if the storag is already initialized.
     */
    @Override
    public GraphExtension init(GraphHopper graphhopper) throws Exception {
        if (storage != null)
            throw new Exception("GraphStorageBuilder has been already initialized.");

        if (parameters.containsKey(ENABLED))
            enabled = Boolean.parseBoolean(parameters.get(ENABLED));

        if (enabled) {
            if (parameters.containsKey(PARAM_KEY_UBER_MOVEMENT))
                uberMovementFile = parameters.get(PARAM_KEY_UBER_MOVEMENT);
            else {
                ErrorLoggingUtility.logMissingConfigParameter(UberTrafficGraphStorageBuilder.class, PARAM_KEY_UBER_MOVEMENT);
            }
            if (parameters.containsKey(PARAM_KEY_OUTPUT_LOG))
                outputLog = Boolean.parseBoolean(parameters.get(PARAM_KEY_OUTPUT_LOG));
            else {
                ErrorLoggingUtility.logMissingConfigParameter(HereTrafficGraphStorageBuilder.class, PARAM_KEY_OUTPUT_LOG);
            }
            storage = new UberTrafficGraphStorage();
            osmNodeId2InternalIdMapping = new LongIntHashMap();
            UberTrafficReader hereTrafficReader = new UberTrafficReader(uberMovementFile);
            this.uberTrafficData = hereTrafficReader.readAndProcessData();
        } else {
            LOGGER.info("Uber heretraffic not enabled.");
        }

        return storage;
    }

    public UberTrafficData getUberTrafficData() {
        return uberTrafficData;
    }

    @Override
    public void processWay(ReaderWay way) {
        // Reset the trafficWayType
    }

    @Override
    public void processEdge(ReaderWay way, EdgeIteratorState edge) {
    }

    @Override
    public void processEdge(ReaderWay way, EdgeIteratorState edge, Coordinate[] coords) {
        if (enabled) {
            totalORSEdges++;
            // Translate all the original edge ids to the new way ids
            //String geojson = createGeojsonFromEdge(edge);
            LineString lineString = edge.fetchWayGeometry(FetchMode.ALL).toLineString(false);

            long wayId = way.getId();
            if (wayId == 272966279){
                System.out.println();
            }
            // TODO find a way to map the edges

            int edgeId = edge.getEdge();
            LongArrayList wayNodes = way.getNodes();
            int edgeBaseNode = edge.getBaseNode();
            int edgeAdjacentNode = edge.getAdjNode();
            UberTrafficPattern pattern = this.uberTrafficData.getPattern(wayId);
            if (pattern != null) {
                long[] allPatternIds = pattern.getAllNodeIds();
                int localHitCounter = 0;
                int localMissCounter = 0;
                for (long patternId : allPatternIds) {
                    if (patternId == 0) {
                        continue;
                    }
                    if (wayNodes.contains(patternId)) {
                        localHitCounter++;
                    } else {
                        localMissCounter++;
                    }
                }
                if (localHitCounter > 2) {
                    this.hitCounterMinTwo++;
                    addOSMGeometryForLogging(lineString.toString());
                } else if (localHitCounter > 0) {
                    this.hitCounterMinOne++;
                    addPartiallyMatchedOSMGeometryForLogging(lineString.toString());
                } else {
                    this.missCounter++;
                    addMissedOSMGeometryForLogging(lineString.toString());
                }
            } else {
                this.missCounter++;
            }
        }
    }

    /**
     * Method identifying the name of the extension which is used in various building processes
     *
     * @return The name of this extension.
     */
    @Override
    public String getName() {
        return BUILDER_NAME;
    }

    public void setOsmNode2InternalMapping(LongIntMap nodeMap) {
        long[] uniqueOriginalOsmIds = this.uberTrafficData.getUniqueOriginalOsmNodeIds();
        int missedCounter = 0;
        if (uniqueOriginalOsmIds.length > 0) {
            for (long osmId : uniqueOriginalOsmIds) {
                int internalMapping = nodeMap.get(osmId);
                if (internalMapping > 0 || internalMapping < -1) {
                    this.osmNodeId2InternalIdMapping.put(osmId, internalMapping);
                } else {
                    missedCounter++;
                }
            }
        }
        LOGGER.debug("Found Uber nodes: " + this.osmNodeId2InternalIdMapping.size() + " | Missed Uber nodes: " + missedCounter);
    }

    private String createGeojsonFromEdge(EdgeIteratorState edge) {
        int decimals = 14;
        SimpleFeatureType TYPE = null;
        try {
            TYPE = DataUtilities.createType("my", "geom:MultiLineString");
        } catch (SchemaException e) {
            e.printStackTrace();
        }
        SimpleFeatureType finalTYPE = TYPE;
        GeometryFactory gf = new GeometryFactory();
        WKTReader reader = new WKTReader(gf);

        GeometryJSON gjson = new GeometryJSON(decimals);
        FeatureJSON featureJSON = new FeatureJSON(gjson);
        Writer stringWriter = new StringWriter();
        DefaultFeatureCollection geojsonCollection = new DefaultFeatureCollection();

        try {
            assert finalTYPE != null;
            SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(finalTYPE);
            LineString lineString = edge.fetchWayGeometry(FetchMode.ALL).toLineString(false);
            Geometry linestring = reader.read(lineString.toString());
            featureBuilder.add(linestring);
            SimpleFeature feature = featureBuilder.buildFeature(null);
            geojsonCollection.add(feature);
        } catch (ParseException e) {
            LOGGER.error("Error adding machedOSMLinks", e);
        }

        try {
            featureJSON.writeFeatureCollection(geojsonCollection, stringWriter);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            stringWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new JSONObject(stringWriter.toString()).toString();


    }

    public boolean isEnabled() {
        return enabled;
    }

    private void writeLogFile() throws SchemaException {
        LOGGER.debug("Write log files.");
        SimpleFeatureType TYPE;
        TYPE = DataUtilities.createType("my", "geom:MultiLineString");
        File osmMatchedFile;
        File osmPartiallyMatchedFile;
        File missedEdgesFile;
        int decimals = 14;
        GeometryJSON gjson = new GeometryJSON(decimals);
        FeatureJSON featureJSON = new FeatureJSON(gjson);
        osmMatchedFile = new File(dateFormat.format(date) + "Uber_OSM_matched_edges_output.geojson");
        osmPartiallyMatchedFile = new File(dateFormat.format(date) + "Uber_OSM_partially_matched_edges_output.geojson");
        missedEdgesFile = new File(dateFormat.format(date) + "Uber_OSM_missed_edges_output.geojson");

        DefaultFeatureCollection matchedOSMCollection = new DefaultFeatureCollection();
        DefaultFeatureCollection partiallyMatchedOSMCollection = new DefaultFeatureCollection();
        DefaultFeatureCollection missedEdgesOSMCollection = new DefaultFeatureCollection();

        GeometryFactory gf = new GeometryFactory();
        WKTReader reader = new WKTReader(gf);


        SimpleFeatureType finalTYPE = TYPE;
        matchedOSMEdges.forEach((value) -> {
            try {
                SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(finalTYPE);
                Geometry linestring = reader.read(value);
                featureBuilder.add(linestring);
                SimpleFeature feature = featureBuilder.buildFeature(null);
                matchedOSMCollection.add(feature);
            } catch (ParseException e) {
                LOGGER.error("Error adding machedOSMLinks", e);
            }
        });
        partiallyMatchedOSMEdges.forEach((value) -> {
            try {
                SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(finalTYPE);
                Geometry linestring = reader.read(value);
                featureBuilder.add(linestring);
                SimpleFeature feature = featureBuilder.buildFeature(null);
                partiallyMatchedOSMCollection.add(feature);
            } catch (ParseException e) {
                LOGGER.error("Error adding machedOSMLinks", e);
            }
        });
        missedOSMEdges.forEach((value) -> {
            try {
                SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(finalTYPE);
                Geometry linestring = reader.read(value);
                featureBuilder.add(linestring);
                SimpleFeature feature = featureBuilder.buildFeature(null);
                missedEdgesOSMCollection.add(feature);
            } catch (ParseException e) {
                LOGGER.error("Error adding machedOSMLinks", e);
            }
        });
        if (matchedOSMCollection.size() > 0) {
            try {
                if (osmMatchedFile.createNewFile()) {
                    featureJSON.writeFeatureCollection(matchedOSMCollection, osmMatchedFile);
                } else {
                    LOGGER.error("Error creating log file for matched OSM data.");
                }
            } catch (IOException e) {
                LOGGER.error("Error writing matched OSM data to log file.", e);
            }
        }
        if (partiallyMatchedOSMCollection.size() > 0) {
            try {
                if (osmPartiallyMatchedFile.createNewFile()) {
                    featureJSON.writeFeatureCollection(partiallyMatchedOSMCollection, osmPartiallyMatchedFile);
                } else {
                    LOGGER.error("Error creating log file for partially matched OSM data.");
                }
            } catch (IOException e) {
                LOGGER.error("Error writing partially matched OSM data to log file.", e);
            }
        }
        if (missedEdgesOSMCollection.size() > 0) {
            try {
                if (missedEdgesFile.createNewFile()) {
                    featureJSON.writeFeatureCollection(missedEdgesOSMCollection, missedEdgesFile);
                } else {
                    LOGGER.error("Error creating log file for missed edges OSM data.");
                }
            } catch (IOException e) {
                LOGGER.error("Error writing missed edges OSM data to log file.", e);
            }
        }
    }

    public void postProcess(ORSGraphHopper graphHopper) throws SchemaException {
        // Schaut nur eine Richtung an. Gibt eine Edge zurück
        //graphHopper.getGraphHopperStorage().createEdgeExplorer().setBaseNode();
        // Bei mehreren Edges
        //weightings.add(new FastestWeighting(footEncoder));
        //new Dijkstra(prepareGraph,prepareWeighting, TraversalMode.NODE_BASED);
        LOGGER.debug("Total Uber edges: " + this.uberTrafficData.getPatterns().size());
        LOGGER.debug("Total ORS edges: " + this.totalORSEdges);
        LOGGER.debug("ORS Edges with Uber match: " + this.hitCounterMinTwo);
        LOGGER.debug("ORS Edges with partial Uber match: " + this.hitCounterMinOne);
        LOGGER.debug("ORS Edges with no Uber match: " + this.missCounter);
        //writeLogFile();
        System.out.println();
    }

    public void addOSMGeometryForLogging(String osmGeometry) {
        matchedOSMEdges.add(osmGeometry);
    }

    public void addPartiallyMatchedOSMGeometryForLogging(String osmGeometry) {
        partiallyMatchedOSMEdges.add(osmGeometry);
    }

    private void addMissedOSMGeometryForLogging(String osmGeometry) {
        missedOSMEdges.add(osmGeometry);
    }

}