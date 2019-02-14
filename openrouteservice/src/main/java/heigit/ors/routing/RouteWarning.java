/*
 * This file is part of Openrouteservice.
 *
 * Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, see <https://www.gnu.org/licenses/>.
 */

package heigit.ors.routing;

/**
 * Holder class for a warning that could be used in the response
 */
public class RouteWarning {
    public final static int ACCESS_RESTRICTION = 1;
    public final static int TOLLWAYS = 2;
    public final static int SKIPPED_SEGMENTS = 3;

    private int warningCode = 0;
    private String warningMessage = "";

    /**
     * Generate the warning object and initialise the contents based on the warning code passed
     * @param warning   The warning code for the warning that should be generated
     */
    public RouteWarning(int warning) {
        warningCode = warning;
        switch(warning) {
            case ACCESS_RESTRICTION:
                warningMessage = "There may be restrictions on some roads";
                break;
            case TOLLWAYS:
                warningMessage = "There are tollways along the route";
                break;
            case SKIPPED_SEGMENTS:
                warningMessage = "There are skipped segments along the route. Durations and accessibility may not be correct";
                break;
        }
    }

    public int getWarningCode() {
        return warningCode;
    }

    public String getWarningMessage() {
        return warningMessage;
    }
}
