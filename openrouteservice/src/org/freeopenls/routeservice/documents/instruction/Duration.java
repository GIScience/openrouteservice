
package org.freeopenls.routeservice.documents.instruction;

import java.math.BigDecimal;

import org.apache.xmlbeans.GDuration;

/**
 * <p><b>Title: Guration</b></p>
 * <p><b>Description:</b> Class for Duration<br></p>
 *
 * <p><b>Copyright:</b> Copyright (c) 2008 by Pascal Neis</p>
 * 
 * @author Pascal Neis, neis@geographie.uni-bonn.de
 * 
 * @version 1.0 2006-05-01
 */
public class Duration {
  
     /**
     * Method that creates from the given seconds a GDuration
     *
     * @param iTimeInSeconds
     *			int TimeInSeconds
     * @return GDuration
     *			GDuration of the TimeInSeconds
     */
	public static GDuration getGDuration(int iTimeInSeconds){
		int iDay = iTimeInSeconds/(60*60*24);
		int iHours = iTimeInSeconds/(60*60) - iDay*24;
		int iMinutes = iTimeInSeconds/(60) - iHours*60 - iDay*24*60;
		int iSeconds = iTimeInSeconds - iMinutes*60 - iHours*60*60 - iDay*24*60*60;
		//new GDuration(sign, year, month, day, hours, minutes, seconds, fractional seconds)
		return new GDuration(1, 0, 0, iDay, iHours, iMinutes, iSeconds, new BigDecimal(0));
	}

     /**
     * Method that creates TimeString from the given Values, e.g.: ....
     *
     * @param iTimeInSeconds
     *			int TimeInSeconds
     * @param sDay
     *			String Value for Day, e.g."Day(s)"
     * @param sHour
     *			String Value for Hour, e.g."Hour(s)"
     * @param sMinute
     *			String Value for Minute, e.g."Minute(s)"
     * @param sSecond
     *			String Value for Second, e.g."Seconds(s)"
     * @return String
     *			GDuration of the TimeInSeconds
     */
	public static String getTimeString(int iTimeInSeconds, String sDay, String sHour, String sMinute, String sSecond){
		String sFinalTime = null;

		int iDays = iTimeInSeconds/(60*60*24);
		int iHours = iTimeInSeconds/(60*60) - iDays*24;
		int iMinutes = iTimeInSeconds/(60) - iHours*60 - iDays*24*60;
		int iSeconds = iTimeInSeconds - iMinutes*60 - iHours*60*60 - iDays*24*60*60;

		if(iDays > 0)
			sFinalTime = iDays+" "+sDay+" "+iHours+" "+sHour+" "+iMinutes+" "+sMinute+" "+iSeconds+" "+sSecond;
		else if(iHours > 0)
			sFinalTime = iHours+" "+sHour+" "+iMinutes+" "+sMinute+" "+iSeconds+" "+sSecond;
		else if (iMinutes > 0)
				sFinalTime = iMinutes+" "+sMinute+" "+iSeconds+" "+sSecond;
		else if (iSeconds > 0)
				sFinalTime = "<1 "+sMinute;

		return sFinalTime;
	}

     /**
     * Method that creates TimeString from the given Values, e.g.: .........
     *
     * @param iTimeInSeconds
     *			int TimeInSeconds
     * @param sDay
     *			String Value for Day, e.g."Day(s)"
     * @param sHour
     *			String Value for Hour, e.g."Hour(s)"
     * @param sMinute
     *			String Value for Minute, e.g."Minute(s)"
     * @return String
     * 			GDuration of the TimeInSeconds
     */
	public static String getTimeString(int iTimeInSeconds, String sDay, String sHour, String sMinute){
		String sFinalTime = null;
		
		int iDays = iTimeInSeconds/(60*60*24);
		int iHours = iTimeInSeconds/(60*60) - iDays*24;
		int iMinutes = iTimeInSeconds/(60) - iHours*60 - iDays*24*60;
		int iSeconds = iTimeInSeconds - iMinutes*60 - iHours*60*60 - iDays*24*60*60;
		
		if(iDays > 0)
			sFinalTime = iDays+" "+sDay+" "+iHours+" "+sHour+" "+iMinutes+" "+sMinute;
		else if(iHours > 0)
			sFinalTime = iHours+" "+sHour+" "+iMinutes+" "+sMinute;
		else if (iMinutes > 0)
				sFinalTime = iMinutes+" "+sMinute;
		else if (iSeconds > 0)
				sFinalTime = "<1 "+sMinute;
		
		return sFinalTime;
	}
}