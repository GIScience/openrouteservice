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
package org.heigit.ors.util;

import org.apache.log4j.Logger;

import java.io.*;

/**
* Simple utilities to return the stack trace of an
* exception as a String.
*/
public final class StackTraceUtility {
  private static final Logger LOGGER = Logger.getLogger(StackTraceUtility.class.getName());

  public static String getStackTrace(Throwable aThrowable) {
    final Writer result = new StringWriter();
    final PrintWriter printWriter = new PrintWriter(result);
    aThrowable.printStackTrace(printWriter);
    return result.toString();
  }

  /**
  * Defines a custom format for the stack trace as String.
  */
  public static String getCustomStackTrace(Throwable aThrowable) {
    //add the class name and any message passed to constructor
    final StringBuilder result = new StringBuilder( "BOO-BOO: " );
    result.append(aThrowable.toString());
    final String NEW_LINE = System.getProperty("line.separator");
    result.append(NEW_LINE);

    //add each element of the stack trace
    for (StackTraceElement element : aThrowable.getStackTrace() ){
      result.append( element );
      result.append( NEW_LINE );
    }
    return result.toString();
  }

  /** Demonstrate output.  */
  public static void main (String... aArguments){
    final Throwable throwable = new IllegalArgumentException("Blah");
    LOGGER.info(getStackTrace(throwable));
    LOGGER.info(getCustomStackTrace(throwable));
  }
} 