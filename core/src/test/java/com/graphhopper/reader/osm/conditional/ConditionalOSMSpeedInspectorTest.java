/*
 *  Licensed to GraphHopper GmbH under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for
 *  additional information regarding copyright ownership.
 *
 *  GraphHopper GmbH licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.graphhopper.reader.osm.conditional;

import com.graphhopper.reader.ReaderWay;
import org.junit.Test;

import java.text.ParseException;
import java.util.*;

import static org.junit.Assert.*;

/**
 * @author Andrzej Oles
 */
public class ConditionalOSMSpeedInspectorTest extends CalendarBasedTest {

    private static List<String> getSampleConditionalTags() {
        List<String> conditionalTags = new ArrayList<>();
        conditionalTags.add("maxspeed");
        conditionalTags.add("maxspeed:hgv");
        return conditionalTags;
    }

    private static ConditionalOSMSpeedInspector getConditionalSpeedInspector() {
        ConditionalOSMSpeedInspector acceptor = new ConditionalOSMSpeedInspector(getSampleConditionalTags());
        acceptor.addValueParser(ConditionalParser.createDateTimeParser());
        return acceptor;
    }

    @Test
    public void testNotConditional() {
        ConditionalOSMSpeedInspector acceptor = getConditionalSpeedInspector();
        ReaderWay way = new ReaderWay(1);
        way.setTag("maxspeed:hgv", "80");
        assertFalse(acceptor.hasConditionalSpeed(way));
        assertFalse(acceptor.isConditionLazyEvaluated());
    }

    @Test
    public void testConditionalTime() {
        ConditionalOSMSpeedInspector acceptor = getConditionalSpeedInspector();
        ReaderWay way = new ReaderWay(1);
        way.setTag("maxspeed:conditional", "60 @ (23:00-05:00)");
        assertTrue(acceptor.hasConditionalSpeed(way));
        assertTrue(acceptor.isConditionLazyEvaluated());
    }

    @Test
    public void testConditionalWeather() {
        ConditionalOSMSpeedInspector acceptor = getConditionalSpeedInspector();
        ReaderWay way = new ReaderWay(1);
        way.setTag("maxspeed:conditional", "60 @ snow");
        assertFalse(acceptor.hasConditionalSpeed(way));
        assertFalse(acceptor.isConditionLazyEvaluated());
    }

    @Test
    public void testConditionalWeight() {
        ConditionalOSMSpeedInspector acceptor = getConditionalSpeedInspector();
        ReaderWay way = new ReaderWay(1);
        way.setTag("maxspeed:conditional", "90 @ (weight>7.5)");
        assertFalse(acceptor.hasConditionalSpeed(way));
        assertFalse(acceptor.isConditionLazyEvaluated());
        acceptor.addValueParser(ConditionalParser.createNumberParser("weight", 10));
        assertTrue(acceptor.hasConditionalSpeed(way));
        assertFalse(acceptor.isConditionLazyEvaluated());
    }

    @Test
    public void testMultipleConditions() {
        ConditionalOSMSpeedInspector acceptor = getConditionalSpeedInspector();
        ReaderWay way = new ReaderWay(1);
        way.setTag("maxspeed:hgv:conditional", "90 @ (weight<=3.5); 70 @ (weight>3.5)");
        assertFalse(acceptor.hasConditionalSpeed(way));
        assertFalse(acceptor.isConditionLazyEvaluated());
        acceptor.addValueParser(ConditionalParser.createNumberParser("weight", 10));
        assertTrue(acceptor.hasConditionalSpeed(way));
        assertFalse(acceptor.isConditionLazyEvaluated());
        //test for value
        assertEquals(acceptor.getTagValue(), "70");

    }

    @Test
    public void testCombinedCondition() throws ParseException {
        ConditionalOSMSpeedInspector acceptor = getConditionalSpeedInspector();
        ReaderWay way = new ReaderWay(1);
        way.setTag("maxspeed:hgv:conditional", "60 @ (22:00-05:00 AND weight>7.5)");
        assertFalse(acceptor.hasConditionalSpeed(way));
        assertFalse(acceptor.isConditionLazyEvaluated());
        acceptor.addValueParser(ConditionalParser.createNumberParser("weight", 10));
        assertTrue(acceptor.hasConditionalSpeed(way));
        assertTrue(acceptor.isConditionLazyEvaluated());
        assertEquals(acceptor.getTagValue(), "60 @ (22:00-05:00)");
    }
}
