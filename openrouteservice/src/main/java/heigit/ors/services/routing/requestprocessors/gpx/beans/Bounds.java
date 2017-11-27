package heigit.ors.services.routing.requestprocessors.gpx.beans;


import java.math.BigDecimal;

public class Bounds {
     private BigDecimal _minLatitude;
     private BigDecimal _minLongitude;
     private BigDecimal _maxLatitude;
     private BigDecimal _maxLongitude;

    public Bounds(BigDecimal _minLatitude, BigDecimal _minLongitude, BigDecimal _maxLatitude, BigDecimal _maxLongitude) {
        this._minLatitude = _minLatitude;
        this._minLongitude = _minLongitude;
        this._maxLatitude = _maxLatitude;
        this._maxLongitude = _maxLongitude;
    }

    private BigDecimal get_minLatitude() {
        return _minLatitude;
    }



    private BigDecimal get_minLongitude() {
        return _minLongitude;
    }



    private BigDecimal get_maxLatitude() {
        return _maxLatitude;
    }



    private BigDecimal get_maxLongitude() {
        return _maxLongitude;
    }


    // Todo are these two needed?
    public BigDecimal[] getStart() {
        BigDecimal[] start = {_minLatitude, _minLongitude};
        return start;
    }
    public BigDecimal[] getStop() {
        BigDecimal[] stop = {_maxLatitude, _maxLongitude};
        return stop;
    }
}
