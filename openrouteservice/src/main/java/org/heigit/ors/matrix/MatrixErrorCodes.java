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

/**
 * This Class handles the error Codes as described in the error_codes.md
 *
 * @author OpenRouteServiceTeam
 * @author Julian Psotta, julian@openrouteservice.org
 */
public class MatrixErrorCodes {
    public static final int BASE = 6000;
    public static final int INVALID_JSON_FORMAT = 6000;
    public static final int MISSING_PARAMETER = 6001;
    public static final int INVALID_PARAMETER_FORMAT = 6002;
    public static final int INVALID_PARAMETER_VALUE = 6003;
    public static final int PARAMETER_VALUE_EXCEEDS_MAXIMUM = 6004;
    public static final int EXPORT_HANDLER_ERROR = 6006;
    public static final int UNSUPPORTED_EXPORT_FORMAT = 6007;
    public static final int EMPTY_ELEMENT = 6008;
    public static final int POINT_NOT_FOUND = 6010;
    public static final int UNKNOWN_PARAMETER = 6011;
    public static final int UNKNOWN = 6099;
    private MatrixErrorCodes() {}
}
