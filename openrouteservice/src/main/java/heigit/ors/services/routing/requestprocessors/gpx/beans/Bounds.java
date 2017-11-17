package heigit.ors.services.routing.requestprocessors.gpx.beans;

import heigit.ors.routing.RouteResult;

import java.math.BigDecimal;

public class Bounds {
     BigDecimal _minLatitude;
     BigDecimal _minLongitude;
     BigDecimal _maxLatitude;
     BigDecimal _maxLongitude;


    private BigDecimal get_minLatitude() {
        return _minLatitude;
    }

    public void set_minLatitude(BigDecimal _minLatitude) {
        this._minLatitude = _minLatitude;
    }

    private BigDecimal get_minLongitude() {
        return _minLongitude;
    }

    public void set_minLongitude(BigDecimal _minLongitude) {
        this._minLongitude = _minLongitude;
    }

    private BigDecimal get_maxLatitude() {
        return _maxLatitude;
    }

    public void set_maxLatitude(BigDecimal _maxLatitude) {
        this._maxLatitude = _maxLatitude;
    }

    private BigDecimal get_maxLongitude() {
        return _maxLongitude;
    }

    public void set_maxLongitude(BigDecimal _maxLongitude) {
        this._maxLongitude = _maxLongitude;
    }

    public Bounds(RouteResult route) {
        // TODO read the start and end coordinates from the route and assign them to the values

    }

    public BigDecimal[] getStart() {
        BigDecimal[] start = {_minLatitude, _minLongitude};
        return start;
    }
    public BigDecimal[] getStop() {
        BigDecimal[] stop = {_maxLatitude, _maxLongitude};
        return stop;
    }
}
