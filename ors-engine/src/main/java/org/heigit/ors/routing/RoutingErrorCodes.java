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
package org.heigit.ors.routing;

/**
 * This Class handles the error Codes as described in the error_codes.md
 *
 * @author OpenRouteServiceTeam
 * @author Julian Psotta, julian@openrouteservice.org
 */
public class RoutingErrorCodes {

    //Keep in sync with documentation: error-codes.md

    public static final int BASE = 2000;
    public static final int INVALID_JSON_FORMAT = 2000;
    public static final int MISSING_PARAMETER = 2001;
    public static final int INVALID_PARAMETER_FORMAT = 2002;
    public static final int INVALID_PARAMETER_VALUE = 2003;
    public static final int REQUEST_EXCEEDS_SERVER_LIMIT = 2004;
    public static final int EXPORT_HANDLER_ERROR = 2006;
    public static final int UNSUPPORTED_EXPORT_FORMAT = 2007;
    public static final int EMPTY_ELEMENT = 2008;
    public static final int ROUTE_NOT_FOUND = 2009;
    public static final int POINT_NOT_FOUND = 2010;
    public static final int INCOMPATIBLE_PARAMETERS = 2011;
    public static final int UNKNOWN_PARAMETER = 2012;

    public static final int PT_ENTRY_NOT_REACHED = 2013;
    public static final int PT_EXIT_NOT_REACHED = 2014;
    public static final int PT_NOT_REACHED = 2015;

    public static final int PT_ROUTE_NOT_FOUND = 2016;

    public static final int PT_MAX_VISITED_NODES_EXCEEDED = 2017;
    public static final int UNKNOWN = 2099;

    private RoutingErrorCodes() {
    }
}
