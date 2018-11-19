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

public class RouteWarning {
    public final static int ACCESS_RESTRICTION = 1;
    public final static int TOLLWAYS = 2;

    private int warningCode = 0;
    private String warningMessage = "";

    public RouteWarning(int warning) {
        warningCode = warning;
        switch(warning) {
            case ACCESS_RESTRICTION:
                warningMessage = "There may be restrictions on some roads";
                break;
            case TOLLWAYS:
                warningMessage = "There are tollways along the route";
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
