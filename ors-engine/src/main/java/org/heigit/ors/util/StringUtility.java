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

public class StringUtility {

    private StringUtility() {
    }

    public static boolean isEmptyTrimmed(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static String substring(String str, char pattern) {
        int pos1 = -1;
        int pos2 = -1;
        for (int j = 0; j < str.length(); j++) {
            if (str.charAt(j) == pattern) {
                if (pos1 == -1)
                    pos1 = j;
                else {
                    pos2 = j;
                    break;
                }
            }
        }
        if (pos1 != -1 && pos2 != -1)
            return str.substring(pos1 + 1, pos2);
        else
            return null;
    }

    public static String trimQuotes(String str) {
        return trim(str, '"');
    }

    public static String trim(String str, char ch) {
        if (str == null)
            return null;
        String result = str;
        int firstChar = str.indexOf(ch);
        int lastChar = str.lastIndexOf(ch);
        int length = str.length();
        if (firstChar == 0 && lastChar == length - 1)
            result = result.substring(1, length - 1);

        return result;
    }
}
