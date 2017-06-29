package heigit.ors.matrix;

import com.vividsolutions.jts.geom.Coordinate;

public class MatrixLocation {
   private Coordinate _coordinate;
   private String _name;
   private double _snappedDistance;
   
   public MatrixLocation(Coordinate coord, String name, double snappedDistance)
   {
	   _coordinate = coord;
	   _name = name;
	   _snappedDistance = snappedDistance;
   }
   
   public Coordinate getCoordinate()
   {
	   return _coordinate;
   }
   
   public String getName()
   {
	   return _name;
   }
   
   public double getSnappedDistance()
   {
	   return _snappedDistance;
   }
}
