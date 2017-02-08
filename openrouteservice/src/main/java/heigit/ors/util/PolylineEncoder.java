package heigit.ors.util;

import com.vividsolutions.jts.geom.Coordinate;

public class PolylineEncoder {
	public static String encode(final Coordinate[] coords, StringBuffer buffer) {
		long lat, lon;
	    long prevLat = 0;
	    long prevLon = 0;

	    final StringBuffer result = new StringBuffer();

	    for (final Coordinate c : coords) {
	      lat = Math.round(c.y * 1e5);
	      lon = Math.round(c.x * 1e5);

	      encode(lat - prevLat, result);
	      encode(lon - prevLon, result);

	      prevLat = lat;
	      prevLon = lon;
	    }
	    
	    return result.toString();
	}
	
	private static void encode(long v, StringBuffer buffer) {
	    v = v < 0 ? ~(v << 1) : v << 1;
	    
	    while (v >= 0x20) 
	    {
	      buffer.append(Character.toChars((int) ((0x20 | (v & 0x1f)) + 63)));
	      v >>= 5;
	    }
	    
	    buffer.append(Character.toChars((int) (v + 63)));
	}
}
