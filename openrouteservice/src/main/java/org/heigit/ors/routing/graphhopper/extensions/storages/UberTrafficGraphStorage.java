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
package org.heigit.ors.routing.graphhopper.extensions.storages;

import com.graphhopper.storage.*;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Graph storage class for the Border Restriction routing
 */
public class UberTrafficGraphStorage extends AbstractTrafficGraphStorage {

    /* pointer for road type */
    private static final int LOCATION_TRAFFIC_PRIORITY = 0;         // byte location of the from UBER traffic data
    private static final int LOCATION_TRAFFIC_START = 1;         // byte start location of the traffic data
    private static final int FORWARD_OFFSET = 0;
    public static final int DAILY_TRAFFIC_PATTERNS_BYTE_COUNT = 24; // The pattern value is transferred to mph to allow byte storage. 1 byte * 24 hours
    private static final int LOCATION_FORWARD_TRAFFIC_PRIORITY = LOCATION_TRAFFIC_PRIORITY + FORWARD_OFFSET;         // byte location of the from traffic link id
    private static final int LOCATION_FORWARD_TRAFFIC = LOCATION_TRAFFIC_START + FORWARD_OFFSET;         // byte location of the from traffic link id
    private static final int LOCATION_FORWARD_TRAFFIC_MAXSPEED = DAILY_TRAFFIC_PATTERNS_BYTE_COUNT + LOCATION_FORWARD_TRAFFIC; // byte location of the daily maximum traffic speed
    private static final int BACKWARD_OFFSET = 26;
    private static final int LOCATION_BACKWARD_TRAFFIC_PRIORITY = LOCATION_TRAFFIC_PRIORITY + BACKWARD_OFFSET;         // byte location of the to traffic link id
    private static final int LOCATION_BACKWARD_TRAFFIC = LOCATION_TRAFFIC_START + BACKWARD_OFFSET;         // byte location of the to traffic link id
    private static final int LOCATION_BACKWARD_TRAFFIC_MAXSPEED = DAILY_TRAFFIC_PATTERNS_BYTE_COUNT + LOCATION_BACKWARD_TRAFFIC; // byte location of the daily maximum traffic speed


    private DataAccess orsEdgesTrafficLookup; // RAMDataAccess

    private int uberEdgeLookupEntryBytes;
    private int edgesCount; // number of edges with custom values
    private int maxEdgeId; // highest edge id for which traffic data is available

    public UberTrafficGraphStorage() {
        int edgeEntryIndex = 0;
        uberEdgeLookupEntryBytes = edgeEntryIndex + LOCATION_BACKWARD_TRAFFIC_MAXSPEED;
        edgesCount = 0;
        maxEdgeId = 0;
    }

    /**
     * Store the linkID <-> patternId matches for all weekdays (Monday - Sunday).</-><br/><br/>
     * <p>
     * This method takes the ID of the traffic edge and adds the weekday specific pattern Id to the lookup.
     *
     * @param edgeId   Id of the edge to store traffic data for.
     * @param baseNode Bade id to matchc the pattern on.
     * @param adjNode
     * @param patterns
     **/
    public void setEdgeIdTrafficPatternLookup(int edgeId, int baseNode, int adjNode, byte[] patterns, int priority) {
        if (priority > 254) {
            System.out.println();
        }
        priority = priority > 254 ? 255 : priority;

        if (edgeId > maxEdgeId)
            maxEdgeId = edgeId;
        ensureEdgesTrafficLinkLookupIndex(maxEdgeId);

        if (patterns.length <= 0)
            return;

        int lastPriority = getEdgeIdSpeedValuePriority(edgeId, baseNode, adjNode);

        for (int i = 0; i < patterns.length; i++) {
            // Priority is used to decide the following cases:
            // 1. No prior value was assigned -> The new value is assigned.
            // 2. A prior value was assigned -> The value will be reassigned if the new priority is higher and the value not 0.
            short speedValue = patterns[i];
            int currentSpeed = getTrafficSpeed(edgeId, baseNode, adjNode, i);
            if (currentSpeed <= 0) {
                setTrafficSpeed(edgeId, baseNode, adjNode, speedValue, i, (byte) priority);
            } else if (priority > lastPriority) {
                int testSpeed = getTrafficSpeed(46, 10389, 10381, 0);
                assert testSpeed == 59 || testSpeed == 0;
                setTrafficSpeed(edgeId, baseNode, adjNode, speedValue, i, (byte) priority);
            }
        }

        // Keep record of edges count with traffic data.
        edgesCount++;
    }

    /**
     * Store the traffic pattern for each 15 minutes for 24 hours.</-><br/><br/>
     * <p>
     * This method takes the pattern Id and adds the speed value to the right quarter to the right hour.
     *
     * @param edgeId     Id of the traffic pattern.
     * @param baseNode
     * @param adjNode
     * @param speedValue Speed value in mph or kph.
     * @param hour       Hour to add the speed value to.
     * @param priority
     **/
    private void setTrafficSpeed(int edgeId, int baseNode, int adjNode, short speedValue, int hour, byte priority) {
        long edgePointer = (long) edgeId * uberEdgeLookupEntryBytes;
        //edgePointer = 1;
        ensureEdgesTrafficLinkLookupIndex(edgeId);
        speedValue = speedValue > 255 ? 255 : speedValue;
        if (baseNode < adjNode) {
            orsEdgesTrafficLookup.setBytes(edgePointer + LOCATION_FORWARD_TRAFFIC + hour, new byte[]{(byte) speedValue}, 1);
            orsEdgesTrafficLookup.setBytes(edgePointer + LOCATION_FORWARD_TRAFFIC_PRIORITY, new byte[]{priority}, 1);
        } else {
            orsEdgesTrafficLookup.setBytes(edgePointer + LOCATION_BACKWARD_TRAFFIC + hour, new byte[]{(byte) speedValue}, 1);
            orsEdgesTrafficLookup.setBytes(edgePointer + LOCATION_BACKWARD_TRAFFIC_PRIORITY, new byte[]{priority}, 1);
        }
    }

    /**
     * Store maximum speed value encountered in a daily traffic pattern
     **/
    private void setMaxTrafficSpeed(int edgeId, int baseNode, int adjNode, short speedValue) {
        long patternPointer = (long) edgeId * uberEdgeLookupEntryBytes;
        //patternPointer = 1;
        ensureEdgesTrafficLinkLookupIndex(edgeId);
        speedValue = speedValue > 255 ? 255 : speedValue;
        int lastMaxTrafficSpeed = getMaxTrafficSpeed(edgeId, baseNode, adjNode);
        if (lastMaxTrafficSpeed < speedValue) {
            if (baseNode < adjNode) {
                orsEdgesTrafficLookup.setBytes(patternPointer + LOCATION_FORWARD_TRAFFIC_MAXSPEED, new byte[]{(byte) speedValue}, 1);
            } else {
                orsEdgesTrafficLookup.setBytes(patternPointer + LOCATION_BACKWARD_TRAFFIC_MAXSPEED, new byte[]{(byte) speedValue}, 1);
            }
        }
    }

    private boolean invalidEdgeId(int edgeId) {
        return (edgeId > maxEdgeId);
    }

    /**
     * Receive the last saved traffic information priority for a certain ors edge id with its direction.</-><br/><br/>
     * <p>
     * This method returns the last stored priority. This is a custom value to decide if present weekly traffic pattern can be overwritten for a certain ors edge.
     * Traffic edges don't necessarily have the same length as the ors edges.
     * Therefore it might happen in the match making process that the same ors edge is matched multiple times on different traffic edges.
     * Since not all of those matches represent the same length of the ors edge it can be assumed that the larger the matches are, the more accurate the result displays reality.
     * <p>
     * e.g. This function can be used to retrieve the stored length of the last ors edge <-> traffic edge match.
     *
     * @param edgeId   Internal ID of the graph edge.
     * @param baseNode Value of the base Node of the edge.
     * @param adjNode  Value of the adjacent Node of the edge.
     **/
    public int getEdgeIdSpeedValuePriority(int edgeId, int baseNode, int adjNode) {
        long edgePointer = (long) edgeId * uberEdgeLookupEntryBytes;
        byte[] priority = new byte[1];
        if (baseNode < adjNode)
            orsEdgesTrafficLookup.getBytes(edgePointer + LOCATION_FORWARD_TRAFFIC_PRIORITY, priority, 1);
        else
            orsEdgesTrafficLookup.getBytes(edgePointer + LOCATION_BACKWARD_TRAFFIC_PRIORITY, priority, 1);
        return Byte.toUnsignedInt(priority[0]);
    }

    /**
     * Receive the correct traffic speed value for the desired hour and edge segment</-><br/><br/>
     * <p>
     * This method returns the traffic speed for one hour in a byte[].
     *
     * @param edgeId    Edge id
     * @param baseNode  Start node id
     * @param adjNode   End node id
     * @param hour      Hour of day
     **/
    public int getTrafficSpeed(int edgeId, int baseNode, int adjNode, int hour) {
        byte[] values = new byte[1];
        long edgePointer = (long) edgeId * uberEdgeLookupEntryBytes;
        //edgePointer = 1;
        if (baseNode < adjNode) {
            orsEdgesTrafficLookup.getBytes(edgePointer + LOCATION_FORWARD_TRAFFIC + hour, values, 1);
        } else {
            orsEdgesTrafficLookup.getBytes(edgePointer + LOCATION_BACKWARD_TRAFFIC + hour, values, 1);
        }
        return Byte.toUnsignedInt(values[0]) <= 0 ? -1 : Byte.toUnsignedInt(values[0]);
    }

    /**
     * Maximum speed value encountered in a daily traffic pattern
     **/
    private int getMaxTrafficSpeed(int edgeId, int baseNode, int adjNode) {
        long patternPointer = (long) edgeId * uberEdgeLookupEntryBytes;
        ensureEdgesTrafficLinkLookupIndex(edgeId);
        byte[] value = new byte[1];
        if (baseNode < adjNode) {
            orsEdgesTrafficLookup.getBytes(patternPointer + LOCATION_FORWARD_TRAFFIC_MAXSPEED, value, 1);
        } else {
            orsEdgesTrafficLookup.getBytes(patternPointer + LOCATION_BACKWARD_TRAFFIC_MAXSPEED, value, 1);
        }
        return Byte.toUnsignedInt(value[0]);
    }

    /**
     * Get the specified custom value of the edge that was assigned to it in the setValueEdge method<br/><br/>
     * <p>
     * The method takes an edgeId, the base and adjacent Node, to define its direction and unix time,
     * to find the appropriate traffic information.
     * Only the weekday, hour, minute are taken into consideration since the traffic information are generalized to that resolution at the moment.
     * <p>
     * ## Time decoding ##
     * The unix time is decoded using GMT+1 for german local time.
     * For the time being we will use the german UTC until more traffic data comes in.
     * <p>
     * <p>
     * ## TODO's ##
     * - enhance internal time encoding and harmonize it in the whole ORS backend when more traffic data comes in.
     *
     * @param edgeId           Internal ID of the edge to get values for.
     * @param baseNode         The baseNode of the edge to define the direction.
     * @param adjNode          The adjNode of the edge to define the direction.
     * @param unixMilliSeconds Time in unix milliseconds.
     * @return Returns the speed value in kph. If no value is found -1 is returned.
     */
    public int getSpeedValue(int edgeId, int baseNode, int adjNode, long unixMilliSeconds, int timeZoneOffset) {
        if (invalidEdgeId(edgeId))
            // Question: does this mean that if no traffic speed value is available, a speed value of -1 km/h one will be used?
            return -1;
        Calendar calendarDate = Calendar.getInstance(TimeZone.getTimeZone("GMT+" + timeZoneOffset));
        calendarDate.setTimeInMillis(unixMilliSeconds);
        int calendarWeekDay = calendarDate.get(Calendar.DAY_OF_WEEK);
        int hour = calendarDate.get(Calendar.HOUR_OF_DAY);
        try {
            return getTrafficSpeed(edgeId, baseNode, adjNode, hour);
        } catch (Exception err) {
            System.out.println("Error getting speeds for edgeId" + edgeId + "baseNode: " + baseNode + "adjNode: " + adjNode + "hour: " + hour + " Error: " + err);
        }
        // Question: does this mean that if no traffic speed value is available, a traffic speed value of -1 km/h one will be used?
        return -1;
    }

    /**
     * Maximum traffic speed value across the whole day
     **/
    public int getMaxSpeedValue(int edgeId, int baseNode, int adjNode) {
        if (invalidEdgeId(edgeId))
            return 0;
        byte[] value = new byte[1];
        // TODO revise
        long edgePointer = (long) edgeId * uberEdgeLookupEntryBytes;
        int directionOffset = (baseNode < adjNode) ? LOCATION_FORWARD_TRAFFIC_MAXSPEED : LOCATION_BACKWARD_TRAFFIC_MAXSPEED;
        orsEdgesTrafficLookup.getBytes(edgePointer + directionOffset, value, 1);
        return Byte.toUnsignedInt(value[0]);
    }

    public boolean hasTrafficSpeed(int edgeId, int baseNode, int adjNode) {
        // Traffic patters are stored for all weekdays so it should be enough to check only for one of them
//        int patternId = getEdgeIdTrafficPatternLookup(edgeId, baseNode, adjNode, HereTrafficEnums.WeekDay.MONDAY);
//        // Pattern IDs start from 1 so 0 is assumed to mean no pattern is assigned
//        return patternId > 0;
        return true;
    }

    public void ensureEdgesTrafficLinkLookupIndex(int edgeId) {
        orsEdgesTrafficLookup.ensureCapacity(((long) edgeId + 1) * uberEdgeLookupEntryBytes);
    }

    public boolean isMatched() {
        return orsEdgesTrafficLookup.getHeader(0) == 1;
    }

    public void setMatched() {
        orsEdgesTrafficLookup.setHeader(0, 1);
    }

    public int getEdgesCount() {
        return edgesCount;
    }


    /**
     * initializes the extended storage by giving the base graph
     *
     * @param graph Provide the graph object.
     * @param dir   The directory where the graph will be initialized.
     */
    @Override
    public void init(Graph graph, Directory dir) {
        if (edgesCount > 0)
            throw new AssertionError("The ORS storage must be initialized only once.");

        this.orsEdgesTrafficLookup = dir.find("ext_uber_traffic_edges_lookup");
    }

    /**
     * initializes the extended storage to be empty - required for testing purposes as the ext_storage aren't created
     * at the time tests are run
     */
    public void init() {
        if (edgesCount > 0)
            throw new AssertionError("The ORS storage must be initialized only once.");
        Directory d = new RAMDirectory();
        this.orsEdgesTrafficLookup = d.find("");
    }


    /**
     * @return true if successfully loaded from persistent storage.
     */
    @Override
    public boolean loadExisting() {
        if (!orsEdgesTrafficLookup.loadExisting())
            throw new IllegalStateException("Unable to load storage 'ext_traffic_edges_traffic_lookup'. corrupt file or directory?");
        uberEdgeLookupEntryBytes = orsEdgesTrafficLookup.getHeader(5);
        maxEdgeId = orsEdgesTrafficLookup.getHeader(9);
        edgesCount = orsEdgesTrafficLookup.getHeader(13);
        return true;
    }

    /**
     * Creates the underlying storage. First operation if it cannot be loaded.
     *
     * @param initBytes Init size in bytes.
     */
    @Override
    public GraphExtension create(long initBytes) {
        orsEdgesTrafficLookup.create(initBytes * uberEdgeLookupEntryBytes);
        return this;
    }

    /**
     * This method makes sure that the underlying data is written to the storage. Keep in mind that
     * a disc normally has an IO cache so that flush() is (less) probably not save against power
     * loses.
     */
    @Override
    public void flush() {
        orsEdgesTrafficLookup.setHeader(5, uberEdgeLookupEntryBytes);
        orsEdgesTrafficLookup.setHeader(9, maxEdgeId);
        orsEdgesTrafficLookup.setHeader(13, edgesCount);
        orsEdgesTrafficLookup.flush();
    }

    /**
     * This method makes sure that the underlying used resources are released. WARNING: it does NOT
     * flush on close!
     */
    @Override
    public void close() {
        orsEdgesTrafficLookup.close();
    }

    @Override
    public boolean isClosed() {
        return false;
    }

    /**
     * @return the allocated storage size in bytes
     */
    @Override
    public long getCapacity() {
        return orsEdgesTrafficLookup.getCapacity();
    }

    /**
     * This method finds as stores for each edge the maximum forward and backward traffic speed across the whole week.
     */
    public void setMaxTrafficSpeeds() {
        for (int edgeId = 0; edgeId <= maxEdgeId; edgeId++) {
            long edgePointer = (long) edgeId * uberEdgeLookupEntryBytes;
            byte[] priorityForward = new byte[1];
            byte[] priorityBackward = new byte[1];
            orsEdgesTrafficLookup.getBytes(edgePointer + LOCATION_FORWARD_TRAFFIC_PRIORITY, priorityForward, 1);
            orsEdgesTrafficLookup.getBytes(edgePointer + LOCATION_BACKWARD_TRAFFIC_PRIORITY, priorityBackward, 1);
            byte[] speedValue = new byte[1];
            byte[] currentMaxValue = new byte[1];
            if (priorityForward[0] > 0) {
                orsEdgesTrafficLookup.getBytes(edgePointer + LOCATION_FORWARD_TRAFFIC_MAXSPEED, currentMaxValue, 1);
                for (int i = 0; i < DAILY_TRAFFIC_PATTERNS_BYTE_COUNT; i++) {
                    orsEdgesTrafficLookup.getBytes(edgePointer + LOCATION_FORWARD_TRAFFIC + i, speedValue, 1);
                    if (currentMaxValue[0] < speedValue[0]) {
                        currentMaxValue[0] = speedValue[0];
                        orsEdgesTrafficLookup.setBytes(edgePointer + LOCATION_FORWARD_TRAFFIC + i, currentMaxValue, 1);
                    }
                }
            }
            if (priorityBackward[0] > 0) {
                orsEdgesTrafficLookup.getBytes(edgePointer + LOCATION_BACKWARD_TRAFFIC_MAXSPEED, currentMaxValue, 1);
                for (int i = 0; i < DAILY_TRAFFIC_PATTERNS_BYTE_COUNT; i++) {
                    orsEdgesTrafficLookup.getBytes(edgePointer + LOCATION_BACKWARD_TRAFFIC + i, speedValue, 1);
                    if (currentMaxValue[0] < speedValue[0]) {
                        currentMaxValue[0] = speedValue[0];
                        orsEdgesTrafficLookup.setBytes(edgePointer + LOCATION_BACKWARD_TRAFFIC + i, currentMaxValue, 1);
                    }
                }
            }
        }
    }
}
