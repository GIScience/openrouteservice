/*
 *  Licensed to GIScience Research Group, Heidelberg University (GIScience)
 *
 *   http://www.giscience.uni-hd.de
 *   http://www.heigit.org
 *
 *  under one or more contributor license agreements. See the NOTICE file 
 *  distributed with this work for additional information regarding copyright 
 *  ownership. The GIScience licenses this file to you under the Apache License, 
 *  Version 2.0 (the "License"); you may not use this file except in compliance 
 *  with the License. You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package heigit.ors.routing.graphhopper.extensions.storages.builders;

import com.graphhopper.GraphHopper;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.storage.GraphExtension;
import com.graphhopper.util.EdgeIteratorState;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import heigit.ors.routing.graphhopper.extensions.reader.borders.CountryBordersPolygon;
import heigit.ors.routing.graphhopper.extensions.reader.borders.CountryBordersReader;
import heigit.ors.routing.graphhopper.extensions.storages.BordersGraphStorage;
import org.apache.log4j.Logger;

import java.util.ArrayList;

/**
 * Class for building the Borders graph extension that allows restricting routes regarding border crossings
 *
 * @author Adam Rousell
 */
public class BordersGraphStorageBuilder extends AbstractGraphStorageBuilder {
    final static Logger LOGGER = Logger.getLogger(BordersGraphStorageBuilder.class.getName());

    private BordersGraphStorage _storage;
    private CountryBordersReader cbReader;

    public BordersGraphStorageBuilder() {
        _storage = new BordersGraphStorage();
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

        // Read the border shapes from the file
        String bordersFile = _parameters.get("boundaries");
        String countryIdsFile = _parameters.get("ids");
        String openBordersFile = _parameters.get("openborders");
        // Read the file containing all of the country border polygons
        this.cbReader = new CountryBordersReader(bordersFile, countryIdsFile, openBordersFile);

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
     * @param ls
     */
    @Override
    public void processWay(ReaderWay way, LineString ls) {
        // Process the way using the geometry provided
        String[] countries = findBorderCrossing(ls);

        // If we find that the length of countries is more than one, then it does cross a border
        if(countries.length > 1) {
            way.setTag("country1", countries[0]);
            way.setTag("country2", countries[1]);
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
        // If there is no border crossing then we set the edge value to be 0

        // First get the start and end countries - if either of these is empty, then there is no crossing
        if(way.hasTag("country1") && way.hasTag("country2")) {
            String startVal = way.getTag("country1");
            String endVal = way.getTag("country2");

            // Lookup values
            short start = Short.parseShort(cbReader.getId(startVal));
            short end = Short.parseShort(cbReader.getId(endVal));

            short type = (cbReader.isOpen(cbReader.getEngName(startVal), cbReader.getEngName(endVal))) ? (short)2 : (short)1;

            _storage.setEdgeValue(edge.getEdge(), type, start, end);
        }
        else {
            _storage.setEdgeValue(edge.getEdge(), (short)0, (short)0, (short)0);
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
     * @param ls        LineString representing the way
     * @return          An array of strings representing the countries that nodes are found in. If the way is only
     *                  found in one country, then only one name is returned.
     */
    public String[] findBorderCrossing(LineString ls) {

        ArrayList<CountryBordersPolygon> countries = new ArrayList<>();

        // Go through the points of the linestring and check what country they are in
        int lsLen = ls.getNumPoints();
        if(lsLen > 1) {
            for(int i=0; i<lsLen; i++) {
                Point p = ls.getPointN(i);
                // Make sure that it is a valid point

                if(!Double.isNaN(p.getCoordinate().x) && !Double.isNaN(p.getCoordinate().y)) {
                    CountryBordersPolygon[] cnts = cbReader.getCandidateCountry(p);
                    for (CountryBordersPolygon cbp : cnts) {
                        // This check is for the bbox as that is quickest for detecting if there is the possibility of a
                        // crossing
                        if (!countries.contains(cbp)) {
                            countries.add(cbp);
                        }
                    }
                }
            }
        }

        // Now get the definite ones that are contained - though this involves another iteration, it will be quicker
        // than the linestring check in the next stage
        if(countries.size() > 1) {
            ArrayList<CountryBordersPolygon> temp = new ArrayList<>();
            for(CountryBordersPolygon cbp : countries) {
                for(int i=0; i<lsLen; i++) {
                    Point p = ls.getPointN(i);
                    if(!Double.isNaN(p.getCoordinate().x) && !Double.isNaN(p.getCoordinate().y)) {
                        if (cbp.inArea(ls.getPointN(i).getCoordinate()) && !temp.contains(cbp)) {
                            temp.add(cbp);
                        }
                    }
                }
            }

            // Replace the arraylist
            countries = temp;
        }


        // Now we have a list of all the countries that the nodes are in - if this is more than one it is likely it is
        // crossing a border, but not certain as in some disputed areas, countries overlap and so it may not cross any
        // border
        if(countries.size() > 1) {
            boolean crosses = false;
            // Check for actually crossing a border
            for(CountryBordersPolygon cp : countries) {
                if(cp.crossesBoundary(ls)) {
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
        String[] names = new String[countries.size()];
        for(int i=0; i<countries.size(); i++) {
            names[i] = countries.get(i).getName();
        }
        return names;
    }
}
