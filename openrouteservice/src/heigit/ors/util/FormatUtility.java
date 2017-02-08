package org.freeopenls.tools;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * <p>
 * <b>Title: ConvertUtility</b>
 * </p>
 * <p>
 * <b>Description:</b><br>
 * </p>
 * 
 * <p>
 * <b>Copyright:</b> Copyright (c) 2014 by Maxim Rylov
 * </p>
 * 
 * @author Maxim Rylov, maxim.rylov@geog.uni-heidelberg.de
 * 
 * @version 1.0 2014-05-15
 */
public class FormatUtility {
	//private static NumberFormat nfCoordRound;
	//private static NumberFormat nfCoordZRound;
	//private static DecimalFormat dfFormatDistanceRound;
	//private static DecimalFormat dfFormatDistanceFloat;
	 private static final ThreadLocal< NumberFormat > nfCoordRound = new ThreadLocal< NumberFormat >() {
	        @Override
	        protected NumberFormat initialValue() {
	        	NumberFormat nf = new DecimalFormat("#0");
	        	nf = NumberFormat.getNumberInstance(Locale.US);
	        	nf.setMaximumFractionDigits(7);
	        	nf.setMinimumFractionDigits(7);
	        	nf.setRoundingMode(RoundingMode.HALF_UP);
	        	return nf;
	        }
	    };
	    
	 private static final ThreadLocal< NumberFormat > nfCoordZRound = new ThreadLocal< NumberFormat >() {
	        @Override
	        protected NumberFormat initialValue() {
	        	NumberFormat nf = new DecimalFormat("#0");
	        	nf = NumberFormat.getNumberInstance(Locale.US);
	        	nf.setGroupingUsed(false);
	        	nf.setMaximumFractionDigits(1);
	        	nf.setMinimumFractionDigits(1);
	        	nf.setRoundingMode(RoundingMode.HALF_UP);
	        	return nf;
	        }
	    };
	 private static final ThreadLocal< DecimalFormat > dfFormatDistanceRound = new ThreadLocal< DecimalFormat >() {
	        @Override
	        protected DecimalFormat initialValue() {
	            return new DecimalFormat("#0");
	        }
	    };
	 private static final ThreadLocal< DecimalFormat > dfFormatDistanceFloat = new ThreadLocal< DecimalFormat >() {
	        @Override
	        protected DecimalFormat initialValue() {
	            return new DecimalFormat("#0.0");
	        }
	    };

	static {
	/*	nfCoordRound = NumberFormat.getNumberInstance(Locale.US);
		nfCoordRound.setMaximumFractionDigits(7);
		nfCoordRound.setMinimumFractionDigits(7);
		nfCoordRound.setRoundingMode(RoundingMode.HALF_UP);*/
		
	/*	nfCoordZRound = NumberFormat.getNumberInstance(Locale.US);
		nfCoordZRound.setGroupingUsed(false);
		nfCoordZRound.setMaximumFractionDigits(1);
		nfCoordZRound.setMinimumFractionDigits(1);
		nfCoordZRound.setRoundingMode(RoundingMode.HALF_UP);*/

		//dfFormatDistanceRound = new DecimalFormat("#0");
		//dfFormatDistanceFloat = new DecimalFormat("#0.0");
	}

	public static String formatDistance(double dist, String units) {
		if (units.equals("M") || units.equals("YD"))
			return dfFormatDistanceRound.get().format(dist);
		else
			return dfFormatDistanceFloat.get().format(dist);
	}
	
	public static String formatDistance(double dist, String units, StringBuffer buffer) {
		buffer.setLength(0);
		if (units.equals("M") || units.equals("YD"))
			DoubleFormatUtil.formatDouble(dist, 0,  8, buffer);
		else
			DoubleFormatUtil.formatDouble(dist, 1,  8, buffer);
		
		return buffer.toString();	
	}


	/**
	 * 
	 * 
	 * @param coord
	 *            Coordinate
	 * @return result String
	 */
	public static String formatCoordinate(Coordinate coord) {
		return nfCoordRound.get().format(coord.x) + " " + nfCoordRound.get().format(coord.y);
	}

	public static String formatCoordinate(Coordinate coord, StringBuffer buffer) {
		buffer.setLength(0);
		DoubleFormatUtil.formatDouble(coord.x, 7,  8, buffer);
		String xValue = buffer.toString();
		buffer.setLength(0);
		DoubleFormatUtil.formatDouble(coord.y, 7,  8, buffer);
		
		return  xValue + " " + buffer.toString();
	}

	public static String formatCoordinate(Coordinate coord, String separator, StringBuffer buffer) {
		buffer.setLength(0);
		DoubleFormatUtil.formatDouble(coord.x, 7,  8, buffer);
		String xValue = buffer.toString();
		buffer.setLength(0);
		DoubleFormatUtil.formatDouble(coord.y, 7,  8, buffer);
		
		return  xValue + separator + buffer.toString();
	}
	
	public static String formatCoordinate(Coordinate coord, String separator) {
		NumberFormat nf = nfCoordRound.get();
		return nf.format(coord.x) + separator + nf.format(coord.y);
	}
	
	public static String formatCoordinate(Coordinate coord, boolean includeZ) {
		NumberFormat nf = nfCoordRound.get();
		if (includeZ && !(coord.z != coord.z))
			return nf.format(coord.x) + " " + nf.format(coord.y) + " " + nfCoordZRound.get().format(coord.z);
		else
			return nf.format(coord.x) + " " + nf.format(coord.y);
	}
	
	public static String formatCoordinate(Coordinate coord, boolean includeZ, StringBuffer buffer) {
		return formatCoordinate(coord.x, coord.y, coord.z, includeZ, buffer);
	}
	
	public static String formatCoordinate(double x, double y, double z, boolean includeZ, StringBuffer buffer) {
		buffer.setLength(0);
		DoubleFormatUtil.formatDouble(x, 7,  8, buffer);
		String xValue = buffer.toString();
		buffer.setLength(0);
		DoubleFormatUtil.formatDouble(y, 7,  8, buffer);
		String yValue = buffer.toString();
		
		if (includeZ && !(z != z))
		{
			buffer.setLength(0);
			DoubleFormatUtil.formatDouble(z, 1,  8, buffer);
			return xValue + " " + yValue + " " + buffer.toString();
		}
		else
			return xValue + " " + yValue;
	}
	
	public static String formatValue(double xy) {
		return nfCoordRound.get().format(xy);
	}
	
	public static String formatValues(double x, double y) {
		NumberFormat nf = nfCoordRound.get();
		return nf.format(x)+ " " + nf.format(y);
	}
	
	public static double roundToDecimals(double d, int c)  
	{   
	   int temp = (int)(d * Math.pow(10 , c));  
	   return ((double)temp)/Math.pow(10 , c);  
	}
}
