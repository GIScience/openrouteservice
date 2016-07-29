

package org.freeopenls.gml;

import org.freeopenls.error.ServiceError;
import org.freeopenls.tools.CoordTransform;

import net.opengis.gml.DirectPositionType;
import net.opengis.gml.EnvelopeType;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;


/**
 * Class for read the gml:Envelope Element<br>
 * And to create from the input data a LinearRing, Polygon, Feature or a FeatureCollection
 * 
 * Copyright: Copyright (c) 2008 by Pascal Neis
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * @version 1.0 2006-05-10
 * @version 1.1 2008-04-22
 */
public class Envelope {
	/** GeometryFactory for create LinearRing, Polygon */
	private GeometryFactory mGeometryFactory = new GeometryFactory();
	/** Coordinate Array for Points of the Envelope */
	private Coordinate mCoordinates[] = null;
	
    /**
     * Constructor<br>
     * NOT Supported:<br>
     * AbstractGMLType - gid<br>
     * AbstractGeometryType - gml:id<br>
     * 
     * @param targetSRS
     * @param envelopeType
     * @throws ServiceError
     */
	public Envelope(String targetSRS, EnvelopeType envelopeType)throws ServiceError{
		String sEnvelopeSRS = "EPSG:4326";
		
		//TODO
		//AbstractGeometryType
		//envelopeType.isSetId();	
		//AbstractGMLType
		//envelopeType.isSetGid();

		DirectPositionType[] dpTypeBoundingBox = envelopeType.getPosArray();

		if(envelopeType.isSetSrsName())
			sEnvelopeSRS = envelopeType.getSrsName();

		mCoordinates = new Coordinate[5];
		mCoordinates[0] = Pos.getCoord(dpTypeBoundingBox[0].getStringValue());	//LowerLeftCorner
		mCoordinates[2] = Pos.getCoord(dpTypeBoundingBox[1].getStringValue());	//UpperRightCorner
		mCoordinates[1] = new Coordinate();			//LowerRightCorner
		mCoordinates[3] = new Coordinate();			//UpperLeftCorner
		Coordinate cTMP = new Coordinate();		//TMP
		
		if(!sEnvelopeSRS.equals(targetSRS)){
			mCoordinates[0] = CoordTransform.transformGetCoord(sEnvelopeSRS, targetSRS, mCoordinates[0]);
			mCoordinates[2] = CoordTransform.transformGetCoord(sEnvelopeSRS, targetSRS, mCoordinates[2]);
		}

		//Sort Coordinates
		if(mCoordinates[0].y > mCoordinates[2].y){
			cTMP.y = mCoordinates[0].y; mCoordinates[0].y = mCoordinates[2].y; mCoordinates[2].y = cTMP.y;
		}
		if(mCoordinates[0].x > mCoordinates[2].x){
			cTMP.x = mCoordinates[0].x; mCoordinates[0].x = mCoordinates[2].x; mCoordinates[2].x = cTMP.x;
		}

		//Copy Coordinates
		mCoordinates[1].x = mCoordinates[2].x; mCoordinates[1].y = mCoordinates[0].y; mCoordinates[3].x = mCoordinates[0].x; mCoordinates[3].y = mCoordinates[2].y; mCoordinates[4] = mCoordinates[0];
	}
	
    /**
     * Method which returns the Coordinate Array from the given data.<br>
     * Attention! **LastPoint==FirstPoint** -> LienarRing
     * @return Coordinate[]
     * 				Returns the Coordinate Array of the Envelope 
     */
	public Coordinate[] getCoordinates(){
		return mCoordinates;
	}
	
    /**
     * Method which creates a LinearRing from the given data.
     * 
     * @return LinearRing
     * 				Returns a LinearRing
     */
	public LinearRing createLinearRing(){
		return mGeometryFactory.createLinearRing(mCoordinates);
	}

    /**
     * Method which creates a Polygon from the given data.
     * 
     * @return Polygon
     * 				Returns a Polygon
     */
	public Polygon createPolygon(){
		LinearRing lr = mGeometryFactory.createLinearRing(mCoordinates);
		return mGeometryFactory.createPolygon(lr, null);
	}

    /**
     * Method which creates a Feature from the given data.<br>
     * Attribut = Geometry ; Object = Polygon
     * 
     * @return Feature
     * 				Returns a Feature
     */
	public Feature createFeat(){
		LinearRing lr = mGeometryFactory.createLinearRing(mCoordinates);
		Polygon p = mGeometryFactory.createPolygon(lr, null);

		FeatureSchema featureS = new FeatureSchema();
		featureS.addAttribute("Geometry", AttributeType.GEOMETRY);
		Feature feat = new BasicFeature(featureS);
		feat.setAttribute("Geometry", p);

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
		LinearRing lr = mGeometryFactory.createLinearRing(mCoordinates);
		Polygon p = mGeometryFactory.createPolygon(lr, null);

		FeatureSchema featureS = new FeatureSchema();
		featureS.addAttribute("Geometry", AttributeType.GEOMETRY);
		FeatureCollection featcoll = new FeatureDataset(featureS);
		Feature feat = new BasicFeature(featureS);
		feat.setAttribute("Geometry", p);
		featcoll.add(feat);
		
		return featcoll;
	}
}
