package heigit.ors.geocoding.geocoders;

import com.vividsolutions.jts.geom.Envelope;

public class RectSearchBoundary implements SearchBoundary {
    private Envelope _env;
    
    public RectSearchBoundary(double minx, double miny, double maxx, double maxy)
    {
    	_env = new Envelope(minx, maxx, miny, maxy);
    }
    
    public RectSearchBoundary(Envelope env)
    {
    	_env = env;
    }
    
    public Envelope getRectangle()
    {
    	return _env;
    }
    
    @Override
    public boolean contains(double lon, double lat)
    {
    	return _env.contains(lon,  lat);
    }
}
