

package org.freeopenls.gml;

import org.freeopenls.error.ErrorTypes;
import org.freeopenls.error.ServiceError;
import org.freeopenls.tools.CoordTransform;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;


import net.opengis.gml.AbstractRingPropertyType;
import net.opengis.gml.DirectPositionType;
import net.opengis.gml.LinearRingType;
import net.opengis.gml.PolygonType;


/**
 * Class for read the gml:Polygon Element<br>
 * And to create from the input data a Polygon, Feature or a FeatureCollection
 * 
 * Copyright: Copyright (c) 2008 by Pascal Neis
 *  
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2006-05-02
 * @version 1.1 2008-04-22
 */
public class Polygon {
	/** LinearRing for Exterior Ring */
	private LinearRing mLinerRingExterior = null;
	/** LinearRingArray for Interior Rings */
	private LinearRing mLinearRingsInterior[] = null;
	/** Polygon */
	private com.vividsolutions.jts.geom.Polygon mPolygon = null;
	
    /**
     * Constructor<br>
     * NOT Supported:<br>
     * AbstractGMLType - gid<br>
     * AbstractGeometryType - gml:id<br>
     * 
     * @param targetSRS
     * @param polygonType
     * @throws ServiceError
     */
	public Polygon(String targetSRS, PolygonType polygonType)throws ServiceError{
		GeometryFactory gf = new GeometryFactory();
		String sSRSExterior = "EPSG:4326";
		String sSRSInterior = "EPSG:4326";
		Coordinate cTMP[] = null;

		//*** Exterior ***
		//minOccurs="0"
		if(polygonType.isSetExterior()){
			AbstractRingPropertyType abr = polygonType.getExterior();

			LinearRingType linearRing = (LinearRingType) abr.getRing().changeType(LinearRingType.type);

			if (linearRing.isSetSrsName())
				sSRSExterior = linearRing.getSrsName();

			cTMP = new Coordinate[linearRing.getPosArray().length];

			for (int i = 0; i < linearRing.getPosArray().length; i++) {
				DirectPositionType dpTMP = linearRing.getPosArray(i);

				cTMP[i] = Pos.getCoord(dpTMP.getStringValue());

				if (dpTMP.isSetSrsName() && dpTMP.getSrsName() != null){
					if (!dpTMP.getSrsName().equals(targetSRS))
						cTMP[i] = CoordTransform.transformGetCoord(dpTMP.getSrsName(), targetSRS, cTMP[i]);
				}
				else{
					if (!sSRSExterior.equals(targetSRS))
						cTMP[i] = CoordTransform.transformGetCoord(sSRSExterior, targetSRS, cTMP[i]);
				}
			}
			
			if(cTMP.length < 4)
				throw ErrorTypes.inconsistent("Polygon", "Polygon must include more then 3 Points!");
			if(!cTMP[0].equals(cTMP[cTMP.length-1]))
				throw ErrorTypes.inconsistent("Polygon", "Polygon build not a closed ring!");
				
			mLinerRingExterior = gf.createLinearRing(cTMP);
		}

		//*** Interior ***
		//minOccurs="0" maxOccurs="unbounded"
		AbstractRingPropertyType ab[] = polygonType.getInteriorArray();
		if(ab.length > 0){
			mLinearRingsInterior = new LinearRing[ab.length];
			for(int j=0 ; j < ab.length ; j++){

				LinearRingType linearRing = (LinearRingType) ab[j].getRing().changeType(LinearRingType.type);
			
				if(linearRing.isSetSrsName())
					sSRSInterior = linearRing.getSrsName();
			
				cTMP = new Coordinate[linearRing.getPosArray().length];
				
				for(int i=0 ; i < linearRing.getPosArray().length  ; i++){
					DirectPositionType dpTMP = linearRing.getPosArray(i);
					
					cTMP[i] = Pos.getCoord(dpTMP.getStringValue());
				
					if(dpTMP.isSetSrsName() && dpTMP.getSrsName() != null){
						if (!dpTMP.getSrsName().equals(targetSRS))
							cTMP[i] = CoordTransform.transformGetCoord(dpTMP.getSrsName(), targetSRS, cTMP[i]);
					}
					else{
						if (sSRSInterior != null && !sSRSInterior.equals(targetSRS))
							cTMP[i] = CoordTransform.transformGetCoord(sSRSInterior, targetSRS, cTMP[i]);

					}
				}
				mLinearRingsInterior[j] = gf.createLinearRing(cTMP);
			}
				
		}

		//System.out.println(this.lrExterior);
		//for(int i=0 ; i < this.lrInterior.length ; i++){
		//	System.out.println(this.lrInterior[i]);
		//}
		mPolygon = new com.vividsolutions.jts.geom.Polygon(mLinerRingExterior, mLinearRingsInterior, gf);

		if(!mPolygon.isValid())
			throw ErrorTypes.unknown("Polygon", "Polygon is NOT valid, please check the points of the polygons!");

	}

    /**
     * Method which return the created Polygon from the given data.
     * 
     * @return Polygon
     * 				Returns Polygon
     */
	public com.vividsolutions.jts.geom.Polygon getPolygon(){
		return mPolygon;
	}

    /**
     * Method which creates a Feature from the given data.<br>
     * Attribut = Geometry ; Object = Polygon
     * 
     * @return Feature
     * 				Returns a Feature
     */
	public Feature createFeat(){
		FeatureSchema featureS = new FeatureSchema();
		featureS.addAttribute("Geometry", AttributeType.GEOMETRY);
		Feature feat = new BasicFeature(featureS);
		feat.setAttribute("Geometry", mPolygon);
		
		return feat;
	}

    /**
     * Method which creates a FeatureCollection from the given data.<br>
     * Attribut = Geometry ; Object = Polygon
     * 
     * @return FeatureCollection
     * 				Returns a FeatureCollection
     */
	public FeatureCollection createFeatColl(){
		FeatureSchema featureS = new FeatureSchema();
		featureS.addAttribute("Geometry", AttributeType.GEOMETRY);
		FeatureCollection featcoll = new FeatureDataset(featureS);
		Feature feat = new BasicFeature(featureS);
		feat.setAttribute("Geometry", mPolygon);
		featcoll.add(feat);
		
		return featcoll;
	}
	
    /**
     * Method which calcualte a CenterPoint from the polygon.
     * 
     * @return Coordinate
     * 				Returns Coordinate CenterPoint
     */
	public Coordinate calculateCenterPoint(){
		Coordinate cCenterPoint = new Coordinate();
		double dXTMP = 0;
		double dYTMP = 0;
		Coordinate cTMP[] = mLinerRingExterior.getCoordinates();
		
		for(int i=0 ; i<cTMP.length ; i++){
			dXTMP = dXTMP + cTMP[i].x;
			dYTMP = dYTMP + cTMP[i].y;
		}
		cCenterPoint.x = dXTMP / cTMP.length;
		cCenterPoint.y = dYTMP / cTMP.length;
		
		return cCenterPoint;
	}
}