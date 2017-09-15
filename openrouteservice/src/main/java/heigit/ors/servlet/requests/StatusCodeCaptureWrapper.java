/*
 *  Licensed to GIScience Research Group, Heidelberg University (GIScience)
 *
 *   http://www.giscience.uni-hd.de
 *   http://www.heigit.org
 *
 *  under one or more contributor license agreements. See the NOTICE file 
 *  distributed with this work for additional information regarding copyright 
 *  ownership. The GIScience licenses this file to you under the Apache License, 
 *  Version 2.0 (the "License"); you may not use this file except in compliance 
 *  with the License. You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package heigit.ors.servlet.requests;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

// suppress calls to sendError() and just setStatus() instead
// do NOT use sendError() otherwise per servlet spec the container will send an html error page
public class StatusCodeCaptureWrapper extends HttpServletResponseWrapper 
{
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