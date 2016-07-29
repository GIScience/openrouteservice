package org.freeopenls.location.search;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlCursor;

import com.vividsolutions.jts.geom.Coordinate;

import net.opengis.xls.AbstractStreetLocatorType;
import net.opengis.xls.AddressType;
import net.opengis.xls.BuildingLocatorType;
import net.opengis.xls.NamedPlaceType;
import net.opengis.xls.StreetAddressType;
import net.opengis.xls.StreetNameType;
import net.opengis.xls.impl.NamedPlaceClassificationImpl;

public class ResultAddress {
	/** Address */
	private AddressType mAddress;
	/** Value for GeocodeMatchCode */
	private double mGeocodeMatchCode = 1.0;
	/** Coordinate of the Address */
	private Coordinate mCoordinate;

	public ResultAddress(String countryCode, String countrySubdivision, String postalCode,
			String municipality, String municipalitySubdivision, String streetName, String houseNumber, String subdivision,
			double geocodeMatchCode, Coordinate coordinate){
		
		mGeocodeMatchCode = geocodeMatchCode;
		mCoordinate = coordinate;
		
		mAddress = AddressType.Factory.newInstance();

		//Set CountryCode - Mandatory
		mAddress.setCountryCode(countryCode);

		//Set PostalCode - Optional
		if(postalCode != null)
			mAddress.setPostalCode(postalCode);

		//* Place *
		//CountrySubdivision - Optional
		if(countrySubdivision != null){
			NamedPlaceType namedplaceCountrySubdivision = mAddress.addNewPlace();
			namedplaceCountrySubdivision.setStringValue(countrySubdivision);
			namedplaceCountrySubdivision.setType(NamedPlaceClassificationImpl.COUNTRY_SUBDIVISION);
		}

		//Municipality - Mandatory
		NamedPlaceType namedplaceMunicipality = mAddress.addNewPlace();
		if(municipality != null)
			namedplaceMunicipality.setStringValue(municipality);
		else
			namedplaceMunicipality.setStringValue("");
		namedplaceMunicipality.setType(NamedPlaceClassificationImpl.MUNICIPALITY);

		//MunicipalitySubdivision - Optional
		if(municipalitySubdivision != null){
			NamedPlaceType namedplaceMunicipalitySubdivision = mAddress.addNewPlace();
			namedplaceMunicipalitySubdivision.setStringValue(municipalitySubdivision);
			namedplaceMunicipalitySubdivision.setType(NamedPlaceClassificationImpl.MUNICIPALITY_SUBDIVISION);
		}

		//StreetName - Mandatory
		StreetAddressType street = mAddress.addNewStreetAddress();
		StreetNameType streetname  = street.addNewStreet();
		if(streetName != null){
			streetname.setOfficialName(streetName);
		
			//House-nr. + Addition - Optional
			if(houseNumber != null && !houseNumber.equals("")){
				AbstractStreetLocatorType abstreetlocator = street.addNewStreetLocation();
				BuildingLocatorType building = (BuildingLocatorType) abstreetlocator.changeType(BuildingLocatorType.type);
				building.setNumber(houseNumber);
				if(subdivision != null)
					building.setSubdivision(subdivision);
			}
			
			//For well formed XML-Doc
			XmlCursor streetCursor = street.newCursor();
			if (streetCursor.toChild(new QName("http://www.opengis.net/xls", "_StreetLocation"))) {
				streetCursor.setName(new QName("http://www.opengis.net/xls","Building"));
			}
			streetCursor.dispose();
		}
	}

	/**
	 * @return the Address
	 */
	public AddressType getAddress() {
		return mAddress;
	}

	/**
	 * @return the GeocodeMatchCode
	 */
	public double getGeocodeMatchCode() {
		return mGeocodeMatchCode;
	}
	
	/**
	 * @return the Coordinate
	 */
	public Coordinate getCoordinate() {
		return mCoordinate;
	}
}
