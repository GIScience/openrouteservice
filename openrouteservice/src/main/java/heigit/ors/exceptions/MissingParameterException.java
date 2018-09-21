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
package heigit.ors.exceptions;

import heigit.ors.common.StatusCode;

public class MissingParameterException extends StatusCodeException 
{
   private static final long serialVersionUID = 507243355121086541L;

   public MissingParameterException(int errorCode, String paramName)
   {
	   super(StatusCode.BAD_REQUEST, errorCode, "Parameter '" + paramName + "' is missing.");
   }
   
   public MissingParameterException(String paramName)
   {
	   this(0, paramName);
   }
}
