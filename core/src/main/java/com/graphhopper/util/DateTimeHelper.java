package com.graphhopper.util;

import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.util.EdgeIteratorState;
import us.dustinj.timezonemap.TimeZoneMap;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class DateTimeHelper {
    private final NodeAccess nodeAccess;
    private final TimeZoneMap timeZoneMap;

    public DateTimeHelper(GraphHopperStorage graph) {
        this.nodeAccess = graph.getNodeAccess();
        this.timeZoneMap = graph.getTimeZoneMap();
    }

    public ZonedDateTime getZonedDateTime(EdgeIteratorState iter, long time) {
        int node = iter.getBaseNode();
        double lat = nodeAccess.getLatitude(node);
        double lon = nodeAccess.getLongitude(node);
        String timeZoneId = timeZoneMap.getOverlappingTimeZone(lat, lon).get().getZoneId();
        ZoneId edgeZoneId = ZoneId.of(timeZoneId);
        Instant edgeEnterTime = Instant.ofEpochMilli(time);
        return ZonedDateTime.ofInstant(edgeEnterTime, edgeZoneId);
    }

    public ZonedDateTime getZonedDateTime(double lat, double lon, String time) {
        LocalDateTime localDateTime = LocalDateTime.parse(time);
        String timeZoneId = timeZoneMap.getOverlappingTimeZone(lat, lon).get().getZoneId();
        return localDateTime.atZone(ZoneId.of(timeZoneId));
    }

    public String getZoneId(double lat, double lon) {
        return timeZoneMap.getOverlappingTimeZone(lat, lon).get().getZoneId();
    }
}
