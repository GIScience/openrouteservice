package org.heigit.ors.routing.graphhopper.extensions.storages.builders;

import com.carrotsearch.hppc.LongArrayList;
import com.carrotsearch.hppc.LongIntHashMap;
import com.carrotsearch.hppc.LongIntMap;
import com.carrotsearch.hppc.LongObjectHashMap;
import com.graphhopper.GraphHopper;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.storage.GraphExtension;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.FetchMode;
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
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class UberTrafficGraphStorageBuilder extends TrafficGraphStorageBuilder{
    static final Logger LOGGER = Logger.getLogger(UberTrafficGraphStorageBuilder.class.getName());

    private static final String PARAM_KEY_OUTPUT_LOG = "output_log";
    private static boolean outputLog = false;

    public static final String BUILDER_NAME = "UberTraffic";

    private static final String ENABLED = "enabled";
    private static final String PARAM_KEY_UBER_MOVEMENT = "movement_data";

    private boolean enabled = false;
    String uberMovementFile = "";

    private UberTrafficGraphStorage storage;
    private LongIntMap osmNodeId2InternalIdMapping;

    private UberTrafficData uberTrafficData;

    private int missCounter = 0;
    private int hitCounterMinTwo = 0;
    private int hitCounterMinOne = 0;
    private int totalORSEdges = 0;

    // TODO move them to the abstract class
    private HashSet<Long> matchedTrafficEdges = new HashSet<>();
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
            LOGGER.info("Uber traffic not enabled.");
        }

        return storage;
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
            long wayId = way.getId();
            int edgeId = edge.getEdge();
            LongArrayList wayNodes = way.getNodes();
            int edgeBaseNode = edge.getBaseNode();
            int edgeAdjacentNode = edge.getAdjNode();
            UberTrafficPattern pattern = this.uberTrafficData.getPattern(wayId);
            if (pattern != null) {
                // Not needed as long as the uber traffic data is not ordered in patterns!
                //int newUberEdgeId = pattern.getNewUberEdgeId();
                Set<Long> duplicates = countDuplicates(wayNodes.toArray(), pattern.getAllNodeIds());
                if (duplicates.size() >= 2) {
                    this.hitCounterMinTwo++;
                    matchedTrafficEdges.add(wayId);
                    // Yeah. Lucky Edge ;).
                    for (int i = 0; i < wayNodes.size(); i++) {
                        long startNode = wayNodes.get(i);
                        LongObjectHashMap<byte[]> patternsByOsmId = pattern.getPatternsByOsmId(startNode);
                        if (patternsByOsmId == null || patternsByOsmId.isEmpty()) {
                            continue;
                        }
                        for (int j = 0; j < wayNodes.size(); j++) {
                            long endNode = wayNodes.get(j);
                            byte[] schedules = patternsByOsmId.get(endNode);
                            if (schedules == null) {
                                continue;
                            }
                            int validSpeedValues = 24 - countValuesInByteArray(schedules, 0);
                            if (schedules.length <= 0) {
                                continue;
                            }

                            int priority = Math.abs(i - j) + validSpeedValues;

                            if (j > i) {
                                // Traffic in Direction of the edge.
                                //
                                this.storage.setEdgeIdTrafficPatternLookup(edgeId, edgeBaseNode, edgeAdjacentNode, schedules, priority);
                            } else {
                                // Traffic with opposite direction.
                                this.storage.setEdgeIdTrafficPatternLookup(edgeId, edgeAdjacentNode, edgeBaseNode, schedules, priority);
                            }
                        }

                    }
                    if (outputLog) {
                        LineString lineString = edge.fetchWayGeometry(FetchMode.ALL).toLineString(false);
                        matchedOSMEdges.add(lineString.toString());
                    }
                } else if (duplicates.size() > 0) {
                    this.hitCounterMinOne++;
                    if (outputLog) {
                        LineString lineString = edge.fetchWayGeometry(FetchMode.ALL).toLineString(false);
                        addPartiallyMatchedOSMGeometryForLogging(lineString.toString());
                    }
                } else {
                    this.missCounter++;
                    if (outputLog) {
                        LineString lineString = edge.fetchWayGeometry(FetchMode.ALL).toLineString(false);
                        addMissedOSMGeometryForLogging(lineString.toString());
                    }
                }
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

    private Set<Long> countDuplicates(long[] array1, long[] array2) {
        List<Long> array1List = Arrays.stream(array1).boxed().collect(Collectors.toList());
        List<Long> array2List = Arrays.stream(array2).boxed().collect(Collectors.toList());
        Set<Long> intersection = new HashSet<Long>(array1List);
        intersection.retainAll(array2List);
        return intersection;
    }

    public void setOsmNode2InternalMapping(LongIntMap nodeMap) {
        long[] uniqueOriginalOsmIds = this.uberTrafficData.getUniqueOriginalOsmNodeIds();
        if (uniqueOriginalOsmIds.length > 0) {
            for (long osmId : uniqueOriginalOsmIds) {
                int internalMapping = nodeMap.get(osmId);
                if (internalMapping > 0 || internalMapping < -1) {
                    this.osmNodeId2InternalIdMapping.put(osmId, internalMapping);
                }
            }
        }
    }

    /**
     * Returns the number of times a value occurs in a given array.
     */
    public static int countValuesInByteArray(byte[] byteArray, int val) {
        int count = 0;
        for (byte b : byteArray) {
            if (b == val) {
                count++;
            }
        }
        return count;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void postProcess(ORSGraphHopper graphHopper) throws SchemaException {
        // TODO Match the uber traffic data that only had one edge node match with the osm data.
        if (!enabled) {
            LOGGER.debug("Uber traffic not enabled.");
        } else if (storage.isMatched()) {
            LOGGER.info("Uber traffic data already matched. Skipping post processing.");
        } else if (!storage.isMatched() && storage.getEdgesCount() > 0) {
            LOGGER.info("Assign max speeds to Uber data.");
            storage.setMaxTrafficSpeeds();
            storage.setMatched();
            storage.flush();
            LOGGER.info("Flush and lock storage.");
            writeLogFile();
            LOGGER.info("========== Successfully processed Uber data ==========");
            LOGGER.info("> Total Processed ORS Edges: " + this.totalORSEdges);
            LOGGER.info("> Total Processed Uber Edges: " + this.uberTrafficData.getPatterns().size());
            LOGGER.info("> ORS Edges with traffic data: " + this.hitCounterMinTwo);
            LOGGER.info("> ORS Edges with (potentially) matched traffic data: " + this.hitCounterMinOne);
            //LOGGER.info("> ORS Edges without traffic data: " + this.missCounter);
            LOGGER.info("> % of ORS Edges having Uber data: " + ((double) hitCounterMinTwo / (double) totalORSEdges) * 100);
            LOGGER.info("> % of Uber Edges matched: " + (100 - ((double) (hitCounterMinOne + missCounter) / (double) this.uberTrafficData.getPatterns().size()) * 100));
            LOGGER.info("> % of Uber Edges missed: " + ((double) (hitCounterMinOne + missCounter) / (double) this.uberTrafficData.getPatterns().size()) * 100);
            LOGGER.info("======================================================");
        } else {
            throw new MissingResourceException("Here traffic is not build, enabled but the Here data sets couldn't be initialized. Make sure the config contains the path variables and they're correct.", this.getClass().toString(), "movement_data");
        }

    }

    public void addPartiallyMatchedOSMGeometryForLogging(String osmGeometry) {
        partiallyMatchedOSMEdges.add(osmGeometry);
    }

    private void addMissedOSMGeometryForLogging(String osmGeometry) {
        missedOSMEdges.add(osmGeometry);
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
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_hh:mm");
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

    @Override
    public void finish() {
    }
}
