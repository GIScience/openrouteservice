/*
 *  Licensed to GIScience Research Group, Heidelberg University (GIScience)
 *
 *       http://www.giscience.uni-hd.de
 *       http://www.heigit.org
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
package org.heigit.ors.util;

public class UnitsConverter {
    private UnitsConverter() {}

    public static double sqMetersToSqMiles(double value)  {
       return value * 3.86102e-7;
    }

    public static double sqMetersToSqKilometers(double value)  {
       return value * 1e-6;
    }

    public static double metersToKilometers(double value) {
       return value * 0.001;
    }

    public static double metersToMiles(double value)
    {
       return value * 0.000621371;
    }

    /**
     * Convert a OSM width value to a decimal value in metres. In osm the width could be stored in many different units
     * and so this method attempts to convert them all to metres.
     *
     * @param unprocessedLinearValue		The obtained width tag value
     * @return				The width value converted to metres
     */
    public static double convertOSMDistanceTagToMeters(String unprocessedLinearValue) {
        double processedLinearValue = -1d;

        // Valid values are:
		/*
		processedLinearValue=x (default metres)
		processedLinearValue=x m		(metre)
		processedLinearValue=x km		(kilometre)
		processedLinearValue=x mi		(mile)
		processedLinearValue=x nmi		(nautical mile)
		processedLinearValue=x'y"		(feet and inches)

		However, many people omit the space, even though they shouldn't
		 */

        if (unprocessedLinearValue.contains(" ")) {
            // we are working with a specified unit
            String[] split = unprocessedLinearValue.split(" ");
            if(split.length == 2) {
                try {
                    processedLinearValue = Double.parseDouble(split[0]);

                    switch(split[1]) {
                        case "m":
                            // do nothing as already in metres
                            break;
                        case "km":
                            processedLinearValue = processedLinearValue / 0.001;
                            break;
                        case "cm":
                            processedLinearValue = processedLinearValue / 100.0;
                            break;
                        case "mi":
                            processedLinearValue = processedLinearValue / 0.000621371;
                            break;
                        case "nmi":
                            processedLinearValue = processedLinearValue / 0.000539957;
                            break;
                        default:
                            // Invalid unit
                            processedLinearValue = -1d;
                    }
                } catch (Exception e) {
                    processedLinearValue = -1d;
                }
            }
        } else if (unprocessedLinearValue.contains("'") && unprocessedLinearValue.contains("\"")) {
            // Working with feet and inches
            String[] split = unprocessedLinearValue.split("'");
            if(split.length == 2) {
                split[1] = split[1].replace("\"", "");
                try {
                    processedLinearValue = Double.parseDouble(split[0]) * 12d; // 12 inches to a foot
                    processedLinearValue += Double.parseDouble(split[1]);

                    // convert to metres
                    processedLinearValue = processedLinearValue * 0.0254;

                } catch (Exception e) {
                    processedLinearValue = -1d;
                }
            }
        } else {
            // Try and read a number and assume it is in metres
            try {
                processedLinearValue = Double.parseDouble(unprocessedLinearValue);
            } catch (Exception e) {
                processedLinearValue = -1d;
            }
        }

        // If the processedLinearValue is still -1, then it could be that they have used an invalid tag, so just try and parse the most common mistakes
        if(processedLinearValue == -1d) {
            // Be careful of the order as 3cm ends in both cm and m, so we should check for cm first
            try {
                if (unprocessedLinearValue.endsWith("cm")) {
                    String[] split = unprocessedLinearValue.split("cm");
                    if (split.length == 2) {
                        processedLinearValue = Double.parseDouble(split[0]) / 100f;
                    }
                } else if (unprocessedLinearValue.endsWith("km")) {
                    String[] split = unprocessedLinearValue.split("km");
                    if (split.length == 2) {
                        processedLinearValue = Double.parseDouble(split[0]) / 0.001f;
                    }
                }else if (unprocessedLinearValue.endsWith("nmi")) {
                    String[] split = unprocessedLinearValue.split("nmi");
                    if (split.length == 2) {
                        processedLinearValue = Double.parseDouble(split[0]) / 0.000539957;
                    }
                } else if (unprocessedLinearValue.endsWith("mi")) {
                    String[] split = unprocessedLinearValue.split("mi");
                    if (split.length == 2) {
                        processedLinearValue = Double.parseDouble(split[0]) / 0.000621371;
                    }
                } else if (unprocessedLinearValue.endsWith("m")) {
                    String[] split = unprocessedLinearValue.split("m");
                    if (split.length == 2) {
                        processedLinearValue = Double.parseDouble(split[0]);
                    }
                }
            } catch (NumberFormatException nfe) {
                // There was an invalid number, so just set it to be the "invalid" value
                processedLinearValue = -1d;
            }
        }

        // If the value is more than three, we need more bits in the encoder to store it, so we can just cap to 3
        if(processedLinearValue > 3)
            processedLinearValue = 3;

        return processedLinearValue;
    }

    /**
     * Convert the String representation of an incline into a %age incline value. in OSM the tag value could already
     * be a %age value, or it could be written as "up", "down", "steep" etc. in which case an incline value is assumed
     *
     * @param unprocessedInclineValue		The value obtained from the incline tag
     * @param declineToIncline              If true, always return a positive value, otherwise decline slopes are returned as negative values
     * @return					a percentage incline value
     */
    public static int convertOSMInclineValueToPercentage(String unprocessedInclineValue, boolean declineToIncline) {

        if (unprocessedInclineValue != null)
        {
            double v = 0d;
            boolean isDegree = false;
            try {
                unprocessedInclineValue = unprocessedInclineValue.replace("%", "");
                unprocessedInclineValue = unprocessedInclineValue.replace(",", ".");
                if (unprocessedInclineValue.contains("°")) {
                    unprocessedInclineValue = unprocessedInclineValue.replace("°", "");
                    isDegree = true;
                }

                // Replace textual descriptions with assumed values
                unprocessedInclineValue = unprocessedInclineValue.replace("up", "10");
                unprocessedInclineValue = unprocessedInclineValue.replace("down", "-10");
                unprocessedInclineValue = unprocessedInclineValue.replace("yes", "10");
                unprocessedInclineValue = unprocessedInclineValue.replace("steep", "15");
                unprocessedInclineValue = unprocessedInclineValue.replace("no", "0");
                unprocessedInclineValue = unprocessedInclineValue.replace("+/-0", "0");
                v = Double.parseDouble(unprocessedInclineValue);
                if (isDegree) {
                    v = Math.tan(v) * 100;
                }
            } catch (Exception ex) {
                // There was a problem, so return a 0 and assume no incline
                v = 0;
            }

            if(declineToIncline && v < 0) {
                v = v * -1.0;
            }

            return (int) Math.round(v);
        }

        return 0;
    }
}
