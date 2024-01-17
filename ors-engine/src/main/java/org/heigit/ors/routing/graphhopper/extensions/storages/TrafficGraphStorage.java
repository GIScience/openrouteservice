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
import com.graphhopper.util.GHUtility;
import org.heigit.ors.routing.graphhopper.extensions.reader.traffic.TrafficEnums;

import java.time.ZoneId;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Graph storage class for the Border Restriction routing
 */
public class TrafficGraphStorage implements GraphExtension {

    public enum Property {ROAD_TYPE}

    // road types
    public enum RoadTypes {
        IGNORE(0),
        MOTORWAY(1),
        MOTORWAY_LINK(2),
        MOTORROAD(3),
        TRUNK(4),
        TRUNK_LINK(5),
        PRIMARY(6),
        PRIMARY_LINK(7),
        SECONDARY(8),
        SECONDARY_LINK(9),
        TERTIARY(10),
        TERTIARY_LINK(11),
        RESIDENTIAL(12),
        UNCLASSIFIED(13);

        public final byte value;

        RoadTypes(int value) {
            this.value = (byte) value;
        }
    }

    /* pointer for road type */
    private static final byte LOCATION_ROAD_TYPE = 0;         // byte location of road type
    private static final int LOCATION_TRAFFIC_PRIORITY = 0;         // byte location of the from traffic link id
    private static final int LOCATION_TRAFFIC = 1;         // byte location of the traffic link id
    private static final int LOCATION_TRAFFIC_MAXSPEED = 15; // byte location of the weekly maximum traffic speed
    private static final int FORWARD_OFFSET = 0;
    private static final int LOCATION_FORWARD_TRAFFIC_PRIORITY = LOCATION_TRAFFIC_PRIORITY + FORWARD_OFFSET;         // byte location of the from traffic link id
    private static final int LOCATION_FORWARD_TRAFFIC = LOCATION_TRAFFIC + FORWARD_OFFSET;         // byte location of the from traffic link id
    private static final int BACKWARD_OFFSET = 16;
    private static final int LOCATION_BACKWARD_TRAFFIC_PRIORITY = LOCATION_TRAFFIC_PRIORITY + BACKWARD_OFFSET;         // byte location of the to traffic link id
    private static final int LOCATION_BACKWARD_TRAFFIC = LOCATION_TRAFFIC + BACKWARD_OFFSET;         // byte location of the to traffic link id

    public static final int PROPERTY_BYTE_COUNT = 1;
    public static final int LINK_LOOKUP_BYTE_COUNT = 32; // 2 bytes per day. 7 days per Week. One week forward. One week backwards. + 1 byte per week for value priority + fwd/bwd maxspeed = 2 * 7 * 2 + 2 + 2 = 32
    public static final int DAILY_TRAFFIC_PATTERNS_BYTE_COUNT = 96; // The pattern value is transferred to mph to allow byte storage. 1 byte * 4 (15min per Hour) * 24 hours
    public static final int MAX_DAILY_TRAFFIC_SPEED_BYTE_COUNT = 1; // Maximum over daily traffic pattern values

    private DataAccess orsEdgesProperties; // RAMDataAccess
    private DataAccess orsEdgesTrafficLinkLookup; // RAMDataAccess
    private DataAccess orsSpeedPatternLookup; // RAMDataAccess

    private ZoneId zoneId = ZoneId.of("Europe/Berlin");

    private int edgePropertyEntryBytes;
    private int edgeLinkLookupEntryBytes;
    private int patternEntryBytes;
    private int edgesCount; // number of edges with custom values
    private int maxEdgeId = 0; // highest edge id for which traffic data is available
    private int patternCount; // number of traffic patterns
    private final byte[] propertyValue;
    private final byte[] speedValue;
    private final byte[] priorityValue;

    public TrafficGraphStorage() {
        int edgeEntryIndex = 0;
        edgePropertyEntryBytes = edgeEntryIndex + PROPERTY_BYTE_COUNT;
        edgeLinkLookupEntryBytes = edgeEntryIndex + LINK_LOOKUP_BYTE_COUNT;
        patternEntryBytes = edgeEntryIndex + DAILY_TRAFFIC_PATTERNS_BYTE_COUNT + MAX_DAILY_TRAFFIC_SPEED_BYTE_COUNT;
        propertyValue = new byte[1];
        speedValue = new byte[1];
        priorityValue = new byte[1];
        edgesCount = 0;
    }

    public static byte getWayTypeFromString(String highway) {
        return switch (highway.toLowerCase()) {
            case "motorway" -> RoadTypes.MOTORWAY.value;
            case "motorway_link" -> RoadTypes.MOTORWAY_LINK.value;
            case "motorroad" -> RoadTypes.MOTORROAD.value;
            case "trunk" -> RoadTypes.TRUNK.value;
            case "trunk_link" -> RoadTypes.TRUNK_LINK.value;
            case "primary" -> RoadTypes.PRIMARY.value;
            case "primary_link" -> RoadTypes.PRIMARY_LINK.value;
            case "secondary" -> RoadTypes.SECONDARY.value;
            case "secondary_link" -> RoadTypes.SECONDARY_LINK.value;
            case "tertiary" -> RoadTypes.TERTIARY.value;
            case "tertiary_link" -> RoadTypes.TERTIARY_LINK.value;
            case "residential" -> RoadTypes.RESIDENTIAL.value;
            case "unclassified" -> RoadTypes.UNCLASSIFIED.value;
            default -> RoadTypes.IGNORE.value;
        };
    }

    /**
     * Set values to the edge based on custom properties<br/><br/>
     * <p>
     * This method takes the internal ID of the edge and adds the desired value e.g. the way type.
     *
     * @param edgeId Internal ID of the graph edge.
     * @param prop   Property indicating the location to store the value at.
     * @param value  Value containing the information that should be places on the index of the prop variable.
     **/
    public void setOrsRoadProperties(int edgeId, Property prop, short value) {
        edgesCount++;
        ensureEdgesPropertyIndex(edgeId);
        long edgePointer = (long) edgeId * edgePropertyEntryBytes;
        if (prop == Property.ROAD_TYPE)
            propertyValue[0] = (byte) value;
        orsEdgesProperties.setBytes(edgePointer + LOCATION_ROAD_TYPE, propertyValue, 1);
    }

    /**
     * Store the linkID <-> patternId matches for all weekdays (Monday - Sunday).</-><br/><br/>
     * <p>
     * This method takes the ID of the traffic edge and adds the weekday specific pattern Id to the lookup.
     *
     * @param edgeKey   Edge key
     * @param patternId Id of the traffic pattern.
     * @param weekday   Enum value for the weekday the traffic pattern Id refers to.
     **/
    public void setEdgeIdTrafficPatternLookup(int edgeKey, int patternId, TrafficEnums.WeekDay weekday, int priority) {
        if (patternId <= 0)
            return;

        priority = Math.min(priority, 255);
        patternId = patternId > 65535 ? 0 : patternId;
        int edgeId = GHUtility.getEdgeFromEdgeKey(edgeKey);
        boolean forward = isForward(edgeKey);
        if (edgeId > maxEdgeId)
            maxEdgeId = edgeId;
        ensureEdgesTrafficLinkLookupIndex(edgeId);

        int lastPriority = getEdgeIdTrafficPatternPriority(edgeId, forward);

        if (getEdgeIdTrafficPatternLookup(edgeKey, weekday) > 0 && lastPriority > priority)
            return;

        long edgePointer = (long) edgeId * edgeLinkLookupEntryBytes;

        priorityValue[0] = (byte) priority;

        if (forward) {
            orsEdgesTrafficLinkLookup.setBytes(edgePointer + LOCATION_FORWARD_TRAFFIC_PRIORITY, priorityValue, 1);
            orsEdgesTrafficLinkLookup.setShort(edgePointer + LOCATION_FORWARD_TRAFFIC + weekday.getByteLocation(), (short) patternId);
        } else {
            orsEdgesTrafficLinkLookup.setBytes(edgePointer + LOCATION_BACKWARD_TRAFFIC_PRIORITY, priorityValue, 1);
            orsEdgesTrafficLinkLookup.setShort(edgePointer + LOCATION_BACKWARD_TRAFFIC + weekday.getByteLocation(), (short) patternId);
        }
    }

    private boolean isForward(int edgeKey) {
        // edge state in storage direction -> edge key is even
        // edge state against storage direction -> edge key is odd
        return edgeKey % 2 == 0;
    }

    /**
     * Store the traffic pattern for each 15 minutes for 24 hours.</-><br/><br/>
     * <p>
     * This method takes the pattern Id and adds the speed value to the right quarter to the right hour.
     *
     * @param patternId     Id of the traffic pattern.
     * @param patternValues Speed values in mph or kph.
     **/
    public void setTrafficPatterns(int patternId, short[] patternValues) {
        patternCount++;
        ensureSpeedPatternLookupIndex(patternId);
        // add entry
        int minuteCounter = 0;
        short maxValue = 0;
        for (int i = 0; i < patternValues.length; i++) {
            if (minuteCounter > 3) {
                minuteCounter = 0;
            }
            short patternValue = patternValues[i];
            if (patternValue > maxValue)
                maxValue = patternValue;
            setTrafficSpeed(patternId, patternValue, i / 4, (minuteCounter * 15));
            minuteCounter++;
        }
        setMaxTrafficSpeed(patternId, maxValue);
    }

    /**
     * Store the traffic pattern for each 15 minutes for 24 hours.</-><br/><br/>
     * <p>
     * This method takes the pattern Id and adds the speed value to the right quarter to the right hour.
     *
     * @param patternId  Id of the traffic pattern.
     * @param speedValue Speed value in mph or kph.
     * @param hour       Hour to add the speed value to.
     * @param minute     Minute to add the speed value to. This is equalized into 15 minutes steps!
     **/
    private void setTrafficSpeed(int patternId, short speedValue, int hour, int minute) {
        int minutePointer = generateMinutePointer(minute);
        long patternPointer = (long) patternId * patternEntryBytes;
        ensureSpeedPatternLookupIndex(patternId);
        speedValue = speedValue > 255 ? 255 : speedValue;
        this.speedValue[0] = (byte) speedValue;
        orsSpeedPatternLookup.setBytes(patternPointer + ((hour * 4L) + minutePointer), this.speedValue, 1);
    }

    /**
     * Store maximum speed value encountered in a daily traffic pattern
     *
     * @param patternId     Id of the traffic pattern.
     * @param maxSpeedValue Speed value in mph or kph.
     **/
    private void setMaxTrafficSpeed(int patternId, short maxSpeedValue) {
        long patternPointer = (long) patternId * patternEntryBytes;
        ensureSpeedPatternLookupIndex(patternId);
        maxSpeedValue = maxSpeedValue > 255 ? 255 : maxSpeedValue;
        this.speedValue[0] = (byte) maxSpeedValue;
        orsSpeedPatternLookup.setBytes(patternPointer + DAILY_TRAFFIC_PATTERNS_BYTE_COUNT, this.speedValue, 1);
    }

    /**
     * Get the specified custom value of the edge that was assigned to it in the setValueEdge method<br/><br/>
     * <p>
     * The method takes an identifier to the edge and then gets the requested value for the edge from the storage
     *
     * @param edgeId Internal ID of the edge to get values for
     * @param prop   The property of the edge to get (TYPE - border type (0,1,2), START - the ID of the country
     *               the edge starts in, END - the ID of the country the edge ends in.
     * @return The value of the requested property
     */
    public int getOrsRoadProperties(int edgeId, Property prop) {
        byte[] propertyValue = new byte[1];
        long edgePointer = (long) edgeId * edgePropertyEntryBytes;
        if (prop == Property.ROAD_TYPE) {
            orsEdgesProperties.getBytes(edgePointer + LOCATION_ROAD_TYPE, propertyValue, 1);
        }
        return Byte.toUnsignedInt(propertyValue[0]);
    }

    /**
     * Receive the matched edgeID <-> linkID matches for both directions.</-><br/><br/>
     * <p>
     * This method returns the linkID matched on the internal edge ID in both directions if present.
     *
     * @param edgeKey Internal ID of the graph edge.
     * @param weekday Enum of Weekday to get the pattern for.
     **/
    public int getEdgeIdTrafficPatternLookup(int edgeKey, TrafficEnums.WeekDay weekday) {
        int edgeId = GHUtility.getEdgeFromEdgeKey(edgeKey);
        if (invalidEdgeId(edgeId))
            return 0;
        long edgePointer = (long) edgeId * edgeLinkLookupEntryBytes;
        if (isForward(edgeKey))
            return Short.toUnsignedInt(orsEdgesTrafficLinkLookup.getShort(edgePointer + LOCATION_FORWARD_TRAFFIC + weekday.getByteLocation()));
        else
            return Short.toUnsignedInt(orsEdgesTrafficLinkLookup.getShort(edgePointer + LOCATION_BACKWARD_TRAFFIC + weekday.getByteLocation()));
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
     * @param edgeId  Internal ID of the graph edge.
     * @param forward Direction
     **/
    private int getEdgeIdTrafficPatternPriority(int edgeId, boolean forward) {
        long edgePointer = (long) edgeId * edgeLinkLookupEntryBytes;
        byte[] priority = new byte[1];
        if (forward)
            orsEdgesTrafficLinkLookup.getBytes(edgePointer + LOCATION_FORWARD_TRAFFIC_PRIORITY, priority, 1);
        else
            orsEdgesTrafficLinkLookup.getBytes(edgePointer + LOCATION_BACKWARD_TRAFFIC_PRIORITY, priority, 1);
        return Byte.toUnsignedInt(priority[0]);
    }

    /**
     * Receive the correct traffic pattern for the desired hour.</-><br/><br/>
     * <p>
     * This method returns the traffic patterns for one hour in 15 minutes steps in a byte[].
     *
     * @param patternId Internal ID of the graph edge.
     * @param hour      Hour to get the patterns for.
     * @param minute    Minute to get the patterns for.
     **/
    public int getTrafficSpeed(int patternId, int hour, int minute) {
        byte[] values = new byte[1];
        int minutePointer = generateMinutePointer(minute);
        long patternPointer = (long) patternId * patternEntryBytes;
        orsSpeedPatternLookup.getBytes(patternPointer + ((hour * 4L) + minutePointer), values, 1);
        return Byte.toUnsignedInt(values[0]);
    }

    /**
     * Maximum speed value encountered in a daily traffic pattern
     **/
    private int getMaxTrafficSpeed(int patternId) {
        byte[] value = new byte[1];
        long patternPointer = (long) patternId * patternEntryBytes;
        orsSpeedPatternLookup.getBytes(patternPointer + DAILY_TRAFFIC_PATTERNS_BYTE_COUNT, value, 1);
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
     * @param edgeKey          Internal Edge Key
     * @param unixMilliSeconds Time in unix milliseconds.
     * @return Returns the speed value in kph. If no value is found -1 is returned.
     */
    public int getSpeedValue(int edgeKey, long unixMilliSeconds, int timeZoneOffset) {
        Calendar calendarDate = Calendar.getInstance(TimeZone.getTimeZone("GMT+" + timeZoneOffset));
        calendarDate.setTimeInMillis(unixMilliSeconds);
        int calendarWeekDay = calendarDate.get(Calendar.DAY_OF_WEEK);
        int hour = calendarDate.get(Calendar.HOUR_OF_DAY);
        int minute = calendarDate.get(Calendar.MINUTE);
        int patternId = getEdgeIdTrafficPatternLookup(edgeKey, TrafficEnums.WeekDay.valueOfCanonical(calendarWeekDay));
        if (patternId > 0)
            return getTrafficSpeed(patternId, hour, minute);
        return -1;
    }

    /**
     * Maximum traffic speed value across the whole week
     **/
    public int getMaxSpeedValue(int edgeKey) {
        int edgeId = GHUtility.getEdgeFromEdgeKey(edgeKey);
        if (invalidEdgeId(edgeId))
            return 0;
        byte[] value = new byte[1];
        long edgePointer = (long) edgeId * edgeLinkLookupEntryBytes;
        int directionOffset = isForward(edgeKey) ? FORWARD_OFFSET : BACKWARD_OFFSET;
        orsEdgesTrafficLinkLookup.getBytes(edgePointer + LOCATION_TRAFFIC_MAXSPEED + directionOffset, value, 1);
        return Byte.toUnsignedInt(value[0]);
    }

    public boolean hasTrafficSpeed(int edgeKey) {
        // Traffic patters are stored for all weekdays so it should be enough to check only for one of them
        int patternId = getEdgeIdTrafficPatternLookup(edgeKey, TrafficEnums.WeekDay.MONDAY);
        // Pattern IDs start from 1 so 0 is assumed to mean no pattern is assigned
        return patternId > 0;
    }

    private void ensureEdgesPropertyIndex(int edgeId) {
        orsEdgesProperties.ensureCapacity(((long) edgeId + 1) * edgePropertyEntryBytes);
    }

    public void ensureEdgesTrafficLinkLookupIndex(int edgeId) {
        orsEdgesTrafficLinkLookup.ensureCapacity(((long) edgeId + 1) * edgeLinkLookupEntryBytes);
    }

    private void ensureSpeedPatternLookupIndex(int patternId) {
        orsSpeedPatternLookup.ensureCapacity(((long) patternId + 1) * patternEntryBytes);
    }

    public boolean isMatched() {
        return orsEdgesTrafficLinkLookup.getHeader(8) == 1;
    }

    public void setMatched() {
        orsEdgesTrafficLinkLookup.setHeader(8, 1);
    }

    /**
     * @return true, if and only if, if an additional field at the graphs node storage is required
     */
    public boolean isRequireNodeField() {
        return true;
    }

    /**
     * @return true, if and only if, if an additional field at the graphs edge storage is required
     */
    public boolean isRequireEdgeField() {
        return true;
    }

    /**
     * @return the default field value which will be set for default when creating nodes
     */
    public int getDefaultNodeFieldValue() {
        return -1;
    }

    /**
     * @return the default field value which will be set for default when creating edges
     */
    public int getDefaultEdgeFieldValue() {
        return -1;
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

        this.orsEdgesProperties = dir.find("ext_traffic_edge_properties");
        this.orsEdgesTrafficLinkLookup = dir.find("ext_traffic_edges_traffic_lookup");
        this.orsSpeedPatternLookup = dir.find("ext_traffic_pattern_lookup");
    }

    /**
     * initializes the extended storage to be empty - required for testing purposes as the ext_storage aren't created
     * at the time tests are run
     */
    public void init() {
        if (edgesCount > 0)
            throw new AssertionError("The ORS storage must be initialized only once.");
        Directory d = new RAMDirectory();
        this.orsEdgesProperties = d.find("");
        this.orsEdgesTrafficLinkLookup = d.find("");
        this.orsSpeedPatternLookup = d.find("");
    }

    /**
     * @return true if successfully loaded from persistent storage.
     */
    @Override
    public boolean loadExisting() {
        if (!orsEdgesProperties.loadExisting())
            throw new IllegalStateException("Unable to load storage 'ext_traffic'. corrupt file or directory?");
        if (!orsEdgesTrafficLinkLookup.loadExisting())
            throw new IllegalStateException("Unable to load storage 'ext_traffic_edges_traffic_lookup'. corrupt file or directory?");
        if (!orsSpeedPatternLookup.loadExisting())
            throw new IllegalStateException("Unable to load storage 'ext_traffic_pattern_lookup'. corrupt file or directory?");
        edgePropertyEntryBytes = orsEdgesProperties.getHeader(0);
        edgeLinkLookupEntryBytes = orsEdgesTrafficLinkLookup.getHeader(0);
        patternEntryBytes = orsSpeedPatternLookup.getHeader(0);
        edgesCount = orsEdgesProperties.getHeader(4);
        maxEdgeId = orsEdgesTrafficLinkLookup.getHeader(4);
        return true;
    }

    /**
     * Creates the underlying storage. First operation if it cannot be loaded.
     *
     * @param initBytes Init size in bytes.
     */
    @Override
    public GraphExtension create(long initBytes) {
        orsEdgesProperties.create(initBytes * edgePropertyEntryBytes);
        orsEdgesTrafficLinkLookup.create(initBytes * edgeLinkLookupEntryBytes);
        orsSpeedPatternLookup.create(initBytes * patternEntryBytes);
        return this;
    }

    /**
     * This method makes sure that the underlying data is written to the storage. Keep in mind that
     * a disc normally has an IO cache so that flush() is (less) probably not save against power
     * loses.
     */
    @Override
    public void flush() {
        orsEdgesProperties.setHeader(0, edgePropertyEntryBytes);
        orsEdgesTrafficLinkLookup.setHeader(0, edgeLinkLookupEntryBytes);
        orsSpeedPatternLookup.setHeader(0, patternEntryBytes);
        orsEdgesProperties.setHeader(4, edgesCount);
        orsEdgesTrafficLinkLookup.setHeader(4, maxEdgeId);
        orsSpeedPatternLookup.setHeader(4, patternCount);
        orsEdgesProperties.flush();
        orsEdgesTrafficLinkLookup.flush();
        orsSpeedPatternLookup.flush();
    }

    /**
     * This method makes sure that the underlying used resources are released. WARNING: it does NOT
     * flush on close!
     */
    @Override
    public void close() {
        orsEdgesProperties.close();
        orsEdgesTrafficLinkLookup.close();
        orsSpeedPatternLookup.close();
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
        return orsEdgesProperties.getCapacity() + orsEdgesTrafficLinkLookup.getCapacity() + orsSpeedPatternLookup.getCapacity();
    }


    private int generateMinutePointer(int minute) {
        if (minute < 15) {
            return 0;
        } else if (minute < 30) {
            return 1;
        } else if (minute < 45) {
            return 2;
        } else {
            return 3;
        }
    }

    /**
     * This method finds as stores for each edge the maximum forward and backward traffic speed across the whole week.
     */
    public void setMaxTrafficSpeeds() {
        int[] directionOffsets = {FORWARD_OFFSET, BACKWARD_OFFSET};

        for (int edgeId = 0; edgeId <= maxEdgeId; edgeId++) {
            long edgePointer = (long) edgeId * edgeLinkLookupEntryBytes;

            for (int directionOffset : directionOffsets) {
                int weeklyMaxSpeed = 0;

                for (TrafficEnums.WeekDay weekDay : TrafficEnums.WeekDay.values()) {
                    int patternId = Short.toUnsignedInt(orsEdgesTrafficLinkLookup.getShort(edgePointer + LOCATION_TRAFFIC + directionOffset + weekDay.getByteLocation()));
                    if (patternId == 0)
                        break;
                    int dailyMaxSpeed = getMaxTrafficSpeed(patternId);
                    if (dailyMaxSpeed > weeklyMaxSpeed)
                        weeklyMaxSpeed = dailyMaxSpeed;
                }

                this.speedValue[0] = (byte) weeklyMaxSpeed;
                orsEdgesTrafficLinkLookup.setBytes(edgePointer + LOCATION_TRAFFIC_MAXSPEED + directionOffset, this.speedValue, 1);
            }
        }
    }

    /**
     * @return number of processed traffic patterns
     */
    public int getPatternCount() {
        return patternCount;
    }

    public void setZoneId(ZoneId zoneId) {
        this.zoneId = zoneId;
    }

    public ZoneId getZoneId() {
        return this.zoneId;
    }
}
