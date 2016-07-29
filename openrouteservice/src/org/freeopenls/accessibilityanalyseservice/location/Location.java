/****************************************************
 Copyright (C) 2006-2007 by Pascal Neis

 Author: Pascal Neis

 Contact: Pascal Neis, Herm-Schuster-Str. 57,
 65510 Hünstetten, Germany, pascal.neis@gmail.com
*****************************************************/

package org.freeopenls.accessibilityanalyseservice.location;

import org.freeopenls.accessibilityanalyseservice.documents.ServiceError;

import com.vividsolutions.jts.geom.Coordinate;

import de.fhMainz.geoinform.aas.AbstractLocationType;
import de.fhMainz.geoinform.aas.AccessibilityType;
import de.fhMainz.geoinform.aas.AddressType;
import de.fhMainz.geoinform.aas.ErrorCodeType;
import de.fhMainz.geoinform.aas.LocationPointType;
import de.fhMainz.geoinform.aas.PositionType;
import de.fhMainz.geoinform.aas.SeverityType;


/**
 * Class for read the LocationPoint<br>
 * Locations can be one of two things:<br>
 * 1. Position<br>
 * 2. Address<br>
 * 
 * @author Pascal Neis, pascal.neis@geoinform.fh-mainz.de
 * @version 1.0 2007-01-03
 */
public class Location {
	/** Coordinate of the Location */
	private Coordinate cLocation = null;

	/**
	 * Constructor
	 * 
	 * @param sDefaultSRS
	 * @param sResultSRS
	 * @param accessibilityType
	 * @throws ServiceError
	 */
	public Location(String sDefaultSRS, String sResultSRS, AccessibilityType accessibilityType)throws ServiceError{
		//Location can be one of two things: Position or Address
		LocationPointType locationpoint = accessibilityType.getLocationPoint();
		AbstractLocationType ablocation = locationpoint.getLocation();

		this.cLocation = checkWhichLocationType(sDefaultSRS, sResultSRS, ablocation);
	}

	/**
	 * Method that returns a coordinate for the requested Location.<br>
	 * Checks what kind of Location it is:<br>
	 * -Position<br>
	 * -POI<br>
	 * -Address
	 * 
	 * @param alType
	 * 			AbstractLocationType
	 * @return Coordinate
	 * @throws ServiceError
	 */
	private Coordinate checkWhichLocationType(String sDefaultSRS, String sResultSRS, AbstractLocationType alType)throws ServiceError{
		Coordinate cTMP = new Coordinate();
		
		//Address
		if(alType instanceof AddressType){
			//AddressType address = (AddressType) ablocation.changeType(AddressType.type);
			//String sCountryCode = address.getCountryCode();
			
			ServiceError se = new ServiceError(SeverityType.ERROR);
			se.addError(ErrorCodeType.NOT_SUPPORTED,
					"Address",
					"The element Address is not supported by this AAS Version!!");
			throw se;
		}
		//Position
		if(alType instanceof PositionType){
			PositionType posType = (PositionType) alType.changeType(PositionType.type);
			cTMP = Position.getPosition(sDefaultSRS, sResultSRS, posType);
		}
		
		return cTMP;
	}

	/**
	 * Method that return Coordinate of the Location
	 * @return Coordinate of the location
	 */
	public Coordinate getCoordLocation(){
		return this.cLocation;
	}
}