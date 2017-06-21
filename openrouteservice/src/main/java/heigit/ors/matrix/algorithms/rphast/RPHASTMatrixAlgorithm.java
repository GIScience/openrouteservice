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
import com.graphhopper.routing.ch.PrepareContractionHierarchies;
import com.graphhopper.routing.phast.MatrixResponse;
import com.graphhopper.routing.phast.MatrixService;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.CHGraph;

import heigit.ors.matrix.MatrixLocationData;
import heigit.ors.matrix.MatrixMetricsType;
import heigit.ors.matrix.MatrixResult;
import heigit.ors.matrix.algorithms.AbstractMatrixAlgorithm;

public class RPHASTMatrixAlgorithm extends AbstractMatrixAlgorithm {
	private MatrixService _mtxService;
	
	public void init(GraphHopper gh, FlagEncoder encoder)
    {
	    super.init(gh, encoder);
		PrepareContractionHierarchies pch = _graphHopper.getCHFactoryDecorator().getPreparations().get(0);

	    _mtxService = new MatrixService(pch, _graphHopper.getGraphHopperStorage().getGraph(CHGraph.class), encoder);
	}
	
	@Override
	public MatrixResult compute(MatrixLocationData srcData, MatrixLocationData dstData, int metrics) {
		MatrixResult mtxResult = new MatrixResult();

		mtxResult.setSources(srcData.getCoordinates());
		mtxResult.setSourceNames(srcData.getNames());

		mtxResult.setDestinations(dstData.getCoordinates());
		mtxResult.setDestinationNames(dstData.getNames());

		MatrixResponse mr = _mtxService.calcMatrix(srcData.getNodeIds(), dstData.getNodeIds(), metrics);
		
		int matIndex = 0;
		if (MatrixMetricsType.isSet(metrics, MatrixMetricsType.Duration))
		{
			mtxResult.setTable(MatrixMetricsType.Duration, getMatrix(mr, matIndex));
			matIndex++;
		}

		if (MatrixMetricsType.isSet(metrics, MatrixMetricsType.Distance))
		{
			mtxResult.setTable(MatrixMetricsType.Distance, getMatrix(mr, matIndex));
			matIndex++;
		}

		if (MatrixMetricsType.isSet(metrics, MatrixMetricsType.Weight))
		{
			mtxResult.setTable(MatrixMetricsType.Weight, getMatrix(mr, matIndex));
			matIndex++;
		}
		
		return mtxResult;
	}
	
	private float[] getMatrix(MatrixResponse mtxResp, int matIndex)
	{
		if (matIndex == 0)
			return mtxResp.getMat0();
		else
			return mtxResp.getMat1();
	}
}
