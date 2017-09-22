/*
 *  Licensed to GIScience Research Group, Heidelberg University (GIScience)
 *
 *   	 http://www.giscience.uni-hd.de
 *   	 http://www.heigit.org
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
package heigit.ors.isochrones.statistics.postgresql;

import java.util.Map;

import heigit.ors.isochrones.Isochrone;
import heigit.ors.isochrones.statistics.AbstractStatisticsProvider;

public class PostgresSQLStatisticsProvider extends AbstractStatisticsProvider {

	@Override
	public void init(Map<String, Object> parameters) throws Exception {
	}

	@Override
	public void close() throws Exception 
	{
		
	}

	@Override
	public double getStatistics(Isochrone isochrone, String property)
	{
		return 1.0;
		
		/*SELECT Round(SUM(c.ratio * c.%%column_name%%)) 
FROM   ( 
              SELECT St_area(St_intersection(a.geog,poly)) / St_area(a.geog) ratio, 
                     a.* 
              FROM   geostat_grd_2016_ageclasses_nuts a, 
                     %%wkb_geom%% 
              WHERE  a.gid IN 
                     ( 
                            SELECT a.gid 
                            FROM   geostat_grd_2016_ageclasses_nuts a, 
                                   %%wkb_geom%% 
WHERE st_intersects (a.geog, poly))) AS c;*/
	}

	@Override
	public String getName() {
		return "postgresql";
	}
}
