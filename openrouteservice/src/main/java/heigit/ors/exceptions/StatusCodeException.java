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

public class StatusCodeException extends Exception 
{
	private static final long serialVersionUID = 5306540089149750357L;
	
	private int _statusCode = 200;
	private int _internalCode = 0;
   
	public StatusCodeException(int statusCode, int internalCode)
	{
		super();
		_statusCode = statusCode;
		_internalCode = internalCode;
	}
	
	public StatusCodeException(int statusCode)
	{
		super();
		
		_statusCode = statusCode;
	}
	
	public StatusCodeException(int statusCode, String message)
	{
		super(message);
		
		_statusCode = statusCode;
	}
	
	public StatusCodeException(int statusCode, int internalCode, String message)
	{
		super(message);
		
		_statusCode = statusCode;
		_internalCode = internalCode;
	}
	
	public int getStatusCode()
	{
		return _statusCode;
	}
	
	public int getInternalCode()
	{
		return _internalCode;
	}
}
