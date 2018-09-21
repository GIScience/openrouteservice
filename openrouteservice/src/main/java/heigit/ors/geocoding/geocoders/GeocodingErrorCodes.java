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
package heigit.ors.geocoding.geocoders;

/**
 * This Class handles the error Codes as described in the error_codes.md
 *
 * @author OpenRouteServiceTeam
 * @author Julian Psotta, julian@openrouteservice.org
 */
public class GeocodingErrorCodes {
   public static int INVALID_JSON_FORMAT = 1000;
   public static int MISSING_PARAMETER = 1001;
   public static int INVALID_PARAMETER_FORMAT = 1002;
   public static int INVALID_PARAMETER_VALUE = 1003;
   public static int PARAMETER_VALUE_EXCEEDS_MAXIMUM = 1004;
   public static int EXPORT_HANDLER_ERROR = 1006;
   public static int UNSUPPORTED_EXPORT_FORMAT = 1007;
   public static int EMPTY_ELEMENT = 1008;
   public static int ADDRESS_NOT_FOUND = 1009;
   public static int UNKNOWN = 1099;
}
