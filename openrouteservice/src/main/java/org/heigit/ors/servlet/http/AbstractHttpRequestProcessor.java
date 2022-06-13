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
package org.heigit.ors.servlet.http;

import org.apache.log4j.Logger;
import org.heigit.ors.exceptions.InternalServerException;

import javax.servlet.http.HttpServletRequest;

public abstract class AbstractHttpRequestProcessor implements HttpRequestProcessor {
    protected static Logger logger = Logger.getLogger(AbstractHttpRequestProcessor.class);

    protected HttpServletRequest request;

    public AbstractHttpRequestProcessor(HttpServletRequest request) throws Exception {
        if (request == null)
            throw new InternalServerException();

        this.request = request;
    }

    public void destroy() {}
}
