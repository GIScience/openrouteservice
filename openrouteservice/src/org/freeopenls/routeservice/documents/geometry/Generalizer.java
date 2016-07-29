
package org.freeopenls.routeservice.documents.geometry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;

/**
 * <p><b>Title: FileDelete</b></p>
 * <p><b>Description:</b> Class for Generailze:<br>
 * - a FeatureCollection with/without the inidiction of a maxium count of points.<br>
 * - LineString<br>
 * *Douglas-Peucker Algorithmus*</p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008 by Pascal Neis</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2006-05-01
 */
public class Generalizer {

    /**
     * Method for generalize a FeatureCollection
     * 
     * @param featcoll
     *				FeatureCollection with Geometry
     * @param dFlatness
     *				double value for flatness deviation
     * @param iMaxPoints
     *				int value for the maxium count of points in FeatureCollesction
     * @param dFlatnessBoost
     *				double value for flatness deviation boost
     * @return FeatureCollection
     * 				Returns a generalize FeatureCollection
     */
	public static FeatureCollection generalizeFeatureCollection(FeatureCollection featcoll, double dFlatness, int iMaxPoints, double dFlatnessBoost) {
		//New FeatureCollection with old FeatureSchema
		FeatureCollection featcollNEW = new FeatureDataset(featcoll.getFeatureSchema());
		//Feature List, to gets the Features (LineStrings)
		List featlist = featcoll.getFeatures();
		int iNumberOfPointsTotal = 1;
		Vector<Coordinate[]> vc = new Vector<Coordinate[]>();
		
		for (int i = 0; i < featlist.size(); i++) {
			Feature feat = (Feature) featlist.get(i);
			LineString lineTMP = (LineString) feat.getGeometry();
			vc.add(lineTMP.getCoordinates());
			iNumberOfPointsTotal = iNumberOfPointsTotal + lineTMP.getNumPoints() - 1;
		}
		Coordinate c[] = new Coordinate[iNumberOfPointsTotal];
		
		//Save FirstPoint
		Coordinate cTMPFirst[] = vc.get(0);
		c[0] = cTMPFirst[0];
		
		int iNum = 1;
		//Save Points in NewCoordinateArray
		for(int i=0 ; i < vc.size() ; i++){
			Coordinate cTMP[] = vc.get(i);
			for(int i2=1 ; i2 < cTMP.length ; i2++){
				c[iNum] = cTMP[i2];
				iNum++;
			}
		}
		
		GeometryFactory gf = new GeometryFactory();	
		LineString lineTMP = gf.createLineString(c);
		LineString ls = generalizeLineString(lineTMP, dFlatness);

		if(ls.getNumPoints() > iMaxPoints)
			return generalizeFeatureCollection(featcoll, dFlatness+dFlatnessBoost, iMaxPoints, dFlatnessBoost);
		else{
			Feature featNEW = new BasicFeature(featcoll.getFeatureSchema());
			featNEW.setAttribute("EdgeID", 0);
			featNEW.setAttribute("Geometry", ls);
			featcollNEW.add(featNEW);

			return featcollNEW;
		}

	}
	
    /**
     * Method for generalize a FeatureCollection
     * 
     * @param featcoll
     *				FeatureCollection with Geometry
     * @param sAttributeNameID
     * 				AttributName for ID to identify the Attribute in the FeatureCollection
     * @param dFlatness
     *				double value for flatness deviation
     * @param iMaxPoints
     *				int value for the maxium count of points in FeatureCollesction
     * @param dFlatnessBoost
     *				double value for flatness deviation boost
     * @return FeatureCollection
     * 				Returns a generalize FeatureCollection
     */
	public static FeatureCollection generalizeFeatureCollection(FeatureCollection featcoll, String sAttributeNameID, double dFlatness, int iMaxPoints, double dFlatnessBoost) {
		//New FeatureCollection with old FeatureSchema
		FeatureCollection featcollNEW = new FeatureDataset(featcoll.getFeatureSchema());
		//Feature List, to gets the Features (LineStrings)
		List featlist = featcoll.getFeatures();
		int iNumberOfPointsTotal = 1;

		for (int i = 0; i < featlist.size(); i++) {
			Feature feat = (Feature) featlist.get(i);
			Feature featNEW = new BasicFeature(featcoll.getFeatureSchema());
			LineString lineTMP = (LineString) feat.getGeometry();
			
			//Generalize only, if the line has more than 2 points (Start & End)
			if(lineTMP.getNumPoints() > 2){
				featNEW.setAttribute("EdgeID", feat.getAttribute(sAttributeNameID));
				LineString lsTMP = generalizeLineString(lineTMP, dFlatness);
				iNumberOfPointsTotal = iNumberOfPointsTotal + lsTMP.getNumPoints() - 1;
				featNEW.setAttribute("Geometry", lsTMP);
			}else{
				featNEW.setAttribute("EdgeID", feat.getAttribute(sAttributeNameID));
				featNEW.setAttribute("Geometry", lineTMP);
			}
			iNumberOfPointsTotal = iNumberOfPointsTotal + 1;
			featcollNEW.add(featNEW);
		}

		if(iNumberOfPointsTotal > iMaxPoints)
			return generalizeFeatureCollection(featcollNEW, sAttributeNameID, dFlatness + dFlatnessBoost, iMaxPoints, dFlatnessBoost);
		else
			return featcollNEW;
	}
	
    /**
     * Method for generalize a FeatureCollection
     * 
     * @param featcoll
     *				FeatureCollection with Geometry
     * @param sAttributeNameID
     * 				AttributName for ID to identify the Attribute in the FeatureCollection
     * @param dFlatness
     *				double value for flatness deviation
     * @return FeatureCollection
     * 				Returns a generalize FeatureCollection
     */
	public static FeatureCollection generalizeFeatureCollection(FeatureCollection featcoll, String sAttributeNameID, double dFlatness) {
		//New FeatureCollection with old FeatureSchema
		FeatureCollection featcollNEW = new FeatureDataset(featcoll.getFeatureSchema());
		//Feature List, to gets the Features (LineStrings)
		List featlist = featcoll.getFeatures();

		for (int i = 0; i < featlist.size(); i++) {
			Feature feat = (Feature) featlist.get(i);
			Feature featNEW = new BasicFeature(featcoll.getFeatureSchema());
			LineString lineTMP = (LineString) feat.getGeometry();
			
			//Generalize only, if the line has more than 2 points (Start & End)
			if(lineTMP.getNumPoints() > 2){
				featNEW.setAttribute("EdgeID", feat.getAttribute(sAttributeNameID));
				featNEW.setAttribute("Geometry", generalizeLineString(lineTMP, dFlatness));
			}else{
				featNEW.setAttribute("EdgeID", feat.getAttribute(sAttributeNameID));
				featNEW.setAttribute("Geometry", lineTMP);
			}
			featcollNEW.add(featNEW);
		}
		return featcollNEW;
	}

    /**
     * Method for generalize a LineString<br>
     * *Douglas-Peucker Algorithmus*
     * 
     * @param line
     *				LineString
     * @param dFlatness
     *				double value for flatness deviation
     * @return LineString
     * 				Returns a generalize LineString
     */
	public static LineString generalizeLineString(LineString line, double dFlatness) {
		
		double a1, a2, b1, b2;
		double sq, dq=0;
		int i = 0, j, k, l;
		double fq = dFlatness * dFlatness;
		double dMAX = 0;
		int iCoordNrTMP = 0;
		//ArrayList for generalize Points
		ArrayList<Integer> iCoordList = new ArrayList<Integer>();
		//Add First Point to the ArrayList
		iCoordList.add(0);
		
		//Check if the linestring is a closed ring/line, then end with the former of the last point
		if(line.getCoordinateN(0).equals(line.getCoordinateN(line.getNumPoints() - 1))){
			iCoordList.add(line.getNumPoints() - 2);
		}
		else{//Add Last Point to the List
			iCoordList.add(line.getNumPoints() - 1);
		}
		
		for (j = 1 ; j < iCoordList.size() ; j++) {
			i = iCoordList.get(j-1);
			l = iCoordList.get(j);
			b1 = line.getCoordinateN(l).x - line.getCoordinateN(i).x;
			b2 = line.getCoordinateN(l).y - line.getCoordinateN(i).y;
			sq = b1 * b1 + b2 * b2;
			dMAX = 0;
			if (sq == 0)
				continue; //????

			//Search the complete Line between Point l & i to find the maximum difference
			for (k=i+1 ; k <= l ; k++) {
				a1 = line.getCoordinateN(k).x - line.getCoordinateN(i).x;
				a2 = line.getCoordinateN(k).y - line.getCoordinateN(i).y;
				dq = (a1 * b2 - a2 * b1) * (a1 * b2 - a2 * b1) / sq;

				if(dq > dMAX){
					dMAX = dq;
					iCoordNrTMP = k;
				}
			}
			//If dMax > dFlatness*dFlatness save the PointNumber in the ArrayList And starts the loop with j=0
			if (dMAX > fq) {
				iCoordList.add(iCoordNrTMP);
				Collections.sort(iCoordList);
				j=0;
			}
		}

		//If LineString is a closed ring/line, add the first as last point to the list.
		if(line.getCoordinateN(0).equals(line.getCoordinateN(line.getNumPoints() - 1))){
			iCoordList.add(line.getNumPoints() - 1);
		}

		//Copy the ArrayList in a CoordinateArray to create a LineString
		Coordinate[] cArray = new Coordinate[iCoordList.size()];
		for (int v = 0; v < iCoordList.size() ; v++) {
			cArray[v] = line.getCoordinateN(iCoordList.get(v));
		}
		//GeometryFactory for create a LineString
		GeometryFactory gf = new GeometryFactory();
		return gf.createLineString(cArray);
	}
	
    /**
     * Method for generalize a Polygon LineString<br>
     * *Douglas-Peucker Algorithmus*
     * 
     * @param line
     *				LineString
     * @param dFlatness
     *				double value for flatness deviation
     * @return LineString
     * 				Returns a generalize LineString
     */
	public static LineString generalizePolygonLineString(LineString line, double dFlatness) {
		
		double a1, a2, b1, b2;
		double sq, dq=0;
		int i = 0, j, k, l;
		double fq = dFlatness * dFlatness;
		double dMAX = 0;
		int iCoordNrTMP = 0;
		//ArrayList for generalize Points
		ArrayList<Integer> iCoordList = new ArrayList<Integer>();
		//Add First Point to the ArrayList
		iCoordList.add(0);
		
		//Check if the linestring is a polygon, then end with the former of the last point
		if(line.getNumPoints()>4 && line.getCoordinateN(0).equals(line.getCoordinateN(line.getNumPoints() - 1))){
			iCoordList.add(line.getNumPoints() - 2);
		}
		else{//Add Last Point to the List
			iCoordList.add(line.getNumPoints() - 1);
		}

		for (j = 1 ; j < iCoordList.size() ; j++) {
			i = iCoordList.get(j-1);
			l = iCoordList.get(j);
			b1 = line.getCoordinateN(l).x - line.getCoordinateN(i).x;
			b2 = line.getCoordinateN(l).y - line.getCoordinateN(i).y;
			sq = b1 * b1 + b2 * b2;
			dMAX = 0;
			if (sq == 0)
				continue; //????

			//Search the complete Line between Point l & i to find the maximum difference
			for (k=i+1 ; k <= l ; k++) {
				a1 = line.getCoordinateN(k).x - line.getCoordinateN(i).x;
				a2 = line.getCoordinateN(k).y - line.getCoordinateN(i).y;
				dq = (a1 * b2 - a2 * b1) * (a1 * b2 - a2 * b1) / sq;

				if(dq > dMAX){
					dMAX = dq;
					iCoordNrTMP = k;
				}
			}
			//If dMax > dFlatness*dFlatness save the PointNumber in the ArrayList And starts the loop with j=0
			if (dMAX > fq) {
				iCoordList.add(iCoordNrTMP);
				Collections.sort(iCoordList);
				j=0;
			}
		}

		//If LineString is a Polygon, add the first as last point to the list.
		if(line.getNumPoints()>4 && line.getCoordinateN(0).equals(line.getCoordinateN(line.getNumPoints() - 1))){
			iCoordList.add(line.getNumPoints() - 1);
		}
		
		if(iCoordList.size() < 4)
			//Return the original LineString, if the number of point < 4. Because a polygon must have 4 points! 
			return line;
		else{
			//Copy the ArrayList in a CoordinateArray to create a LineString
			Coordinate[] cArray = new Coordinate[iCoordList.size()];
			for (int v = 0; v < iCoordList.size() ; v++) {
				cArray[v] = line.getCoordinateN(iCoordList.get(v));
			}
			//GeometryFactory for create a LineString
			GeometryFactory gf = new GeometryFactory();
			return gf.createLineString(cArray);
		}
	}
}
