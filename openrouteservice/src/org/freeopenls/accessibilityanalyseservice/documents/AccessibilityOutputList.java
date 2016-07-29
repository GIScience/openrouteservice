/****************************************************
 Copyright (C) 2007 by Pascal Neis

 Author: Pascal Neis

 Contact: Pascal Neis, Herm-Schuster-Str. 57,
 65510 Hünstetten, Germany, pascal.neis@gmail.com
*****************************************************/

package org.freeopenls.accessibilityanalyseservice.documents;

import java.text.DecimalFormat;
import java.util.ArrayList;


import org.freeopenls.accessibilityanalyseservice.AASConfigurator;
import org.freeopenls.accessibilityanalyseservice.Locality;
import org.freeopenls.tools.CoordTransform;

import net.opengis.gml.DirectPositionType;
import net.opengis.gml.PointType;

import de.fhMainz.geoinform.aas.AccessibilityOutputListType;
import de.fhMainz.geoinform.aas.AccessibilityOutputRequestType;
import de.fhMainz.geoinform.aas.AccessibilityOutputType;
import de.fhMainz.geoinform.aas.DistanceUnitType;

/**
 * Class for AccessibilityOutputList.<br>
 * Defines the OutputList of the Accessiblity Analyse Locations.
 * 
 * @author Pascal Neis,	pascal.neis@geoinform.fh-mainz.de
 * @version 1.0 2007-01-05
 */
public class AccessibilityOutputList {

	/**
	 * Method that generate OutputList of the Accessiblity Analyse Locations.
	 * 
	 * @param accessoutpureqType
	 * @param listAccessibilityLocality
	 * @return AccessibilityOutputListType
	 * @throws ServiceError
	 */
	public static AccessibilityOutputListType getAnalyseOutputList(AASConfigurator AASconfig, AccessibilityOutputRequestType accessoutpureqType, String sSRS, String sResponseSRS,  ArrayList<Locality> listAccessibilityLocality)throws ServiceError{
		///////////////////////////////////////
		//*** Check and Set variables for AccessibilityOutputList ***
		Boolean boolName = false;					//Optional
		Boolean boolTime = false;					//Optional
		Boolean boolDistance = false;				//Optional
		Boolean boolCoordinate = false;				//Optional
		DistanceUnitType.Enum distUnit = DistanceUnitType.M;	//Default Unit for Distance

		if(accessoutpureqType.isSetName())
			boolName = accessoutpureqType.getName();
		if(accessoutpureqType.isSetTime())
			boolTime = accessoutpureqType.getTime();
		if(accessoutpureqType.isSetDistance())
			boolDistance = accessoutpureqType.getDistance();
		if(accessoutpureqType.isSetDistanceUnit())
			distUnit = accessoutpureqType.getDistanceUnit();
		if(accessoutpureqType.isSetCoordinate())
			boolCoordinate = accessoutpureqType.getCoordinate();
		
		///////////////////////////////////////
        //*** Create new AccessibilityOutputList Type ***
		AccessibilityOutputListType accessoutputlistType = AccessibilityOutputListType.Factory.newInstance();
		//For Nice Output!!
		DecimalFormat df = new DecimalFormat( "0.00" );
		DecimalFormat dfCoord = new DecimalFormat( "0.0000000" );

		//Adds Locations
		for(int iIndex=0 ; iIndex < listAccessibilityLocality.size() ; iIndex++){
			AccessibilityOutputType accessoutTMP = accessoutputlistType.addNewAccessibilityOutput();
			Locality localityTMP = listAccessibilityLocality.get(iIndex);
			
			//Set ID
			accessoutTMP.setID(localityTMP.sID);
			//Set Name
			if(boolName)
				accessoutTMP.setName(localityTMP.sName);
			//Set Time
			if(boolTime)
				accessoutTMP.setTime(Duration.getGDuration((int)localityTMP.dTime));
			//Set Distance
			if(boolDistance)
				accessoutTMP.setDistance(df.format(DistanceUnit.getDistanceFromMeterToXY(distUnit,localityTMP.dDistance)).replace(",","."));
			//Set Coordinate
			if(boolCoordinate){
				PointType point = accessoutTMP.addNewPoint();
				if(!sSRS.equals(sResponseSRS)){
					try{
						localityTMP.cLocation = CoordTransform.transformGetCoord(sSRS, sResponseSRS, localityTMP.cLocation);
					}catch (org.freeopenls.error.ServiceError se) {
						// TODO: handle exception
					}
				}
				point.setSrsName(sResponseSRS);
				DirectPositionType directpos = point.addNewPos();
				directpos.setStringValue(dfCoord.format(localityTMP.cLocation.x).replace(",",".") +" "+ dfCoord.format(localityTMP.cLocation.y).replace(",","."));
			}
		}
		
		return accessoutputlistType;
	}
}