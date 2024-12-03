package org.heigit.ors.api.responses.export.topojson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.heigit.ors.api.responses.export.ExportResponse;
import org.heigit.ors.common.Pair;
import org.heigit.ors.export.ExportResult;
import org.locationtech.jts.geom.Coordinate;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description= "The Export Response conatains edges with edge weights and topology information from the requested BBox")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TopoJsonExportResponse extends ExportResponse {
    
    public TopoJson topoJson;

    public final String type = "Topology";
    

    public TopoJsonExportResponse(ExportResult exportResult) {
        super(exportResult);
        
        ArrayList<Geometry> geometries;
        geometries = new ArrayList<>();

        Map<Arc, Integer> arcs;
        arcs = new HashMap<>();
        int arcCount = 0;

        Map<Integer, Coordinate> nodes = exportResult.getLocations(); 
        for (Map.Entry<Pair<Integer, Integer>, Double> edgeWeight: exportResult.getEdgeWeights().entrySet()) {
            

            Pair<Integer, Integer> fromTo = edgeWeight.getKey();
            
            
            List<Double> from = getXY(nodes, fromTo.first);
            List<Double> to = getXY(nodes, fromTo.second);

            List<List<Double>> coordinates = List.of(from, to);
            
            Arc arc = Arc.builder()
                .coordinates(coordinates)
                .build();

            arcs.put(arc, arcCount);
            
            List<Integer> arcList = List.of(arcCount);
            
            
            Map<String, Object> properties = new HashMap<>();
            properties.put("weight", edgeWeight.getValue());

            Geometry geometry = Geometry.builder()
                .type("LineString")
                .properties(properties)
                .arcs(arcList)
                .build();
            
            geometries.add(geometry);
        }

        
        Layer layer = Layer.builder()
            .type("GeometryCollection")
            .geometries(geometries)
            .build();
        
        Layers layers = Layers.builder()
            .layer(layer)
            .build();

        topoJson = TopoJson.builder()
            .type(type)
            .objects(layers)
            .arcs(null)
            .bbox(null)
            .build();
    }


    private List<Double> getXY(Map<Integer, Coordinate> nodes, int id) {
        Coordinate coordinate = nodes.get(id);
        List<Double> XY = List.of(coordinate.x, coordinate.y);
        return XY;
    }
}
