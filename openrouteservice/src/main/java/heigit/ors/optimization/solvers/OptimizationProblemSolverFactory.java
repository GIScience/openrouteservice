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
package heigit.ors.optimization.solvers;

import java.util.Map;

import heigit.ors.optimization.solvers.simulatedannealing.SimulatedAnnealingSolver;

public class OptimizationProblemSolverFactory {
	public static OptimizationProblemSolver createSolver(String name, Map<String, Object> opts)
	{
		OptimizationProblemSolver solver = null;

		switch(name.toLowerCase())
		{
		case "simulatedannealing":
			solver = new SimulatedAnnealingSolver(); 
			break;
		}

		solver.init(opts);

		return solver;
	}
}
