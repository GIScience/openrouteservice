package org.heigit.ors.routing.graphhopper.extensions.util;

/**
 * Used to store a priority value in the way flags of an edge. Used in combination with
 * PriorityWeighting
 *
 * @author Peter Karich
 */
public enum PriorityCode {
    WORST(0),
    AVOID_AT_ALL_COSTS(1),
    REACH_DEST(2),
    AVOID_IF_POSSIBLE(3),
    UNCHANGED(4),
    PREFER(5),
    VERY_NICE(6),
    BEST(7);
    private final int value;

    PriorityCode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    /**
     * This method returns the PriorityCode.value in a range between 0 and 1 suitable for direct usage in a Weighting.
     */
    public static double getFactor(int val) {
        return (double) val / BEST.getValue();
    }

}
