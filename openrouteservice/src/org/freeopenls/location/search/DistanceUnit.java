
package org.freeopenls.location.search;

import net.opengis.xls.DistanceUnitType;

/**
 * <p><b>Title: DistanceUnit</b></p>
 * <p><b>Description:</b> Class for DistanceUnit<br></p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008 by Pascal Neis</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2006-11-20
 */
public class DistanceUnit {
	
    /**
     * Method that return the Parameter to calculte a distance in Meter [m] to the requested DistanceUnitType.Enum<br>
     * e.g. "KM" -> Paramter = 0.001
     * 
     * @param distUnit DistanceUnitType.Enum
     */
	public static double getUnitParameter(DistanceUnitType.Enum  distUnit){
		double dUnitParameter = 0;
		if(distUnit.equals(DistanceUnitType.KM)){
			dUnitParameter = 0.001;
		}else if(distUnit.equals(DistanceUnitType.M)){
			dUnitParameter = 1;
		}else if(distUnit.equals(DistanceUnitType.DM)){
			dUnitParameter = 10;
		}else if(distUnit.equals(DistanceUnitType.MI)){
			dUnitParameter = 0.0006213712;//1MI = 1.609344KM
		}else if(distUnit.equals(DistanceUnitType.YD)){
			dUnitParameter = 1.0936;
		}else if(distUnit.equals(DistanceUnitType.FT)){
			dUnitParameter = 3.28083;
		}
		return dUnitParameter;
	}
}
