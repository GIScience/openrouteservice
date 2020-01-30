package com.graphhopper.routing.util;

import ch.poole.conditionalrestrictionparser.ConditionalRestrictionParser;
import ch.poole.conditionalrestrictionparser.Restriction;
import com.graphhopper.routing.profiles.BooleanEncodedValue;
import com.graphhopper.routing.profiles.DecimalEncodedValue;
import com.graphhopper.routing.weighting.DateTimeConverter;
import com.graphhopper.storage.ConditionalEdgesMap;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.util.EdgeIteratorState;
import us.dustinj.timezonemap.TimeZoneMap;

import java.io.ByteArrayInputStream;
import java.time.ZonedDateTime;
import java.util.ArrayList;

public class SpeedCalculator {
    //protected final FlagEncoder flagEncoder;
    protected final DecimalEncodedValue avSpeedEnc;

    // time-dependent stuff
    private final BooleanEncodedValue conditionalEnc;
    private final ConditionalEdgesMap conditionalEdges;
    private final DateTimeConverter dateTimeConverter;

    public SpeedCalculator(GraphHopperStorage graph, FlagEncoder encoder, TimeZoneMap timeZoneMap) {
        avSpeedEnc = encoder.getAverageSpeedEnc();

        // time-dependent stuff
        EncodingManager encodingManager = graph.getEncodingManager();
        String encoderName = encodingManager.getKey(encoder, "conditional_speed");

        if (!encodingManager.hasEncodedValue(encoderName)) {
            throw new IllegalStateException("No conditional speed associated with the flag encoder");
        }

        conditionalEnc = encodingManager.getBooleanEncodedValue(encoderName);
        conditionalEdges = graph.getConditionalSpeed(encoder);

        this.dateTimeConverter = new DateTimeConverter(graph, timeZoneMap);
    }

    public double getSpeed(EdgeIteratorState edge, boolean reverse, long time) {
        double speed = reverse ? edge.getReverse(avSpeedEnc) : edge.get(avSpeedEnc);

        // retrieve time-dependent maxspeed here
        if (time != -1 && edge.get(conditionalEnc)) {
            ZonedDateTime zonedDateTime = dateTimeConverter.localDateTime(edge, time);
            String value = conditionalEdges.getValue(edge.getEdge());
            double result = getSpeed(value, zonedDateTime);
            if (result != -1)
                speed = result;
        }

        return speed;
    }

    private double getSpeed(String conditional, ZonedDateTime zonedDateTime)  {
        try {
            ConditionalRestrictionParser crparser = new ConditionalRestrictionParser(new ByteArrayInputStream(conditional.getBytes()));
            ArrayList<Restriction> restrictions = crparser.restrictions();

            // iterate over restrictions starting from the last one in order to match to the most specific one
            for (int i = restrictions.size() - 1 ; i >= 0; i--) {
                Restriction restriction = restrictions.get(i);
                // stop as soon as time matches the combined conditions
                if (TimeDependentConditionalEvaluator.match(restriction.getConditions(), zonedDateTime)) {
                    double speed = Double.parseDouble(restriction.getValue());
                    if (Double.isInfinite(speed) || Double.isNaN(speed) || speed < 0)
                        throw new IllegalStateException("Invalid speed stored in edge! " + speed);
                    return speed;
                }
            }
        } catch (ch.poole.conditionalrestrictionparser.ParseException e) {
            //nop
        }
        return -1;
    }

}
