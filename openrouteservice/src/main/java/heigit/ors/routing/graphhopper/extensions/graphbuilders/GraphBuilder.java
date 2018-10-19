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
package heigit.ors.routing.graphhopper.extensions.graphbuilders;

import java.util.List;
import java.util.Map;

import com.carrotsearch.hppc.LongArrayList;
import com.graphhopper.GraphHopper;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.util.EdgeIteratorState;

import heigit.ors.plugins.Plugin;
import heigit.ors.routing.graphhopper.extensions.DataReaderContext;

public interface GraphBuilder extends Plugin {
	void init(GraphHopper graphhopper) throws Exception;
	
	boolean createEdges(DataReaderContext readerCntx, ReaderWay way, LongArrayList osmNodeIds, long wayFlags, List<EdgeIteratorState> createdEdges) throws Exception;
	
	void finish();
	
	String getName();
	
	void setParameters(Map<String, String> parameters);
}
