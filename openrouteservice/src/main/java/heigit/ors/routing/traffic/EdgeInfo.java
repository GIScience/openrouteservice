/*  This file is part of Openrouteservice.
 *
 *  Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the 
 *  GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 
 *  of the License, or (at your option) any later version.

 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details.

 *  You should have received a copy of the GNU Lesser General Public License along with this library; 
 *  if not, see <https://www.gnu.org/licenses/>.  
 */
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
