package com.graphhopper.routing.util;

public class WaySurfaceDescription
{
	public byte WayType;
	public byte SurfaceType;
	
	public void Reset()
	{
		WayType = 0;
		SurfaceType = 0;
	}
	
	public Boolean equals(WaySurfaceDescription desc)
	{
		if (desc == null)
			return false;
		
		return WayType == desc.WayType && SurfaceType == desc.SurfaceType;
	}
}
