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
package heigit.ors.routing.util.extrainfobuilders;

import com.graphhopper.routing.util.SteepnessUtil;
import com.graphhopper.util.DistanceCalc3D;
import com.graphhopper.util.Helper;
import com.graphhopper.util.PointList;

import heigit.ors.routing.RouteExtraInfo;
import heigit.ors.routing.RouteSegmentItem;

public class SteepnessExtraInfoBuilder extends RouteExtraInfoBuilder 
{
	private boolean _firstSegment = true;
	private double _x0, _y0, _z0, _x1, _y1, _z1;
	private double _elevDiff = 0;
	private double _cumElevation = 0.0;
	private double _maxAltitude = Double.MIN_VALUE;
	private double _minAltitude = Double.MAX_VALUE;
	private double _prevMinAltitude, _prevMaxAltitude;
	private double _splitLength = 0.0;
	private int _prevGradientCat = 0;
	private int _pointsCount = 0;
	private RouteSegmentItem _prevSegmentItem;
	private DistanceCalc3D _distCalc;
	private boolean _lastEdge;
	
    public SteepnessExtraInfoBuilder(RouteExtraInfo extraInfo) 
    {
		super(extraInfo);
		_distCalc = Helper.DIST_3D;
	}

	public void addSegment(double value, long valueIndex, PointList geom, double dist, boolean lastEdge)
    {
		_lastEdge = lastEdge;
    }
	
	public void addPoints(PointList geom)
	{
		int nPoints = geom.getSize() - 1;
		if (nPoints == 0)
			return;		
		
		int j0 = 0;
		
		if (_firstSegment)
		{
			j0 = 1;

			_x0 = geom.getLon(0);
			_y0 = geom.getLat(0);
			_z0 = geom.getEle(0);
			
			_maxAltitude = _z0;
			_minAltitude = _z0;
			_pointsCount++;
			
			_firstSegment = false;
		}
		
		for (int j = j0; j < nPoints; ++j) {
			_x1 = geom.getLon(j);
			_y1 = geom.getLat(j);
			_z1 = geom.getEle(j);
			
			_elevDiff = _z1 - _z0;
			_cumElevation += _elevDiff;
			double segLength = _distCalc.calcDist(_y0, _x0, _z0, _y1, _x1, _z1);

			_prevMinAltitude = _minAltitude;
			_prevMaxAltitude = _maxAltitude;
			if (_z1 > _maxAltitude)
				_maxAltitude = _z1;
			if (_z1 < _minAltitude)
				_minAltitude = _z1;

			//if ((_maxAltitude - _z1 > SteepnessUtil.ELEVATION_THRESHOLD || _z1 - _minAltitude > SteepnessUtil.ELEVATION_THRESHOLD) && _splitLength > 30)
			if ((_prevMaxAltitude - _z1 > SteepnessUtil.ELEVATION_THRESHOLD || _z1 - _prevMinAltitude > SteepnessUtil.ELEVATION_THRESHOLD) && _splitLength > 30)
			{
				boolean bApply = true;
				int elevSign = (_cumElevation -  _elevDiff) > 0 ? 1 : -1;
				double gradient = elevSign*100*(_prevMaxAltitude - _prevMinAltitude) / _splitLength;
				
				if (_prevGradientCat != 0 )
				{
					double zn= Double.MIN_NORMAL;
					
					if (j + 1 < nPoints)
					  zn = geom.getEle(j + 1);

					if (zn != Double.MIN_VALUE)
					{						
						double elevGap = segLength/30;
						if (elevSign > 0 /* && Math.Abs(prevSplit.Gradient - gradient) < gradientDiff)//*/ && _prevGradientCat > 0)
						{
							if (Math.abs(zn - _z1) < elevGap)
								bApply = false;
						}
						else if(/*Math.Abs(prevSplit.Gradient - gradient) < gradientDiff)//*/_prevGradientCat < 0)
						{
							if (Math.abs(zn - _z1) < elevGap)
								bApply = false;
						}
					}
				}
				
				if (bApply)
				{
					int gradientCat = SteepnessUtil.getCategory(gradient);
					int startIndex = _prevSegmentItem != null ? _prevSegmentItem.getTo() : 0;

					if (_prevGradientCat == gradientCat && _prevSegmentItem != null)
					{
						_prevSegmentItem.setTo(_prevSegmentItem.getTo() + _pointsCount);
						_prevSegmentItem.setDistance(_prevSegmentItem.getDistance() + _splitLength);
					}
					else
					{

						RouteSegmentItem item = new RouteSegmentItem(startIndex, startIndex + _pointsCount, gradientCat, _splitLength);
						_extraInfo.add(item);
						_prevSegmentItem = item;
					}
					
					_pointsCount = 0;
					_prevGradientCat = gradientCat;
					_minAltitude = Math.min(_z0, _z1);
					_maxAltitude = Math.max(_z0, _z1);
					_splitLength = 0.0;
					
					_cumElevation = _elevDiff;
				}
			}
			
			_splitLength += segLength;
			
			_x0 = _x1;
			_y0 = _y1;
			_z0 = _z1;
			
			_pointsCount++;
		}
		
		if (_lastEdge && _splitLength > 0)
		{
			_elevDiff = _maxAltitude - _minAltitude;
			if (_extraInfo.isEmpty() && _splitLength < 50 && _elevDiff < SteepnessUtil.ELEVATION_THRESHOLD)
				_elevDiff = 0;
			
			double gradient = (_cumElevation > 0 ? 1: -1)*100*_elevDiff / _splitLength;
			int gradientCat = SteepnessUtil.getCategory(gradient);
			
			if (_prevSegmentItem != null && (_prevGradientCat == gradientCat || _splitLength < 30))
			{
				_prevSegmentItem.setTo(_prevSegmentItem.getTo() + _pointsCount);
			}
			else
			{
				int startIndex = _prevSegmentItem != null ? _prevSegmentItem.getTo() : 0;
				
				RouteSegmentItem item = new RouteSegmentItem(startIndex, startIndex + _pointsCount, gradientCat, _splitLength);
				_extraInfo.add(item);
				
				_prevSegmentItem = item;
				_prevGradientCat = gradientCat;
				_pointsCount = 0;
			}
		}
	}
	
	public void finish()
	{
	}
}
