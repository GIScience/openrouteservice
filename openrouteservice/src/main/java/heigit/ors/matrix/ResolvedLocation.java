package heigit.ors.matrix;

import com.vividsolutions.jts.geom.Coordinate;

import heigit.ors.common.NamedLocation;

public class ResolvedLocation extends NamedLocation {
   private double _snappedDistance;
   
   public ResolvedLocation(Coordinate coord, String name, double snappedDistance)
   {
	   super(coord, name);
	   _snappedDistance = snappedDistance;
   }
   
   public double getSnappedDistance()
   {
	   return _snappedDistance;
   }
}
