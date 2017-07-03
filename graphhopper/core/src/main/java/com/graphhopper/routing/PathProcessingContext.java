package com.graphhopper.routing;

import com.graphhopper.routing.util.EdgeAnnotator;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.PathProcessor;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.util.ByteArrayBuffer;
import com.graphhopper.util.Translation;

public class PathProcessingContext {
	private FlagEncoder _encoder;
	private Translation _translation;
	private ByteArrayBuffer _byteBuffer;
	private EdgeAnnotator _edgeAnnotator;
	private PathProcessor _pathProcessor;
	private Weighting _weighting;
	private int _pathIndex = 0;
	
	public PathProcessingContext(Translation tr)
	{
		this(null, null, tr, null, null, null);
	}
	
	public PathProcessingContext(FlagEncoder encoder, Weighting weighting, Translation tr, EdgeAnnotator annotator, PathProcessor pathProcessor, ByteArrayBuffer buffer)
	{
		_encoder = encoder;
		_weighting = weighting;
		_translation = tr;
		_edgeAnnotator = annotator;
		_pathProcessor = pathProcessor;
		_byteBuffer = buffer;
	}
	
	public FlagEncoder getEncoder()
	{
		return _encoder;
	}
	
	public Weighting getWeighting()
	{
		return _weighting;
	}
	
	public Translation getTranslation()
	{
		return _translation;
	}
	
	public EdgeAnnotator getEdgeAnnotator()
	{
		return _edgeAnnotator;
	}
	
	public PathProcessor getPathProcessor()
	{
		return _pathProcessor;
	}
	
	public ByteArrayBuffer getByteBuffer()
	{
		return _byteBuffer;
	}

	public int getPathIndex() {
		return _pathIndex;
	}

	public void setPathIndex(int pathIndex) {
		_pathIndex = pathIndex;
	}
}
