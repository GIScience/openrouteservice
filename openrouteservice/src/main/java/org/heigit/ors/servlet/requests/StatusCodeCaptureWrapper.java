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
package org.heigit.ors.servlet.requests;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;

// suppress calls to sendError() and just setStatus() instead
// do NOT use sendError() otherwise per servlet spec the container will send an html error page
public class StatusCodeCaptureWrapper extends HttpServletResponseWrapper  {
    private Integer statusCode;

    public StatusCodeCaptureWrapper(HttpServletRequest request, HttpServletResponse response) {
        super(response);
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    @Override
    public void sendError(int sc) throws IOException {
        // do NOT use sendError() otherwise per servlet spec the container will send an html error page
        this.setStatus(sc);
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        // do NOT use sendError() otherwise per servlet spec the container will send an html error page
        this.setStatus(sc, msg);
    }

    @Override
    public void setStatus(int sc) {
        this.statusCode = sc;
        super.setStatus(sc);
    }

    @Override
    public void setStatus(int sc, String sm) {
        this.statusCode = sc;
        super.setStatus(sc);
    }
}