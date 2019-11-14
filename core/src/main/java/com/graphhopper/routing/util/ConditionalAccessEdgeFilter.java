package com.graphhopper.routing.util;

import ch.poole.conditionalrestrictionparser.Condition;
import ch.poole.conditionalrestrictionparser.ConditionalRestrictionParser;
import ch.poole.conditionalrestrictionparser.Restriction;
import ch.poole.openinghoursparser.*;
import com.graphhopper.routing.profiles.BooleanEncodedValue;
import com.graphhopper.util.EdgeIteratorState;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;


public class ConditionalAccessEdgeFilter implements TimeDependentEdgeFilter {
    private final BooleanEncodedValue conditionalEnc;
    private final boolean fwd;
    private final boolean bwd;

    public ConditionalAccessEdgeFilter(BooleanEncodedValue conditionalEnc, boolean fwd, boolean bwd) {
        this.conditionalEnc = conditionalEnc;
        this.fwd = fwd;
        this.bwd = bwd;
    }

    @Override
    public final boolean accept(EdgeIteratorState iter, long time) {
        boolean conditional = fwd && iter.get(conditionalEnc) || bwd && iter.getReverse(conditionalEnc);
        // TODO: get actual edge timeZoneID
        ZoneId edgeZoneId = ZoneId.of("Europe/Berlin");
        Instant edgeEnterTime = Instant.ofEpochMilli(time);
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(edgeEnterTime, edgeZoneId);
        boolean result = conditional && accept(iter.getConditional(), zonedDateTime);
        if (conditional)
            System.out.println(iter.getEdge() + ": " + iter.getConditional() + " -> " + result);  //FIXME: debug string
        return result;
    }

    boolean accept(String conditional, ZonedDateTime zonedDateTime) {
        boolean matchValue = false;

        try {
            ConditionalRestrictionParser crparser = new ConditionalRestrictionParser(new ByteArrayInputStream(conditional.getBytes()));

            ArrayList<Restriction> restrictions = crparser.restrictions();

            // iterate over restrictions starting from the last one in order to match to the most specific one
            for (int i = restrictions.size() - 1 ; i >= 0; i--) {
                Restriction restriction = restrictions.get(i);

                matchValue = "yes".equals(restriction.getValue());

                List<Condition> conditions = restriction.getConditions();

                // stop as soon as time matches the combined conditions
                if (match(conditions, zonedDateTime))
                    return matchValue;
            }

            // no restrictions with matching conditions found
            return !matchValue;

        } catch (ch.poole.conditionalrestrictionparser.ParseException e) {
            //nop
        }

        return false;
    }

    boolean match(List<Condition> conditions,  ZonedDateTime zonedDateTime) {
        for (Condition condition : conditions) {
            try {
                OpeningHoursParser parser = new OpeningHoursParser(new ByteArrayInputStream(condition.toString().getBytes()));
                List<Rule> rules = parser.rules(false);
                // failed to match any of the rules
                if (!matchRules(rules, zonedDateTime))
                    return false;
            } catch (Exception e) {
                return false;
            }
        }
        // all of the conditions successfully matched
        return true;
    }

    private boolean hasExtendedTime(Rule rule) {
        List<TimeSpan> times = rule.getTimes();
        if (times==null || times.isEmpty())
            return false;
        for (TimeSpan timeSpan: times) {
            // only the end time can exceed 24h
            int end = timeSpan.getEnd();
            if (end != TimeSpan.UNDEFINED_TIME && end > TimeSpan.MAX_TIME)
                return true;
        }
        return false;
    }

    boolean matchRules(List<Rule> rules, ZonedDateTime zonedDateTime) {
        TimePoint timePoint = new TimePoint(zonedDateTime, false);
        TimePoint timePointExtended = new TimePoint(zonedDateTime, true);
        for (Rule rule: rules) {
            if (matches(timePoint, rule))
                return true;
            if (hasExtendedTime(rule) && matches(timePointExtended, rule))
                return true;
        }
        // no matching rule found
        return false;
    }

    boolean inYearRange(TimePoint timePoint, List<YearRange> years) {
        for (YearRange yearRange: years)
            if (inRange(timePoint.getYear(), yearRange.getStartYear(), yearRange.getEndYear(), YearRange.UNDEFINED_YEAR))
                return true;
        return false;
    }

    boolean inDateRange(TimePoint timePoint, List<DateRange> dates) {
        for (DateRange dateRange: dates) {
            DateWithOffset startDate = dateRange.getStartDate();
            DateWithOffset endDate = dateRange.getEndDate();

            if (endDate == null) {
                if (timePoint.atDate(startDate))
                    return true;
            } else {
                if (timePoint.inRange(startDate, endDate))
                    return true;
            }
        }
        return false;
    }

    boolean inWeekdayRange(TimePoint timePoint, List<WeekDayRange> days) {
        for (WeekDayRange weekDayRange: days)
            if (inRange(timePoint.getWeekday(), weekDayRange.getStartDay(), weekDayRange.getEndDay()))
                return true;
        return false;
    }

    boolean inRange(int value, Enum start, Enum end) {
        if (start == null)
            return true; // unspecified range matches to any value
        if (value >= start.ordinal()) {
            if (end == null)
                return value == start.ordinal();
            else
                return value <= end.ordinal();
        }
        else
            return false;
    }

    boolean inTimeRange(TimePoint timePoint, List<TimeSpan> times) {
        for (TimeSpan timeSpan: times)
            if (inRange(timePoint.getMinutes(), timeSpan.getStart(), timeSpan.getEnd(), TimeSpan.UNDEFINED_TIME))
                return true;
        return false;
    }

    boolean inRange(int value, int start, int end, int undefined) {
        if (start == undefined)
            return true; // unspecified range matches to any value
        if (value >= start) {
            if (end == undefined)
                return value == start;
            else
                return value <= end;
        }
        else
            return false;
    }

    boolean matches(TimePoint timePoint, Rule rule) {

        List<YearRange> years = rule.getYears();
        if (years!=null && !years.isEmpty())
            if (!inYearRange(timePoint, years))
                return false;

        List<DateRange> dates = rule.getDates();
        if (dates!=null && !dates.isEmpty())
            if (!inDateRange(timePoint, dates))
                return false;

        List<WeekDayRange> days = rule.getDays();
        if (days!=null && !days.isEmpty())
            if (!inWeekdayRange(timePoint, days))
                return false;

        List<TimeSpan> times = rule.getTimes();
        if (times!=null && !times.isEmpty())
            if (!inTimeRange(timePoint, times))
                return false;

        return true;
    }

    public boolean acceptsBackward() {
        return bwd;
    }

    public boolean acceptsForward() {
        return fwd;
    }

    @Override
    public String toString() {
        return conditionalEnc + ", bwd:" + bwd + ", fwd:" + fwd;
    }
}
