/*
 *  Licensed to GIScience Research Group, Heidelberg University (GIScience)
 *
 *   http://www.giscience.uni-hd.de
 *   http://www.heigit.org
 *
 *  under one or more contributor license agreements. See the NOTICE file 
 *  distributed with this work for additional information regarding copyright 
 *  ownership. The GIScience licenses this file to you under the Apache License, 
 *  Version 2.0 (the "License"); you may not use this file except in compliance 
 *  with the License. You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package heigit.ors.util;

import java.text.DecimalFormat;

import org.apache.log4j.Logger;

public class RuntimeUtility {

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
	
	public static String getMemorySize(long size)
	{
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
        if (value % 1 == 0) {
            return false;
        } else {
            return true;
        }
    }
}
