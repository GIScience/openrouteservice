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

import org.heigit.ors.routing.graphhopper.extensions.WheelchairAttributes;

import com.graphhopper.routing.util.EncodedValueOld;
import com.graphhopper.storage.DataAccess;
import com.graphhopper.storage.Directory;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphExtension;

import org.apache.log4j.Logger;
import org.heigit.ors.routing.graphhopper.extensions.flagencoders.WheelchairFlagEncoder;

public class WheelchairAttributesGraphStorage implements GraphExtension {
	private static final Logger LOGGER = Logger.getLogger(WheelchairFlagEncoder.class.getName());

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

	private byte[] buffer;

	// bit encoders
	private EncodedValueOld surfaceEncoder;
	private EncodedValueOld smoothnessEncoder;
	private EncodedValueOld trackTypeEncoder;
	private EncodedValueOld sideFlagEncoder;
	private EncodedValueOld kerbHeightEncoder;
	private EncodedValueOld hasKerbHeightEncoder;
	private EncodedValueOld inclineEncoder;
	private EncodedValueOld hasInclineEncoder;
	private EncodedValueOld widthEncoder;
	private EncodedValueOld surfaceReliableEncoder;
	private EncodedValueOld smoothnessReliableEncoder;
	private EncodedValueOld trackTypeReliableEncoder;
	private EncodedValueOld inclineReliableEncoder;
	private EncodedValueOld widthReliableEncoder;

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

		hasInclineEncoder = new EncodedValueOld("hasIncline", shift, 1, 1, 0, 1);
		shift += 1;
		inclineEncoder = new EncodedValueOld("incline", shift, 5, 1, 0, INCLINE_MAX_VALUE);
		shift += inclineEncoder.getBits();

		hasKerbHeightEncoder = new EncodedValueOld("hasKerbHeight", shift, 1, 1, 0, 1);
		shift += 1;
		kerbHeightEncoder = new EncodedValueOld("kerbHeight", shift, 4, 1, 0, KERB_MAX_VALUE);
		shift += kerbHeightEncoder.getBits();

		widthEncoder = new EncodedValueOld("width", shift, 5, 10, 0, WIDTH_MAX_VALUE);
		shift += widthEncoder.getBits();

		sideFlagEncoder = new EncodedValueOld("side", shift, 2, 1,0,2);
		shift += sideFlagEncoder.getBits();

		surfaceReliableEncoder = new EncodedValueOld("surfaceReliable", shift, 1, 1, 0, 1);
		shift += 1;

		smoothnessReliableEncoder = new EncodedValueOld("smoothnessReliable", shift, 1, 1, 0, 1);
		shift += 1;

		trackTypeReliableEncoder = new EncodedValueOld("trackTypeReliable", shift, 1, 1, 0, 1);
		shift += 1;

		inclineReliableEncoder = new EncodedValueOld("inclineReliable", shift, 1, 1, 0, 1);
		shift += 1;

		widthReliableEncoder = new EncodedValueOld("widthReliable", shift, 1, 1, 0, 1);
	}

	public void init(Graph graph, Directory dir) {
		if (edgesCount > 0)
			throw new AssertionError("The ORS storage must be initialized only once.");

		this.orsEdges = dir.find("ext_wheelchair");
	}

	public void setSegmentSize(int bytes) {
		orsEdges.setSegmentSize(bytes);
	}

	public GraphExtension create(long initBytes) {
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
		/* Dieser Teil wird momentan nur zu Debuging-Zwecken verwendet
		WheelchairAttributes test_attrs = new WheelchairAttributes();
 		test_attrs.reset();
		orsEdges.getBytes(edgePointer + efWheelchairAttributes, buffer, BYTE_COUNT);
		decodeAttributes(attrs, buffer);
		*/
	}

	private void encodeAttributes(WheelchairAttributes attrs, byte[] buffer) {
		/*
		 *       | flag  | surface | smoothness | tracktype | hasKerbHeight | kerbHeight | hasIncline | incline | width  | side  | reliability
		 * lsb-> | 1 bit | 5 bits  |  4 bits    | 3 bits    | 1 bit         | 6 bits     | 1 bit      | 4 bits  | 6 bits | 2 bit | 5 bits	   | 38 bits in total which can fit into 5 bytes
		 * 	
		 * 
		 */

		if (attrs.hasValues()) {
			long encodedValue = 0;
			// set first bit to 1 to mark that we have wheelchair specific attributes for this edge
			encodedValue |= (1L << 0);
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

			if (attrs.isSurfaceReliable()) {
				encodedValue = surfaceReliableEncoder.setValue(encodedValue, 1);
			}

			if (attrs.isSmoothnessReliable()) {
				encodedValue = smoothnessReliableEncoder.setValue(encodedValue, 1);
			}

			if (attrs.isTrackTypeReliable()) {
				encodedValue = trackTypeReliableEncoder.setValue(encodedValue, 1);
			}

			if (attrs.isInclineReliable()) {
				encodedValue = inclineReliableEncoder.setValue(encodedValue, 1);
			}

			if (attrs.isWidthReliable()) {
				encodedValue = widthReliableEncoder.setValue(encodedValue, 1);
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

			iValue = surfaceReliableEncoder.getValue(encodedValue);
			attrs.setSurfaceReliable((iValue != 0));

			iValue = smoothnessReliableEncoder.getValue(encodedValue);
			attrs.setSmoothnessReliable((iValue != 0));

			iValue = trackTypeReliableEncoder.getValue(encodedValue);
			attrs.setTrackTypeReliable((iValue != 0));

			iValue = inclineReliableEncoder.getValue(encodedValue);
			attrs.setInclineReliable((iValue != 0));

			iValue = widthReliableEncoder.getValue(encodedValue);
			attrs.setWidthReliable((iValue != 0));



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

	public GraphExtension copyTo(GraphExtension clonedStorage) {
		if (!(clonedStorage instanceof WheelchairAttributesGraphStorage)) {
			throw new IllegalStateException("the extended storage to clone must be the same");
		}

		WheelchairAttributesGraphStorage clonedTC = (WheelchairAttributesGraphStorage) clonedStorage;

		orsEdges.copyTo(clonedTC.orsEdges);
		clonedTC.edgesCount = edgesCount;

		return clonedStorage;
	}

	@Override
	public boolean isClosed() {
		return false;
	}
}
