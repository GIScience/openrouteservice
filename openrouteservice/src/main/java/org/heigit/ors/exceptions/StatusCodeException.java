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

public class StatusCodeException extends Exception  {
	private static final long serialVersionUID = 5306540089149750357L;
	
	private int statusCode = 200;
	private int internalCode = 0;
   
	public StatusCodeException(int statusCode, int internalCode) {
		super();
		this.statusCode = statusCode;
		this.internalCode = internalCode;
	}
	
	public StatusCodeException(int statusCode) {
		super();
		this.statusCode = statusCode;
	}
	
	public StatusCodeException(int statusCode, String message) {
		super(message);
		this.statusCode = statusCode;
	}
	
	public StatusCodeException(int statusCode, int internalCode, String message) {
		super(message);
		this.statusCode = statusCode;
		this.internalCode = internalCode;
	}
	
	public int getStatusCode()
	{
		return statusCode;
	}
	
	public int getInternalCode()
	{
		return internalCode;
	}
}
