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

// Authors: M. Rylov 

package heigit.ors.routing.traffic;

public class EdgeInfo {
	private Integer mEdgeId;
	private short[] mCodes;
	private String mMessage;

	public EdgeInfo(Integer edgeId, short[] codes) {
		mEdgeId = edgeId;
		mCodes = codes;
	}

	public EdgeInfo(Integer edgeId, short[] codes, String message) {
		mEdgeId = edgeId;
		mCodes = codes;
		mMessage = message;
	}

	public String getMessage() {
		return mMessage;
	}

	public short[] getCodes() {
		return mCodes;
	}

	public Integer getEdgeId() {
		return mEdgeId;
	}

	public String getCodesAsString() {
		String result = "";
		int nSize = mCodes.length;
		for (int i = 0; i < nSize; i++) {
			result += mCodes[i] + (i < nSize - 1 ? "," : "");
		}

		return result;
	}
}
