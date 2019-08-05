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

package heigit.ors.api.responses.isochrones.GeoJSONIsochronesResponseObjects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.graphhopper.util.shapes.BBox;
import com.vividsolutions.jts.geom.Envelope;
import heigit.ors.api.requests.isochrones.IsochronesRequest;
import heigit.ors.api.responses.common.BoundingBox.BoundingBoxFactory;
import heigit.ors.api.responses.isochrones.IsochronesResponse;
import heigit.ors.api.responses.isochrones.IsochronesResponseInfo;
import heigit.ors.exceptions.InternalServerException;
import heigit.ors.exceptions.ParameterValueException;
import heigit.ors.isochrones.IsochroneMap;
import heigit.ors.isochrones.IsochroneMapCollection;
import heigit.ors.isochrones.IsochroneUtility;
import heigit.ors.isochrones.IsochronesIntersection;
import heigit.ors.util.GeomUtility;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

public class GeoJSONIsochronesResponse extends IsochronesResponse {
    @JsonProperty("type")
    public final String type = "FeatureCollection";

    @JsonProperty("bbox")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty(value = "Bounding box that covers all returned isochrones", example = "[49.414057, 8.680894, 49.420514, 8.690123]")
    public double[] getBBoxAsArray() {
        return bbox.getAsArray();
    }

    public GeoJSONIsochronesResponse(IsochronesRequest request, IsochroneMapCollection isoMaps) throws ParameterValueException, InternalServerException {
        super(request);
        this.isochroneResults = new ArrayList<>();
        for (IsochroneMap isoMap : isoMaps.getIsochroneMaps()) {
            this.isochroneResults.addAll(new GeoJSONIsochronesMap(isoMap).buildGeoJSONIsochrones());
        }

        if (request.hasIntersections() && request.getIntersections()) {
            List<IsochronesIntersection> isoIntersections = IsochroneUtility.computeIntersections(isoMaps);
            if (isoIntersections != null && !isoIntersections.isEmpty()) {
                for (IsochronesIntersection isoIntersection : isoIntersections) {
                    this.isochroneResults.add(new GeoJSONIsochronesIntersection(isoIntersection, request));
                }
            }
        }
        constructBBox(isoMaps, request);
    }

    private void constructBBox(IsochroneMapCollection isoMaps, IsochronesRequest request) throws ParameterValueException {
        List<BBox> bboxes = new ArrayList<>();
        for (IsochroneMap isochroneMap : isoMaps.getIsochroneMaps()) {
            Envelope isochroneMapEnvelope = isochroneMap.getEnvelope();
            BBox isochroneMapBBox = new BBox(isochroneMapEnvelope.getMinX(), isochroneMapEnvelope.getMaxX(), isochroneMapEnvelope.getMinY(), isochroneMapEnvelope.getMaxY());
            bboxes.add(isochroneMapBBox);
        }
        BBox bounding = GeomUtility.generateBoundingFromMultiple(bboxes.toArray(new BBox[0]));
        bbox = BoundingBoxFactory.constructBoundingBox(bounding, request);
    }

    @JsonProperty("features")
    public List<GeoJSONIsochroneBase> getIsochrones() {
        return isochroneResults;
    }

    @JsonProperty("metadata")
    public IsochronesResponseInfo getProperties() {
        return this.responseInformation;
    }
}

