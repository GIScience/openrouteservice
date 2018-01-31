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
package heigit.ors.routing.graphhopper.extensions.storages.builders;

import java.util.Map;

import com.graphhopper.GraphHopper;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.storage.GraphExtension;
import com.graphhopper.util.EdgeIteratorState;
import com.vividsolutions.jts.geom.LineString;

public abstract class AbstractGraphStorageBuilder implements GraphStorageBuilder
{
	protected Map<String, String> _parameters;
	
	public abstract GraphExtension init(GraphHopper graphhopper) throws Exception;

	public abstract void processWay(ReaderWay way);

	public void processWay(ReaderWay way, LineString ls) { processWay(way);}
	
	public abstract void processEdge(ReaderWay way, EdgeIteratorState edge);
	
	public void setParameters(Map<String, String> parameters)
	{
		_parameters = parameters;
	}
	
	public abstract String getName();
	
	public void finish()
	{
		
	}
}
