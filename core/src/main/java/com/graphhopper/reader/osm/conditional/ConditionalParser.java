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

import ch.poole.conditionalrestrictionparser.*;
import ch.poole.openinghoursparser.OpeningHoursParser;
import ch.poole.openinghoursparser.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Parses a conditional tag according to
 * http://wiki.openstreetmap.org/wiki/Conditional_restrictions.
 * <p>
 *
 * @author Robin Boldt
 * @author Andrzej Oles
 */
public class ConditionalParser {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Set<String> restrictedValues;
    private final List<ConditionalValueParser> valueParsers = new ArrayList<>(5);
    private final boolean enabledLogs;

    private String simpleValue;
    private String unevaluatedRestrictions = "";
    private Conditions unevaluatedConditions;

    public ConditionalParser(Set<String> restrictedValues) {
        this(restrictedValues, false);
    }

    public ConditionalParser(Set<String> restrictedValues, boolean enabledLogs) {
        // use map => key & type (date vs. double)
        this.restrictedValues = restrictedValues;
        this.enabledLogs = enabledLogs;

        if (hasRestrictedValues()) {
            if (restrictedValues.contains("yes"))
                this.simpleValue = "yes";
            else this.simpleValue = "no";
        }
    }

    public static ConditionalValueParser createNumberParser(final String assertKey, final Number obj) {
        return new ConditionalValueParser() {
            @Override
            public ConditionState checkCondition(Condition condition) throws ParseException {
                if (condition.isExpression())
                    return checkCondition(condition.toString());
                else
                    return ConditionState.INVALID;
            }
            @Override
            public ConditionState checkCondition(String conditionalValue) throws ParseException {
                int indexLT = conditionalValue.indexOf("<");
                if (indexLT > 0 && conditionalValue.length() > 2) {
                    final String key = conditionalValue.substring(0, indexLT).trim();
                    if (!assertKey.equals(key))
                        return ConditionState.INVALID;

                    if (conditionalValue.charAt(indexLT + 1) == '=')
                        indexLT++;
                    final double value = parseNumber(conditionalValue.substring(indexLT + 1));
                    if (obj.doubleValue() < value)
                        return ConditionState.TRUE;
                    else
                        return ConditionState.FALSE;
                }

                int indexGT = conditionalValue.indexOf(">");
                if (indexGT > 0 && conditionalValue.length() > 2) {
                    final String key = conditionalValue.substring(0, indexGT).trim();
                    if (!assertKey.equals(key))
                        return ConditionState.INVALID;

                    // for now just ignore equals sign
                    if (conditionalValue.charAt(indexGT + 1) == '=')
                        indexGT++;

                    final double value = parseNumber(conditionalValue.substring(indexGT + 1));
                    if (obj.doubleValue() > value)
                        return ConditionState.TRUE;
                    else
                        return ConditionState.FALSE;
                }

                return ConditionState.INVALID;
            }
        };
    }

    public static ConditionalValueParser createDateTimeParser() {
        return new ConditionalValueParser() {
            @Override
            public ConditionState checkCondition(String conditionString) {
                ArrayList<Rule> rules;
                try {
                    OpeningHoursParser parser = new OpeningHoursParser(new ByteArrayInputStream(conditionString.getBytes()));
                    rules = parser.rules(false);
                }
                catch (Exception e) {
                    return ConditionState.INVALID;
                }
                if (rules.isEmpty())
                    return ConditionState.INVALID;
                else {
                    String parsedConditionString = ch.poole.openinghoursparser.Util.rulesToOpeningHoursString(rules);
                    return ConditionState.UNEVALUATED.setCondition(new Condition(parsedConditionString, true));
                }
            }
            @Override
            public ConditionState checkCondition(Condition condition) {
                if (condition.isOpeningHours())
                    return checkCondition(condition.toString()); // attempt to properly parse the condition
                else
                    return ConditionState.INVALID;
            }
        };
    }

    /**
     * This method adds a new value parser. The one added last has a higher priority.
     */
    public ConditionalParser addConditionalValueParser(ConditionalValueParser vp) {
        valueParsers.add(0, vp);
        return this;
    }

    public ConditionalParser setConditionalValueParser(ConditionalValueParser vp) {
        valueParsers.clear();
        valueParsers.add(vp);
        return this;
    }

    // attempt to parse the value with any of the registered parsers
    private boolean checkAtomicCondition(Condition condition) throws ParseException {
        for (ConditionalValueParser valueParser : valueParsers) {
            ConditionalValueParser.ConditionState c = valueParser.checkCondition(condition);
            if (c.isValid()) {
                if (c.isEvaluated())
                    return c.isCheckPassed();
                else { // condition could not be evaluated but might evaluate to true during query
                    unevaluatedConditions.addCondition(c.getCondition());
                    return true;
                }
            }
        }
        return false;
    }

    // all of the combined conditions need to be met
    private boolean checkCombinedCondition(List<Condition> conditions) throws ParseException {
        // combined conditions, must be all matched
        for (Condition condition: conditions) {
            if (!checkAtomicCondition(condition))
                return false;
        }
        return true;
    }


    public boolean checkCondition(String tagValue) throws ParseException {
        if (tagValue == null || tagValue.isEmpty() || !tagValue.contains("@"))
            return false;

        ArrayList<Restriction> parsedRestrictions = new ArrayList<>();
        unevaluatedRestrictions = "";

        try {
            ConditionalRestrictionParser parser = new ConditionalRestrictionParser(new ByteArrayInputStream(tagValue.getBytes()));

            ArrayList<Restriction> restrictions = parser.restrictions();

            // iterate over restrictions starting from the last one in order to match to the most specific one
            for (int i = restrictions.size() - 1 ; i >= 0; i--) {
                Restriction restriction = restrictions.get(i);

                String restrictionValue = restriction.getValue();

                if (hasRestrictedValues()) {
                    // check whether the encountered value is on the list
                    if (!restrictedValues.contains(restrictionValue))
                        continue;
                }
                else {
                    simpleValue = restrictionValue;
                }

                List<Condition> conditions = restriction.getConditions();

                unevaluatedConditions = new Conditions(new ArrayList<Condition>(), restriction.inParen());

                if (checkCombinedCondition(conditions)) {
                    // check for unevaluated conditions
                    if (unevaluatedConditions.getConditions().isEmpty()) {
                        return true; // terminate once the first matching condition which can be fully evaluated is encountered
                    }
                    else {
                        parsedRestrictions.add(new Restriction(simpleValue, unevaluatedConditions));
                    }
                }
            }
        } catch (ch.poole.conditionalrestrictionparser.ParseException e) {
            if (enabledLogs)
                logger.warn("Parser exception for " + tagValue + " " + e.toString());
            return false;
        }
        // at this point either no matching restriction was found or the encountered restrictions need to be lazy evaluated
        if (parsedRestrictions.isEmpty()) {
            return false;
        }
        else {
            unevaluatedRestrictions = Util.restrictionsToString(parsedRestrictions);
            return true;
        }
    }

    public String getRestrictions() {
        if (hasUnevaluatedRestrictions())
            return unevaluatedRestrictions;
        else
            return simpleValue;
    }

    public boolean hasUnevaluatedRestrictions() {
        return !unevaluatedRestrictions.isEmpty();
    }

    protected static double parseNumber(String str) {
        int untilIndex = str.length() - 1;
        for (; untilIndex >= 0; untilIndex--) {
            if (Character.isDigit(str.charAt(untilIndex)))
                break;
        }
        return Double.parseDouble(str.substring(0, untilIndex + 1));
    }

    private boolean hasRestrictedValues() {
        return !( restrictedValues==null || restrictedValues.isEmpty() );
    }
}
