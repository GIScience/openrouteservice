/****************************************************
 Copyright (C) 2006-2007 by Pascal Neis

 Author: Pascal Neis

 Contact: Pascal Neis, Herm-Schuster-Str. 57,
 65510 Hünstetten, Germany, pascal.neis@gmail.com
*****************************************************/

package org.freeopenls.accessibilityanalyseservice.documents;

import de.fhMainz.geoinform.aas.DistanceUnitType;

/**
 * Class DistanceUnit - For transform to Unit XY or 
 * 
 * @author Pascal Neis	pascal.neis@geoinform.fh-mainz.de
 * @version 1.0 2006-12-22
 */
public class DistanceUnit {

	/**
	 * Method that transform distance in meters to the quoted Unit
	 * 
	 * @param distUnit - Unit to transform to
	 * @param dDistanceInMeter - Distance in meters
	 * @return double Distance in the quoted Unit
	 */
	public static double getDistanceFromMeterToXY(DistanceUnitType.Enum  distUnit, double dDistanceInMeter){
		double dUnitParameter = 0;
		if(distUnit.equals(DistanceUnitType.KM)){
			dUnitParameter = 1000;
		}else if(distUnit.equals(DistanceUnitType.M)){
			dUnitParameter = 1;
		}else if(distUnit.equals(DistanceUnitType.DM)){
			dUnitParameter = 10;
		}else if(distUnit.equals(DistanceUnitType.MI)){
			dUnitParameter = 1609.344;
		}else if(distUnit.equals(DistanceUnitType.YD)){
			dUnitParameter = 1.0936;
		}else if(distUnit.equals(DistanceUnitType.FT)){
			dUnitParameter = 3.28083;
		}
		return dDistanceInMeter/dUnitParameter;
	}

	/**
	 * Method that transform the quoted distance to meters
	 * @param distUnit - DistanceUnit of the quoted Disatnce
	 * @param dDistance - double Distance
	 * @return double Distance in meters
	 */
	public static double getDistanceInMeter(DistanceUnitType.Enum  distUnit, double dDistance){
		double dUnitParameter = 0;
		if(distUnit.equals(DistanceUnitType.KM)){
			dUnitParameter = 0.001;
		}else if(distUnit.equals(DistanceUnitType.M)){
			dUnitParameter = 1;
		}else if(distUnit.equals(DistanceUnitType.DM)){
			dUnitParameter = 10;
		}else if(distUnit.equals(DistanceUnitType.MI)){
			dUnitParameter = 0.001609344;
		}else if(distUnit.equals(DistanceUnitType.YD)){
			dUnitParameter = 1.0936;
		}else if(distUnit.equals(DistanceUnitType.FT)){
			dUnitParameter = 3.28083;
		}
		return dDistance/dUnitParameter;
	}
}
