/*+-------------+----------------------------------------------------------*
 *|        /\   |   University of Bonn                                     *
 *|       |  |  |     Department of Geography                              *
 *|      _|  |_ |     Chair of Cartography                                 *
 *|    _/      \|                                                          *
 *|___|         |                                                          *
 *|             |     Meckenheimer Allee 172                               *
 *|             |     D-53115 Bonn, Germany                                *
 *+-------------+----------------------------------------------------------*/

package org.freeopenls.location;

import java.util.ArrayList;

import org.freeopenls.error.ServiceError;

import net.opengis.xls.AbstractStreetLocatorType;
import net.opengis.xls.AddressType;
import net.opengis.xls.BuildingLocatorType;
import net.opengis.xls.NamedPlaceClassification;
import net.opengis.xls.NamedPlaceType;
import net.opengis.xls.StreetAddressType;
import net.opengis.xls.StreetNameType;

/**
 * <p><b>Title: Address</b></p>
 * <p><b>Description:</b> Class for read the xls:Address element and search <br>
 * via SearchAdress and SearchFreeFormAddress in database</p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008</p>
 * <p><b>Institution:</b> University of Bonn, Department of Geography</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2007-04-04
 * @version 1.1 2008-05-08
 */
public class Address{
	/** Results of founded Address **/
	//private ArrayList<ResultAddress> mResults = new ArrayList<ResultAddress>();

	/**
	 * Constructor - Read the Address and search in DB to find the edge of the address.<br>
	 * NOT supported and TODO:<br>
	 * - Addressee<br>
	 * - Building: Buildingname<br>
	 * - StreetName: DirectionalPrefix, TypePrefix, TypeSuffix, DirectionalSuffix, MuniOctant<br>
	 * - PlaceType: NamedPlaceClassification<br>
	 * 
	 * @param addressType
	 * 			Address
	 * @throws ServiceError
	 */
	public Address(AddressType addressType, int maximumResponses, int levenstheinDistance)throws ServiceError{

		//Set variables for Address
		String countryCode = addressType.getCountryCode();	//Mandatory
		String freeFormAddress = null;						//Optional
		String countrySubdivision = null;
		String municipality = null;
		String municipalitySubdivision = null;
		String postalCode = null;							//Optional
		NamedPlaceType placeArray[];
		StreetNameType streetnameType[];					//Optional
		String streets[] = null;
		String houseNumber = null;
		String subdivision = null;
		
		/*TODO
		String sAddressee = "";							//Optional
		if(addressType.isSetAddressee()){
			sAddressee = addressType.getAddressee();
		}
		*/


	///////////////////////////////////////
	//CHOICE
	///////////////////////////////////////
		//*** FreeFormAddress ****
		if(addressType.isSetFreeFormAddress()){
			freeFormAddress = addressType.getFreeFormAddress();
		}
	//---
		///////////////////////////////////////
		//*** StreetAddress ***
		if(addressType.isSetStreetAddress()){
			StreetAddressType streetaddressType = addressType.getStreetAddress();
			streetnameType = streetaddressType.getStreetArray();
			
			//StreetLocation
			if(streetaddressType.isSetStreetLocation()){
				AbstractStreetLocatorType abstreetlocator = streetaddressType.getStreetLocation();
				BuildingLocatorType building = (BuildingLocatorType) abstreetlocator.changeType(BuildingLocatorType.type);
				if(building.isSetNumber()){
					if(building.getNumber() != null && !building.getNumber().equals(""))
						houseNumber = building.getNumber();
					if(building.getSubdivision() != null && !building.getSubdivision().equals(""))
						subdivision = building.getSubdivision();

					/*TODO
					if(building.isSetSubdivision());
						building.getSubdivision();
					if(building.isSetBuildingName());
						building.getBuildingName();
					*/
				}
			}
			
			//StreetName
			if(streetnameType.length > 0){
				streets = new String[streetnameType.length];
				for(int i=0 ; i < streetnameType.length ; i++){
					//TODO
					if(streetnameType[i].isSetDirectionalPrefix()){}	//Optional
					if(streetnameType[i].isSetTypePrefix()){}			//Optional
					if(streetnameType[i].isSetTypeSuffix()){}			//Optional
					if(streetnameType[i].isSetDirectionalSuffix()){}	//Optional
					if(streetnameType[i].isSetMuniOctant()){}			//Optional
					
					if(streetnameType[i].isSetOfficialName()){			//Optional
						streets[i] = streetnameType[i].getOfficialName();
					}else{
						streets[i] = streetnameType[i].getStringValue();
					}
					
					if(streets[i] == null)
						streets[i] = "";
				}
			}else{
				streets = new String[1];
				streets[0] = "";
			}
		}
		
		//Place
		placeArray = addressType.getPlaceArray();		//minOccurs="0" maxOccurs="unbounded"
			//NamedPlaceClassification.Enum ePlaceTypeArray[] = null;
		if(placeArray.length > 0){
			for(int i=0 ; i < placeArray.length ; i++){
				NamedPlaceClassification.Enum type = placeArray[i].getType();
				if(type.equals(NamedPlaceClassification.COUNTRY_SUBDIVISION))
					countrySubdivision = placeArray[i].getStringValue();
				if(type.equals(NamedPlaceClassification.MUNICIPALITY))
					municipality = placeArray[i].getStringValue();
				if(type.equals(NamedPlaceClassification.MUNICIPALITY_SUBDIVISION))
					municipalitySubdivision = placeArray[i].getStringValue();

				/*TODO -> PlaceType
				//Country Subdivision, Country Secondary Subdivision, Municipality, Municipality Subdivision
				//Land-Unterteilung, Land-Sekundärunterteilung, Stadtbezirk, Stadtbezirk Unterteilung 
				ePlaceTypeArray[i] = placeArray[i].getType();
				*/
			}
		}
		
		//PostalCode
		if(addressType.isSetPostalCode() && addressType.isSetStreetAddress()){	//minOccurs="0"
			postalCode = addressType.getPostalCode();
		}

	//---

		//FreeForm
		if(addressType.isSetFreeFormAddress()){
			//SearchFreeFormAddress freeForm = new SearchFreeFormAddress(connParamterDB, connection);
			//mResults = freeForm.search(freeFormAddress, maximumResponses);
			//TODO
			//SearchFreeText freeText = new SearchFreeText(connection);
			//mResults = freeText.search(freeFormAddress, maximumResponses);
			
		}else{
		//StreetAddress
//			SearchAddress searchAddress = new SearchAddress(connParamterDB, connection, levenstheinDistance);
//			searchAddress.search(countryCode, countrySubdivision, postalCode, municipality, 
//					municipalitySubdivision, streets[0], houseNumber, subdivision);
//			
//			if(searchAddress.getResultAdress().size() != 0)
//				mResults.addAll(searchAddress.getResultAdress());
//			else{
//				searchAddress.searchLevenshtein(countryCode, countrySubdivision, postalCode, municipality, 
//						municipalitySubdivision, streets[0], houseNumber, subdivision);
//				mResults.addAll(searchAddress.getResultAdress());
//			}
			
			//TODO
		//	SearchStructAddress structAddress = new SearchStructAddress(connection);
		//	mResults = structAddress.search(countryCode, countrySubdivision, postalCode, municipality, 
		//			municipalitySubdivision, streets[0], houseNumber, subdivision);
		}
	}

//	private int getNumberFromString(String s){
//		Pattern p = Pattern.compile( "[0-9]" );
//		Matcher m = p.matcher( s );
//		if(!m.matches()){
//			String sNr = "";
//			for(int i=0 ; i<s.length() ; i++){
//				String sTMP = s.substring(i, i+1);
//				Matcher mTMP = p.matcher( sTMP );
//				if(mTMP.matches())
//					sNr = sNr + sTMP;
//			}
//			s = sNr;
//		}
//		return Integer.parseInt(s);
//	}

	/**
	 * @return the Results
	 */
/*	public ArrayList<ResultAddress> getResults() {
		return mResults;
	}*/
}