
package org.freeopenls.gml;

import org.freeopenls.error.ErrorTypes;
import org.freeopenls.error.ServiceError;
import org.freeopenls.tools.CoordTransform;

import net.opengis.gml.DirectPositionType;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * <p><b>Title: Pos</b></p>
 * <p><b>Description:</b> Class for read the gml:pos Element<br></p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008 by Pascal Neis</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2006-05-15
 * @version 1.1 2008-04-22
 */
public class Pos {

    /**
     * Method which read the coordinate from the DirectPositionType.<br>
     * return the CenterPoint from the Arc/CircleByCenterPoint given data
     * <br>
     * NOT Supported and TODO:<br>
     * attribute = dimension
     * 
     * @param targetSRS
     * @param pointSRS
     * @param dpType
     * @return Coordinate
     * @throws ServiceError
     */
	public static Coordinate getCoord(String targetSRS, String pointSRS, DirectPositionType dpType)throws ServiceError{
		Coordinate cTMP;
		String sSRS = "EPSG:4326"; //Default

		if(pointSRS != null)
			sSRS = pointSRS;
		if(dpType.isSetSrsName())
			sSRS = CoordTransform.getEPSGCode(dpType.getSrsName());
		
		String sPosStringValue = dpType.getStringValue();

		//TODO
		//dpType.isSetDimension()
		
		if(sPosStringValue != null){
			String[] tmp = sPosStringValue.split(" ");
			if(tmp.length <1)
				throw ErrorTypes.valueNotRecognized("xls:Position / gml:Point / gml:pos", sPosStringValue);
			else if(tmp.length == 2)
				cTMP = new Coordinate(Double.valueOf(tmp[0]), Double.valueOf(tmp[1]));
			else if(tmp.length == 3)
				cTMP = new Coordinate(Double.valueOf(tmp[0]), Double.valueOf(tmp[1]), Double.valueOf(tmp[2]));
			else
				throw ErrorTypes.valueNotRecognized("xls:Position / gml:Point / gml:pos", sPosStringValue);
		}else{
			throw ErrorTypes.valueNotRecognized("xls:Position / gml:Point / gml:pos", sPosStringValue);
		}

		if (!sSRS.equals(targetSRS)){
			Coordinate tmp = CoordTransform.transformGetCoord(sSRS, targetSRS, cTMP);
			cTMP.x = tmp.x; cTMP.y = tmp.y;
		}

		return cTMP;
	}

    /**
     * Method that returns the coordinate of the StringValue. (parse by " ")
     * 
     * @param sStringValue
     * 			String of the coordinates value
     * @return Coordinate
     * @throws ServiceError
     */
	public static Coordinate getCoord(String sStringValue)throws ServiceError{
		Coordinate cTMP;
		if(sStringValue != null){
			String[] tmp = sStringValue.split(" ");
			if(tmp.length <1)
				throw ErrorTypes.valueNotRecognized("xls:Position / gml:Point / gml:pos", sStringValue);
			else if(tmp.length == 2)
				cTMP = new Coordinate(Double.valueOf(tmp[0]), Double.valueOf(tmp[1]));
			else if(tmp.length == 3)
				cTMP = new Coordinate(Double.valueOf(tmp[0]), Double.valueOf(tmp[1]), Double.valueOf(tmp[2]));
			else
				throw ErrorTypes.valueNotRecognized("xls:Position / gml:Point / gml:pos", sStringValue);
		}else{
			throw ErrorTypes.valueNotRecognized("xls:Position / gml:Point / gml:pos", sStringValue);
		}
		return cTMP;
	}
}
