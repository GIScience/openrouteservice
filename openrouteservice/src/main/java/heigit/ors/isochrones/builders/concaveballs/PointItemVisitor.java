package heigit.ors.isochrones.builders.concaveballs;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.index.ItemVisitor;

public class PointItemVisitor implements ItemVisitor 
{
	private double _threshold;
	private boolean bFound;
	private double lat;
	private double lon;

	public PointItemVisitor(double lon, double lat, double threshold) {
		this.lat = lat;
		this.lon = lon;
		this._threshold = threshold;
	}

	public void setThreshold(double value)
	{
		_threshold = value;
	}
	
	public void setPoint(double lon, double lat) {
		this.lat = lat;
		this.lon = lon;
		bFound = false;
	}

	public void visitItem(Object item) {
		if (bFound == false) {
			Coordinate p = (Coordinate) item;

			double dx = p.x - lon;
			if (dx > _threshold)
				return;

			double dy = p.y - lat;
			if (Math.abs(dy) > _threshold )
				return;

			double dist = Math.sqrt(dx*dx+dy*dy);
			if (dist < _threshold)
				bFound = true;
		}
	}

	public boolean isNeighbourFound() {
		return bFound;
	}
}

