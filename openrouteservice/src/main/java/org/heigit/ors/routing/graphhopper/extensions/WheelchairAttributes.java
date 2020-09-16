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
package org.heigit.ors.routing.graphhopper.extensions;

public class WheelchairAttributes {
    public enum Side {
    	LEFT,
		RIGHT,
		UNKNOWN
    }

    public enum Attribute {
    	INCLINE,
		KERB,
		WIDTH,
		SURFACE,
		TRACK,
		SMOOTHNESS
	}

	private static final int EMPTY_INT = -1;

	private int incline = EMPTY_INT;
	private int slopedKerbHeight = EMPTY_INT;
	private int width = EMPTY_INT;
	private int surfaceType = EMPTY_INT;
	private int trackType = EMPTY_INT;
	private int smoothnessType = EMPTY_INT;
	private Side side = Side.UNKNOWN;
	private boolean hasAttributes = false;
	private boolean inclineReliable = false;
	private boolean widthReliable = false;
	private boolean surfaceReliable = false;
	private boolean trackTypeReliable = false;
	private boolean smoothnessReliable = false;

	public boolean hasValues()
	{
		return hasAttributes;
	}

	public void reset() {
		hasAttributes = false;
		incline = EMPTY_INT;
		slopedKerbHeight = EMPTY_INT;
		width = EMPTY_INT;
		surfaceType = EMPTY_INT;
		trackType = EMPTY_INT;
		smoothnessType = EMPTY_INT;
		side = Side.UNKNOWN;
		inclineReliable = false;
		widthReliable = false;
		surfaceReliable = false;
		trackTypeReliable = false;
		smoothnessReliable = false;

	}

	public int getIncline() {
		return incline;
	}

	public void setIncline(double incline) {
		this.incline = (int) Math.round(incline);
		hasAttributes = true;
	}

	public int getSurfaceType() {
		return surfaceType;
	}

	public void setSurfaceType(int surfaceType) {
		this.surfaceType = surfaceType;
		hasAttributes = true;
	}

	public int getSlopedKerbHeight() {
		return slopedKerbHeight;
	}

	public void setSlopedKerbHeight(int slopedKerbHeight) {
		this.slopedKerbHeight = slopedKerbHeight;
		hasAttributes = true;
	}

	public int getTrackType() {
		return trackType;
	}

	public void setTrackType(int trackType) {
		this.trackType = trackType;
		hasAttributes = true;
	}

	public int getSmoothnessType() {
		return smoothnessType;
	}

	public void setSmoothnessType(int smoothnessType) {
		this.smoothnessType = smoothnessType;
		hasAttributes = true;
	}

	public int getWidth() { return width; }

	public void setWidth(int width)  {
		this.width = width;
		hasAttributes = true;
	}

	public Side getSide() {
		return side;
	}

	public void setSide(Side side) {
		this.side = side;
	}

	public boolean isInclineReliable() {
		return inclineReliable;
	}

	public void setInclineReliable(boolean inclineReliable) {
		this.inclineReliable = inclineReliable;
	}

	public boolean isWidthReliable() {
		return widthReliable;
	}

	public void setWidthReliable(boolean widthReliable) {
		this.widthReliable = widthReliable;
	}

	public boolean isSurfaceReliable() {
		return surfaceReliable;
	}

	public void setSurfaceReliable(boolean surfaceReliable) {
		this.surfaceReliable = surfaceReliable;
	}

	public boolean isTrackTypeReliable() {
		return trackTypeReliable;
	}

	public void setTrackTypeReliable(boolean trackTypeReliable) {
		this.trackTypeReliable = trackTypeReliable;
	}

	public boolean isSmoothnessReliable() {
		return smoothnessReliable;
	}

	public void setSmoothnessReliable(boolean smoothnessReliable) {
		this.smoothnessReliable = smoothnessReliable;
	}

	public void setAttribute(Attribute attribute, int value) {
		switch(attribute) {
			case INCLINE: setIncline(value);
			    break;
			case KERB: setSlopedKerbHeight(value);
                break;
			case SMOOTHNESS: setSmoothnessType(value);
                break;
			case SURFACE: setSurfaceType(value);
                break;
			case WIDTH: setWidth(value);
                break;
			case TRACK: setTrackType(value);
                break;
		}
	}


	public void setReliableAttribute(Attribute attribute, int value) {
		switch(attribute) {
			case INCLINE: setIncline(value);
				setInclineReliable(true);
				break;
			case KERB: setSlopedKerbHeight(value);
				break;
			case SMOOTHNESS: setSmoothnessType(value);
				setSmoothnessReliable(true);
				break;
			case SURFACE: setSurfaceType(value);
				setSurfaceReliable(true);
				break;
			case WIDTH: setWidth(value);
				setWidthReliable(true);
				break;
			case TRACK: setTrackType(value);
				setTrackTypeReliable(true);
				break;
		}
	}

	public boolean equalsWheelchairAttributes(WheelchairAttributes attrs) {
		return surfaceType == attrs.surfaceType && smoothnessType == attrs.smoothnessType
				&& trackType == attrs.trackType && slopedKerbHeight == attrs.slopedKerbHeight
				&& incline == attrs.incline && width == attrs.width && side == attrs.side
				&& surfaceReliable == attrs.surfaceReliable && smoothnessReliable == attrs.smoothnessReliable
				&& trackTypeReliable == attrs.trackTypeReliable && inclineReliable == attrs.inclineReliable
				&& widthReliable == attrs.widthReliable;
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

	    if(src.hasAttributes)
	        at.hasAttributes = true;

	    if(src.incline != EMPTY_INT)
	        at.incline = src.incline;
	    if(src.slopedKerbHeight != EMPTY_INT)
	        at.slopedKerbHeight = src.slopedKerbHeight;
	    if(src.width != EMPTY_INT)
	        at.width = src.width;
	    if(src.surfaceType != EMPTY_INT)
	        at.surfaceType = src.surfaceType;
	    if(src.trackType != EMPTY_INT)
	        at.trackType = src.trackType;
	    if(src.smoothnessType != EMPTY_INT)
	        at.smoothnessType = src.smoothnessType;
		if(src.side != Side.UNKNOWN)
			at.side = src.side;
		if(src.surfaceReliable)
			at.surfaceReliable = true;
		if(src.smoothnessReliable)
			at.smoothnessReliable = true;
		if(src.trackTypeReliable)
			at.trackTypeReliable = true;
		if(src.inclineReliable)
			at.inclineReliable = true;
		if(src.widthReliable)
			at.widthReliable = true;
	    return at;
    }

	public WheelchairAttributes copy() {
		WheelchairAttributes at = new WheelchairAttributes();
		at.hasAttributes = this.hasAttributes;
		at.incline = this.incline;
		at.width = this.width;
		at.surfaceType = this.surfaceType;
		at.smoothnessType = this.smoothnessType;
		at.trackType = this.trackType;
		at.slopedKerbHeight = this.slopedKerbHeight;
		at.side = this.side;
		at.surfaceReliable = this.surfaceReliable;
		at.smoothnessReliable = this.smoothnessReliable;
		at.trackTypeReliable = this.trackTypeReliable;
		at.inclineReliable = this.inclineReliable;
		at.widthReliable = this.widthReliable;
		return at;
	}
}
