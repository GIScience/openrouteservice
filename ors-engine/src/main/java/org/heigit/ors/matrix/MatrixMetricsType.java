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
package org.heigit.ors.matrix;

import com.graphhopper.util.Helper;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MatrixMetricsType {
    public static final int UNKNOWN = 0;
    public static final int DURATION = 1;
    public static final int DISTANCE = 2;
    public static final int WEIGHT = 4;
    public static final String KEY_DURATION = "duration";
    public static final String KEY_DISTANCE = "distance";
    public static final String KEY_WEIGHT = "weight";
    public static final String KEY_UNKNOWN = "unknown";

    private MatrixMetricsType() {
    }

    public static boolean isSet(int metrics, int value) {
        return (metrics & value) == value;
    }

    public static int getFromString(String value) {
        if (Helper.isEmpty(value))
            return 0;

        String[] values = value.toLowerCase().split("\\|");
        int res = UNKNOWN;

        for (String str : values) {
            switch (str) {
                case KEY_DURATION:
                    res |= DURATION;
                    break;
                case KEY_DISTANCE:
                    res |= DISTANCE;
                    break;
                case KEY_WEIGHT:
                    res |= WEIGHT;
                    break;
                default:
                    return UNKNOWN;
            }
        }

        return res;
    }

    public static String getMetricNameFromInt(int metric) {
        return switch (metric) {
            case MatrixMetricsType.DURATION -> KEY_DURATION;
            case MatrixMetricsType.DISTANCE -> KEY_DISTANCE;
            case MatrixMetricsType.WEIGHT -> KEY_WEIGHT;
            default -> KEY_UNKNOWN;
        };
    }

    public static Set<String> getMetricsNamesFromInt(int metric) {
        return switch (metric) {
            case MatrixMetricsType.DURATION -> new HashSet<>(List.of(KEY_DURATION));
            case MatrixMetricsType.DISTANCE -> new HashSet<>(List.of(KEY_DISTANCE));
            case MatrixMetricsType.WEIGHT -> new HashSet<>(List.of(KEY_WEIGHT));
            case MatrixMetricsType.DURATION | MatrixMetricsType.DISTANCE ->
                    new HashSet<>(Arrays.asList(KEY_DURATION, KEY_DISTANCE));
            default -> new HashSet<>(List.of(KEY_UNKNOWN));
        };
    }
}
