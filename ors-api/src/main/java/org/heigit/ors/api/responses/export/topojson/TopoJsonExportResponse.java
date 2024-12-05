package org.heigit.ors.api.responses.export.topojson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.heigit.ors.api.responses.common.boundingbox.BoundingBox;
import org.heigit.ors.api.responses.export.ExportResponse;
import org.heigit.ors.common.Pair;
import org.heigit.ors.export.ExportResult;
import org.locationtech.jts.geom.Coordinate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import org.locationtech.jts.geom.LineString;

@Schema(description= "The Export Response conatains edges with edge weights and topology information from the requested BBox")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TopoJsonExportResponse extends ExportResponse {

    @JsonProperty("type")
    public String type;
    @JsonProperty("objects")
    public Layers objects;
    @JsonProperty("arcs")
    public List<List<List<Double>>> arcs;
    @JsonProperty("bbox")
    public List<Double> bbox;


    public TopoJsonExportResponse(ExportResult exportResult) {
        super(exportResult);

        ArrayList<Geometry> geometries;
        geometries = new ArrayList<>();

        ArrayList<Arc> arcList;
        arcList = new ArrayList<>();
        int arcCount = 0;

        Map<Integer, Coordinate> nodes = exportResult.getLocations(); 
        for (Map.Entry<Pair<Integer, Integer>, Double> edgeWeight: exportResult.getEdgeWeights().entrySet()) {
            

            Pair<Integer, Integer> fromTo = edgeWeight.getKey();


            List<Double> from = getXY(nodes, fromTo.first);
            List<Double> to = getXY(nodes, fromTo.second);

            LineString lineString = (LineString) exportResult.getEdgeExtras().get(fromTo).get("geometry");
            long osmId = (long) exportResult.getEdgeExtras().get(fromTo).get("osm_id");

            List<List<Double>> coordinates = List.of(from, to);

            Arc arc = Arc.builder()
                .coordinates(coordinates)
                .build();

            arcList.add(arc);
            

            List<Integer> arcIndexList = List.of(arcCount);
            arcCount += 1;

            Map<String, Object> properties = new HashMap<>();
            properties.put("weight", edgeWeight.getValue());

            Geometry geometry = Geometry.builder()
                .type("LineString")
                .properties(properties)
                .arcs(arcIndexList)
                .build();

            geometries.add(geometry);
        }

        List<List<List<Double>>> arcCoordinateList = arcList.stream()
                .map(Arc::getCoordinates)
                .collect(Collectors.toList());


        Layer layer = Layer.builder()
            .type("GeometryCollection")
            .geometries(geometries)
            .build();

        Layers layers = Layers.builder()
            .layer(layer)
            .build();


        List<List<Double>> transposedArcs = transpose(arcCoordinateList);
        List<Double> boundingBox = minMaxXY(transposedArcs);
        
        type = "Topology";
        objects = layers;
        arcs = arcList;
        bbox = boundingBox;
    }

    private List<Double> minMaxXY(List<List<Double>> coordinates) {
        double minX = Collections.min(coordinates.get(0));
        double minY = Collections.min(coordinates.get(1));
        double maxX = Collections.max(coordinates.get(0));
        double maxY = Collections.max(coordinates.get(1));
        return List.of(minX, minY, maxX, maxY);
    }

    private List<List<Double>> transpose(List<List<List<Double>>> arcCoordinates) {
        List<List<Double>> coordinateList = new ArrayList<>();
        arcCoordinates.forEach(coordinateList::addAll); 
        ArrayList<Double> lon = new ArrayList<>();
        ArrayList<Double> lat = new ArrayList<>();
        for (List<Double> coordinate: coordinateList) {
            lon.add(coordinate.get(0));
            lat.add(coordinate.get(1));
        }
        List<List<Double>> coordinates = List.of(lon, lat);
        return coordinates; 
    }

    private List<Double> getXY(Map<Integer, Coordinate> nodes, int id) {
        Coordinate coordinate = nodes.get(id);
        List<Double> XY = List.of(coordinate.x, coordinate.y);
        return XY;
    }
}
