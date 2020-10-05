package com.graphhopper.reader.osm.conditional;

import ch.poole.conditionalrestrictionparser.Condition;

import java.text.ParseException;

/**
 * This interface defines how to parse a OSM value from conditional restrictions.
 */
public interface ConditionalValueParser {

    /**
     * This method checks if the condition is satisfied for this parser.
     */
    ConditionState checkCondition(String conditionalValue) throws ParseException;

    ConditionState checkCondition(Condition conditionalValue) throws ParseException;

    enum ConditionState {
        TRUE(true, true, true),
        FALSE(true, true, false),
        INVALID(false, false, false),
        UNEVALUATED(true, false, false);

        boolean valid;
        boolean evaluated;
        boolean checkPassed;

        Condition condition;

        ConditionState(boolean valid, boolean evaluated, boolean checkPassed) {
            this.valid = valid;
            this.evaluated = evaluated;
            this.checkPassed = checkPassed;
        }

        public boolean isValid() {
            return valid;
        }

        public boolean isEvaluated() {
            return evaluated;
        }

        public boolean isCheckPassed() {
            if (!isValid())
                throw new IllegalStateException("Cannot call this method for invalid state");
            if (!isEvaluated())
                throw new IllegalStateException("Cannot call this method for unevaluated state");
            return checkPassed;
        }

        public ConditionState setCondition(Condition condition) {
            this.condition = condition;
            return this;
        }

        public Condition getCondition() {
            return condition;
        }
    }
}
