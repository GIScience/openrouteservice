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

public class GeoJSONIsochronesResponse {

}
//  @JsonProperty("type")
//   public final String type = "FeatureCollection";

//   @JsonProperty("bbox")
//    @JsonInclude(JsonInclude.Include.NON_EMPTY)
//    @ApiModelProperty(value = "Bounding box that covers all returned isochrones", example = "[49.414057, 8.680894, 49.420514, 8.690123]")
//   public double[] getBBox() {
//       return bbox.getAsArray();
//   }

//public GeoJSONIsochronesResponse(IsochroneRequest request, IsochroneMapCollection isochroneMaps) throws StatusCodeException {

//super(request);

// features

//TravellerInfo traveller = null;

//for (IsochroneMap isoMap : isochroneMaps.getIsochroneMaps()) {

//    new GeoJSONIndividualIsochronesResponse(result, request)

//    traveller = request.getTravellers().get(isoMap.getTravellerId());
// }

// bbox

// info

//*/
/*List<BBox> bboxes = new ArrayList<>();
        for(RouteResult result : routeResults) {
            bboxes.add(result.getSummary().getBBox());
        }

        BBox bounding = GeomUtility.generateBoundingFromMultiple(bboxes.toArray(new BBox[bboxes.size()]));

        bbox = BoundingBoxFactory.constructBoundingBox(bounding, request);*//*



    //}

    @JsonProperty("features")
    public List getRoutes() {
        return routeResults;
    }

    @JsonProperty("properties")
    public IsochronesResponseInfo getProperties() {
        return this.responseInformation;
    }
}
*/
