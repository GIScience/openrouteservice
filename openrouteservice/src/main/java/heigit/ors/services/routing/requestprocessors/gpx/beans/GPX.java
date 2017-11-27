package heigit.ors.services.routing.requestprocessors.gpx.beans;


import java.math.BigDecimal;
import java.util.List;

public class GPX extends Route {

    private List<WayPoint> _points;
    private List<Route> _routes;
    private List<Track> _tracks; // Not implemented so far TODO implement tracks??
    private Extensions extensions;


    public List<Route> get_routes() {
        return _routes;
    }

    public void set_routes(List<Route> _routes) {
        this._routes = _routes;
    }

    public void addRoute(Route route) {
        this._routes.add(route);
    }

    public List<Track> get_tracks() {
        return _tracks;
    }

    public void set_tracks(List<Track> _tracks) {
        this._tracks = _tracks;
    }

    public List<WayPoint> get_points() {
        return _points;
    }

    public void set_points(List<WayPoint> _points) {
        this._points = _points;
    }

    public void setExtensions() {
        this.extensions = null;
    }

    public Object getExtension(String key) {
        return extensions.getValue(key);
    }

    public void addExtension(String key, Object value) {
        extensions.addValue(key, value);
    }
}
