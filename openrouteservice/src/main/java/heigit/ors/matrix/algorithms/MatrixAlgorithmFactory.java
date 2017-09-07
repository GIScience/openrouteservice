/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014-2017
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/
package heigit.ors.matrix.algorithms;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.FlagEncoder;

import heigit.ors.matrix.MatrixRequest;
import heigit.ors.matrix.algorithms.dijkstra.DijkstraMatrixAlgorithm;
import heigit.ors.matrix.algorithms.rphast.RPHASTMatrixAlgorithm;

public class MatrixAlgorithmFactory {
	public static MatrixAlgorithm createAlgorithm(MatrixRequest req, GraphHopper gh, FlagEncoder encoder) {
		MatrixAlgorithm alg = null;

		if (!req.getFlexibleMode() && gh.isCHEnabled())
			alg = new RPHASTMatrixAlgorithm();
		else
			alg = new DijkstraMatrixAlgorithm();

		return alg;
	}
}
