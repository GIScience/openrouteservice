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

package heigit.ors.api.errors;

import heigit.ors.exceptions.InternalServerException;

public class GenericErrorCodes {
    private GenericErrorCodes() throws InternalServerException {
        throw new InternalServerException("GenericErrorCodes should never be instantiated");
    }

    public static final int INVALID_JSON_FORMAT = 0;
    public static final int MISSING_PARAMETER = 1;
    public static final int INVALID_PARAMETER_FORMAT = 2;
    public static final int INVALID_PARAMETER_VALUE = 3;
    public static final int REQUEST_EXCEEDS_SERVER_LIMIT = 4;
    public static final int EXPORT_HANDLER_ERROR = 6;
    public static final int UNSUPPORTED_EXPORT_FORMAT = 7;
    public static final int EMPTY_ELEMENT = 8;
    public static final int ROUTE_NOT_FOUND = 9;
    public static final int POINT_NOT_FOUND = 10;
    public static final int UNKNOWN_PARAMETER = 11;
    public static final int UNKNOWN = 99;
}
