package com.graphhopper.routing;

import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.PathProcessor;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.util.Translation;

// ORS-GH MOD - Modification by Maxim Rylov: Added a new class.
public class PathProcessingContext {
	private FlagEncoder _encoder;
	private Translation _translation;
	private PathProcessor _pathProcessor;
	private Weighting _weighting;

	public PathProcessingContext(FlagEncoder encoder, Weighting weighting, Translation tr, PathProcessor pathProcessor)
	{
		_encoder = encoder;
		_weighting = weighting;
		_translation = tr;
		_pathProcessor = pathProcessor;
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
	
	public PathProcessor getPathProcessor()
	{
		return _pathProcessor;
	}
}
