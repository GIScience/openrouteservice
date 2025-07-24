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
import com.graphhopper.storage.GraphExtension;
import com.graphhopper.util.EdgeIteratorState;
import org.apache.log4j.Logger;
import org.heigit.ors.routing.graphhopper.extensions.reader.borders.CountryBordersPolygon;
import org.heigit.ors.routing.graphhopper.extensions.reader.borders.CountryBordersReader;
import org.heigit.ors.routing.graphhopper.extensions.storages.BordersGraphStorage;
import org.heigit.ors.util.ErrorLoggingUtility;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;

import static org.heigit.ors.routing.graphhopper.extensions.storages.BordersGraphStorage.*;

/**
 * Class for building the Borders graph extension that allows restricting routes regarding border crossings
 *
 * @author Adam Rousell
 */
public class BordersGraphStorageBuilder extends AbstractGraphStorageBuilder {
    static final Logger LOGGER = Logger.getLogger(BordersGraphStorageBuilder.class.getName());

    private static final String PARAM_KEY_IDS = "ids";
    private static final String PARAM_KEY_BOUNDARIES = "boundaries";
    private static final String PARAM_KEY_OPEN_BORDERS = "openborders";
    private static final String TAG_KEY_COUNTRY = "country";
    private static final String TAG_KEY_COUNTRY1 = "country1";
    private static final String TAG_KEY_COUNTRY2 = "country2";
    private static final int EMPTY_NODE = -1;
    private static final int TOWER_NODE = -2;
    private HashMap<Integer, String> wayNodeTags;

    private BordersGraphStorage storage;
    private CountryBordersReader cbReader;
    private boolean preprocessed;
    private final GeometryFactory gf;

    public static final String BUILDER_NAME = "Borders";

    public BordersGraphStorageBuilder() {
        gf = new GeometryFactory();
    }

    /**
     * Initialize the Borders graph extension <br/><br/>
     * Files required for the process are obtained from the ors-config.json and passed to a CountryBordersReader object
     * which stores information required for the process (i.e. country geometries and border types)
     *
     * @param graphhopper
     * @return
     * @throws Exception
     */
    @Override
    public GraphExtension init(GraphHopper graphhopper) throws Exception {
        if (storage != null)
            throw new Exception("GraphStorageBuilder has been already initialized.");

        File expectedStorageFileLocation1 = Path.of(graphhopper.getGraphHopperLocation() + "/ext_borders").toFile();
        File expectedStorageFileLocation2 = Path.of(graphhopper.getGraphHopperLocation() + "/ext_borders_cbr").toFile();

        if (this.cbReader == null && (!expectedStorageFileLocation1.exists() || !expectedStorageFileLocation2.exists())) {
            cbReader = createCountryBordersReader();
            cbReader.serialize(expectedStorageFileLocation2);
        }
        if (cbReader == null && expectedStorageFileLocation2.exists()) {
            cbReader = CountryBordersReader.deserialize(expectedStorageFileLocation2);
        }
        storage = new BordersGraphStorage();

        return storage;
    }

    private CountryBordersReader createCountryBordersReader() throws IOException {
        String bordersFile = "";
        String countryIdsFile = "";
        String openBordersFile = "";

        preprocessed = Boolean.TRUE.equals(parameters.getPreprocessed());

        if (!preprocessed) {
            if (parameters.getBoundaries() != null) {
                bordersFile = parameters.getBoundaries().toString();
            } else {
                ErrorLoggingUtility.logMissingConfigParameter(BordersGraphStorageBuilder.class, PARAM_KEY_BOUNDARIES);
                // We cannot continue without the information
                throw new MissingResourceException("An OSM file enriched with country tags or a boundary geometry file is needed to use the borders extended storage!", BordersGraphStorage.class.getName(), PARAM_KEY_BOUNDARIES);
            }
        }

        if (parameters.getIds() != null)
            countryIdsFile = parameters.getIds().toString();
        else
            ErrorLoggingUtility.logMissingConfigParameter(BordersGraphStorageBuilder.class, PARAM_KEY_IDS);

        if (parameters.getOpenborders() != null)
            openBordersFile = parameters.getOpenborders().toString();
        else
            ErrorLoggingUtility.logMissingConfigParameter(BordersGraphStorageBuilder.class, PARAM_KEY_OPEN_BORDERS);

        // Read the file containing all the country border polygons
        return new CountryBordersReader(bordersFile, countryIdsFile, openBordersFile);
    }

    /**
     * Overwrite the current reader with a custom-built CountryBordersReader.
     *
     * @param cbr The CountryBordersReader object to be used
     */
    public void setBordersBuilder(CountryBordersReader cbr) {
        cbReader = cbr;
    }

    @Override
    public void processWay(ReaderWay way) {
        LOGGER.warn("Borders requires geometry for the way!");
    }

    /**
     * Process a way read from the reader and determine whether it crosses a country border. If it does, then country
     * names are stored which identify the countries it crosses. For preprocessed data, it extracts country codes from
     * node tags, otherwise it performs a geometric lookup of the countries.
     *
     * @param way The OSM way being processed
     * @param coords Array of coordinates representing the way's geometry
     * @param nodeTags Map of internal node IDs to their tags
     */
    @Override
    public void processWay(ReaderWay way, Coordinate[] coords, Map<Integer, Map<String, String>> nodeTags) {
        if (cbReader == null) {
            return;
        }

        if (preprocessed) {
            extractNodeToCountryMapping(nodeTags);
        } else {
            lookupCountriesAndSetWayTags(way, coords);
        }
    }

    private void extractNodeToCountryMapping(Map<Integer, Map<String, String>> nodeTags) {
        wayNodeTags = new HashMap<>();
        if (nodeTags == null) {
            return;
        }

        nodeTags.forEach((internalNodeId, tagPairs) -> {
            int nodeId = convertTowerNodeId(internalNodeId);
            if (nodeId != EMPTY_NODE) {
                wayNodeTags.put(nodeId, tagPairs.get(TAG_KEY_COUNTRY));
            }
        });
    }

    private void lookupCountriesAndSetWayTags(ReaderWay way, Coordinate[] coords) {
        String[] countries = findBorderCrossing(coords);
        if (countries.length > 1 && !countries[0].equals(countries[1])) {
            way.setTag(TAG_KEY_COUNTRY1, countries[0]);
            way.setTag(TAG_KEY_COUNTRY2, countries[1]);
        } else if (countries.length == 1) {
            way.setTag(TAG_KEY_COUNTRY1, countries[0]);
            way.setTag(TAG_KEY_COUNTRY2, countries[0]);
        }
    }

    private int convertTowerNodeId(int id) {
        if (id < TOWER_NODE)
            return -id - 3;

        return EMPTY_NODE;
    }

    /**
     * Method to process the edge and store it in the graph.
     * <p>
     * For preprocessed data, extracts country codes from node tags stored for the edge's nodes.
     * For non-preprocessed data, reads country tags from the way (introduced in the processWay method).
     * It then determines the type of border crossing (0 for no border, 1 for controlled, and 2 for open)
     * and stores this information in the graph.
     *
     * @param way  The OSM way obtained from the OSM reader. This way corresponds to the edge to be processed
     * @param edge The graph edge to be processed
     */
    @Override
    public void processEdge(ReaderWay way, EdgeIteratorState edge) {
        if (storage == null) {
            return;
        }

        short countryId1 = 0;
        short countryId2 = 0;

        if (preprocessed) {
            countryId1 = getCountryIdForNode(edge.getBaseNode());
            countryId2 = getCountryIdForNode(edge.getAdjNode());
        } else {
            countryId1 = getCountryIdFromWay(way, TAG_KEY_COUNTRY1);
            countryId2 = getCountryIdFromWay(way, TAG_KEY_COUNTRY2);
        }

        short borderType = getBorderType(countryId1, countryId2);
        storage.setEdgeValue(edge.getEdge(), borderType, countryId1, countryId2);
    }

    private short getCountryIdForNode(int nodeId) {
        String countryCode = wayNodeTags.getOrDefault(nodeId, "");
        try {
            return CountryBordersReader.getCountryIdByISOCode(countryCode);
        } catch (Exception ignore) {
            return 0;
        }
    }

    private short getCountryIdFromWay(ReaderWay way, String tagKey) {
        String countryValue = way.getTag(tagKey);
        try {
            return Short.parseShort(cbReader.getId(countryValue));
        } catch (Exception ignore) {
            return 0;
        }
    }

    private short getBorderType(short countryId1, short countryId2) {
        if (countryId1 == countryId2) {
            return NO_BORDER;
        }
        String countryName1 = cbReader.getName(countryId1);
        String countryName2 = cbReader.getName(countryId2);

        return cbReader.isOpen(countryName1, countryName2) ? OPEN_BORDER : CONTROLLED_BORDER;
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

    /**
     * Method to identify the countries that a way is found in. basically iterates over the country boundaries read from
     * the file adn then does geometric calculations to identify wheich country each node of the way is in.
     *
     * @param coords Coordinates of the way
     * @return An array of strings representing the countries that nodes are found in. If the way is only
     * found in one country, then only one name is returned.
     */
    public String[] findBorderCrossing(Coordinate[] coords) {

        ArrayList<CountryBordersPolygon> countries = new ArrayList<>();

        boolean hasInternational = false;
        boolean overlap = false;

        // Go through the points of the linestring and check what country they are in
        int lsLen = coords.length;
        if (lsLen > 1) {
            for (int i = 0; i < lsLen; i++) {
                // Make sure that it is a valid point
                Coordinate c = coords[i];
                if (!Double.isNaN(c.x) && !Double.isNaN(c.y)) {
                    CountryBordersPolygon[] cnts = cbReader.getCandidateCountry(c);
                    for (CountryBordersPolygon cbp : cnts) {
                        // This check is for the bbox as that is quickest for detecting if there is the possibility of a
                        // crossing
                        if (!countries.contains(cbp)) {
                            countries.add(cbp);
                        }
                    }

                    // If we ended up with no candidates for the point, then we indicate that at least one point is
                    // in international territory
                    if (cnts.length == 0)
                        hasInternational = true;
                }
            }
        }
        // Now get the definite ones that are contained - though this involves another iteration, it will be quicker
        // than the linestring check in the next stage
        if (countries.size() > 1) {
            ArrayList<CountryBordersPolygon> temp = new ArrayList<>();

            for (int i = 0; i < lsLen; i++) {
                // Loop through each point of the line and check whcih countries it is in. This should only be 1 unless
                // there is an overlap
                Coordinate c = coords[i];
                if (!Double.isNaN(c.x) && !Double.isNaN(c.y)) {
                    // Check each country candidate
                    boolean found = false;
                    int countriesFound = 0;

                    for (CountryBordersPolygon cbp : countries) {
                        if (cbp.inArea(c)) {
                            found = true;
                            countriesFound++;
                            if (!temp.contains(cbp)) {
                                // At this point we only want to add countries that are not present. Basically, if a
                                // boundary polygon of the same name is in the list, don't add the country again
                                temp.add(cbp);
                            }
                        }
                    }

                    if (countriesFound > 1) {
                        overlap = true;
                    }

                    if (!found) {
                        hasInternational = true;
                    }
                }
            }


            // Replace the arraylist
            countries = temp;
        }

        // Now we have a list of all the countries that the nodes are in - if this is more than one it is likely it is
        // crossing a border, but not certain as in some disputed areas, countries overlap and so it may not cross any
        // border.
        if (countries.size() > 1 && overlap) {
            boolean crosses = false;
            // Construct the linesting
            LineString ls = gf.createLineString(coords);
            // Check for actually crossing a border, though we only want to do this for overlapping polygons
            for (CountryBordersPolygon cp : countries) {
                // We only want to do this check in the case where all points are in two countrie as this signifies an
                // overlap
                if (cp.crossesBoundary(ls)) {
                    // it crosses a border
                    crosses = true;
                    break;
                }
            }

            if (!crosses) {
                // We want to indicate that it is in the same country, so to do that we only pass one country back
                CountryBordersPolygon cp = countries.get(0);
                countries.clear();
                countries.add(cp);
            }
        }

        // Now get the names of the countries
        ArrayList<String> names = new ArrayList<>();
        for (int i = 0; i < countries.size(); i++) {
            if (!names.contains(countries.get(i).getName()))
                names.add(countries.get(i).getName());
        }

        // If there is an international point and at least one country name, then we know it is a border
        if (hasInternational && !countries.isEmpty()) {
            names.add(CountryBordersReader.INTERNATIONAL_NAME);
        }

        return names.toArray(new String[0]);
    }

    public CountryBordersReader getCbReader() {
        return cbReader;
    }
}
