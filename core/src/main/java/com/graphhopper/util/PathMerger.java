/*
 *  Licensed to Peter Karich under one or more contributor license
 *  agreements. See the NOTICE file distributed with this work for
 *  additional information regarding copyright ownership.
 *
 *  Peter Karich licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the
 *  License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.graphhopper.util;

import com.graphhopper.GHResponse;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.util.EdgeAnnotator;
import com.graphhopper.routing.util.EdgeWaySurfaceDescriptor;
import com.graphhopper.routing.util.PathProcessor;

import java.util.LinkedList;
import java.util.List;

/**
 * This class merges a list of points into one point recognizing the specified places.
 * <p>
 * @author Peter Karich
 * @author ratrun
 */
public class PathMerger
{
    private boolean enableInstructions = true;
    private boolean simplifyResponse = true;
    private DouglasPeucker douglasPeucker;
    private boolean calcPoints = true;

    public void doWork( GHResponse rsp, List<Path> paths, Translation tr ) // Runge
    {
      doWork(rsp, paths, null, null, tr, null);
    }

    public void doWork( GHResponse rsp, List<Path> paths, EdgeAnnotator edgeAnnotator, PathProcessor pathProcessor,Translation tr, ArrayBuffer arrayBuffer)
    {
        int origPoints = 0;
        long fullTimeInMillis = 0;
        double fullWeight = 0;
        double fullDistance = 0;
        boolean allFound = true;

        if (pathProcessor != null)
        {
        	if (paths.size() > 0)
        		pathProcessor.start(paths.get(0).getEncoder());
        	else 
        		pathProcessor.start(null);
        }
        
        InstructionList fullInstructions = new InstructionList(tr);
        PointList fullPoints = PointList.EMPTY;
        for (int pathIndex = 0; pathIndex < paths.size(); pathIndex++)
        {
            Path path = paths.get(pathIndex);
            fullTimeInMillis += path.getTime();
            fullDistance += path.getDistance();
            fullWeight += path.getWeight();
            if (enableInstructions)
            {
                InstructionList il = path.calcInstructions(pathIndex, edgeAnnotator, pathProcessor,tr, arrayBuffer);

                if (!il.isEmpty())
                {
                    if (fullPoints.isEmpty())
                    {
                        PointList pl = il.get(0).getPoints();
                        // do a wild guess about the total number of points to avoid reallocation a bit
                        fullPoints = new PointList(il.size() * Math.min(10, pl.size()), pl.is3D());
                    }

                    for (Instruction i : il)
                    {
                        if (simplifyResponse)
                        {
                            origPoints += i.getPoints().size();
                            douglasPeucker.simplify(i.getPoints());
                        }
                        fullInstructions.add(i);
                        fullPoints.add(i.getPoints());
                    }

                    // if not yet reached finish replace with 'reached via'
                    if (pathIndex + 1 < paths.size())
                    {
                        ViaInstruction newInstr = new ViaInstruction(fullInstructions.get(fullInstructions.size() - 1));
                        newInstr.setViaCount(pathIndex + 1);
                        fullInstructions.replaceLast(newInstr);
                    }
                }

            } else if (calcPoints)
            {
                PointList tmpPoints = path.calcPoints();
                if (fullPoints.isEmpty())
                    fullPoints = new PointList(tmpPoints.size(), tmpPoints.is3D());

                if (simplifyResponse)
                {
                    origPoints = tmpPoints.getSize();
                    douglasPeucker.simplify(tmpPoints);
                }
                fullPoints.add(tmpPoints);
            }

            allFound = allFound && path.isFound();
        }
        
        if (pathProcessor != null)
        	pathProcessor.finish();

        if (!fullPoints.isEmpty())
        {
            String debug = rsp.getDebugInfo() + ", simplify (" + origPoints + "->" + fullPoints.getSize() + ")";
            rsp.setDebugInfo(debug);

        	if (pathProcessor != null)
            	 fullPoints = pathProcessor.processPoints(fullPoints);

            if (fullPoints.is3D)
            {
            	calcAscentDescent(rsp, fullPoints);
            }
        }

        if (enableInstructions)
            rsp.setInstructions(fullInstructions);

        if (!allFound)
        {
            rsp.addError(new RuntimeException("Connection between locations not found"));
        }

        rsp.setPoints(fullPoints).
                setRouteWeight(fullWeight).
                setDistance(fullDistance).setTime(fullTimeInMillis);
    }

    // Runge
    private void calcAscentDescent(final GHResponse rsp, final PointList pointList )
    {
    	double ELEVATION_THRESHOLD = 20;
    	int ascendingOrDescending = 0;

    	double totalAscent = 0;
    	double totalDescent = 0;

    	double climbStart = 0;
    	double maxAltitude = Double.MIN_VALUE;

    	double descentStart = 0;
    	double minAltitude = Double.MAX_VALUE;
    	
        int nPoints = pointList.size();
        
    	for (int i = 0; i < nPoints; ++i)
    	{
    		double sample = pointList.getElevation(i);

    		if (ascendingOrDescending == 0) {
    			// First sample
    			ascendingOrDescending = 1; 
    			climbStart = sample;
    			maxAltitude = sample;
    		} else if (ascendingOrDescending == 1) {
    			if (sample > maxAltitude) {
    				maxAltitude = sample;
    			} else if (sample < (maxAltitude - ELEVATION_THRESHOLD) ) {
    				// bounces in sample that are smaller than THRESHOLD are ignored. If 
    				// the sample is far below maxAltitude... it is not a bounce, record
    				// the climb and move to a descending state
    				totalAscent +=  (maxAltitude - climbStart);
    				// Prepare for descent.
    				ascendingOrDescending = -1;
    				descentStart = maxAltitude;
    				minAltitude = sample;
    			}
    		} else { // climbingOrDescending == DESCENDING
    			// similar code goes here to measure descents
    			if (sample < minAltitude) {
    				minAltitude = sample;
    			} else 
    				if (sample > (minAltitude + ELEVATION_THRESHOLD) ) {
    				totalDescent += (descentStart - minAltitude);
    				ascendingOrDescending = 1;
    				climbStart = minAltitude;
    				maxAltitude = sample;
    			}
    		}
    	}
    	
    	double ascendMeters = 0.0;
    	double descendMeters = 0.0;

    	if (ascendingOrDescending == 1) {
    		ascendMeters = totalAscent + (maxAltitude - climbStart);
    		descendMeters = totalDescent;
    	} else {
    		ascendMeters= totalAscent;
    		descendMeters = totalDescent + (descentStart - minAltitude);
    	}

    	rsp.setAscent(ascendMeters);
    	rsp.setDescent(descendMeters);
    }
   
    public PathMerger setCalcPoints( boolean calcPoints )
    {
        this.calcPoints = calcPoints;
        return this;
    } 

    public PathMerger setDouglasPeucker( DouglasPeucker douglasPeucker )
    {
        this.douglasPeucker = douglasPeucker;
        return this;
    }

    public PathMerger setSimplifyResponse( boolean simplifyRes )
    {
        this.simplifyResponse = simplifyRes;
        return this;
    }

    public PathMerger setEnableInstructions( boolean enableInstructions )
    {
        this.enableInstructions = enableInstructions;
        return this;
    }
}
