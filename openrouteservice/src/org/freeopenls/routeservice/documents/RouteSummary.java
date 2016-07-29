
package org.freeopenls.routeservice.documents;

import java.math.BigDecimal;

import org.apache.log4j.Logger;
import org.freeopenls.error.ServiceError;
import org.freeopenls.routeservice.documents.instruction.DistanceUnit;
import org.freeopenls.routeservice.routing.RouteResult;

import net.opengis.gml.EnvelopeType;
import net.opengis.xls.DistanceType;
import net.opengis.xls.RouteSummaryType;

/**
 * <p><b>Title: RouteSummary</b></p>
 * <p><b>Description:</b> Class for RouteSummary - Defines the Summary of the route.<br></p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008 by Pascal Neis</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2006-06-05
 */
public class RouteSummary {
    /**
     * Method that create and set the RouteSummaryType.<br>
     * TotalTime, TotalDistance, BoundingBox.
     * 
     * @param routeResult
     * @return RouteSummaryType
     * @throws ServiceError
     */
	public static RouteSummaryType createRouteSummary(RouteResult routeResult, RouteSummaryType rsType)throws ServiceError{
		//Set Parameter for change the Length in the requested Unit
		double dUnitParameter = DistanceUnit.getUnitParameter(routeResult.getDistanceUnit());
		double dTotalDistance = routeResult.getTotalDistance() * dUnitParameter;
		double dActualDistance = routeResult.getActualDistance() * dUnitParameter;
		
		rsType.setTotalTime(routeResult.getDuration());
		DistanceType distanceType = rsType.addNewTotalDistance();
		distanceType.setUom(routeResult.getDistanceUnit());
		distanceType.setValue(new BigDecimal(String.valueOf(dTotalDistance)).divide(new BigDecimal(1),1,BigDecimal.ROUND_HALF_UP));
		
		if (dActualDistance != dTotalDistance)
		{
			DistanceType distanceType2 = rsType.addNewActualDistance();
			distanceType2.setUom(routeResult.getDistanceUnit());
			distanceType2.setValue(new BigDecimal(String.valueOf(dActualDistance)).divide(new BigDecimal(1),1,BigDecimal.ROUND_HALF_UP));
		}
		
		if (routeResult.getTotalAscent() != 0.0 || routeResult.getTotalDescent() != 0.0)
		{
			DistanceType ascent = rsType.addNewAscent();
			ascent.setUom(routeResult.getDistanceUnit());
			ascent.setValue(new BigDecimal(String.valueOf(routeResult.getTotalAscent())).divide(new BigDecimal(1),1,BigDecimal.ROUND_HALF_UP));

			DistanceType descent = rsType.addNewDescent();
			descent.setUom(routeResult.getDistanceUnit());
			descent.setValue(new BigDecimal(String.valueOf(routeResult.getTotalDescent())).divide(new BigDecimal(1),1,BigDecimal.ROUND_HALF_UP));
		}
		
		//SetBoundingBox
		EnvelopeType envType = rsType.addNewBoundingBox();
		routeResult.getEnvelopeRoute().setValue(envType, routeResult.getResponseSRS());
		
		return rsType;
	}
}
