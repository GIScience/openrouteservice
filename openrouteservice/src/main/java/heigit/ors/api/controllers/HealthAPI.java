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
@RequestMapping("/health")
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
