/****************************************************
 Copyright (C) 2006-2007 by Pascal Neis

 Author: Pascal Neis

 Contact: Pascal Neis, Herm-Schuster-Str. 57,
 65510 Hünstetten, Germany, pascal.neis@gmail.com
*****************************************************/

package org.freeopenls.accessibilityanalyseservice.location;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.freeopenls.accessibilityanalyseservice.documents.ServiceError;


import com.vividsolutions.jts.geom.Coordinate;

import de.fhMainz.geoinform.aas.AbstractStreetLocatorType;
import de.fhMainz.geoinform.aas.AddressType;
import de.fhMainz.geoinform.aas.BuildingLocatorType;
import de.fhMainz.geoinform.aas.PlaceType;
import de.fhMainz.geoinform.aas.StreetAddressType;
import de.fhMainz.geoinform.aas.StreetNameType;


/**
 * Class for read the aas:Address element<br>
 * 
 * @author Pascal Neis, pascal.neis@geoinform.fh-mainz.de
 * @version 1.0 2007-01-03
 */
public class Address{
	/** Coordinate of the Position/Address */
	private Coordinate cCoord = new Coordinate(0,0);
	
	/**
	 * Constructor
	 */
	public Address(AddressType addressType)throws ServiceError{
		
		//Set variables for Address
		String sCountryCode = addressType.getCountryCode(); //Mandatory
		String sPostalCode = "0";							//Optional
		String sPlaceName = "";
		String sStrName = "";
		int iHouseNumber = 0;
		
		//StreetAddress
		StreetAddressType streetaddressType = addressType.getStreetAddress();
		StreetNameType streetnameType = streetaddressType.getStreetArray(0);
			
		//StreetLocation
		if(streetaddressType.isSetStreetLocation()){
			AbstractStreetLocatorType abstreetlocator = streetaddressType.getStreetLocation();
			BuildingLocatorType building = (BuildingLocatorType) abstreetlocator.changeType(BuildingLocatorType.type);
			if(building.isSetNumber()){
				if(building.getNumber() != null && !building.getNumber().equals(""))
					iHouseNumber = getNumberFromString(building.getNumber());
			}
		}
			
		//StreetName
		sStrName = streetnameType.getName();
			
		//Place
		PlaceType place = addressType.getPlace();
		sPlaceName = place.getName();
		
		//PostalCode
		sPostalCode  = addressType.getPostalCode();
		
System.out.println("ADDRESS: "+sCountryCode+" "+sPostalCode+" "+sPlaceName+" "+sStrName+" "+iHouseNumber);


/*
		//Search Address in AddressBook PostgGIS Database
		AddressSearchDB addressSearch = new AddressSearchDB(RSConfigurator.getInstance());
				
		if(addressType.isSetFreeFormAddress()){
			ResultPointsGeom = addressSearch.SearchInDBFreeForm(sFreeFormAddress, this.hmCoordToNode);
		}else{
			ResultPointsGeom = addressSearch.SearchInDB(sCountryCode, sPostalCode, sPlaceArray[0], sStreets[0], iHouseNumber, this.hmCoordToNode);
			if(ResultPointsGeom.size() == 0){
				ResultPointsGeom = addressSearch.SearchInDBINTENSIV(sCountryCode, sPostalCode, sPlaceArray[0], sStreets[0], iHouseNumber, this.hmCoordToNode);
				if(ResultPointsGeom.size() == 0){
					ResultPointsGeom = addressSearch.SearchInDBFreeForm(sCountryCode+" "+sPostalCode+" "+sPlaceArray[0]+" "+sStreets[0]+" "+iHouseNumber, this.hmCoordToNode);
				}
			}
			ResultPointsGeom = addressSearch.getAddressPoints();
		}
		//for(int i=0 ; i<ResultEdgeIDs.size() ; i++){
		//	//System.out.println(ResultEdgeIDs.get(i));
		//}
		//System.out.println("---");

		
		//Always return the first-found Address -> 
		this.cCoord = ResultPointsGeom.get(0).getCoordinate();
*/

	}


	/**
	 * Method that returns the int value in a string
	 * 
	 * @param s
	 * 			String with a number, eg. "DE-65510"
	 * @return int
	 */
	private int getNumberFromString(String s){
		Pattern p = Pattern.compile( "[0-9]" );
		Matcher m = p.matcher( s );
		if(!m.matches()){
			String sNr = "";
			for(int i=0 ; i<s.length() ; i++){
				String sTMP = s.substring(i, i+1);
				Matcher mTMP = p.matcher( sTMP );
				if(mTMP.matches())
					sNr = sNr + sTMP;
			}
			s = sNr;
		}
		return Integer.parseInt(s);
	}

	/**
	 * Method that returns the Coordinate of the Address
	 * @return Coordinate
	 */
	public Coordinate getCoord(){
		return this.cCoord;
	}
}