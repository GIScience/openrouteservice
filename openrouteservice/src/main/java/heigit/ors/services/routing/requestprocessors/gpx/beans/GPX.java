package heigit.ors.services.routing.requestprocessors.gpx.beans;


import java.util.ArrayList;
import java.util.List;

public class GPX {

    private Metadata metadata;
    private List<WayPoint> _points = new ArrayList<>();
    private List<Route> _routes = new ArrayList<>();
    private List<Track> _tracks = new ArrayList<>();
    private Extensions extensions = new Extensions();


    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public List<WayPoint> get_points() {
        return _points;
    }


    public List<Route> get_routes() {
        return _routes;
    }


    public List<Track> get_tracks() {
        return _tracks;
    }


    public Extensions getExtensions() {
        return extensions;
    }


    public void addPoint(WayPoint wayPoint){
        _points.add(wayPoint);
    }

    public void addRoute(Route route) {
        _routes.add(route);
    }

    public void addTrack(Track track){
        _tracks.add(track);
    }

    public void addExtension(String key, Object value) {
        extensions.addValue(key, value);
    }
}
