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
package org.heigit.ors.common;

public class StatusCode {
    /**
     * Status code (200) indicating the request succeeded normally.
     */
    public static final int OK = javax.servlet.http.HttpServletResponse.SC_OK;
	 /**
     * Status code (400) indicating the request sent by the client was
     * syntactically incorrect.
     */
   public static final int BAD_REQUEST = javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
   /**
    * Status code (405) indicating that the method specified in the
    * <code><em>Request-Line</em></code> is not allowed for the resource
    * identified by the <code><em>Request-URI</em></code>.
    */
   public static final int METHOD_NOT_ALLOWED = javax.servlet.http.HttpServletResponse.SC_METHOD_NOT_ALLOWED;
   
   /**
    * Status code (500) indicating an error inside the HTTP server
    * which prevented it from fulfilling the request.
    */
   public static final int INTERNAL_SERVER_ERROR = javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
   
   /**
    * Status code (501) indicating the HTTP server does not support
    * the functionality needed to fulfill the request.
    */
   public static final int NOT_IMPLEMENTED = javax.servlet.http.HttpServletResponse.SC_NOT_IMPLEMENTED;
   
   /**
    * Status code (503) indicating that the End Point is
    * temporarily overloaded, and unable to handle the request.
    */
   public static final int SERVICE_UNAVAILABLE =  javax.servlet.http.HttpServletResponse.SC_SERVICE_UNAVAILABLE;

   /**
    * Status code (404) indicating that the request was processed but
    * no information was found (i.e. a geocoding request that did not find a corresponding address)
    */
   public static final int NOT_FOUND = javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

   private StatusCode() {}
}
