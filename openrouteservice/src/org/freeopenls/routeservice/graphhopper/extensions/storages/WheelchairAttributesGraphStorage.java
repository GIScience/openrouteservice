/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/

// Authors: M. Rylov 

package org.freeopenls.routeservice.graphhopper.extensions.storages;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.freeopenls.routeservice.graphhopper.extensions.util.WheelchairRestrictionCodes;

import com.graphhopper.storage.DataAccess;
import com.graphhopper.storage.Directory;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphExtension;

public class WheelchairAttributesGraphStorage implements GraphExtension {
	/* pointer for no entry */
	protected final int NO_ENTRY = -1;
	protected final int EF_FEATURETYPE, EF_WHEELCHAIR_ATTRIBUTES;

	protected DataAccess orsEdges;
	protected int edgeEntryIndex = 0;
	protected int edgeEntryBytes;
	protected int edgesCount; // number of edges with custom values

	private Graph graph;
	private byte[] byteValues;

	public WheelchairAttributesGraphStorage() {
		EF_FEATURETYPE = nextBlockEntryIndex(1);
		EF_WHEELCHAIR_ATTRIBUTES = nextBlockEntryIndex(4);

	
		edgeEntryBytes = edgeEntryIndex + 4;
		edgesCount = 0;
		byteValues = new byte[5];
	}

	public void init(Graph graph, Directory dir) {
		if (edgesCount > 0)
			throw new AssertionError("The ORS storage must be initialized only once.");

		this.graph = graph;
		this.orsEdges = dir.find("edges_ors_wheel");
	}

	protected final int nextBlockEntryIndex(int size) {
		edgeEntryIndex += size;
		return edgeEntryIndex;
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
			throw new IllegalStateException("cannot load ORS edges. corrupt file or directory? " );

		edgeEntryBytes = orsEdges.getHeader(0);
		edgesCount = orsEdges.getHeader(4);
		return true;
	}

	void ensureEdgesIndex(int edgeIndex) {
		orsEdges.ensureCapacity(((long) edgeIndex + 1) * edgeEntryBytes);
	}

public void setEdgeValue(int edgeId, int featureTypeFlag, double[] wheelchairAttributes) {
		
		edgesCount++;
		ensureEdgesIndex(edgeId);

		// add entry
		long edgePointer = (long) edgeId * edgeEntryBytes;
		
		byteValues[0] = (byte)featureTypeFlag;
		orsEdges.setBytes(edgePointer + EF_FEATURETYPE, byteValues, 1);
		
		if (wheelchairAttributes == null) {
			byteValues[WheelchairRestrictionCodes.SURFACE] = 0;
			byteValues[WheelchairRestrictionCodes.SMOOTHNESS] = 0;
			byteValues[WheelchairRestrictionCodes.SLOPED_CURB] = 0;
			byteValues[WheelchairRestrictionCodes.TRACKTYPE] = 0;
			byteValues[WheelchairRestrictionCodes.INCLINE] = 0;
		}
		else {
			byteValues[WheelchairRestrictionCodes.SURFACE] = (byte)(wheelchairAttributes[WheelchairRestrictionCodes.SURFACE]);
			byteValues[WheelchairRestrictionCodes.SMOOTHNESS] = (byte)(wheelchairAttributes[WheelchairRestrictionCodes.SMOOTHNESS]);
			// byteValues[WheelchairRestrictionCodes.SLOPED_CURB] = (byte)(wheelchairAttributes[WheelchairRestrictionCodes.SLOPED_CURB] *10d);
			byteValues[WheelchairRestrictionCodes.SLOPED_CURB] = (byte)(wheelchairAttributes[WheelchairRestrictionCodes.SLOPED_CURB]);
			byteValues[WheelchairRestrictionCodes.TRACKTYPE] = (byte)(wheelchairAttributes[WheelchairRestrictionCodes.TRACKTYPE]);
			// byteValues[WheelchairRestrictionCodes.INCLINE] = (byte)(wheelchairAttributes[WheelchairRestrictionCodes.INCLINE] * 10d);
			byteValues[WheelchairRestrictionCodes.INCLINE] = (byte)(wheelchairAttributes[WheelchairRestrictionCodes.INCLINE]);
		}
		orsEdges.setBytes(edgePointer + EF_WHEELCHAIR_ATTRIBUTES, byteValues, 5);
	}

	public int getEdgeFeatureTypeFlag(int edgeId, byte[] buffer) {
		long edgePointer = (long) edgeId * edgeEntryBytes;
		orsEdges.getBytes(edgePointer + EF_FEATURETYPE, buffer, 1);
		
		int result = buffer[0];
	    if (result < 0)
	    	result = (int)result & 0xff;
		
		return result;
	}
	
	public void getWheelchairAttributes(int edgeId, byte[] buffer) {
		long edgePointer = (long) edgeId * (long) edgeEntryBytes;
		//TODO: remove these lines
		/*
		if (buffer == null) {
			buffer = new byte[20];
		}
		*/
		// buffer = new byte[20];
		// orsEdges.getBytes(edgePointer + EF_WHEELCHAIR_ATTRIBUTES, buffer, 20);
		orsEdges.getBytes(edgePointer + EF_WHEELCHAIR_ATTRIBUTES, buffer, 5);
		/*
		try {
			orsEdges.getBytes(edgePointer + EF_WHEELCHAIR_ATTRIBUTES, buffer, 5);
		}
		//TODO: Why is this Exception occurring??
		catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
			final StringWriter sw = new StringWriter();
		    final PrintWriter pw = new PrintWriter(sw, true);
		    e.printStackTrace(pw);
		    System.out.println("ORSOSMReader.processEdge(), "+sw.getBuffer().toString());
		}
		*/
		/*
		//TODO: 
		double[] result = new double[5];
		
		result[WheelchairRestrictionCodes.SURFACE] = buffer[WheelchairRestrictionCodes.SURFACE];
		result[WheelchairRestrictionCodes.SMOOTHNESS] = buffer[WheelchairRestrictionCodes.SMOOTHNESS];
		result[WheelchairRestrictionCodes.SLOPED_CURB] = (double)(buffer[WheelchairRestrictionCodes.SLOPED_CURB] & 0xff) / 10d;
		result[WheelchairRestrictionCodes.TRACKTYPE] = buffer[WheelchairRestrictionCodes.TRACKTYPE];
		result[WheelchairRestrictionCodes.INCLINE] = (double)(buffer[WheelchairRestrictionCodes.INCLINE] & 0xff) / 10d;
		return result;
		*/
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
