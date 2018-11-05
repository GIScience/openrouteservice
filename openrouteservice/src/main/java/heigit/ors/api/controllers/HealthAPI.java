/*
 * This file is part of Openrouteservice.
 *
 * Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, see <https://www.gnu.org/licenses/>.
 */

package heigit.ors.api.controllers;

import heigit.ors.routing.RoutingProfileManagerStatus;
import org.json.simple.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v2/health")
public class HealthAPI {
    @GetMapping
    public ResponseEntity<?> fetchHealth() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject jsonResponse = new JSONObject();
        HttpStatus status;

        if (!RoutingProfileManagerStatus.isReady())
        {
            jsonResponse.put("status", "not ready");
            status = HttpStatus.SERVICE_UNAVAILABLE;
        }
        else
        {
            jsonResponse.put("status", "ready");
            status = HttpStatus.OK;
        }

        return new ResponseEntity<>(jsonResponse.toJSONString(), headers, status);
    }
}
