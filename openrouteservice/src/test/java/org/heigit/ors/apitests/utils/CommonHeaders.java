package org.heigit.ors.apitests.utils;

import io.restassured.http.Header;
import io.restassured.http.Headers;

public class CommonHeaders {
    public static Header contentTypeJson = new Header("Content-Type", "application/json");
    public static Header acceptApplicationGeoJson = new Header("Accept", "application/geo+json");
    public static Header acceptApplicationJson = new Header("Accept", "application/json");
    public static Header acceptApplicationGpx = new Header("Accept", "application/gpx+xml");

    public static Headers jsonContent = new Headers(contentTypeJson, acceptApplicationJson);
    public static Headers geoJsonContent = new Headers(contentTypeJson, acceptApplicationGeoJson);
    public static Headers gpxContent = new Headers(contentTypeJson, acceptApplicationGpx);
}
