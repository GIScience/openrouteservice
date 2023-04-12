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
package org.heigit.ors.routing.graphhopper.extensions.reader.borders;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.heigit.ors.geojson.GeometryJSON;
import org.heigit.ors.util.CSVUtility;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.*;

public class CountryBordersReader {
    private static final Logger LOGGER = Logger.getLogger(CountryBordersReader.class);

    public static final String INTERNATIONAL_NAME = "INTERNATIONAL";
    public static final String INTERNATIONAL_ID = "-1";
    public static final String KEY_PROPERTIES = "properties";

    private final String borderFile;
    private final String nameField;
    private final String hierarchyIdField;

    private final String idsPath;
    private final String openPath;

    private final HashMap<String, CountryInfo> ids = new HashMap<>();
    private final HashMap<String, ArrayList<String>> openBorders = new HashMap<>();
    private final HashMap<String, Integer> isoCodes = new HashMap<>();

    private final HashMap<Long, CountryBordersHierarchy> hierarchies = new HashMap<>();

    // Package scoped for testing purposes
    static CountryBordersReader currentInstance = null;

    /**
     * Empty constructor which does not read any data - the user must explicitly pass information
     */
    public CountryBordersReader() {
        borderFile = "";
        nameField = "name";
        hierarchyIdField = "hierarchy";
        idsPath = "";
        openPath = "";

        currentInstance = this;
    }

    /**
     * Create a CountryBordersReader object and read in data for borders, ids and open borders.
     *
     * @param filepath      Path to the borders (polygon) data
     * @param idsPath       Path to a csv file containing numeric identifiers for countries (and english name)
     * @param openPath      Path to a csv file containing pairs of country names which have open borders
     */
    public CountryBordersReader(String filepath, String idsPath, String openPath) throws IOException {
        borderFile = filepath;
        nameField = "name";
        hierarchyIdField = "hierarchy";

        this.idsPath = idsPath;
        this.openPath = openPath;

        try {
            JSONObject data = readBordersData();
            LOGGER.info("Border geometries read");

            createGeometries(data);

            readIds();
            LOGGER.info("Border ids data read");

            readOpenBorders();
            LOGGER.info("Border openness data read");
        } catch (IOException ioe) {
            // Problem with reading the data
            LOGGER.error("Could not access file(s) required for border crossing analysis");
            throw ioe;
        }
        currentInstance = this;
    }

    // for test mocks
    public void addHierarchy(Long id, CountryBordersHierarchy hierarchy) {
        if(!hierarchies.containsKey(id)) {
            hierarchies.put(id, hierarchy);
        }
    }

    // for test mocks
    public void addId(String id, String localName, String englishName, String cca2, String cca3) {
        if(!ids.containsKey(localName)) {
            ids.put(localName, new CountryInfo(id, localName, englishName));
            isoCodes.put(cca2.trim().toUpperCase(), Integer.parseInt(id));
            isoCodes.put(cca3.trim().toUpperCase(), Integer.parseInt(id));
        }
    }

    /**
     * Add an open border entry to the list of open borders. An entry for both directions will be created if it does not
     * alread exist (i.e. passing Germany & France will result in two entries - Germany->France and France->Germany).
     *
     * @param country1
     * @param country2
     */
    public void addOpenBorder(String country1, String country2) {
        if(openBorders.containsKey(country1)) {
            // The key exists, so now add the second country if it is not present
            if(!openBorders.get(country1).contains(country2)) {
                openBorders.get(country1).add(country2);
            }
        } else {
            ArrayList<String> c2 = new ArrayList<>();
            c2.add(country2);
            openBorders.put(country1, c2);
        }

        if(openBorders.containsKey(country2)) {// The key exists, so now add the second country if it is not present
            if(!openBorders.get(country2).contains(country1)) {
                openBorders.get(country2).add(country1);
            }
        } else {
            ArrayList<String> c1 = new ArrayList<>();
            c1.add(country1);
            openBorders.put(country2, c1);
        }
    }

    /**
     * Method to read the geometries from a GeoJSON file that represent the boundaries of different countries. Ideally
     * it should be written using many small objects split into hierarchies.
     *
     * If the file is a .tar.gz format, it will decompress it and then store the reulting data to be read into the
     * JSON object.
     *
     * @return      A (Geo)JSON object representing the contents of the file
     */
    private JSONObject readBordersData() throws IOException {
        String data = "";

        BufferedReader buf = null;
        TarArchiveInputStream tis = null;
        try (InputStream is = new FileInputStream(borderFile)) {
            if(borderFile.endsWith(".tar.gz")) {
                // We are working with a compressed file
                tis = new TarArchiveInputStream(
                        new GzipCompressorInputStream(
                                new BufferedInputStream(is)
                        )
                );

                TarArchiveEntry entry;
                StringBuilder sb = new StringBuilder();

                while((entry = tis.getNextTarEntry()) != null) {
                    if(!entry.isDirectory()) {
                        byte[] bytes = new byte[(int) entry.getSize()];
                        while (tis.read(bytes) > 0)
                            sb.append(new String(bytes));
                    }
                }
                data = sb.toString();
            } else {
                // Assume a normal file so read line by line

                buf = new BufferedReader(new InputStreamReader(is));

                String line = "";
                StringBuilder sb = new StringBuilder();

                while ((line = buf.readLine()) != null) {
                    sb.append(line);
                }

                data = sb.toString();
            }
        } catch (IOException ioe) {
            LOGGER.warn("Cannot access borders file!");
            throw ioe;
        } finally {
            try {
                if(tis != null)
                    tis.close();
                if(buf != null)
                    buf.close();
            } catch (IOException ioe) {
                LOGGER.warn("Error closing file reader buffers!");
            }
        }

        return new JSONObject(data);
    }

    /**
     * Generate geometries from a GeoJSON object. These CountryBordersPolygons are stored in CountryBordersHierarchy
     * objects to speed up the searching process.
     *
     * @param json
     */
    private void createGeometries(JSONObject json) {
        JSONArray features = json.getJSONArray("features");

        int objectCount = 0;
        int hierarchyCount = 0;

        int len = features.length();

        for(int i=0; i<len; i++) {
            try {
                JSONObject obj = features.getJSONObject(i);
                Geometry geom = GeometryJSON.parse(obj.getJSONObject("geometry"));

                // Also need the id of the country and its hierarchy id
                String id = obj.getJSONObject(KEY_PROPERTIES).getString(nameField);

                Long hId = -1L;

                // If there is no hierarchy info, then we set the id of the hierarchy to be a default of 1
                if(obj.getJSONObject(KEY_PROPERTIES).has(hierarchyIdField))
                    hId = obj.getJSONObject(KEY_PROPERTIES).getLong(hierarchyIdField);

                // Create the borders object
                CountryBordersPolygon c = new CountryBordersPolygon(id, geom);

                // add to the hierarchy
                if(c != null) {
                    if(!hierarchies.containsKey(hId)) {
                        hierarchies.put(hId, new CountryBordersHierarchy());
                        hierarchyCount++;
                    }

                    hierarchies.get(hId).add(c);
                    objectCount++;
                }

            } catch (Exception e) {
                LOGGER.warn("Error reading country polygon from borders file!" + e.getMessage());
            }
        }

        LOGGER.info(objectCount + " countries read in " + hierarchyCount + " hiearchies");
    }

    /**
     * Method for getting a list of country objects that the given point can be found within. This could be more than
     * one if the point is found in overlapping regions.
     *
     * @param c     The point that you want to know which country is in
     * @return      An array of CountryBorderPolygons that the point is within the geometry of.
     */
    public CountryBordersPolygon[] getCountry(Coordinate c) {
        ArrayList<CountryBordersPolygon> countries = new ArrayList<>();
        Iterator it = hierarchies.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<Long, CountryBordersHierarchy> pair = (Map.Entry)it.next();
            CountryBordersHierarchy h =  pair.getValue();
            if(h.inBbox(c)) {
                // Now need to check the countries
                List<CountryBordersPolygon> ps = h.getPolygons();
                for(CountryBordersPolygon cp : ps) {
                    if(cp.inBbox(c) && cp.inArea(c)) {
                        countries.add(cp);
                    }
                }
            }
        }

        return countries.toArray(new CountryBordersPolygon[countries.size()]);
    }

    /**
     * Method for getting a list of country objects that the given point COULD be found within. This could be more than
     * one if the point is found in overlapping regions. This tests against bounding boxes, and so the countries
     * returned may not actually surround the point. The method should be used to get a quick approximation as to
     * whether the country is a candidate for containing the point.
     *
     * @param c     The point that you want to know which country is in
     * @return      An array of CountryBorderPolygons that the point is within the geometry of.
     */
    public CountryBordersPolygon[] getCandidateCountry(Coordinate c) {
        ArrayList<CountryBordersPolygon> countries = new ArrayList<>();
        Iterator it = hierarchies.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<Long, CountryBordersHierarchy> pair = (Map.Entry)it.next();
            CountryBordersHierarchy h =  pair.getValue();
            if(h.inBbox(c)) {
                // Now need to check the countries
                List<CountryBordersPolygon> ps = h.getPolygons();
                for(CountryBordersPolygon cp : ps) {
                    if(cp.inBbox(c)) {
                        countries.add(cp);
                    }
                }
            }
        }

        return countries.toArray(new CountryBordersPolygon[countries.size()]);
    }

    /**
     * Get the unique identifier of the country (read from a CSV file in the constructor)
     *
     * @param name      The local name of the country
     * @return          The unique identifier
     */
    public String getId(String name) {
        if(name.equals(INTERNATIONAL_NAME))
            return INTERNATIONAL_ID;

        if(ids.containsKey(name))
            return ids.get(name).id;
        else
            return "";
    }

    /**
     * Get the English name of the country (read from the id CSV)
     *
     * @param name      The local name of the country
     * @return          The English name of the country
     */
    public String getEngName(String name) {
        if(name.equals(INTERNATIONAL_NAME))
            return INTERNATIONAL_NAME;

        if(ids.containsKey(name))
            return ids.get(name).nameEng;
        else
            return "";
    }

    /**
     * Get whether a border between two specified countries is open or closed
     *
     * @param c1        The first country of the border (English name)
     * @param c2        The second country of the border (English name)
     * @return
     */
    public boolean isOpen(String c1, String c2) {
        if(openBorders.containsKey(c1)) {
            return openBorders.get(c1).contains(c2);
        } else if(openBorders.containsKey(c2))
            return openBorders.get(c2).contains(c1);

        return false;
    }

    /**
     * Get country ID by ISO 3166-1 Alpha-2 / Alpha-3 code. Static method that uses the last created instance of
     * this class. (Usually there should be only one instance for each server instance, and even if not, the
     * borders data used should be the same)
     *
     * @param code      The code to look up
     * @return          The ID of the country or 0 if not found
     */
    public static int getCountryIdByISOCode(String code) {
        return currentInstance != null ? currentInstance.isoCodes.getOrDefault(code.toUpperCase(), 0) : 0;
    }

    /**
     * Read information from the id csv. This includes a unique identifier, the local name of the country and the
     * English name of the country. Optionally reads ISO codes from column 4 and 5 (expecting them to contain the
     * Alpha-2 and Alpha-3 codes respectively) for the getCountryIdByISOCode method.
     */
    private void readIds() {
        // First read the csv file
        List<List<String>> data = CSVUtility.readFile(idsPath);

        // Loop through and store in the hashmap
        int countries = 0;
        int isoCCA2 = 0;
        int isoCCA3 = 0;
        for(List<String> col : data) {
            if(col.size() >= 3) {
                ids.put(col.get(1), new CountryInfo(col.get(0), col.get(1), col.get(2)));
                countries++;
            }
            int intID = 0;
            try {
                intID = Integer.parseInt(col.get(0));
            } catch (NumberFormatException e) {
                LOGGER.error("Invalid country ID " + col.get(0));
                continue;
            }
            if(col.size() >= 4 && !col.get(3).trim().isEmpty()) {
                isoCodes.put(col.get(3).trim().toUpperCase(), intID);
                isoCCA2++;
            }
            if(col.size() == 5 && !col.get(4).trim().isEmpty()) {
                isoCodes.put(col.get(4).trim().toUpperCase(), intID);
                isoCCA3++;
            }
        }
        LOGGER.info(countries + " country IDs read");
        if (isoCCA2 > 0) {
            if (isoCCA2 < countries) {
                LOGGER.warn((countries - isoCCA2)+ " countries have no ISO 3166-1 CCA2 code assigned.");
            } else {
                LOGGER.info("ISO 3166-1 CCA2 codes enabled for all countries");
            }
        }
        if (isoCCA3 > 0) {
            if (isoCCA3 < countries) {
                LOGGER.warn((countries - isoCCA3)+ " countries have no ISO 3166-1 CCA3 code assigned.");
            } else {
                LOGGER.info("ISO 3166-1 CCA3 codes enabled for all countries");
            }
        }
    }

    /**
     * Read information about whether a border between two countries is open. If a border is in the file, then it is
     * an open border.
     */
    private void readOpenBorders() {
        // First read the csv file
        List<List<String>> data = CSVUtility.readFile(openPath);

        // Loop through and store in the hashmap
        for(List<String> col : data) {
            if(col.size() == 2) {
                // See if there is already the start country
                if(!openBorders.containsKey(col.get(0))) {
                    openBorders.put(col.get(0), new ArrayList<>());
                }
                openBorders.get(col.get(0)).add(col.get(1));
            }
        }
    }

    /**
     * Holder class for storing information about a country read from the ids csv.
     */
    private class CountryInfo {
        private String id;
        private String name;
        private String nameEng;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getNameEng() {
            return nameEng;
        }

        public void setNameEng(String nameEng) {
            this.nameEng = nameEng;
        }

        public CountryInfo(String id, String name, String nameEng) {
            this.id = id;
            this.name = name;
            this.nameEng = nameEng;
        }
    }


}
