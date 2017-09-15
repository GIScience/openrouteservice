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
package heigit.ors.matrix;

import com.graphhopper.util.Helper;

public class MatrixMetricsType {
	public static final int Unknown = 0;
	public static final int Duration = 1;
	public static final int Distance = 2;
	public static final int Weight = 4;
	
    public static boolean isSet(int metrics, int value) {
        return (metrics & value) == value;
    }
    
    public int getMetricsCount(int metrics)
    {
    	int res = 0;
    	
    	if (isSet(metrics, Duration))
    		res++;

    	if (isSet(metrics, Distance))
    		res++;

    	if (isSet(metrics, Weight))
    		res++;

    	return res;
    }

	public static int getFromString(String value)
	{
        if (Helper.isEmpty(value))
            return 0;
        
        String[] values = value.toLowerCase().split("\\|");
        int res = Unknown;
        
        for(String str : values)
        {
        	switch(str)
        	{
        	case "duration":
        		res |= Duration;
        		break;
        	case "distance":
        		res |= Distance;
        		break;
        	case "weight":
        		res |= Weight;
        		break;
       		default:
        		return Unknown;
        	}
        }
        
		return res;
	}
}
