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
package org.heigit.ors.routing.graphhopper.extensions.reader.traffic;

import com.graphhopper.util.DistanceCalcEarth;
import org.locationtech.jts.geom.MultiLineString;
import org.apache.log4j.Logger;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.DefaultFeatureCollection;
import org.heigit.ors.util.CSVUtility;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;

import java.io.File;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.util.*;

public class HereTrafficReader {
    private static final Logger LOGGER = Logger.getLogger(HereTrafficReader.class);

    private boolean isInitialized;
    private String streetGeometriesFile;
    private String patternsReferenceFile;
    private String patternsFile;

    private final TrafficData hereTrafficData = new TrafficData();

    private static HereTrafficReader currentInstance;

    DistanceCalcEarth distCalc;

    /**
     * Empty constructor which does not read any data - the user must explicitly pass information
     */
    public HereTrafficReader() {
        this.streetGeometriesFile = "";
        this.patternsFile = "";
        this.patternsReferenceFile = "";
        this.distCalc = new DistanceCalcEarth();
        currentInstance = this;
        isInitialized = false;
    }

    /**
     * Constructor - the user must explicitly pass information
     */
    public HereTrafficReader(String streetGeometriesFile, String patterns15MinutesFile, String refPatternIdsFile) {
        this.streetGeometriesFile = streetGeometriesFile;
        this.patternsFile = patterns15MinutesFile;
        this.patternsReferenceFile = refPatternIdsFile;
        this.distCalc = new DistanceCalcEarth();
        currentInstance = this;
        isInitialized = false;
    }

    public void readData() throws IOException {
        if (streetGeometriesFile.equals("") || patternsFile.equals("") || patternsReferenceFile.equals(""))
            return;
        try {
            SimpleFeatureCollection rawGeometries = readHereGeometries();
            createHereGeometries(rawGeometries);
            LOGGER.info("Here link geometries pre-processed");

            HashMap<Integer, EnumMap<TrafficEnums.TravelDirection, Integer[]>> referencePatterns = readRefPatterns();
            LOGGER.info("Here reference patterns pre-processed");

            Map<Integer, TrafficPattern> patterns = readPatterns();
            LOGGER.info("Here patterns pre-processed");


            generatePatterns(referencePatterns, patterns);
            LOGGER.info("Here input data processed successfully");

            isInitialized = true;
        } catch (IOException ioe) {
            // Problem with reading the data
            LOGGER.error("Could not access file(s) required for Here traffic data");
            throw ioe;
        }
        currentInstance = this;
    }

    public boolean isInitialized() {
        return this.isInitialized;
    }

    private void generatePatterns(HashMap<Integer, EnumMap<TrafficEnums.TravelDirection, Integer[]>> referencePatterns, Map<Integer, TrafficPattern> patterns) {
        for (Map.Entry<Integer, EnumMap<TrafficEnums.TravelDirection, Integer[]>> linkIdEntry : referencePatterns.entrySet()) {
            Integer linkId = linkIdEntry.getKey();
            if (hereTrafficData.hasLink(linkId)) {
                TrafficLink link = hereTrafficData.getLink(linkId);
                EnumMap<TrafficEnums.TravelDirection, Integer[]> travelDirectionPatterns = referencePatterns.get(linkId);
                if (travelDirectionPatterns.containsKey(TrafficEnums.TravelDirection.TO)) {
                    Integer[] travelPatternReferences = travelDirectionPatterns.get(TrafficEnums.TravelDirection.TO);
                    for (int i = 0; i < travelPatternReferences.length; i++) {
                        Integer patternReference = travelPatternReferences[i];
                        if (patterns.containsKey(patternReference)) {
                            hereTrafficData.setPattern(patterns.get(patternReference));
                            link.setTrafficPatternId(TrafficEnums.TravelDirection.TO, TrafficEnums.WeekDay.values()[i], patternReference);
                        }
                    }
                } else if (travelDirectionPatterns.containsKey(TrafficEnums.TravelDirection.FROM)) {
                    Integer[] travelPatternReferences = travelDirectionPatterns.get(TrafficEnums.TravelDirection.FROM);
                    for (int i = 0; i < travelPatternReferences.length; i++) {
                        Integer patternReference = travelPatternReferences[i];
                        if (patterns.containsKey(patternReference)) {
                            hereTrafficData.setPattern(patterns.get(patternReference));
                            link.setTrafficPatternId(TrafficEnums.TravelDirection.FROM, TrafficEnums.WeekDay.values()[i], patternReference);
                        }
                    }
                }

                hereTrafficData.setLink(link);
            }
        }
    }

    private HashMap<Integer, EnumMap<TrafficEnums.TravelDirection, Integer[]>> readRefPatterns() {
        List<List<String>> rawPatternReferenceList = CSVUtility.readFile(patternsReferenceFile);
        HashMap<Integer, EnumMap<TrafficEnums.TravelDirection, Integer[]>> processedPatternReferenceList = new HashMap<>();
        for (List<String> rawPatternReference : rawPatternReferenceList) {
            EnumMap<TrafficEnums.TravelDirection, Integer[]> patternMap = new EnumMap<>(TrafficEnums.TravelDirection.class);
            Integer linkId = Integer.parseInt(rawPatternReference.get(0));
            TrafficEnums.TravelDirection travelDirection = TrafficEnums.TravelDirection.forValue(rawPatternReference.get(1));
            if (travelDirection == null || rawPatternReference.size() != 9) {
                // Skip this entry as its not a complete week pattern.
                continue;
            }
            Integer[] patternList = new Integer[rawPatternReference.size() - 2];
            for (int i = 2; i < rawPatternReference.size(); i++) {
                patternList[i - 2] = Integer.parseInt(rawPatternReference.get(i));
            }
            patternMap.put(travelDirection, patternList);
            processedPatternReferenceList.put(linkId, patternMap);
        }
        return processedPatternReferenceList;
    }

    private Map<Integer, TrafficPattern> readPatterns() {
        List<List<String>> patterns = CSVUtility.readFile(patternsFile);
        Map<Integer, TrafficPattern> hereTrafficPatterns = new HashMap<>();
        for (List<String> pattern : patterns) {
            int patternID = Integer.parseInt(pattern.get(0));
            short[] patternValues = new short[pattern.size() - 1];
            for (int i = 1; i < pattern.size(); i++) {
                patternValues[i - 1] = Short.parseShort(pattern.get(i));
            }
            TrafficPattern hereTrafficPattern = new TrafficPattern(patternID, TrafficEnums.PatternResolution.MINUTES_15, patternValues);
            hereTrafficPatterns.put(patternID, hereTrafficPattern);
        }
        return hereTrafficPatterns;
    }


    /**
     * Method to read the geometries from a GeoJSON file that represent the boundaries of different countries. Ideally
     * it should be written using many small objects split into hierarchies.
     * <p>
     * If the file is a .tar.gz format, it will decompress it and then store the reulting data to be read into the
     * JSON object.
     *
     * @return A (Geo)JSON object representing the contents of the file
     */
    private SimpleFeatureCollection readHereGeometries() throws IOException {
        SimpleFeatureCollection collection = new DefaultFeatureCollection();
        try {
            File file = new File(streetGeometriesFile);
            FileDataStore store = FileDataStoreFinder.getDataStore(file);
            SimpleFeatureSource featureSource = store.getFeatureSource();
            collection = featureSource.getFeatures();
        } catch (IOException e) {
            LOGGER.error("Error reading here shape file with error: " + e);
            throw e;
        } catch (Exception e) {
            LOGGER.error("Unknown error while reading here shape file with error: " + e);
            throw e;
        }
        return collection;
    }

    /**
     * Construct the Here Links from the raw featureCollection
     *
     * @param featureCollection Raw featureCollection.
     */
    private void createHereGeometries(SimpleFeatureCollection featureCollection) throws InvalidObjectException {
        if (featureCollection == null || featureCollection.isEmpty()) {
            return;
        }
        int linkCounter = 0;
        SimpleFeatureIterator iterator = featureCollection.features();
        GeometryFactory gf = new GeometryFactory();
        WKTReader reader = new WKTReader(gf);
        try {
            while (iterator.hasNext()) {
                SimpleFeature feature = iterator.next();
                int linkId = Integer.parseInt(String.valueOf(feature.getAttribute("LINK_ID")));
                MultiLineString defaultGeometry = (MultiLineString) feature.getDefaultGeometry();
                Collection<Property> properties = feature.getProperties();
                if (defaultGeometry.getNumGeometries() == 1) {
                    String geometryString = defaultGeometry.getGeometryN(0).toText();
                    try {
                        hereTrafficData.setLink(new TrafficLink(linkId, reader.read(geometryString), properties, distCalc));
                    } catch (ParseException e) {
                        LOGGER.info("Couldn't parse here geometry for Link_ID: " + linkId);
                    }
                } else {
                    LOGGER.debug("Geometry malformed. Skip parsing here geometry for Link_ID: " + linkId);
                }
                // process feature
                linkCounter += 1;
            }
        } finally {
            iterator.close();
        }
        LOGGER.info(linkCounter + " Here links found");
    }

    public TrafficData getHereTrafficData() {
        return hereTrafficData;
    }

}
