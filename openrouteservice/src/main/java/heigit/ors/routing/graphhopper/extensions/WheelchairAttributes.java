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
package heigit.ors.routing.graphhopper.extensions;

public class WheelchairAttributes {
    private static final int EMPTY_INT = -1;
    private static final float EMPTY_FLOAT = 0.0F;

    public enum Side { LEFT, RIGHT, UNKNOWN}

    public enum Attribute {
    	INCLINE,
		KERB,
		WIDTH,
		SURFACE,
		TRACK,
		SMOOTHNESS
	}

	private float _incline = EMPTY_FLOAT;
	private float _slopedKerbHeight = EMPTY_FLOAT;
	private float _width = EMPTY_FLOAT;
	private int _surfaceType = EMPTY_INT;
	private int _trackType = EMPTY_INT;
	private int _smoothnessType = EMPTY_INT;
	private Side _side = Side.UNKNOWN;


	private boolean _hasAttributes = false;

	public WheelchairAttributes()
	{

	}

	public boolean hasValues()
	{
		return _hasAttributes;
	}

	public void reset()
	{
		_hasAttributes = false;
		_incline = EMPTY_FLOAT;
		_slopedKerbHeight = EMPTY_FLOAT;
		_width = EMPTY_FLOAT;
		_surfaceType = EMPTY_INT;
		_trackType= EMPTY_INT;
		_smoothnessType = EMPTY_INT;
		_side = Side.UNKNOWN;
	}

	public float getIncline() {
		return _incline;
	}

	public void setIncline(float incline) {
		_incline = incline;
		_hasAttributes = true;
	}

	public int getSurfaceType() {
		return _surfaceType;
	}

	public void setSurfaceType(int surfaceType) {
		_surfaceType = surfaceType;
		_hasAttributes = true;
	}

	public float getSlopedKerbHeight() {
		return _slopedKerbHeight;
	}

	public void setSlopedKerbHeight(float slopedKerbHeight) {
		_slopedKerbHeight = slopedKerbHeight;
		_hasAttributes = true;
	}

	public int getTrackType() {
		return _trackType;
	}

	public void setTrackType(int trackType) {
		_trackType = trackType;
		_hasAttributes = true;
	}

	public int getSmoothnessType() {
		return _smoothnessType;
	}

	public void setSmoothnessType(int smoothnessType) {
		_smoothnessType = smoothnessType;
		_hasAttributes = true;
	}

	public float getWidth() { return _width; }

	public void setWidth(float width)  {
		_width = width;
		_hasAttributes = true;
	}

	public Side getSide() {
		return _side;
	}

	public void setSide(Side side) {
		this._side = side;
	}

	public void setAttribute(Attribute attribute, String valueAsText) {
		switch(attribute) {
			case INCLINE: setIncline(Float.parseFloat(valueAsText));
			    break;
			case KERB: setSlopedKerbHeight(Float.parseFloat(valueAsText));
                break;
			case SMOOTHNESS: setSmoothnessType(Integer.parseInt(valueAsText));
                break;
			case SURFACE: setSurfaceType(Integer.parseInt(valueAsText));
                break;
			case WIDTH: setWidth(Float.parseFloat(valueAsText));
                break;
			case TRACK: setTrackType(Integer.parseInt(valueAsText));
                break;
		}
	}
	
	public boolean equals(WheelchairAttributes attrs)
	{
		return _surfaceType == attrs._surfaceType && _smoothnessType == attrs._smoothnessType
				&& _trackType == attrs._trackType && _slopedKerbHeight == attrs._slopedKerbHeight
				&& _incline == attrs._incline && _width == attrs._width && _side == attrs._side;
	}

	/**
	 * Merge the passed WheelchairAttributes object into this one. The merge only takes place if there is no value for
	 * that attribute in this object.
	 *
	 * @param src
	 * @return
	 */
	public WheelchairAttributes merge(WheelchairAttributes src) {
	    WheelchairAttributes at = this.copy();

	    if(src._hasAttributes)
	        at._hasAttributes = true;

	    if(src._incline != EMPTY_FLOAT)
	        at._incline = src._incline;
	    if(src._slopedKerbHeight != EMPTY_FLOAT)
	        at._slopedKerbHeight = src._slopedKerbHeight;
	    if(src._width != EMPTY_FLOAT)
	        at._width = src._width;
	    if(src._surfaceType != EMPTY_INT)
	        at._surfaceType = src._surfaceType;
	    if(src._trackType != EMPTY_INT)
	        at._trackType = src._trackType;
	    if(src._smoothnessType != EMPTY_INT)
	        at._smoothnessType = src._smoothnessType;
		if(src._side != Side.UNKNOWN)
			at._side = src._side;
	    return at;
    }

	public WheelchairAttributes copy() {
		WheelchairAttributes at = new WheelchairAttributes();
		at._hasAttributes = this._hasAttributes;
		at._incline = this._incline;
		at._width = this._width;
		at._surfaceType = this._surfaceType;
		at._smoothnessType = this._smoothnessType;
		at._trackType = this._trackType;
		at._slopedKerbHeight = this._slopedKerbHeight;
		at._side = this._side;

		return at;
	}
}
