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
package org.heigit.ors.mapmatching;

public class MapMatchingErrorCodes {
   public static final int INVALID_JSON_FORMAT = 800;
   public static final int MISSING_PARAMETER = 801;
   public static final int INVALID_PARAMETER_FORMAT = 802;
   public static final int INVALID_PARAMETER_VALUE = 803;
   public static final int PARAMETER_VALUE_EXCEEDS_MAXIMUM = 804;
   public static final int UNKNOWN = 899;
   private MapMatchingErrorCodes() {}
}
