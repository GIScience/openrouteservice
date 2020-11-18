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

import com.graphhopper.GraphHopper;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.VirtualEdgeIteratorState;
import com.graphhopper.storage.GraphExtension;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.DistanceCalc;
import com.graphhopper.util.DistanceCalcEarth;
import com.graphhopper.util.EdgeIteratorState;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import me.tongfei.progressbar.ProgressBar;
import org.apache.log4j.Logger;
import org.geotools.data.DataUtilities;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geojson.geom.GeometryJSON;
import org.heigit.ors.mapmatching.RouteSegmentInfo;
import org.heigit.ors.routing.graphhopper.extensions.ORSGraphHopper;
import org.heigit.ors.routing.graphhopper.extensions.TrafficRelevantWayType;
import org.heigit.ors.routing.graphhopper.extensions.edgefilters.TrafficEdgeFilter;
import org.heigit.ors.routing.graphhopper.extensions.reader.traffic.HereTrafficReader;
import org.heigit.ors.routing.graphhopper.extensions.reader.traffic.TrafficEnums;
import org.heigit.ors.routing.graphhopper.extensions.reader.traffic.TrafficPattern;
import org.heigit.ors.routing.graphhopper.extensions.storages.GraphStorageUtils;
import org.heigit.ors.routing.graphhopper.extensions.storages.OsmIdGraphStorage;
import org.heigit.ors.routing.graphhopper.extensions.storages.TrafficGraphStorage;
import org.heigit.ors.routing.graphhopper.extensions.reader.traffic.TrafficLink;
import org.heigit.ors.util.ErrorLoggingUtility;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.MissingResourceException;

public class HereTrafficGraphStorageBuilder extends AbstractGraphStorageBuilder {
    static final Logger LOGGER = Logger.getLogger(HereTrafficGraphStorageBuilder.class.getName());
    protected final HashSet<String> routeUsage;
    private int trafficWayType = TrafficRelevantWayType.UNWANTED;

    private static String PARAM_KEY_OUTPUT_LOG = "output_log";
    private static boolean outputLog = false;

    public static final String BUILDER_NAME = "HereTraffic";

    private static final Date date = Calendar.getInstance().getTime();
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_hh:mm");

    private static final String PARAM_KEY_STREETS = "streets";
    private static final String PARAM_KEY_PATTERNS_15MINUTES = "pattern_15min";
    private static final String PARAM_KEY_REFERENCE_PATTERN = "ref_pattern";
    private static final String PARAM_KEY_SIMILARITY_FACTOR = "similarity_factor";
    private static final String PARAM_KEY_MATCHED_DATA = "sharableTrafficMatches";
    private static final String FIXED_KEY_SHARED_MATCHES = "non_sharableTrafficMatches";
    private static double similarityFactor = .60;
    private DistanceCalc distCalc;

    TrafficEdgeFilter trafficEdgeFilter;


    private TrafficGraphStorage storage;
    private HereTrafficReader htReader;

    //    TODO variables for loading and saving old matches for faster access
    private HashMap<Long, HashSet<Integer>> osmId2TrafficEdgeId; // one osm id can correspond to multiple edges
    private HashMap<Integer, HashMap<TrafficGraphStorage.Direction, HashSet<Integer>>> edgeId2TrafficEdgeId; // one edge id corresponds to max two traffic edges. one per direction.
    private HashMap<Integer, HashSet<Long>> traffidEdgeId2OsmId; // one edge id can correspond to multiple edges
    private HashMap<Integer, HashSet<Integer>> traffidEdgeId2OriginalEdgeId; // one edge id can correspond to multiple edges
    private HashMap<Integer, String> matchedOSMEdges;

    private LinkedList<String> allOSMEdgeGeometries = new LinkedList<>();
    private HashMap<Integer, String> matchedHereLinks = new HashMap<>();
    private LinkedList<String> matchedOSMLinks = new LinkedList<>();

    private Integer biggestOSMEdge = 0;
    private Integer averageOSMEdge = 0;
    private Integer smallestOSMEdge = Integer.MAX_VALUE;
    private OsmIdGraphStorage graphExtensionOsmId;
    private String matchedEdgetotrafficPath;
    private int missedHereCounter = 0;

    SimpleFeatureType TYPE =
            DataUtilities.createType(
                    "my", "geom:MultiLineString");

    public HereTrafficGraphStorageBuilder() throws SchemaException {
        routeUsage = new HashSet<>(4);
        routeUsage.add("bus");
        routeUsage.add("trolleybus");
        routeUsage.add("ferry");
        routeUsage.add("tram");
        osmId2TrafficEdgeId = new HashMap<>();
        edgeId2TrafficEdgeId = new HashMap<>();
        traffidEdgeId2OsmId = new HashMap<>();
        matchedOSMEdges = new HashMap<>();
        traffidEdgeId2OriginalEdgeId = new HashMap<>();
        distCalc = new DistanceCalcEarth();
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
        distCalc = new DistanceCalcEarth();

        if (this.htReader == null) {
            // Read the border shapes from the file
            // First check if parameters are present
            String streetsFile;
            String patterns15MinutesFile;
            String refPatternIdsFile;
            String oldMatchedEdgeToTrafficFile;
            if (parameters.containsKey(PARAM_KEY_STREETS))
                streetsFile = parameters.get(PARAM_KEY_STREETS);
            else {
                ErrorLoggingUtility.logMissingConfigParameter(HereTrafficGraphStorageBuilder.class, PARAM_KEY_STREETS);
                // We cannot continue without the information
                throw new MissingResourceException("The Here traffic shp file is needed to use the traffic extended storage!", HereTrafficGraphStorageBuilder.class.getName(), PARAM_KEY_STREETS);
            }
            if (parameters.containsKey(PARAM_KEY_PATTERNS_15MINUTES))
                patterns15MinutesFile = parameters.get(PARAM_KEY_PATTERNS_15MINUTES);
            else {
                ErrorLoggingUtility.logMissingConfigParameter(HereTrafficGraphStorageBuilder.class, PARAM_KEY_PATTERNS_15MINUTES);
                // We cannot continue without the information
                throw new MissingResourceException("The Here 15 minutes traffic patterns file is needed to use the traffic extended storage!", HereTrafficGraphStorageBuilder.class.getName(), PARAM_KEY_PATTERNS_15MINUTES);
            }
            if (parameters.containsKey(PARAM_KEY_REFERENCE_PATTERN))
                refPatternIdsFile = parameters.get(PARAM_KEY_REFERENCE_PATTERN);
            else {
                ErrorLoggingUtility.logMissingConfigParameter(HereTrafficGraphStorageBuilder.class, PARAM_KEY_REFERENCE_PATTERN);
                // We cannot continue without the information
                throw new MissingResourceException("The Here traffic pattern reference file is needed to use the traffic extended storage!", HereTrafficGraphStorageBuilder.class.getName(), PARAM_KEY_REFERENCE_PATTERN);
            }
            if (parameters.containsKey(PARAM_KEY_SIMILARITY_FACTOR))
                similarityFactor = Double.parseDouble(parameters.get(PARAM_KEY_SIMILARITY_FACTOR));
            else {
                ErrorLoggingUtility.logMissingConfigParameter(HereTrafficGraphStorageBuilder.class, PARAM_KEY_SIMILARITY_FACTOR);
                // We cannot continue without the information
                throw new MissingResourceException("The Here similarity factor for the geometry matching algorithm is not set!", HereTrafficGraphStorageBuilder.class.getName(), PARAM_KEY_SIMILARITY_FACTOR);
            }
            if (parameters.containsKey(PARAM_KEY_OUTPUT_LOG))
                outputLog = Boolean.parseBoolean(parameters.get(PARAM_KEY_OUTPUT_LOG));
            else {
                ErrorLoggingUtility.logMissingConfigParameter(HereTrafficGraphStorageBuilder.class, PARAM_KEY_OUTPUT_LOG);
                // We cannot continue without the information
                throw new MissingResourceException("The Here similarity factor for the geometry matching algorithm is not set!", HereTrafficGraphStorageBuilder.class.getName(), PARAM_KEY_SIMILARITY_FACTOR);
            }

            if (parameters.containsKey(PARAM_KEY_MATCHED_DATA)) {
                oldMatchedEdgeToTrafficFile = parameters.get(PARAM_KEY_MATCHED_DATA);
                matchedEdgetotrafficPath = Paths.get(oldMatchedEdgeToTrafficFile).toFile().getAbsolutePath();
//                loadTrafficData(matchedEdgetotrafficPath);
            }

            // Read the file containing all of the country border polygons
            this.htReader = new HereTrafficReader(streetsFile, patterns15MinutesFile, refPatternIdsFile);
        }

        storage = new TrafficGraphStorage();
        distCalc = new DistanceCalcEarth();

        return storage;
    }

    @Override
    public void processWay(ReaderWay way) {

        // Reset the trafficWayType
        trafficWayType = TrafficGraphStorage.IGNORE;

        boolean hasHighway = way.hasTag("highway");
        Iterator<Map.Entry<String, Object>> it = way.getProperties();
        while (it.hasNext()) {
            Map.Entry<String, Object> pairs = it.next();
            String key = pairs.getKey();
            String value = pairs.getValue().toString();
            if (hasHighway && key.equals("highway")) {
                trafficWayType = TrafficGraphStorage.getWayTypeFromString(value);
            }
        }
    }

    @Override
    public void processEdge(ReaderWay way, EdgeIteratorState edge) {
        // processEdge(ReaderWay way, EdgeIteratorState edge, com.vividsolutions.jts.geom.Coordinate[] coords) overwrites this function.
        // If the coords are directly delivered it becomes much faster than querying it from the edge
    }

    @Override
    public void processEdge(ReaderWay way, EdgeIteratorState edge, com.vividsolutions.jts.geom.Coordinate[] coords) {
        String lineString = edge.fetchWayGeometry(3).toLineString(false).toString();
        allOSMEdgeGeometries.push(lineString);
        // TODO RAD
        if (lineString.length() > biggestOSMEdge)
            biggestOSMEdge = lineString.length();
        if (lineString.length() < smallestOSMEdge)
            smallestOSMEdge = lineString.length();
        if (averageOSMEdge <= 0)
            averageOSMEdge += lineString.length();
        else
            averageOSMEdge = (lineString.length() + averageOSMEdge) / 2;
        storage.setOrsRoadProperties(edge.getEdge(), TrafficGraphStorage.Property.ROAD_TYPE, (short) trafficWayType);
        if (this.osmId2TrafficEdgeId.get(way.getId()) != null) {
            HashSet<Integer> trafficLinkIds = this.osmId2TrafficEdgeId.get(way.getId());
            for (Integer trafficLinkId :
                    trafficLinkIds) {
                HashSet<Integer> existingOrigindalEdgeIds = traffidEdgeId2OriginalEdgeId.putIfAbsent(trafficLinkId, new HashSet<>(Collections.singletonList(edge.getEdge())));
                if (existingOrigindalEdgeIds != null) {
                    existingOrigindalEdgeIds.add(edge.getEdge());
                    traffidEdgeId2OriginalEdgeId.put(trafficLinkId, existingOrigindalEdgeIds);
                }
            }
        }
    }

    public void writeLogFiles() throws ParseException, java.text.ParseException, IOException {
        if (outputLog) {
            File osmFile = null;
            File osmMatchedFile = null;
            File hereMatchedFile = null;
            File hereFile = null;
            int decimals = 14;
            GeometryJSON gjson = new GeometryJSON(decimals);
            FeatureJSON featureJSON = new FeatureJSON(gjson);
            osmFile = new File(dateFormat.format(date) + "_OSM_edges_output.geojson");
            osmMatchedFile = new File(dateFormat.format(date) + "_OSM_matched_edges_output.geojson");
            hereMatchedFile = new File(dateFormat.format(date) + "_Here_matched_edges_output.geojson");
            hereFile = new File(dateFormat.format(date) + "_Here_edges_output.geojson");

            DefaultFeatureCollection allOSMCollection = new DefaultFeatureCollection();
            DefaultFeatureCollection matchedOSMCollection = new DefaultFeatureCollection();
            DefaultFeatureCollection allHereCollection = new DefaultFeatureCollection();
            DefaultFeatureCollection matchedHereCollection = new DefaultFeatureCollection();

            GeometryFactory gf = new GeometryFactory();
            WKTReader reader = new WKTReader(gf);

            for (String value : allOSMEdgeGeometries) {
                try {
                    SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);
                    com.vividsolutions.jts.geom.Geometry linestring = reader.read(value);
                    featureBuilder.add(linestring);
                    SimpleFeature feature = featureBuilder.buildFeature(null);
                    allOSMCollection.add(feature);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }


            matchedOSMLinks.forEach((value) -> {
                try {
                    SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);
                    com.vividsolutions.jts.geom.Geometry linestring = reader.read(value);
                    featureBuilder.add(linestring);
                    SimpleFeature feature = featureBuilder.buildFeature(null);
                    matchedOSMCollection.add(feature);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            });

            matchedHereLinks.forEach((linkID, emptyString) -> {
                try {
                    String hereLinkGeometry = htReader.getHereTrafficData().getLink(linkID).getLinkGeometry().toString();
                    SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);
                    com.vividsolutions.jts.geom.Geometry linestring = reader.read(hereLinkGeometry);
                    featureBuilder.add(linestring);
                    SimpleFeature feature = featureBuilder.buildFeature(null);
                    matchedHereCollection.add(feature);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            });

            for (TrafficLink trafficLink : htReader.getHereTrafficData().getLinks()) {
                try {
                    String hereLinkGeometry = trafficLink.getLinkGeometry().toString();
                    SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);
                    com.vividsolutions.jts.geom.Geometry linestring = reader.read(hereLinkGeometry);
                    featureBuilder.add(linestring);
                    SimpleFeature feature = featureBuilder.buildFeature(null);
                    allHereCollection.add(feature);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            if (!allOSMCollection.isEmpty())
                osmFile.createNewFile();
                featureJSON.writeFeatureCollection(allOSMCollection,osmFile);
            if (!matchedOSMCollection.isEmpty())
                osmMatchedFile.createNewFile();
                featureJSON.writeFeatureCollection(matchedOSMCollection,osmMatchedFile);
            if (!allHereCollection.isEmpty())
                hereMatchedFile.createNewFile();
                featureJSON.writeFeatureCollection(allHereCollection,hereFile);
            if (!matchedHereCollection.isEmpty())
                hereFile.createNewFile();
                featureJSON.writeFeatureCollection(matchedHereCollection,hereMatchedFile);

        }
    }


    public void saveTrafficData(String filePath) {
        Path path = Paths.get(filePath);
        try {
            if (osmId2TrafficEdgeId.size() > 0) {
                try (FileOutputStream fos = new FileOutputStream(path.toString());
                     ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                    oos.writeObject(osmId2TrafficEdgeId);
                    LOGGER.info("Successfully stored the traffic matches for fast matching.");
                } catch (IOException ioe) {
                    LOGGER.error(Arrays.toString(ioe.getStackTrace()));
                }
            }
            if (edgeId2TrafficEdgeId.size() > 0) {
                Path edgeId2TrafficEdgeIdPath = path.getParent().resolve(FIXED_KEY_SHARED_MATCHES);
                try (FileOutputStream fos = new FileOutputStream(edgeId2TrafficEdgeIdPath.toString());
                     ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                    oos.writeObject(edgeId2TrafficEdgeId);
                    LOGGER.info("Successfully stored the traffic matches for fast matching.");
                } catch (IOException ioe) {
                    LOGGER.error(Arrays.toString(ioe.getStackTrace()));
                }
            }
        } catch (Exception ex) {
            LOGGER.info("Couldn't save traffic data.");

        }
    }

    public void loadTrafficData(String filePath) {
        Path path = Paths.get(filePath).getParent();
        File osmId2TrafficEdgeIdMatches = Paths.get(filePath).toFile();
        File edgeId2TrafficEdgeIdMatches = path.resolve(FIXED_KEY_SHARED_MATCHES).toFile();
        if (osmId2TrafficEdgeIdMatches.exists()) {
            try (FileInputStream fis = new FileInputStream(osmId2TrafficEdgeIdMatches);
                 ObjectInputStream ois = new ObjectInputStream(fis)) {
                osmId2TrafficEdgeId = (HashMap<Long, HashSet<Integer>>) ois.readObject();
                LOGGER.info("Successfully read old matching data");
            } catch (IOException ioe) {
                LOGGER.error(Arrays.toString(ioe.getStackTrace()));
            } catch (ClassNotFoundException c) {
                LOGGER.error("Class not found");
                LOGGER.error(Arrays.toString(c.getStackTrace()));
            }
        } else {
            osmId2TrafficEdgeId = new HashMap<>();
            LOGGER.error("Couldn't load given traffic data. Starting from scratch.");
        }
        if (edgeId2TrafficEdgeIdMatches.exists()) {
            try (FileInputStream fis = new FileInputStream(edgeId2TrafficEdgeIdMatches);
                 ObjectInputStream ois = new ObjectInputStream(fis)) {
                edgeId2TrafficEdgeId = (HashMap<Integer, HashMap<TrafficGraphStorage.Direction, HashSet<Integer>>>) ois.readObject();
                LOGGER.info("Successfully read matching data for profile sharing.");
            } catch (IOException ioe) {
                LOGGER.error(Arrays.toString(ioe.getStackTrace()));
            } catch (ClassNotFoundException c) {
                LOGGER.error("Class not found");
                LOGGER.error(Arrays.toString(c.getStackTrace()));
            }
        } else {
            osmId2TrafficEdgeId = new HashMap<>();
            LOGGER.error("Couldn't load given traffic data. Starting from scratch.");
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

    public HereTrafficReader getHtReader() {
        return htReader;
    }

    public void addHereSegmentForLogging(Integer linkID) {
        matchedHereLinks.putIfAbsent(linkID, "");
    }

    public void addOSMGeometryForLogging(String osmGeometry) {
        matchedOSMLinks.add(osmGeometry);
    }

    private RouteSegmentInfo[] matchLinkToSegments(ORSGraphHopper graphHopper, TrafficLink trafficLink, Geometry geometry, boolean bothDirections) {
        RouteSegmentInfo[] matchedSegments = new RouteSegmentInfo[0];
        if (geometry == null) {
            LOGGER.info("Teadrop node.");
            return matchedSegments;
        }
        try {
            if (trafficEdgeFilter == null) {
                trafficEdgeFilter = new TrafficEdgeFilter(graphHopper.getGraphHopperStorage());
            }
            if (!traffidEdgeId2OriginalEdgeId.isEmpty() && traffidEdgeId2OriginalEdgeId.get(trafficLink.getLinkId()) == null) {
                return matchedSegments;
            }
            trafficEdgeFilter.setHereFunctionalClass(trafficLink.getFunctionalClass());
            trafficEdgeFilter.setOriginalEdgeIds(traffidEdgeId2OriginalEdgeId.get(trafficLink.getLinkId()));
            matchedSegments = graphHopper.getMatchedSegmentsInternal(geometry, trafficLink, 20, 200, 10, trafficEdgeFilter, bothDirections);
        } catch (Exception e) {
            LOGGER.info("Error while matching: " + e);
        }
        return matchedSegments;
    }

    public void addEdge2TrafficMatch(Integer edgeId, Integer originalEdgeId, Integer linkId, TrafficGraphStorage.Direction travelDirection) {
        if (edgeId2TrafficEdgeId == null) {
            edgeId2TrafficEdgeId = new HashMap<>();
        }
        if (traffidEdgeId2OsmId == null) {
            traffidEdgeId2OsmId = new HashMap<>();
        }
        if (osmId2TrafficEdgeId == null) {
            osmId2TrafficEdgeId = new HashMap<>();
        }
        if (matchedOSMEdges == null) {
            matchedOSMEdges = new HashMap<>();
        }
        // Add traffic id match to lookup table
        edgeId2TrafficEdgeId.putIfAbsent(linkId, new HashMap());
        HashMap<TrafficGraphStorage.Direction, HashSet<Integer>> existingDirections = edgeId2TrafficEdgeId.get(linkId);
        existingDirections.putIfAbsent(travelDirection, new HashSet<>());
        HashSet<Integer> existingEdgeIds = existingDirections.get(travelDirection);
        existingEdgeIds.add(edgeId);
        edgeId2TrafficEdgeId.put(linkId, existingDirections);


        Long osmID = null;
        if (graphExtensionOsmId != null && originalEdgeId != null)
            osmID = graphExtensionOsmId.getEdgeValue(originalEdgeId);
        if (osmID == null) return;

        HashSet<Integer> existingEdgesIds = osmId2TrafficEdgeId.putIfAbsent(osmID, new HashSet<>(Collections.singletonList(linkId)));
        if (existingEdgesIds != null) {
            existingEdgesIds.add(linkId);
            osmId2TrafficEdgeId.put(osmID, existingEdgesIds);
        }

        HashSet<Long> existingOsmIds = traffidEdgeId2OsmId.putIfAbsent(linkId, new HashSet<>(Collections.singletonList(osmID)));
        if (existingOsmIds != null) {
            existingOsmIds.add(osmID);
            traffidEdgeId2OsmId.put(linkId, existingOsmIds);
        }
    }

    @Override
    public void postProcess(ORSGraphHopper graphHopper) {
        if (!storage.isMatched()) {
            if (graphHopper.getGraphHopperStorage() != null) {
                graphExtensionOsmId = GraphStorageUtils.getGraphExtension(graphHopper.getGraphHopperStorage(), OsmIdGraphStorage.class);
            }
            LOGGER.info("Starting MapMatching traffic data");
            processTrafficPatterns();
            Collection<Integer> removableLinks = processLinks(htReader.getHereTrafficData().getLinks(), graphHopper);
            htReader.getHereTrafficData().removeLinkIdCollection(removableLinks); // Remove here links without Traffic information to reduce ram usage.

            LOGGER.info("Storing matches.");
//            saveTrafficData(matchedEdgetotrafficPath);
            storage.setMatched();
            storage.flush();
            LOGGER.info("Flush and lock storage.");
        } else {
            LOGGER.info("Traffic data already matched.");
        }
        // TODO RAD
//        GraphHopperStorage graphHopperStorage = graphHopper.getGraphHopperStorage();
//
//        for (GraphExtension ge : GraphStorageUtils.getGraphExtensions(graphHopperStorage)) {
//            if (ge instanceof TrafficGraphStorage) {
//                long seconds1 = Long.parseLong("632354626000");
//                int patterValue1 = ((TrafficGraphStorage) ge).getEdgeIdTrafficPatternLookup(14277, 5022, 222, TrafficEnums.WeekDay.SUNDAY);
//                int speedValue1 = ((TrafficGraphStorage) ge).getSpeedValue(14277, 5022, 222, seconds1);
//                assert patterValue1 == 1309;
//                assert speedValue1 == 31;
//
//                long seconds2 = Long.parseLong("632527426000");
//                int patterValue2 = ((TrafficGraphStorage) ge).getEdgeIdTrafficPatternLookup(14278, 222, 5180, TrafficEnums.WeekDay.TUESDAY);
//                int speedValue2 = ((TrafficGraphStorage) ge).getSpeedValue(14278, 222, 5180, seconds2);
//                assert patterValue2 == 5538;
//                assert speedValue2 == 27;
//
//                long seconds3 = Long.parseLong("632613826000");
//                int patterValue3 = ((TrafficGraphStorage) ge).getEdgeIdTrafficPatternLookup(40, 8282, 250, TrafficEnums.WeekDay.WEDNESDAY);
//                int speedValue3 = ((TrafficGraphStorage) ge).getSpeedValue(40, 8282, 250, seconds3);
//                assert patterValue3 == 29;
//                assert speedValue3 == 30;
//
//                System.out.println("");
//            }
//        }
        // TODO RAD
    }

    private void processTrafficPatterns() {
        Map<Integer, TrafficPattern> patterns = htReader.getHereTrafficData().getPatterns();
        patterns.forEach((patternId, pattern) -> {
            storage.setTrafficPatterns(pattern.getPatternId(), pattern.getValues());
        });
//        TODO RAD
//        int trafficSpeed = storage.getTrafficSpeed(8194, 0, 13);
//        System.out.println("");
        // 36
//        TODO RAD
    }

    private Collection<Integer> processLinks(Collection<TrafficLink> links, ORSGraphHopper graphHopper) {
        Collection<Integer> removableLinks = new ArrayList<>();
        ProgressBar pb = new ProgressBar("Matching Here Links", links.size()); // name, initial max
        pb.start();
        for (TrafficLink hereTrafficLink : links) {
            pb.step();
            if (!hereTrafficLink.isPotentialTrafficSegment()) {
                removableLinks.add(hereTrafficLink.getLinkId());
                missedHereCounter += 1;
                continue;
            }
            processLink(hereTrafficLink, graphHopper);
        }
        pb.stop();
        return removableLinks;
    }

    private void processLink(TrafficLink hereTrafficLink, ORSGraphHopper graphHopper) {
        RouteSegmentInfo[] matchedSegmentsFrom = new RouteSegmentInfo[]{};
        RouteSegmentInfo[] matchedSegmentsTo = new RouteSegmentInfo[]{};
        if (edgeId2TrafficEdgeId.get(hereTrafficLink.getLinkId()) != null) {
            HashMap<TrafficGraphStorage.Direction, HashSet<Integer>> propertyHashSetHashMap = edgeId2TrafficEdgeId.get(hereTrafficLink.getLinkId());
            if (propertyHashSetHashMap.containsKey(TrafficGraphStorage.Direction.FROM_TRAFFIC))
                for (Integer edgeId : propertyHashSetHashMap.get(TrafficGraphStorage.Direction.FROM_TRAFFIC)) {
                    addEdge2TrafficMatch(edgeId, null, hereTrafficLink.getLinkId(), TrafficGraphStorage.Direction.FROM_TRAFFIC);
                }
            if (propertyHashSetHashMap.containsKey(TrafficGraphStorage.Direction.TO_TRAFFIC))
                for (Integer edgeId : propertyHashSetHashMap.get(TrafficGraphStorage.Direction.TO_TRAFFIC)) {
                    addEdge2TrafficMatch(edgeId, null, hereTrafficLink.getLinkId(), TrafficGraphStorage.Direction.TO_TRAFFIC);
                }
        }
        // TODO RAD START
//        else if (hereTrafficLink.getLinkId() == 53061704 || hereTrafficLink.getLinkId() == 808238429)
////         TODO RAD END
        if (hereTrafficLink.isBothDirections()) {
            // Both Directions
            // Split
            matchedSegmentsFrom = matchLinkToSegments(graphHopper, hereTrafficLink, hereTrafficLink.getFromGeometry(), false);
            matchedSegmentsTo = matchLinkToSegments(graphHopper, hereTrafficLink, hereTrafficLink.getToGeometry(), false);
        } else if (hereTrafficLink.isOnlyFromDirection()) {
            // One Direction
            matchedSegmentsFrom = matchLinkToSegments(graphHopper, hereTrafficLink, hereTrafficLink.getFromGeometry(), false);
        } else {
            matchedSegmentsTo = matchLinkToSegments(graphHopper, hereTrafficLink, hereTrafficLink.getToGeometry(), false);
        }

        processSegments(hereTrafficLink, matchedSegmentsFrom, TrafficEnums.TravelDirection.FROM, graphHopper.getGraphHopperStorage());
        processSegments(hereTrafficLink, matchedSegmentsTo, TrafficEnums.TravelDirection.TO, graphHopper.getGraphHopperStorage());
    }

    private void processSegments(TrafficLink trafficLink, RouteSegmentInfo[] matchedSegments, TrafficEnums.TravelDirection direction, GraphHopperStorage graph) {
        if (matchedSegments == null)
            return;
        for (RouteSegmentInfo routeSegment : matchedSegments) {
            if (routeSegment == null) continue;
            processSegment(trafficLink, routeSegment, direction, graph);

        }
    }

    private void processSegment(TrafficLink trafficLink, RouteSegmentInfo routeSegment, TrafficEnums.TravelDirection direction, GraphHopperStorage graph) {
        Map<TrafficEnums.WeekDay, Integer> trafficPatternIds = trafficLink.getTrafficPatternIds(direction);
        for (EdgeIteratorState edge : routeSegment.getEdges()) {
            LineString lineString = edge.fetchWayGeometry(3).toLineString(false);
//            addOSMGeometryForLogging(lineString.toString());
            double priority = distCalc.calcDist(lineString.getStartPoint().getX(), lineString.getStartPoint().getY(), lineString.getEndPoint().getX(), lineString.getEndPoint().getY());
            if (edge instanceof VirtualEdgeIteratorState) {
                VirtualEdgeIteratorState virtualEdge = (VirtualEdgeIteratorState) edge;
                int originalEdgeId;
                int originalBaseNodeId;
                int originalAdjNodeId;
                if (virtualEdge.getAdjNode() < graph.getNodes()) {
                    EdgeIteratorState originalEdgeIter = graph.getEdgeIteratorState(virtualEdge.getOriginalEdge(), virtualEdge.getAdjNode());
                    originalEdgeId = originalEdgeIter.getEdge();
                    originalBaseNodeId = originalEdgeIter.getBaseNode();
                    originalAdjNodeId = originalEdgeIter.getAdjNode();
                } else if (virtualEdge.getBaseNode() < graph.getNodes()) {
                    EdgeIteratorState originalEdgeIter = graph.getEdgeIteratorState(virtualEdge.getOriginalEdge(), virtualEdge.getBaseNode());
                    originalEdgeId = originalEdgeIter.getEdge();
                    originalBaseNodeId = originalEdgeIter.getAdjNode();
                    originalAdjNodeId = originalEdgeIter.getBaseNode();
                } else {
                    continue;
                }
                final int finalOriginalEdgeId = originalEdgeId;
                final int finalOriginalBaseNodeId = originalBaseNodeId;
                final int finalOriginalAdjNodeId = originalAdjNodeId;
                trafficPatternIds.forEach((weekDay, patternId) -> storage.setEdgeIdTrafficPatternLookup(finalOriginalEdgeId, finalOriginalBaseNodeId, finalOriginalAdjNodeId, patternId, weekDay, priority));
                addHereSegmentForLogging(trafficLink.getLinkId());
                addOSMGeometryForLogging(lineString.toString());
            } else {
                trafficPatternIds.forEach((weekDay, patternId) -> storage.setEdgeIdTrafficPatternLookup(edge.getEdge(), edge.getBaseNode(), edge.getAdjNode(), patternId, weekDay, priority));
                addHereSegmentForLogging(trafficLink.getLinkId());
                addOSMGeometryForLogging(lineString.toString());
            }
        }
    }
}