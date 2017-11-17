package heigit.ors.services.routing.requestprocessors.gpx.beans;

import java.util.List;

public class GPX {

    private String VERSION = "1.1";
    private String CREATOR = "ORS - https://www.openrouteservice.org";
    private String _creator;
    private String _version;
    private Metadata _metadata;
    private List<Route> _routes;

    public String getVERSION() {
        return VERSION;
    }

    public void setVERSION(String VERSION) {
        this.VERSION = VERSION;
    }

    public String getCREATOR() {
        return CREATOR;
    }

    public void setCREATOR(String CREATOR) {
        this.CREATOR = CREATOR;
    }

    public String get_creator() {
        return _creator;
    }

    public void set_creator(String _creator) {
        this._creator = _creator;
    }

    public String get_version() {
        return _version;
    }

    public void set_version(String _version) {
        this._version = _version;
    }

    public Metadata get_metadata() {
        return _metadata;
    }

    public void set_metadata(Metadata _metadata) {
        this._metadata = _metadata;
    }

    public List<Route> get_routes() {
        return _routes;
    }

    public void addRoute(Route route) {
        this._routes.add(route);
    }

    public String write() {
        return null;
    }
}
