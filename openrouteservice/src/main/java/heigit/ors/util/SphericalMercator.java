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

import java.lang.Math;

public class SphericalMercator 
{
	public static final double RADIUS = 6378137.0; /* in meters on the equator */
	public static final double PI_DIV_2 = Math.PI/2.0;
	public static final double PI_DIV_4 = Math.PI/4.0;

	public static double yToLat(double aY) {
		return Math.toDegrees(Math.atan(Math.exp(aY / RADIUS)) * 2 - PI_DIV_2);
	}
	public static double xToLon(double aX) {
		return Math.toDegrees(aX / RADIUS);
	}

	public static double latToY(double aLat) {
		return Math.log(Math.tan(PI_DIV_4 + Math.toRadians(aLat) / 2)) * RADIUS;
	}  

	public static double lonToX(double aLong) {
		return Math.toRadians(aLong) * RADIUS;
	}
}