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
package org.heigit.ors.exceptions;

import org.heigit.ors.common.StatusCode;

public class ParameterOutOfRangeException extends StatusCodeException {
    private static final long serialVersionUID = 7728944138955234463L;

    private static final String PARAMETER_KEY = "Parameter";

    public ParameterOutOfRangeException(int errorCode, String paramName) {
        super(StatusCode.BAD_REQUEST, errorCode, PARAMETER_KEY + " '" + paramName + "' is out of range.");
    }

    public ParameterOutOfRangeException(int errorCode, String paramName, String customMessage) {
        super(StatusCode.BAD_REQUEST, errorCode, PARAMETER_KEY + " '" + paramName + "' is out of range: " + customMessage);
    }

    public ParameterOutOfRangeException(int errorCode, String paramName, String value, String maxRangeValue) {
        super(StatusCode.BAD_REQUEST, errorCode, PARAMETER_KEY + " '" + paramName + "=" + value + "' is out of range. Maximum possible value is " + maxRangeValue + ".");
    }

    public ParameterOutOfRangeException(String paramName, String value, String maxRangeValue) {
        this(-1, paramName, value, maxRangeValue);
    }
}
