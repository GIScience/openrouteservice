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
package org.heigit.ors.api.servlet.requests;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

/**
 * Suppresses calls to sendError() and uses setStatus() instead to avoid sending a html error page.
 * See {@link jakarta.servlet.http.HttpServletResponse#sendError(int)}
 */
public class StatusCodeCaptureWrapper extends HttpServletResponseWrapper  {

    public StatusCodeCaptureWrapper(HttpServletResponse response) {
        super(response);
    }

    @Override
    public void sendError(int sc) {
        super.setStatus(sc);
    }

    @Override
    public void sendError(int sc, String msg) {
        super.setStatus(sc);
    }
}