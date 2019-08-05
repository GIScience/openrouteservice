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
package heigit.ors.routing;

/**
 * This Class handles the error Codes as described in the error_codes.md
 *
 * @author OpenRouteServiceTeam
 * @author Julian Psotta, julian@openrouteservice.org
 */
public class RoutingErrorCodes {
    public static int BASE = 2000;
    public static int INVALID_JSON_FORMAT = 2000;
    public static int MISSING_PARAMETER = 2001;
    public static int INVALID_PARAMETER_FORMAT = 2002;
    public static int INVALID_PARAMETER_VALUE = 2003;
    public static int REQUEST_EXCEEDS_SERVER_LIMIT = 2004;
    public static int EXPORT_HANDLER_ERROR = 2006;
    public static int UNSUPPORTED_EXPORT_FORMAT = 2007;
    public static int EMPTY_ELEMENT = 2008;
    public static int ROUTE_NOT_FOUND = 2009;
    public static int POINT_NOT_FOUND = 2010;
    public static int INCOMPATIBLE_PARAMETERS = 2011;
    public static int UNKNOWN_PARAMETER = 2012;
    public static int UNKNOWN = 2099;
}
