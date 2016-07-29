package org.freeopenls.location.search;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.text.DecimalFormat;

import net.opengis.gml.DirectPositionType;
import net.opengis.gml.PointType;
import net.opengis.xls.DistanceType;
import net.opengis.xls.DistanceUnitType;
import net.opengis.xls.POIWithDistanceType;
import net.opengis.xls.PointOfInterestType;

import org.freeopenls.tools.CoordTransform;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class SearchUtility {

	public static POIWithDistanceType addLocation(ResultSet resultSet, String databaseSRS, String responseSRS, Coordinate location, boolean useDisabled, WKTReader reader)throws Exception{
		if (location == null)
		   location = new Coordinate();
		
		Coordinate coordinate = getCoordinate(resultSet.getString("geom"), reader);
		if(!databaseSRS.equals(responseSRS)){
			coordinate = CoordTransform.transformGetCoord(databaseSRS, responseSRS, coordinate); 
		}
		
		//POIWithDistance
		POIWithDistanceType poiWithDistance = POIWithDistanceType.Factory.newInstance();
		
		//POI
		PointOfInterestType poi = poiWithDistance.addNewPOI();
		String poiName = resultSet.getString("name");
		if(poiName==null ||poiName.equals(""))
			poiName = "";
		poi.setPOIName(poiName);
		String poiType = resultSet.getString("type");
		
		if (useDisabled)
		{
			String disabled = resultSet.getString("disabled");
			poi.setDescription(poiType+";"+disabled);
		}
		else
		{
			poi.setDescription(poiType);
		}
		
		poi.setID(resultSet.getString("osm_id"));
		
		PointType point = poi.addNewPoint();
		//Point
		DirectPositionType directpos = point.addNewPos();
		directpos.setStringValue(coordinate.x+" "+coordinate.y);
		directpos.setSrsName(responseSRS);

		//Distance
		DistanceType distance = poiWithDistance.addNewDistance();
		double dDistanceToAddress = location.distance(coordinate);
		dDistanceToAddress = 0.0; // Set distance to 0.0 for this request
		
		if(dDistanceToAddress > 1000){
			dDistanceToAddress = dDistanceToAddress/1000;
			distance.setUom(DistanceUnitType.KM);
		}
		else
			distance.setUom(DistanceUnitType.M);

		DecimalFormat df = new DecimalFormat("0");
		distance.setValue(new BigDecimal(df.format(dDistanceToAddress)));
	
		return poiWithDistance;
	}
	
	private static Coordinate getCoordinate(String resultset, WKTReader reader){
		Point point = null;
		try{
			Geometry geom = reader.read(resultset);

			if (geom instanceof MultiPoint)
				point = (Point) ((MultiPoint) geom).getGeometryN(0);
			else if (geom instanceof Point)
				point = (Point) geom;
		}catch(ParseException pe){
		//	mLogger.error(pe);
		}
		return point.getCoordinate();
	}
}
