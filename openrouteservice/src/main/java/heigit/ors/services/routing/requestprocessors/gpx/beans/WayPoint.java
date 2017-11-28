package heigit.ors.services.routing.requestprocessors.gpx.beans;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class WayPoint {
    private Double _lat;
    private Double _lon;
    private double _ele;
    private final String _dateTime;
    private String _name;
    private String _comment;
    private String _description;
    private String _source;
    private Extensions _extensions = new Extensions();

    public WayPoint(double _lat, double _lon) {
        this._lon = _lon;
        this._lat = _lat;
        this._dateTime = new DateTime().getTime();

    }

    public WayPoint(double _lat, double _lon, double _ele) {
        this._lon = _lon;
        this._lat = _lat;
        this._ele = _ele;
        this._dateTime = new DateTime().getTime();

    }

    public Double get_lon() {
        return _lon;
    }

    public Double get_lat() {
        return _lat;
    }

    public double get_ele() {
        return _ele;
    }

    public String get_dateTime() {
        return _dateTime;
    }

    public String get_name() {
        return _name;
    }

    public void set_name(String _name) {
        this._name = _name;
    }

    public String get_comment() {
        return _comment;
    }

    public void set_comment(String _comment) {
        this._comment = _comment;
    }

    public String get_description() {
        return _description;
    }

    public void set_description(String _description) {
        this._description = _description;
    }

    public String get_source() {
        return _source;
    }

    public void set_source(String _source) {
        this._source = _source;
    }

    public Object get_extension(String key) {
        return _extensions.getValue(key);
    }

    public void add_extension(String key, Object value) {
        _extensions.addValue(key, value);
    }

}
