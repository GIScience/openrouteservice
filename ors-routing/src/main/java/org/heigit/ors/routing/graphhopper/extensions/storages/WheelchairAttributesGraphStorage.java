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
package org.heigit.ors.routing.graphhopper.extensions.storages;

import com.graphhopper.storage.*;
import org.heigit.ors.routing.graphhopper.extensions.WheelchairAttributes;
import org.heigit.ors.routing.graphhopper.extensions.flagencoders.EncodedValueOld;

public class WheelchairAttributesGraphStorage implements GraphExtension {
	protected static final int WIDTH_MAX_VALUE = 300;
	protected static final int KERB_MAX_VALUE = 15;
	protected static final int INCLINE_MAX_VALUE = 30;
	protected static final int TRACK_TYPE_MAX_VALUE = 5;
	protected static final int SMOOTHNESS_MAX_VALUE = 8;
	protected static final int SURFACE_MAX_VALUE = 30;

	/* pointer for no entry */
	protected final int efWheelchairAttributes;

	protected DataAccess orsEdges;
	protected int edgeEntryIndex = 0;
	protected int edgeEntryBytes;
	protected int edgesCount; // number of edges with custom values

	private final byte[] buffer;

	// bit encoders
	private final EncodedValueOld surfaceEncoder;
	private final EncodedValueOld smoothnessEncoder;
	private final EncodedValueOld trackTypeEncoder;
	private final EncodedValueOld sideFlagEncoder;
	private final EncodedValueOld kerbHeightEncoder;
	private final EncodedValueOld hasKerbHeightEncoder;
	private final EncodedValueOld inclineEncoder;
	private final EncodedValueOld hasInclineEncoder;
	private final EncodedValueOld widthEncoder;
	private final EncodedValueOld surfaceQualityKnownEncoder;
	private final EncodedValueOld pedestrianisedEncoder;

	public static final int BYTE_COUNT = 5;

	public WheelchairAttributesGraphStorage()  {
		buffer = new byte[BYTE_COUNT];
		efWheelchairAttributes = 0;

		edgeEntryBytes = edgeEntryIndex + BYTE_COUNT;
		edgesCount = 0;

		int shift = 1;
		surfaceEncoder = new EncodedValueOld("surface", shift, 5, 1, 0, SURFACE_MAX_VALUE);
		shift += surfaceEncoder.getBits();

		smoothnessEncoder = new EncodedValueOld("smoothness", shift, 4, 1, 0, SMOOTHNESS_MAX_VALUE);
		shift += smoothnessEncoder.getBits();

		trackTypeEncoder = new EncodedValueOld("tracktype", shift, 3, 1, 0, TRACK_TYPE_MAX_VALUE);
		shift += trackTypeEncoder.getBits();

		inclineEncoder = new EncodedValueOld("incline", shift, 5, 1, 0, INCLINE_MAX_VALUE);
		shift += inclineEncoder.getBits();

		kerbHeightEncoder = new EncodedValueOld("kerbHeight", shift, 4, 1, 0, KERB_MAX_VALUE);
		shift += kerbHeightEncoder.getBits();

		widthEncoder = new EncodedValueOld("width", shift, 5, 10, 0, WIDTH_MAX_VALUE);
		shift += widthEncoder.getBits();

		sideFlagEncoder = new EncodedValueOld("side", shift, 2, 1,0,2);
		shift += sideFlagEncoder.getBits();

		hasKerbHeightEncoder = new EncodedValueOld("hasKerbHeight", shift, 1, 1, 0, 1);
		shift += 1;

		hasInclineEncoder = new EncodedValueOld("hasIncline", shift, 1, 1, 0, 1);
		shift += 1;

		surfaceQualityKnownEncoder = new EncodedValueOld("surfaceQualityKnown", shift, 1, 1, 0, 1);
		shift += 1;

		pedestrianisedEncoder = new EncodedValueOld("pedestrianised", shift, 1, 1, 0, 1);

	}

	public void init(Graph graph, Directory dir) {
		if (edgesCount > 0)
			throw new AssertionError("The ORS storage must be initialized only once.");

		this.orsEdges = dir.find("ext_wheelchair");
	}

	public WheelchairAttributesGraphStorage create(long initBytes) {
		orsEdges.create(initBytes * edgeEntryBytes);
		return this;
	}

	public void flush() {
		orsEdges.setHeader(0, edgeEntryBytes);
		orsEdges.setHeader(4, edgesCount);
		orsEdges.flush();
	}

	public void close() {
		orsEdges.close();
	}

	@Override
	public long getCapacity() {
		return orsEdges.getCapacity();
	}

	public int entries() {
		return edgesCount;
	}

	public boolean loadExisting() {
		if (!orsEdges.loadExisting())
			throw new IllegalStateException("Unable to load storage 'ext_wheelchair'. corrupt file or directory? " );

		edgeEntryBytes = orsEdges.getHeader(0);
		edgesCount = orsEdges.getHeader(4);
		return true;
	}

	void ensureEdgesIndex(int edgeIndex) {
		orsEdges.ensureCapacity(((long) edgeIndex + 1) * edgeEntryBytes);
	}

	public void setEdgeValues(int edgeId, WheelchairAttributes attrs) {

		edgesCount++;
		ensureEdgesIndex(edgeId);

		long edgePointer = (long) edgeId * edgeEntryBytes;

		encodeAttributes(attrs, buffer);


		orsEdges.setBytes(edgePointer + efWheelchairAttributes, buffer, BYTE_COUNT);

	}

	private void encodeAttributes(WheelchairAttributes attrs, byte[] buffer) {
		/*
		 *       | flag  | surface | smoothness | tracktype | incline | kerbHeight | width  | side  | hasKerbHeight | hasIncline | surfaceQualityKnown | pedestrianised
		 * lsb-> | 1 bit | 5 bits  |  4 bits    | 3 bits    | 5 bits  | 4 bits     | 5 bits | 2 bit | 1 bit         | 1 bit      | 1 bit               | 1 bit          | 33 bits in total which can fit into 5 bytes
		 * 	
		 * 
		 */

		if (attrs.hasValues()) {
			long encodedValue = 0;
			// set first bit to 1 to mark that we have wheelchair specific attributes for this edge
			encodedValue |= (1L);
			if (attrs.getSurfaceType() > 0)
				encodedValue = surfaceEncoder.setValue(encodedValue, attrs.getSurfaceType());

			if (attrs.getSmoothnessType() > 0)
				encodedValue = smoothnessEncoder.setValue(encodedValue, attrs.getSmoothnessType());

			if (attrs.getTrackType() > 0)
				encodedValue = trackTypeEncoder.setValue(encodedValue, attrs.getTrackType());

			if (attrs.getIncline() > -1) {
				encodedValue = hasInclineEncoder.setValue(encodedValue, 1);
				encodedValue = inclineEncoder.setValue(encodedValue, attrs.getIncline());
			}

			if (attrs.getSlopedKerbHeight() > 0.0) {
				encodedValue = hasKerbHeightEncoder.setValue(encodedValue, 1);
				encodedValue = kerbHeightEncoder.setValue(encodedValue, attrs.getSlopedKerbHeight());
			}

			if (attrs.getWidth() > 0.0)
				encodedValue = widthEncoder.setValue(encodedValue, attrs.getWidth());

			switch(attrs.getSide()) {
				case LEFT:
					encodedValue = sideFlagEncoder.setValue(encodedValue, 1);
					break;
				case RIGHT:
					encodedValue = sideFlagEncoder.setValue(encodedValue, 2);
					break;
				case UNKNOWN:
				default:
					break;
			}

			if (attrs.isSurfaceQualityKnown()) {
				encodedValue = surfaceQualityKnownEncoder.setValue(encodedValue, 1);
			}

			if (attrs.isSuitable()) {
				encodedValue = pedestrianisedEncoder.setValue(encodedValue, 1);
			}

			buffer[4] = (byte) ((encodedValue >> 32) & 0xFF);
			buffer[3] = (byte) ((encodedValue >> 24) & 0xFF);
			buffer[2] = (byte) ((encodedValue >> 16) & 0xFF);
			buffer[1] = (byte) ((encodedValue >> 8) & 0xFF);
			buffer[0] = (byte) ((encodedValue) & 0xFF);
		} else {
			buffer[0] = 0;
			buffer[1] = 0;
			buffer[2] = 0;
			buffer[3] = 0;
			buffer[4] = 0;
		}
	}	

	private void decodeAttributes(WheelchairAttributes attrs, byte[] buffer) {
		attrs.reset();

		if (buffer[0] == 0)
			return;

		long encodedValue = (long) buffer[0] & 0xFF;
		encodedValue |= (long) (buffer[1] & 0xFF) << 8;
		encodedValue |= (long) (buffer[2] & 0xFF) << 16;
		encodedValue |= (long) (buffer[3] & 0xFF) << 24;
		encodedValue |= (long) (buffer[4] & 0xFF) << 32;

		if ((1 & encodedValue) != 0) {
			long iValue = surfaceEncoder.getValue(encodedValue);
			if (iValue != 0)
				attrs.setSurfaceType((int) iValue);

			iValue = smoothnessEncoder.getValue(encodedValue);
			if (iValue != 0)
				attrs.setSmoothnessType((int) iValue);

			iValue = trackTypeEncoder.getValue(encodedValue);
			if (iValue != 0)
				attrs.setTrackType((int) iValue);

			long hasIncline = hasInclineEncoder.getValue(encodedValue);
			if (hasIncline > 0) {
				iValue = inclineEncoder.getValue(encodedValue);
				attrs.setIncline((int) (iValue));
			}

			long hasKerbHeight = hasKerbHeightEncoder.getValue(encodedValue);
			if (hasKerbHeight > 0) {
				iValue = kerbHeightEncoder.getValue(encodedValue);
				attrs.setSlopedKerbHeight((int) (iValue));
			}

			iValue = widthEncoder.getValue(encodedValue);
			if (iValue != 0)
				attrs.setWidth((int) (iValue));

			iValue = sideFlagEncoder.getValue(encodedValue);
			switch((int) iValue) {
				case 1:
					attrs.setSide(WheelchairAttributes.Side.LEFT);
					break;
				case 2:
					attrs.setSide(WheelchairAttributes.Side.RIGHT);
					break;
				default:
					attrs.setSide(WheelchairAttributes.Side.UNKNOWN);
			}

			iValue = surfaceQualityKnownEncoder.getValue(encodedValue);
			attrs.setSurfaceQualityKnown((iValue != 0));

			iValue = pedestrianisedEncoder.getValue(encodedValue);
			attrs.setSuitable((iValue != 0));
		}
	}

	public void getEdgeValues(int edgeId, WheelchairAttributes attrs, byte[] buffer) {
		long edgePointer = (long) edgeId * (long) edgeEntryBytes;
		orsEdges.getBytes(edgePointer + efWheelchairAttributes, buffer, BYTE_COUNT);
		decodeAttributes(attrs, buffer);
	}

	public boolean isRequireNodeField() {
		return false;
	}

	public boolean isRequireEdgeField() {
		// we require the additional field in the graph to point to the first
		// entry in the node table
		return true;
	}

	public int getDefaultNodeFieldValue() {
		throw new UnsupportedOperationException("Not supported by this storage");
	}

	public int getDefaultEdgeFieldValue() {
		return -1;
	}

	@Override
	public boolean isClosed() {
		return false;
	}

}
