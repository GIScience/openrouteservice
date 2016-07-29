/****************************************************
 Copyright (C) 2006-2007 by Pascal Neis

 Author: Pascal Neis

 Contact: Pascal Neis, Herm-Schuster-Str. 57,
 65510 Hünstetten, Germany, pascal.neis@gmail.com
*****************************************************/

package org.freeopenls.accessibilityanalyseservice.location;


import org.freeopenls.accessibilityanalyseservice.documents.ServiceError;
import org.freeopenls.gml.Pos;
import org.freeopenls.tools.CoordTransform;

import net.opengis.gml.PointType;

import com.vividsolutions.jts.geom.Coordinate;

import de.fhMainz.geoinform.aas.PositionType;

/**
 * Class for read the aas:Position element
 * 
 * @author Pascal Neis, pascal.neis@geoinform.fh-mainz.de
 * @version 1.0 2007-01-03
 */
public class Position{

	/**
	 * Method that returns a coordinate for the requested Position.<br>
	 * 
	 * @param posType - Positon
	 * @return Coordinate of the Position
	 * @throws ServiceError
	 */
	public static Coordinate getPosition(String sDefaultSRS, String sResultSRS, 
			PositionType posType)throws ServiceError{
		Coordinate cTMP = new Coordinate();
		
		//Get Point
		PointType pointType = posType.getPoint();
		
		//Get SRS
		String sPointSRS = sDefaultSRS;
		if(pointType.isSetSrsName()){
			sPointSRS = CoordTransform.getEPSGCode(pointType.getSrsName());
		}

		try{
			//Get Coordinate
			cTMP = Pos.getCoord(pointType.getPos().getStringValue());
		}catch(org.freeopenls.error.ServiceError se){
			System.out.println("Position "+se.getMessages());
		}
		
		// Check SRS
		if(!sResultSRS.equalsIgnoreCase(sPointSRS)){
			try{
				cTMP = CoordTransform.transformGetCoord(sPointSRS, sResultSRS, cTMP);
			}catch (org.freeopenls.error.ServiceError se ) {
				System.out.println("Position "+se.getMessages());
			}
		}

		return cTMP;
	}
}
