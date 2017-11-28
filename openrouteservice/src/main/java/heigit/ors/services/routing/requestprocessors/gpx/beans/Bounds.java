package heigit.ors.services.routing.requestprocessors.gpx.beans;


public class Bounds {
     private double _minLatitude;
     private double _minLongitude;
     private double _maxLatitude;
     private double _maxLongitude;

    public Bounds(double _minLatitude, double _minLongitude, double _maxLatitude, double _maxLongitude) {
        this._minLatitude = _minLatitude;
        this._minLongitude = _minLongitude;
        this._maxLatitude = _maxLatitude;
        this._maxLongitude = _maxLongitude;
    }

    private double get_minLatitude() {
        return _minLatitude;
    }



    private double get_minLongitude() {
        return _minLongitude;
    }



    private double get_maxLatitude() {
        return _maxLatitude;
    }



    private double get_maxLongitude() {
        return _maxLongitude;
    }


    // Todo are these two needed?
    public double[] getStart() {
        double[] start = {_minLatitude, _minLongitude};
        return start;
    }
    public double[] getStop() {
        double[] stop = {_maxLatitude, _maxLongitude};
        return stop;
    }
}
