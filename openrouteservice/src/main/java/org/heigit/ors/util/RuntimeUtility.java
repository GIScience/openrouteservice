/*  This file is part of Openrouteservice.
 *
 *  Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the 
 *  GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 
 *  of the License, or (at your option) any later version.

 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details.

 *  You should have received a copy of the GNU Lesser General Public License along with this library; 
 *  if not, see <https://www.gnu.org/licenses/>.  
 */
package org.heigit.ors.util;

import java.text.DecimalFormat;

import org.apache.log4j.Logger;

public class RuntimeUtility {
    private RuntimeUtility() {}

	public static void clearMemory(Logger logger) {
		logger.info("====> Recycling garbage...");
		printRAMInfo("Before: ", logger);
		Runtime.getRuntime().gc();
		printRAMInfo("After: ", logger);
		logger.info("========================================================================");
	}

	public static void printRAMInfo(String hint, Logger logger) {
		logger.info(hint + " Total - " + getMemorySize(Runtime.getRuntime().totalMemory()) + ", Free - "
				+ getMemorySize(Runtime.getRuntime().freeMemory()) + ", Max: " + getMemorySize(Runtime.getRuntime().maxMemory())
				+ ", Used - "
				+ getMemorySize(Runtime.getRuntime().totalMemory() -(Runtime.getRuntime().freeMemory())));
	}
	
	public static String getMemorySize(long size) {
        String hrSize = null;
        double b = size;
        double k = size/1024.0;
        double m = ((size/1024.0)/1024.0);
        double g = (((size/1024.0)/1024.0)/1024.0);
        double t = ((((size/1024.0)/1024.0)/1024.0)/1024.0);

        DecimalFormat dec1 = new DecimalFormat("0.00");
        DecimalFormat dec2 = new DecimalFormat("0");
        if (t>1) {
            hrSize = isDouble(t) ? dec1.format(t).concat(" TB") : dec2.format(t).concat(" TB");
        } else if (g>1) {
            hrSize = isDouble(g) ? dec1.format(g).concat(" GB") : dec2.format(g).concat(" GB");
        } else if (m>1) {
            hrSize = isDouble(m) ? dec1.format(m).concat(" MB") : dec2.format(m).concat(" MB");
        } else if (k>1) {
            hrSize = isDouble(k) ? dec1.format(k).concat(" KB") : dec2.format(k).concat(" KB");
        } else {
            hrSize = isDouble(b) ? dec1.format(b).concat(" B") : dec2.format(b).concat(" B");
        }
        return hrSize;
    }
	
	private static boolean isDouble(double value) {
        return value % 1 != 0;
    }
}
