package heigit.ors.api.responses.routing.GPXRouteResponseObjects;

import com.vividsolutions.jts.geom.Coordinate;
import heigit.ors.routing.RouteResult;
import heigit.ors.routing.RouteSegment;
import heigit.ors.routing.RouteStep;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "rte")
public class GPXRouteElement {
    @XmlElement(name = "rtept")
    List<GPXRoutePointElement> routePoints;

    public GPXRouteElement() { }

    public GPXRouteElement(RouteResult result) {
        routePoints = new ArrayList<>();
        Coordinate[] routeCoordinates = result.getGeometry();
        List<RouteSegment> segments = result.getSegments();
        List<RouteStep> steps = new ArrayList<>();

        for(RouteSegment segment : segments) {
            steps.addAll(segment.getSteps());
        }

        for(int i=0; i<steps.size(); i++) {
            RouteStep step = steps.get(i);
            Coordinate c = routeCoordinates[step.getWayPoints()[0]];
            routePoints.add(new GPXRoutePointElement(step, c.x, c.y, c.z, i));
        }
    }
}
