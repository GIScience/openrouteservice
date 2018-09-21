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

public class ServerLimitExceededException extends StatusCodeException 
{
	private static final long serialVersionUID = 7128944138955234463L;

	public ServerLimitExceededException(int errorCode, String message)
	{
		super(StatusCode.BAD_REQUEST, errorCode, "Request parameters exceed the server configuration limits. " + message);
	}
	
	public ServerLimitExceededException(String message)
	{
		this(-1, message);
	}
}
