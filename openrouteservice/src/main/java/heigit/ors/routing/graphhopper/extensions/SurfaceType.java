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
package heigit.ors.routing.graphhopper.extensions;

public class SurfaceType {
	public static final int Unknown = 0;
	public static final int Paved = 1;
	public static final int Unpaved = 2;
	public static final int Asphalt = 3;
	public static final int Concrete = 4;
	public static final int Cobblestone = 5;
	public static final int Metal = 6;
	public static final int Wood = 7;
	public static final int CompactedGravel = 8;
	public static final int FineGravel = 9;
	public static final int Gravel = 10;
	public static final int Dirt = 11;
	public static final int Ground = 12;
	public static final int Ice = 13;
	public static final int PavingStone = 14;
	public static final int Sand = 15;
	public static final int Woodchips = 16;
	public static final int Grass = 17;
	public static final int GrassPaver = 18;

	public static int getFromString(String surface) {
		
		if (surface.contains(";"))
			surface = surface.split(";")[0];
			
		if ("paved".equalsIgnoreCase(surface)) {
			return SurfaceType.Paved;
		} else if ("unpaved".equalsIgnoreCase(surface)) {
			return SurfaceType.Unpaved;
		} else if ("asphalt".equalsIgnoreCase(surface)) {
			return SurfaceType.Asphalt;
		} else if ("concrete".equalsIgnoreCase(surface) || "concrete:lanes".equalsIgnoreCase(surface)
				|| "concrete:plates".equalsIgnoreCase(surface)) {
			return SurfaceType.Concrete;
		} else if ("paving_stones".equalsIgnoreCase(surface) || "paving_stones:20".equalsIgnoreCase(surface) || "paving_stones:30".equalsIgnoreCase(surface) || "paving_stones:50".equalsIgnoreCase(surface) || "paved_stones".equalsIgnoreCase(surface)) {
			return SurfaceType.PavingStone;
		} else if ("cobblestone:flattened".equalsIgnoreCase(surface)
				|| "sett".equalsIgnoreCase(surface)) {
			return SurfaceType.PavingStone;
		} else if ("cobblestone".equalsIgnoreCase(surface)) {
			return SurfaceType.Cobblestone;
		}  else if ("metal".equalsIgnoreCase(surface)) {
			return SurfaceType.Metal;
		} else if ("wood".equalsIgnoreCase(surface)) {
			return SurfaceType.Wood;
		} else if ("compacted".equalsIgnoreCase(surface) || "pebblestone".equalsIgnoreCase(surface)) {
			return SurfaceType.CompactedGravel;
		} else if ("fine_gravel".equalsIgnoreCase(surface)) {
			return SurfaceType.FineGravel;
		} else if ("gravel".equalsIgnoreCase(surface)) {
			return SurfaceType.Gravel;
		} else if ("dirt".equalsIgnoreCase(surface)) {
			return SurfaceType.Dirt;
		} else if ("ground".equalsIgnoreCase(surface) || "earth".equalsIgnoreCase(surface)
				|| "mud".equalsIgnoreCase(surface)) {
			return SurfaceType.Ground;
		} else if ("ice".equalsIgnoreCase(surface) || "snow".equalsIgnoreCase(surface)) {
			return SurfaceType.Ice;
		} else if ("sand".equalsIgnoreCase(surface)) {
			return SurfaceType.Sand;
		} else if ("woodchips".equalsIgnoreCase(surface)) {
			return SurfaceType.Woodchips;
		} else if ("grass".equalsIgnoreCase(surface)) {
			return SurfaceType.Grass;
		}  else if ("grass_paver".equalsIgnoreCase(surface)) {
			return SurfaceType.GrassPaver;
		}

		return SurfaceType.Unknown;
	}
}
