/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014-2016
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/
package heigit.ors.matrix.algorithms.rphast;

import com.graphhopper.GraphHopper;

import heigit.ors.matrix.MatrixLocationData;
import heigit.ors.matrix.MatrixMetricsType;
import heigit.ors.matrix.MatrixResult;
import heigit.ors.matrix.algorithms.MatrixAlgorithm;

public class RPHASTMatrixAlgorithm implements MatrixAlgorithm {

	@Override
	public MatrixResult compute(GraphHopper gh, MatrixLocationData srcData, MatrixLocationData dstData, int metrics) {
		MatrixResult mtxResult = new MatrixResult();

		mtxResult.setSources(srcData.getCoordinates());
		mtxResult.setSourceNames(srcData.getNames());

		mtxResult.setDestinations(dstData.getCoordinates());
		mtxResult.setDestinationNames(dstData.getNames());

		int size = srcData.getSize() * dstData.getSize();

		if (MatrixMetricsType.isSet(metrics, MatrixMetricsType.Distance))
		{
			float[] values = new float[size]; 
			mtxResult.setTable(MatrixMetricsType.Distance, values);
		}

		if (MatrixMetricsType.isSet(metrics, MatrixMetricsType.Duration))
		{
			float[] values = new float[size]; 
			mtxResult.setTable(MatrixMetricsType.Duration, values);
		}
		
		if (MatrixMetricsType.isSet(metrics, MatrixMetricsType.Weight))
		{
			float[] values = new float[size]; 
			mtxResult.setTable(MatrixMetricsType.Weight, values);
		}

		return mtxResult;
	}
}
