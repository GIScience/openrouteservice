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
package heigit.ors.exceptions;

import heigit.ors.common.StatusCode;

public class ParameterOutOfRangeException extends StatusCodeException 
{
	private static final long serialVersionUID = 7728944138955234463L;

	public ParameterOutOfRangeException(int errorCode, String paramName, String value, String maxRangeValue)
	{
		super(StatusCode.BAD_REQUEST, errorCode, "Parameter '" + paramName + "="+ value +"' is out of range. Maximum possible value is " + maxRangeValue + ".");
	}
	
	public ParameterOutOfRangeException(String paramName, String value, String maxRangeValue)
	{
		this(-1, paramName, value, maxRangeValue);
	}
}
