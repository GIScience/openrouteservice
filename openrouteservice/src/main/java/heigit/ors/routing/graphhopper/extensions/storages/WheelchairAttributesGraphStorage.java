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
package heigit.ors.routing.graphhopper.extensions.storages;

import heigit.ors.routing.graphhopper.extensions.WheelchairAttributes;

import com.graphhopper.routing.util.EncodedDoubleValue;
import com.graphhopper.routing.util.EncodedValue;
import com.graphhopper.storage.DataAccess;
import com.graphhopper.storage.Directory;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphExtension;

public class WheelchairAttributesGraphStorage implements GraphExtension
{
	protected final static int WIDTH_MAX_VALUE = 3;
	protected final static int KERB_MAX_VALUE = 15;
	protected final static int INCLINE_MAX_VALUE = 30;
	protected final static int TRACK_TYPE_MAX_VALUE = 5;
	protected final static int SMOOTHNESS_MAX_VALUE = 8;
	protected final static int SURFACE_MAX_VALUE = 30;
	/* pointer for no entry */
	protected final int NO_ENTRY = -1;
	protected final int EF_WHEELCHAIR_ATTRIBUTES;

	protected DataAccess orsEdges;
	protected int edgeEntryIndex = 0;
	protected int edgeEntryBytes;
	protected int edgesCount; // number of edges with custom values

	private byte[] _buffer;

	// bit encoders
	private EncodedValue _surfaceEncoder;
	private EncodedValue _smoothnessEncoder;
	private EncodedValue _trackTypeEncoder;
	private EncodedDoubleValue _curbHeightEncoder;
	private EncodedDoubleValue _inclineEncoder;
	private EncodedDoubleValue _widthEncoder;
	
	public static int BYTE_COUNT = 6;

	public WheelchairAttributesGraphStorage() 
	{
		_buffer = new byte[BYTE_COUNT];
		EF_WHEELCHAIR_ATTRIBUTES = 0;

		edgeEntryBytes = edgeEntryIndex + BYTE_COUNT;
		edgesCount = 0;

		int shift = 1;
		_surfaceEncoder = new EncodedValue("surface", shift, 5, 1, 0, SURFACE_MAX_VALUE);
		shift += _surfaceEncoder.getBits();
		_smoothnessEncoder = new EncodedValue("smoothness", shift, 4, 1, 0, SMOOTHNESS_MAX_VALUE);
		shift += _smoothnessEncoder.getBits();
		_trackTypeEncoder = new EncodedValue("tracktype", shift, 3, 1, 0, TRACK_TYPE_MAX_VALUE);
		shift += _trackTypeEncoder.getBits();
		_inclineEncoder = new EncodedDoubleValue("incline", shift, 6, 0.5, 0, INCLINE_MAX_VALUE);
		shift += _inclineEncoder.getBits();
		_curbHeightEncoder = new EncodedDoubleValue("curbHeight", shift, 4, 1, 0, KERB_MAX_VALUE);
		shift += _curbHeightEncoder.getBits();
		_widthEncoder = new EncodedDoubleValue("width", shift,5,0.1,0, WIDTH_MAX_VALUE);
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
		orsEdges.create((long) initBytes * edgeEntryBytes);
		return this;
	}

	public void flush() {
		orsEdges.setHeader(0, edgeEntryBytes);
		orsEdges.setHeader(1 * 4, edgesCount);
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

		encodeAttributes(attrs, _buffer);

		orsEdges.setBytes(edgePointer + EF_WHEELCHAIR_ATTRIBUTES, _buffer, BYTE_COUNT);
	}

	private void encodeAttributes(WheelchairAttributes attrs, byte[] buffer)
	{
		/*
		 *       | flag  | surface | smoothness | tracktype | curbHeight | incline | width  | side    |
		 * lsb-> | 1 bit | 5 bits  |  4 bits    | 3 bits    | 6 bits     | 4 bits  | 6 bits | 12 bits | 41 bits in total which can fit into 6 bytes
		 * 	
		 * 
		 */

		if (attrs.hasValues())
		{
			long encodedValue = 0;
			// set first bit to 1 to mark that we have wheelchair specific attributes for this edge
			encodedValue |= (1L << 0);
			if (attrs.getSurfaceType() > 0)
				encodedValue = _surfaceEncoder.setValue(encodedValue, attrs.getSurfaceType());

			if (attrs.getSmoothnessType() > 0)
				encodedValue = _smoothnessEncoder.setValue(encodedValue, attrs.getSmoothnessType());

			if (attrs.getTrackType() > 0)
				encodedValue = _trackTypeEncoder.setValue(encodedValue, attrs.getTrackType());

			encodedValue = _inclineEncoder.setDoubleValue(encodedValue, 15 + attrs.getIncline());

			if (attrs.getSlopedCurbHeight() > 0.0)
				encodedValue = _curbHeightEncoder.setDoubleValue(encodedValue, attrs.getSlopedCurbHeight()*100);

			if (attrs.getWidth() > 0.0)
				encodedValue = _widthEncoder.setDoubleValue(encodedValue, attrs.getWidth());

			buffer[3] = (byte) ((encodedValue >> 24) & 0xFF);
			buffer[2] = (byte) ((encodedValue >> 16) & 0xFF);
			buffer[1] = (byte) ((encodedValue >> 8) & 0xFF);
			buffer[0] = (byte) ((encodedValue) & 0xFF);
		}
		else
		{
			buffer[0] = 0;
			buffer[1] = 0;
			buffer[2] = 0;
			buffer[3] = 0;
		}
  /*
		WheelchairAttributes attr2 = new WheelchairAttributes();
		decodeAttributes(attr2, buffer);
		if (attrs.hasValues() && !attrs.equals(attr2))
			attr2 = null;*/
	
	}	

	private void decodeAttributes(WheelchairAttributes attrs, byte[] buffer)
	{
		attrs.reset();

		if (buffer[0] == 0)
			return;

		long encodedValue = ((buffer[0] & 0xFF) | (buffer[1] & 0xFF) << 8 |	(buffer[2] & 0xFF) << 16 | (buffer[3] & 0xFF) <<24);

		if ((1 & (encodedValue >> 0)) != 0)
		{
			long iValue = _surfaceEncoder.getValue(encodedValue);
			if (iValue != 0)
				attrs.setSurfaceType((int) iValue);

			iValue = _smoothnessEncoder.getValue(encodedValue);
			if (iValue != 0)
				attrs.setSmoothnessType((int) iValue);

			iValue = _trackTypeEncoder.getValue(encodedValue);
			if (iValue != 0)
				attrs.setTrackType((int) iValue);

			double dValue = _inclineEncoder.getDoubleValue(encodedValue) - 15.0;
			if (dValue != 0.0)
				attrs.setIncline((float) (dValue));

			dValue = _curbHeightEncoder.getDoubleValue(encodedValue);
			if (dValue != 0.0)
				attrs.setSlopedCurbHeight((float) (dValue / 100.0));

			dValue = _widthEncoder.getDoubleValue(encodedValue);
			if (dValue != 0.0)
				attrs.setWidth((float) (dValue));

		}
	}

	public void getEdgeValues(int edgeId, WheelchairAttributes attrs, byte[] buffer) {
		long edgePointer = (long) edgeId * (long) edgeEntryBytes;
		orsEdges.getBytes(edgePointer + EF_WHEELCHAIR_ATTRIBUTES, buffer, BYTE_COUNT);

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
		// TODO Auto-generated method stub
		return false;
	}
}
