/****************************************************
 Copyright (C) 2007 by Pascal Neis

 Author: Pascal Neis

 Contact: Pascal Neis, Herm-Schuster-Str. 57,
 65510 Hünstetten, Germany, pascal.neis@gmail.com
*****************************************************/

package org.freeopenls.accessibilityanalyseservice.documents;


import org.freeopenls.accessibilityanalyseservice.AASConfigurator;
import org.freeopenls.tools.CoordTransform;

import com.vividsolutions.jts.geom.Coordinate;

import net.opengis.gml.EnvelopeType;

import de.fhMainz.geoinform.aas.AccessibilitySummaryType;

/**
 * Class for AccessibilitySummary.<br>
 * Defines the Summary of the Accessibility Analyse.
 *  
 * @author Pascal Neis, pascal.neis@geoinform.fh-mainz.de
 * @version 1.0 2007-01-22
 */
public class AccessibilitySummary {

	/**
     * Method that create and set the AccessibilitySummary Type.<br>
     * BoundingBox, Number of locations ...
     * 
     * @param sSRS - Name of the SRS
	 * @param envelope - Envelope of Accessiblity Area
	 * @param iNumberOfLocations
	 * @return AccessibilitySummaryType
	 */
	public static AccessibilitySummaryType getAnalyseSummary(AccessibilitySummaryType asType, AASConfigurator AASConfig, String sSRS, String sResponseSRS, Envelope envelope, int iNumberOfLocations)throws ServiceError{
		
		//Create RouteSummary
		if(!sSRS.equals(sResponseSRS)){
			try{
				Coordinate cMin = CoordTransform.transformGetCoord(sSRS, sResponseSRS, envelope.getLowerCorner());
				Coordinate cMax = CoordTransform.transformGetCoord(sSRS, sResponseSRS, envelope.getUpperCorner());
				Coordinate[] tmp = new Coordinate[]{cMin,cMax};
				envelope = new Envelope(sResponseSRS, tmp);
			}catch (org.freeopenls.error.ServiceError se) {
				//TODO
				System.out.println(se);
			}
		}

		EnvelopeType env = asType.addNewBoundingBox();
		env.setSrsName(sResponseSRS);
		env.setPosArray(envelope.getEnvelopeType().getPosArray());
		asType.setNumberOfLocations(Integer.toString(iNumberOfLocations));
		
		return asType;
	}
}