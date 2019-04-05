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
package heigit.ors.routing.graphhopper.extensions.storages.builders;

import com.graphhopper.GraphHopper;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.storage.GraphExtension;
import com.graphhopper.util.EdgeIteratorState;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import heigit.ors.exceptions.MissingConfigParameterException;
import heigit.ors.routing.graphhopper.extensions.reader.borders.CountryBordersPolygon;
import heigit.ors.routing.graphhopper.extensions.reader.borders.CountryBordersReader;
import heigit.ors.routing.graphhopper.extensions.storages.BordersGraphStorage;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.MissingResourceException;

/**
 * Class for building the Borders graph extension that allows restricting routes regarding border crossings
 *
 * @author Adam Rousell
 */
public class BordersGraphStorageBuilder extends AbstractGraphStorageBuilder {
    final static Logger LOGGER = Logger.getLogger(BordersGraphStorageBuilder.class.getName());

    private BordersGraphStorage _storage;
    private CountryBordersReader cbReader;

    private GeometryFactory gf;

    public BordersGraphStorageBuilder() {
        gf = new GeometryFactory();
    }

    /**
     * Initialize the Borders graph extension <br/><br/>
     * Files required for the process are obtained from the app.config and passed to a CountryBordersReader object
     * which stores information required for the process (i.e. country geometries and border types)
     *
     * @param graphhopper
     * @return
     * @throws Exception
     */
    @Override
    public GraphExtension init(GraphHopper graphhopper) throws Exception {
        if (_storage != null)
            throw new Exception("GraphStorageBuilder has been already initialized.");

        if(this.cbReader == null) {
            // Read the border shapes from the file
            // First check if parameters are present
            String bordersFile = "", countryIdsFile = "", openBordersFile = "";

            if(_parameters.containsKey("boundaries"))
                bordersFile = _parameters.get("boundaries");
            else {
                new MissingConfigParameterException(BordersGraphStorageBuilder.class, "boundaries");
                // We cannot continue without the information
                throw new MissingResourceException("A boundary geometry file is needed to use the borders extended storage!", BordersGraphStorage.class.getName(), "boundaries");
            }

            if(_parameters.containsKey("ids"))
                countryIdsFile = _parameters.get("ids");
            else
                new MissingConfigParameterException(BordersGraphStorageBuilder.class, "ids");

            if(_parameters.containsKey("openborders"))
                openBordersFile = _parameters.get("openborders");
            else
                new MissingConfigParameterException(BordersGraphStorageBuilder.class, "openborders");

            // Read the file containing all of the country border polygons
            this.cbReader = new CountryBordersReader(bordersFile, countryIdsFile, openBordersFile);
        }

        _storage = new BordersGraphStorage();
        return _storage;

    }

    /**
     * COverwrite the current reader with a custom built CountryBordersReader.
     *
     * @param cbr       The CountryBordersReader object to be used
     */
    public void setBordersBuilder(CountryBordersReader cbr) {
        this.cbReader = cbr;
    }

    @Override
    public void processWay(ReaderWay way) {
        LOGGER.warn("Borders requires geometry for the way!");
    }

    /**
     * Process a way read from the reader and determine whether it crosses a country border. If it does, then country
     * names are stored which identify the countries it crosses.
     *
     * @param way
     * @param coords
     */
    @Override
    public void processWay(ReaderWay way, Coordinate[] coords, HashMap<Integer, HashMap<String,String>> nodeTags) {
        // Process the way using the geometry provided
        // if we don't have the reader object, then we can't do anything
        if(cbReader != null) {
            String[] countries = findBorderCrossing(coords);

            // If we find that the length of countries is more than one, then it does cross a border
            if (countries.length > 1 && !countries[0].equals(countries[1])) {
                way.setTag("country1", countries[0]);
                way.setTag("country2", countries[1]);
            }
        }
    }

     /**
     * Method to process the edge and store it in the graph.<br/><br/>
     * <p>
     * It checks the way to see if it has start and end country tags (introduced in the processWay method) and then
      * determines the type of border crossing (1 for controlled and 2 for open)
     *
     * @param way  The OSM way obtained from the OSM reader. This way corresponds to the edge to be processed
     * @param edge The graph edge to be process
     */
    @Override
    public void processEdge(ReaderWay way, EdgeIteratorState edge) {
        // Make sure we actually have the storage initialised - if there were errors accessing the data then this could be the case
        if(_storage != null) {
            // If there is no border crossing then we set the edge value to be 0

            // First get the start and end countries - if either of these is empty, then there is no crossing
            if (way.hasTag("country1") && way.hasTag("country2")) {
                String startVal = way.getTag("country1");
                String endVal = way.getTag("country2");

                // Lookup values
                short start = 0, end = 0;

                try {
                    start = Short.parseShort(cbReader.getId(startVal));
                    end = Short.parseShort(cbReader.getId(endVal));
                } catch (NumberFormatException nfe) {
                    LOGGER.error("Error in lookup for ids " + startVal + " and " + endVal);
                }

                short type = (cbReader.isOpen(cbReader.getEngName(startVal), cbReader.getEngName(endVal))) ? (short) 2 : (short) 1;

                _storage.setEdgeValue(edge.getEdge(), type, start, end);
            } else {
                _storage.setEdgeValue(edge.getEdge(), (short) 0, (short) 0, (short) 0);
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
        return "Borders";
    }

    /**
     * Method to identify the countries that a way is found in. basically iterates over the country boundaries read from
     * the file adn then does geometric calculations to identify wheich country each node of the way is in.
     *
     * @param coords    Coordinates of the way
     * @return          An array of strings representing the countries that nodes are found in. If the way is only
     *                  found in one country, then only one name is returned.
     */
    public String[] findBorderCrossing(Coordinate[] coords) {

        ArrayList<CountryBordersPolygon> countries = new ArrayList<>();

        boolean hasInternational = false;
        boolean overlap = false;

        // Go through the points of the linestring and check what country they are in
        int lsLen = coords.length;
        if(lsLen > 1) {
            for(int i=0; i<lsLen; i++) {
                // Make sure that it is a valid point
                Coordinate c = coords[i];
                if(!Double.isNaN(c.x) && !Double.isNaN(c.y)) {
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
                    if(cnts.length == 0)
                        hasInternational = true;
                }
            }
        }
        // Now get the definite ones that are contained - though this involves another iteration, it will be quicker
        // than the linestring check in the next stage
        if(countries.size() > 1) {
            ArrayList<CountryBordersPolygon> temp = new ArrayList<>();

            for(int i=0; i<lsLen; i++) {
                // Loop through each point of the line and check whcih countries it is in. This should only be 1 unless
                // there is an overlap
                Coordinate c = coords[i];
                if(!Double.isNaN(c.x) && !Double.isNaN(c.y)) {
                    // Check each country candidate
                    boolean found = false;
                    int countriesFound = 0;

                    for(CountryBordersPolygon cbp : countries) {
                        if (cbp.inArea(c)) {
                            found = true;
                            countriesFound++;
                            if(!temp.contains(cbp)) {
                                // At this point we only want to add countries that are not present. Basically, if a
                                // boundary polygon of the same name is in the list, don't add the country again
                                temp.add(cbp);
                            }
                        }
                    }

                    if(countriesFound > 1) {
                        overlap = true;
                    }

                    if(!found) {
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
        if(countries.size() > 1 && overlap) {
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

            if(!crosses) {
                // We want to indicate that it is in the same country, so to do that we only pass one country back
                CountryBordersPolygon cp = countries.get(0);
                countries.clear();
                countries.add(cp);
            }
        }

        // Now get the names of the countries
        ArrayList<String> names = new ArrayList<>();
        for(int i=0; i<countries.size(); i++) {
            if (!names.contains(countries.get(i).getName()))
                names.add(countries.get(i).getName());
        }

        // If there is an international point and at least one country name, then we know it is a border
        if(hasInternational && countries.size() > 0) {
            names.add(CountryBordersReader.INTERNATIONAL_NAME);
        }

        return names.toArray(new String[names.size()]);
    }
}
