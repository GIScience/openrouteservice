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
package heigit.ors.routing.traffic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.graphhopper.util.DistanceCalc;
import com.graphhopper.util.DistanceCalcEarth;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

public class TrafficFeatureInfo {
	class CategoryComparator implements Comparator<Integer> {
	    @Override
	    public int compare(Integer a, Integer b) {
	        return a.compareTo(b);
	    }
	}
	
  private List<Integer> eventCodes;
  private List<String> messages;
  private List<Integer> edgeIds;
  private Date beginTime;
  private Date endTime;
  private Geometry geometry;
  private Envelope envelope;
  private double distance = -1;
  private String geomJsonString;
  
  public TrafficFeatureInfo(List<Integer> eventCodes, List<String> messages, List<Integer> edgeIds)
  {
	  this.eventCodes = new ArrayList<>(eventCodes);
	  Collections.sort(this.eventCodes, new CategoryComparator());
	  this.messages = messages;
	  this.edgeIds = edgeIds;
  }
  
  public void setDuration(Date beginTime, Date endTime)
  {
	  this.beginTime = beginTime;
	  this.endTime = endTime;
  }
  
  public Date getStartTime()
  {
	  return beginTime;
  }
  
  public Date getEndTime()
  {
	  return endTime;
  }
  
  public List<Integer> getEventCodes()
  {
	  return eventCodes;
  }
  
  public String getEventCodesAsString()
  {
	  String result = "";
	  int nSize = eventCodes.size();
	  for (int i = 0; i < nSize; i++)
	  {
		  result += eventCodes.get(i) + (i < nSize - 1 ? ",": "");
	  }
		  
	  return result;
  }
  
  public List<String> getMessages()
  {
	  return messages;
  }
  
  public String getMessage()
  {
	  String res = "";
	  if (messages != null)
	  {
		  for (int i = 0;i < messages.size();i++)
		  {
			  if (i > 0)
				 res += " ";
			  res += messages.get(i);
		  }
	  }
	  return res;
  }
  
  public List<Integer> getEdgeIds()
  {
	  return edgeIds;
  }
  
  public Envelope getEnvelope()
  {
	  if (envelope == null && geometry != null)
		  envelope = geometry.getEnvelopeInternal();
	  
	  return envelope;
  }
 
  public Geometry getGeometry()
  {
	  return geometry;
  }
  
  public void setGeometry(Geometry geom)
  {
	  geometry = geom;
	  distance = -1;
  }
  
  public String getGeometryJsonString()
  {
	  return geomJsonString;
  }
  
  public void setGeometryJsonString(String value)
  {
	  geomJsonString = value;
  }
  
  public double getDistance()
  {
	  if (distance == -1 && geometry != null)
	  {
		  DistanceCalc dc = new DistanceCalcEarth();
		  distance = 0.0;
		  
		  Coordinate[] coords = geometry.getCoordinates();
		  for (int i = 0; i< coords.length - 1; i++)
		  {
			  Coordinate c0 = coords[i];
			  Coordinate c1 = coords[i+1];
			  distance += dc.calcDist(c0.y, c0.x, c1.y, c1.x);
		  }
	  }
	  
	  return distance;
  }
}
