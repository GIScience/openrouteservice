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

public class ParameterValueException extends StatusCodeException  {
   private static final long serialVersionUID = 507243355121086541L;

   public ParameterValueException(int errorCode, String paramName) {
	   super(StatusCode.BAD_REQUEST, errorCode, String.format("Parameter '%s' has incorrect value or format.", paramName));
   }
   
   public ParameterValueException(int errorCode, String paramName, String paramValue) {
	   super(StatusCode.BAD_REQUEST, errorCode, String.format("Parameter '%s' has incorrect value of '%s'.", paramName, paramValue));
   }

   public ParameterValueException(int errorCode, String paramName, String paramValue, String extraInformation) {
        super(StatusCode.BAD_REQUEST, errorCode, String.format("Parameter '%s' has incorrect value of '%s'. %s", paramName, paramValue, extraInformation));
   }
   
   public ParameterValueException(String paramName)
   {
	   this(0, paramName);
   }
}
