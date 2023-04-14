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
package org.heigit.ors.routing.graphhopper.extensions.reader.ubertraffic;

import com.carrotsearch.hppc.LongObjectHashMap;
import com.graphhopper.util.DistanceCalcEarth;
import org.apache.log4j.Logger;
import org.heigit.ors.util.CSVUtility;

import java.util.List;

public class UberTrafficReader {
    private static final Logger LOGGER = Logger.getLogger(UberTrafficReader.class);

    private boolean isInitialized;
    private String uberMovementFile;

    private static UberTrafficReader currentInstance;

    DistanceCalcEarth distCalc;

    /**
     * Constructor - the user must explicitly pass information
     */
    public UberTrafficReader(String uberMovementFile) {
        this.uberMovementFile = uberMovementFile;
        this.distCalc = new DistanceCalcEarth();
        currentInstance = this;
        isInitialized = false;
    }

    public UberTrafficData readAndProcessData() {
        UberTrafficData uberTrafficData = new UberTrafficData();
        if (uberMovementFile.equals(""))
            return uberTrafficData;
        LongObjectHashMap<UberTrafficPattern> patterns = readPatterns();

        uberTrafficData.setPatternsByOsmId(patterns);


        LOGGER.info("Uber patterns pre-processed");

        this.isInitialized = true;

        return uberTrafficData;
    }

    public boolean isInitialized() {
        return this.isInitialized;
    }

    private LongObjectHashMap<UberTrafficPattern> readPatterns() {
        List<List<String>> patterns = CSVUtility.readFile(uberMovementFile, false);
        LongObjectHashMap<UberTrafficPattern> uberTrafficPatterns = new LongObjectHashMap<>();
        List<String> header = patterns.remove(0);
        int yearIndex = header.indexOf("year");
        int quarterIndex = header.indexOf("quarter");
        int hourOfDayIndex = header.indexOf("hour_of_day");
        int osmWayIdIndex = header.indexOf("osm_way_id");
        int osmStartNodeIdIndex = header.indexOf("osm_start_node_id");
        int osmEndNodeIdIndex = header.indexOf("osm_end_node_id");
        int speedKphMeanIndex = header.indexOf("speed_kph_mean");
        int speekKphStddevIndex = header.indexOf("speed_kph_stddev");
        for (List<String> pattern : patterns) {
            int year = Integer.parseInt(pattern.get(yearIndex));
            int quarter = Integer.parseInt(pattern.get(quarterIndex));
            int hour_of_day = Integer.parseInt(pattern.get(hourOfDayIndex));
            long osm_way_id = Long.parseLong(pattern.get(osmWayIdIndex));
            long osm_start_node_id = Long.parseLong(pattern.get(osmStartNodeIdIndex));
            long osm_end_node_id = Long.parseLong(pattern.get(osmEndNodeIdIndex));
            float speed_kph_mean = Float.parseFloat(pattern.get(speedKphMeanIndex));
            float speed_kph_stdev = Float.parseFloat(pattern.get(speekKphStddevIndex));
            UberTrafficPattern  uberTrafficPattern = uberTrafficPatterns.get(osm_way_id);
            if (uberTrafficPattern == null) {
                uberTrafficPattern = new UberTrafficPattern(osm_way_id, UberTrafficEnums.PatternResolution.MINUTES_60);
            }

            uberTrafficPattern.addPattern(osm_start_node_id, osm_end_node_id, hour_of_day, speed_kph_mean);
            uberTrafficPatterns.put(osm_way_id, uberTrafficPattern);

        }
        return uberTrafficPatterns;
    }
}
