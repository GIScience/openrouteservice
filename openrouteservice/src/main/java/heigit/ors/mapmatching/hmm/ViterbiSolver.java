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
package heigit.ors.mapmatching.hmm;

public class ViterbiSolver {
	
	public int[] findPath(double[] startProbability, double[][] transitionProbability, double[][] emissionProbability, boolean scaled)
	{
		int nObservations = emissionProbability[0].length;
		int nStates = startProbability.length;

		// probability that the most probable hidden states ends at state i
	    double[][] delta = new double[nObservations][nStates];

	    // previous hidden state in the most probable state leading up to state i
	    int[][] phi = new int[nObservations - 1][nStates];

	    // initialize the return array
	    int[] sequence = new int[nObservations];

	    if (scaled) {
	        for (int i = 0; i < nStates; i++) {
	          delta[0][i] = Math.log(startProbability[i] * emissionProbability[i][0]);
	        }
	      } else {

	        for (int i = 0; i < nStates; i++) {
	          delta[0][i] = startProbability[i] * emissionProbability[i][0];
	        }
	      }
	    
	    if (scaled) {
	      for (int t = 1; t < nObservations; t++) {
	        for (int i = 0; i < nStates; i++) {
	          // find the maximum probability and most likely state leading up to this
	          int maxState = 0;
	          double maxProb = delta[t - 1][0] + Math.log(transitionProbability[0][i]);
	          for (int j = 1; j < nStates; j++) {
	            double prob = delta[t - 1][j] + Math.log(transitionProbability[j][i]);
	            if (prob > maxProb) {
	              maxProb = prob;
	              maxState = j;
	            }
	          }
	          delta[t][i] = maxProb + Math.log(emissionProbability[i][t]);
	          phi[t - 1][i] = maxState;
	        }
	      }
	    } else {
	      for (int t = 1; t < nObservations; t++) {
	        for (int i = 0; i < nStates; i++) {
	          int maxState = 0;
	          double maxProb = delta[t - 1][0] * transitionProbability[0][i];
	          for (int j = 1; j < nStates; j++) {
	            double prob = delta[t - 1][j] * transitionProbability[j][i];
	            if (prob > maxProb) {
	              maxProb = prob;
	              maxState = j;
	            }
	          }
	          delta[t][i] = maxProb * emissionProbability[i][t];
	          phi[t - 1][i] = maxState;
	        }
	      }
	    }
	    
	    double maxProb;
	    if (scaled) {
	      maxProb = Double.NEGATIVE_INFINITY;
	    } else {
	      maxProb = 0.0;
	    }
	    for (int i = 0; i < nStates; i++) {
	      if (delta[nObservations - 1][i] > maxProb) {
	        maxProb = delta[nObservations - 1][i];
	        sequence[nObservations - 1] = i;
	      }
	    }

	    // backtrack
	    for (int t = nObservations - 2; t >= 0; t--) {
	      sequence[t] = phi[t][sequence[t + 1]];
	    }
	    
	    return sequence;
	}
}
